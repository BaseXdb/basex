package org.basex.query.up.primitives;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Contains helper methods for adding documents.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DBNew {
  /** Inputs to be added. */
  public final List<NewInput> inputs;

  /** Query context. */
  private final QueryContext qc;
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** Main options for all inputs to be added. */
  private final List<MainOptions> options;
  /** New database nodes. */
  private Data data;

  /**
   * Constructor.
   * @param qc query context
   * @param opts main options
   * @param info input info (can be {@code null})
   * @param inputs list of inputs
   */
  public DBNew(final QueryContext qc, final MainOptions opts, final InputInfo info,
      final NewInput... inputs) {

    this.qc = qc;
    this.info = info;

    final int il = inputs.length;
    this.inputs = new ArrayList<>(il);
    options = new ArrayList<>(il);
    for(final NewInput input : inputs) {
      this.inputs.add(input);
      options.add(opts);
    }
  }

  /**
   * Merges updates.
   * @param add inputs to be added
   */
  public void merge(final DBNew add) {
    inputs.addAll(add.inputs);
    options.addAll(add.options);
  }

  /**
   * Inserts all documents to be added to a temporary database.
   * @param name name of database
   * @param create create new database
   * @return resulting data clip (can be {@code null})
   * @throws QueryException query exception
   */
  public DataClip prepare(final String name, final boolean create) throws QueryException {
    try {
      final long is = inputs.size();
      if(is > 0) {
        // check if new resources will be cached on disk
        final boolean cache = cache(create);
        if(is == 1) {
          // single input: create temporary database
          data = tmpData(name, 0, cache);
        } else {
          // multiple input: create temporary database and insert inputs
          final Context ctx = qc.context;
          final MainOptions mopts = ctx.options;
          final StaticOptions sopts = ctx.soptions;
          final String dbname = cache ? sopts.createTempDb(name) : name;
          data = cache ? CreateDB.create(dbname, Parser.emptyParser(mopts), ctx, mopts) :
            new MemData(mopts);
          data.startUpdate(mopts);
          try {
            for(int i = 0; i < is; i++) {
              final Data tmpData = tmpData(dbname, i, cache);
              try {
                copy(tmpData, data, false);
              } finally {
                DropDB.drop(tmpData, sopts);
              }
            }
          } finally {
            data.finishUpdate(mopts);
          }
        }
      }
      return data == null ? null : new DataClip(data).context(qc.context);
    } catch(final QueryException ex) {
      if(data != null) new DataClip(data).context(qc.context).finish();
      throw ex;
    } catch(final IOException ex) {
      if(data != null) new DataClip(data).context(qc.context).finish();
      throw UPDBERROR_X.get(info, ex);
    } finally {
      options.clear();
      inputs.clear();
    }
  }

  /**
   * Adds the contents of the temporary database to the target database.
   * @param target database instance
   * @param replace if {@code true}, existing binary or value resources at the target paths
   *   are overwritten; if {@code false}, a conflict is raised
   * @throws QueryException exception
   */
  public void addTo(final Data target, final boolean replace) throws QueryException {
    try {
      copy(data, target, replace);
    } catch(final IOException ex) {
      throw UPDBERROR_X.get(info, ex);
    }
  }

  /**
   * Checks if disk caching is requested or required for at least one document.
   * @param create create new database
   * @return result of check
   */
  private boolean cache(final boolean create) {
    for(final NewInput input : inputs) {
      // binary and value resources always require an on-disk temporary database
      if(input.type == ResourceType.BINARY || input.type == ResourceType.VALUE) return true;
    }
    for(final MainOptions dbopts : options) {
      Boolean b = dbopts.get(MainOptions.ADDCACHE);
      if(b != null && b) return true;
      if(create) {
        if(dbopts.get(MainOptions.PARSER) == MainParser.RAW) return true;
        b = dbopts.get(MainOptions.ADDRAW);
        if(b != null && b) return true;
      }
    }
    return false;
  }

  /**
   * Creates a temporary database instance with the contents of the specified input.
   * @param name name of database
   * @param i index of current input
   * @param cache cache data to disk
   * @return database
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private Data tmpData(final String name, final int i, final boolean cache)
      throws IOException, QueryException {
    // free memory: clear list entries after retrieval
    final NewInput input = inputs.get(i);
    final MainOptions mopts = options.get(i);
    inputs.set(i, null);
    options.set(i, null);

    // existing node: create data clip for copied instance
    final XNode node = input.node;
    if(node != null) return xmlNodeData(name, input, node, mopts);

    final StaticOptions sopts = qc.context.soptions;
    final String dbname = cache ? sopts.createTempDb(name) : name;

    // binary or value resource: empty database, write content directly to its file area
    final boolean file = input.type == ResourceType.BINARY || input.type == ResourceType.VALUE;
    final Parser parser = file ? Parser.emptyParser(mopts) :
      new DirParser(input.io, mopts).target(input.path);
    final Builder builder = file || cache
      ? new DiskBuilder(dbname, parser, sopts, mopts)
      : new MemBuilder(dbname, parser);
    builder.binariesDir(sopts.dbPath(dbname));
    final Data d = builder.build();
    if(file) writeFileResource(d, input);
    return d;
  }

  /**
   * Creates a memory-backed database instance for an XML node input.
   * @param name name of database
   * @param input new input
   * @param node node to be stored (will be wrapped into a document if needed)
   * @param mopts options
   * @return memory data instance
   * @throws QueryException query exception
   */
  private MemData xmlNodeData(final String name, final NewInput input, final XNode node,
      final MainOptions mopts) throws QueryException {
    XNode src = node;
    if(src.kind() != Kind.DOCUMENT) src = FDoc.build(token(name)).node(src).finish();
    final MemData mdata = (MemData) src.copy(mopts, qc).data();
    mdata.update(0, Data.DOC, token(input.path));
    return mdata;
  }

  /**
   * Writes a binary or value resource to the binary directory of a temporary database.
   * @param d temporary database
   * @param input new input
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private void writeFileResource(final Data d, final NewInput input)
      throws IOException, QueryException {
    final IOFile file = d.meta.file(input.path, input.type);
    file.parent().md();
    if(input.type == ResourceType.BINARY) {
      try(InputStream is = input.value instanceof final Bin bin ? bin.input(info) :
        input.io.inputStream()) {
        file.write(is);
      }
    } else {
      try(DataOutput out = new DataOutput(file)) {
        Stores.write(out, input.value);
      }
    }
  }

  /**
   * Adds the contents of the source database to the target database.
   * Binary and value resources never permit duplicates: if the target already contains a
   * resource at the same path, the operation is rejected unless {@code replace} is set.
   * @param source source database
   * @param target target database
   * @param replace overwrite existing binary or value resources instead of raising a conflict
   * @throws IOException I/O exception
   * @throws QueryException conflict exception
   */
  private void copy(final Data source, final Data target, final boolean replace)
      throws IOException, QueryException {
    // insert documents
    target.insert(target.meta.size, -1, new DataClip(source));
    // move file resources
    for(final ResourceType type : Resources.BINARIES) {
      final IOFile srcDir = source.meta.dir(type), trgDir = target.meta.dir(type);
      if(srcDir != null && srcDir.exists()) {
        trgDir.md();
        for(final String path : srcDir.descendants()) {
          final IOFile srcFile = new IOFile(srcDir, path), trgFile = new IOFile(trgDir, path);
          if(trgFile.exists()) {
            if(!replace) throw DB_CONFLICT5_X.get(info, path);
            trgFile.delete();
          }
          trgFile.parent().md();
          Files.move(Paths.get(srcFile.path()), Paths.get(trgFile.path()));
        }
      }
    }
  }
}
