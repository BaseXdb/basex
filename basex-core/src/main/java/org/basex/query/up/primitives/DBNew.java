package org.basex.query.up.primitives;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.out.DataOutput;
import org.basex.io.serial.*;
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
  /** Data clip with the new documents (can be {@code null}). */
  private DataClip clip;

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
    if(inputs.isEmpty()) return null;
    try {
      // temporary instance will be dropped when the updates have been applied or have failed
      final Data data = build(name, create);
      clip = new DataClip(data).context(qc.context);
      qc.updates().register(clip);
      for(final NewInput input : inputs) {
        if(input.type != ResourceType.XML) writeFileResource(data, input);
      }
      return clip;
    } catch(final IOException ex) {
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
   * @throws QueryException query exception
   */
  public void addTo(final Data target, final boolean replace) throws QueryException {
    try {
      copy(clip.data, target, replace);
    } catch(final IOException ex) {
      throw UPDBERROR_X.get(info, ex);
    }
  }

  /**
   * Creates a temporary database instance with the contents of all inputs.
   * @param name name of database
   * @param create create new database
   * @return database
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private Data build(final String name, final boolean create)
      throws IOException, QueryException {
    // single node: create main-memory copy
    final NewInput first = inputs.getFirst();
    if(inputs.size() == 1 && first.node != null) {
      final MemData mdata = (MemData) doc(first).copy(options.getFirst(), qc).data();
      mdata.update(0, Data.DOC, token(first.path));
      return mdata;
    }

    // binary and value resources, and raw files of new databases, require an on-disk instance
    boolean disk = false;
    final List<Parser> parsers = new ArrayList<>();
    final int is = inputs.size();
    for(int i = 0; i < is; i++) {
      final NewInput input = inputs.get(i);
      final MainOptions mopts = options.get(i);
      final boolean xml = input.type == ResourceType.XML;
      if(xml) parsers.add(input.node != null ? new NodeParser(input, mopts) :
        new DirParser(input.io, mopts).target(input.path));
      disk |= !xml || create && (mopts.get(MainOptions.PARSER) == MainParser.RAW ||
          mopts.get(MainOptions.ADDRAW) == Boolean.TRUE);
    }

    final MainOptions mopts = options.getFirst();
    final Parser parser = parsers.isEmpty() ? Parser.emptyParser(mopts) :
      parsers.size() == 1 ? parsers.getFirst() : new MultiParser(parsers);
    final StaticOptions sopts = qc.context.soptions;
    final String dbName = disk ? sopts.createTempDb(name) : name;
    final Builder builder = disk ? new DiskBuilder(dbName, parser, sopts, mopts) :
      new SpillBuilder(name, parser, sopts);
    builder.binariesDir(sopts.dbPath(dbName));
    try {
      return qc.pushJob(builder).build();
    } finally {
      qc.popJob();
    }
  }

  /**
   * Returns the node of an input, or a document node that wraps it.
   * @param input new input
   * @return document node
   */
  private static XNode doc(final NewInput input) {
    final XNode node = input.node;
    return node.kind() == Kind.DOCUMENT ? node :
      FDoc.build(token(input.path)).node(node).finish();
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
    if(file.exists()) throw DB_CONFLICT5_X.get(info, input.path);
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
   * @throws QueryException query exception
   */
  private void copy(final Data source, final Data target, final boolean replace)
      throws IOException, QueryException {
    // insert documents
    target.insert(target.nodes(), -1, new DataClip(source));
    // move file resources
    for(final ResourceType type : Resources.BINARIES) {
      final IOFile srcDir = source.meta.dir(type), trgDir = target.meta.dir(type);
      if(srcDir != null && srcDir.exists()) {
        trgDir.md();
        for(final String path : srcDir.descendants()) {
          final IOFile srcFile = new IOFile(srcDir, path), trgFile = new IOFile(trgDir, path);
          // existing targets may be directories, which cannot be replaced by a move
          if(trgFile.exists()) {
            if(!replace) throw DB_CONFLICT5_X.get(info, path);
            trgFile.delete();
          }
          srcFile.moveTo(trgFile, false);
        }
      }
    }
  }

  /**
   * Parser for node inputs.
   */
  private static final class NodeParser extends Parser {
    /** Input. */
    private final NewInput input;

    /**
     * Constructor.
     * @param input new input
     * @param mopts main options
     */
    NodeParser(final NewInput input, final MainOptions mopts) {
      super((IO) null, mopts);
      this.input = input;
    }

    @Override
    public void parse(final Builder build) throws IOException {
      new BuilderSerializer(build) {
        @Override
        protected void openDoc(final byte[] name) throws IOException {
          super.openDoc(token(input.path));
        }
      }.serialize(doc(input));
    }
  }
}
