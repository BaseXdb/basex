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
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Contains helper methods for adding documents.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class DBNew {
  /** Inputs to be added. */
  public final List<NewInput> inputs;

  /** Query context. */
  private final QueryContext qc;
  /** Input info. */
  private final InputInfo info;
  /** Main options for all inputs to be added. */
  private final List<MainOptions> options;
  /** New database nodes. */
  private Data data;

  /**
   * Constructor.
   * @param qc query context
   * @param opts main options
   * @param info input info
   * @param list list of inputs
   */
  public DBNew(final QueryContext qc, final MainOptions opts, final InputInfo info,
      final NewInput... list) {

    this.qc = qc;
    this.info = info;

    final int ll = list.length;
    inputs = new ArrayList<>(ll);
    options = new ArrayList<>(ll);
    for(final NewInput input : list) {
      inputs.add(input);
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
                copy(tmpData, data);
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
    } catch(final IOException | QueryException ex) {
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
   * @throws QueryException exception
   */
  public void addTo(final Data target) throws QueryException {
    try {
      copy(data, target);
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
    for(final MainOptions dbopts : options) {
      Object v = dbopts.get(MainOptions.ADDCACHE);
      if(v instanceof Boolean && (Boolean) v) return true;
      if(create) {
        if(dbopts.get(MainOptions.PARSER) == MainParser.RAW) return true;
        v = dbopts.get(MainOptions.ADDRAW);
        if(v instanceof Boolean && (Boolean) v) return true;
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
    ANode node = input.node;
    if(node != null) {
      if(node.type != NodeType.DOCUMENT_NODE) node = new FDoc(name).add(node);
      final MemData mdata = (MemData) node.copy(mopts, qc).data();
      mdata.update(0, Data.DOC, token(input.path));
      return mdata;
    }

    final StaticOptions sopts = qc.context.soptions;
    final Parser parser = new DirParser(input.io, mopts).target(input.path);

    // create temporary database on disk if requested, or if binary data needs to be written
    final Builder builder;
    final String dbname = cache ? sopts.createTempDb(name) : name;
    if(cache) {
      builder = new DiskBuilder(dbname, parser, sopts, mopts);
    } else {
      builder = new MemBuilder(dbname, parser);
    }
    builder.binariesDir(sopts.dbPath(dbname));
    return builder.build();
  }

  /**
   * Adds the contents of the source database to the target database.
   * @param source source database
   * @param target target database
   * @throws IOException I/O exception
   */
  private static void copy(final Data source, final Data target) throws IOException {
    // insert documents
    target.insert(target.meta.size, -1, new DataClip(source));
    // move file resources
    final IOFile srcDir = source.meta.dir(), trgDir = target.meta.dir();
    if(srcDir != null && srcDir.exists()) {
      trgDir.md();
      for(final String path : srcDir.descendants()) {
        final IOFile srcFile = new IOFile(srcDir, path), trgFile = new IOFile(trgDir, path);
        trgFile.delete();
        trgFile.parent().md();
        Files.move(Paths.get(srcFile.path()), Paths.get(trgFile.path()));
      }
    }
  }
}
