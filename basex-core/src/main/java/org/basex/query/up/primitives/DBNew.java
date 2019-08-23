package org.basex.query.up.primitives;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Contains helper methods for adding documents.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class DBNew {
  /** Inputs to be added. */
  public final List<NewInput> inputs;
  /** Insertion sequence. */
  public Data data;

  /** Query context. */
  private final QueryContext qc;
  /** Input info. */
  private final InputInfo info;
  /** Main options for all inputs to be added. */
  private final List<DBOptions> dboptions;

  /**
   * Constructor.
   * @param qc query context
   * @param options database options
   * @param info input info
   * @param list list of inputs
   */
  public DBNew(final QueryContext qc, final DBOptions options, final InputInfo info,
      final NewInput... list) {

    this.qc = qc;
    this.info = info;

    final int is = list.length;
    inputs = new ArrayList<>(is);
    dboptions = new ArrayList<>(is);
    for(final NewInput input : list) {
      inputs.add(input);
      dboptions.add(options);
    }
  }

  /**
   * Merges updates.
   * @param add inputs to be added
   */
  public void merge(final DBNew add) {
    inputs.addAll(add.inputs);
    dboptions.addAll(add.dboptions);
  }

  /**
   * Inserts all documents to be added to a temporary database.
   * @param name name of database
   * @throws QueryException query exception
   */
  public void prepare(final String name) throws QueryException {
    if(inputs.isEmpty()) return;

    // choose first options instance (relevant options are the same)
    final Context ctx = qc.context;
    final MainOptions mopts = ctx.options;
    final StaticOptions sopts = ctx.soptions;
    final boolean cache = cache();
    try {
      final String dbname = cache ? sopts.randomDbName(name) : name;
      data = cache ? CreateDB.create(dbname, Parser.emptyParser(mopts), ctx, mopts) :
        new MemData(mopts);
      data.startUpdate(mopts);
      try {
        final long is = inputs.size();
        for(int i = 0; i < is; i++) {
          final Data tmpData = tmpData(dbname, i);
          try {
            data.insert(data.meta.size, -1, new DataClip(tmpData));
          } finally {
            DropDB.drop(tmpData, sopts);
          }
        }
      } finally {
        data.finishUpdate(mopts);
      }
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }

  /**
   * Drops the temporary database instance.
   */
  public void finish() {
    if(data != null) {
      DropDB.drop(data, qc.context.soptions);
      data = null;
    }
  }

  /**
   * Checks if caching was enabled for at least one document.
   * @return result of check
   */
  private boolean cache() {
    for(final DBOptions dbopts : dboptions) {
      final Object cache = dbopts.get(MainOptions.ADDCACHE);
      if(cache instanceof Boolean && (Boolean) cache) return true;
    }
    return false;
  }

  /**
   * Creates a temporary database instance with the contents of the specified input.
   * @param name name of database
   * @param i index of current input
   * @return database
   * @throws IOException I/O exception
   */
  private Data tmpData(final String name, final int i) throws IOException {
    // free memory: clear list entries after retrieval
    final NewInput input = inputs.get(i);
    final MainOptions mopts = dboptions.get(i).assignTo(new MainOptions(qc.context.options, true));
    inputs.set(i, null);
    dboptions.set(i, null);

    // existing node: create data clip for copied instance
    ANode node = input.node;
    if(node != null) {
      if(node.type != NodeType.DOC) node = new FDoc(name).add(node);
      final MemData mdata = (MemData) node.copy(mopts, qc).data();
      mdata.update(0, Data.DOC, token(input.path));
      return mdata;
    }

    final StaticOptions sopts = qc.context.soptions;
    final DirParser parser = new DirParser(input.io, mopts);
    parser.target(input.path);

    final Builder builder;
    // create temporary database on disk if requested, or if binary data needs to be written
    if(mopts.get(MainOptions.ADDCACHE) || parser.binary()) {
      builder = new DiskBuilder(sopts.randomDbName(name), parser, sopts, mopts);
    } else {
      builder = new MemBuilder(name, parser);
    }
    return builder.binaryDir(sopts.dbPath(name)).build();
  }
}
