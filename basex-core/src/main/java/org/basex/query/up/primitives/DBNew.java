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
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Contains helper methods for adding documents.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class DBNew {
  /** Inputs to add. */
  public final List<NewInput> inputs;
  /** Insertion sequence. */
  public Data data;

  /** Query context. */
  private final QueryContext qc;
  /** Input info. */
  private final InputInfo info;
  /** Main options. */
  private final List<DBOptions> dboptions = new ArrayList<>();

  /**
   * Constructor.
   * @param qc query context
   * @param inputs input
   * @param options database options
   * @param info input info
   */
  public DBNew(final QueryContext qc, final List<NewInput> inputs, final DBOptions options,
      final InputInfo info) {

    this.qc = qc;
    this.inputs = inputs;
    this.info = info;

    final int is = inputs.size();
    for(int i = 0; i < is; i++) dboptions.add(options);
  }

  /**
   * Merges updates.
   * @param add inputs to be added
   */
  public void merge(final DBNew add) {
    final int is = add.inputs.size();
    for(int i = 0; i < is; i++) {
      inputs.add(add.inputs.get(i));
      dboptions.add(add.dboptions.get(i));
    }
  }

  /**
   * Inserts all documents to be added to a temporary database.
   * @param name name of database
   * @throws QueryException query exception
   */
  public void prepare(final String name) throws QueryException {
    if(inputs.isEmpty()) return;

    // cache data if at least one input needs to be cached
    boolean cache = false;
    for(final DBOptions dbopts : dboptions) {
      final Object obj = dbopts.get(MainOptions.ADDCACHE);
      if(obj instanceof Boolean && ((Boolean) obj).booleanValue()) {
        cache = true;
        break;
      }
    }

    // choose first options instance (relevant options are the same)
    final Context ctx = qc.context;
    final MainOptions mopts = ctx.options;
    final StaticOptions sopts = ctx.soptions;
    try {
      data = cache ? CreateDB.create(sopts.randomDbName(name),
          Parser.emptyParser(mopts), ctx, mopts) : new MemData(mopts);
      data.startUpdate(mopts);
      final long ds = inputs.size();
      for(int i = 0; i < ds; i++) {
        final DataClip clip = data(name, i);
        // clear list to recover memory
        inputs.set(i, null);
        try {
          data.insert(data.meta.size, -1, clip);
        } finally {
          DropDB.drop(clip.data, sopts);
        }
      }
      data.finishUpdate(mopts);
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }

  /**
   * Finalizes the operation.
   */
  public void finish() {
    DropDB.drop(data, qc.context.soptions);
  }

  /**
   * Creates a {@link DataClip} instance for the specified document.
   * @param name name of database
   * @param i index of current input
   * @return database clip
   * @throws IOException I/O exception
   */
  private DataClip data(final String name, final int i) throws IOException {
    // add document node
    final Context ctx = qc.context;
    final StaticOptions soptions = ctx.soptions;
    final NewInput input = inputs.get(i);
    final MainOptions mopts = dboptions.get(i).assignTo(new MainOptions(qc.context.options, true));
    final boolean addcache = mopts.get(MainOptions.ADDCACHE);

    ANode node = input.node;
    if(node != null) {
      if(node.type != NodeType.DOC) node = new FDoc(name).add(node);
      final MemData mdata = (MemData) node.dbNodeCopy(mopts).data();
      mdata.update(0, Data.DOC, token(input.path));
      return new DataClip(mdata);
    }

    // add input
    final String dbpath = soptions.randomDbName(name);
    final Parser parser = new DirParser(input.io, mopts, new IOFile(dbpath)).target(input.path);
    return (addcache ? new DiskBuilder(dbpath, parser, soptions, mopts)
                     : new MemBuilder(name, parser)).dataClip();
  }
}
