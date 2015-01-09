package org.basex.query.up.primitives;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Contains helper methods for adding documents.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DBNew {
  /** Query context. */
  private final QueryContext qc;
  /** Input info. */
  private final InputInfo info;

  /** Inputs to add. */
  public List<NewInput> inputs;
  /** Insertion sequence. */
  public Data data;

  /**
   * Constructor.
   * @param qc query context
   * @param inputs input
   * @param info input info
   */
  public DBNew(final QueryContext qc, final List<NewInput> inputs, final InputInfo info) {
    this.qc = qc;
    this.inputs = inputs;
    this.info = info;
  }

  /**
   * Inserts all documents to be added to a temporary database.
   * @param md temporary database
   * @param name name of database
   * @param options database options
   * @throws QueryException query exception
   */
  public void addDocs(final MemData md, final String name, final DBOptions options)
      throws QueryException {

    final MainOptions opts = new MainOptions(qc.context.options);
    options.assign(opts);
    addDocs(new MemData(md, opts), name, opts);
  }

  /**
   * Inserts all documents to be added to a temporary database.
   * @param dt target database
   * @param name name of database
   * @param options main options
   * @throws QueryException query exception
   */
  public void addDocs(final MemData dt, final String name, final MainOptions options)
      throws QueryException {

    data = dt;
    final long ds = inputs.size();
    for(int i = 0; i < ds; i++) {
      data.insert(data.meta.size, -1, data(inputs.get(i), name, options));
      // clear list to recover memory
      inputs.set(i, null);
    }
    inputs = null;
  }

  /**
   * Creates a {@link DataClip} instance for the specified document.
   * @param ni new database input
   * @param dbname name of database
   * @param options main options
   * @return database clip
   * @throws QueryException query exception
   */
  private DataClip data(final NewInput ni, final String dbname, final MainOptions options)
      throws QueryException {

    // add document node
    final Context ctx = qc.context;
    if(ni.node != null) {
      final MemData mdata = (MemData) ni.node.dbCopy(options).data;
      mdata.update(0, Data.DOC, ni.path);
      return new DataClip(mdata);
    }

    // add input
    final IOFile dbpath = ctx.soptions.dbpath(string(ni.dbname));
    try {
      final Parser parser = new DirParser(ni.io, options, dbpath).target(string(ni.path));
      return new MemBuilder(dbname, parser).dataClip();
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }
}
