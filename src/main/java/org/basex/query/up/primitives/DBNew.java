package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
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
 * Update primitive for adding documents to databases.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class DBNew extends BasicOperation {
  /** Inputs to add. */
  protected List<NewInput> inputs;
  /** Query context. */
  protected final QueryContext qc;
  /** Insertion sequence. */
  protected Data md;

  /**
   * Constructor.
   * @param t type of update
   * @param d target database
   * @param in database inputs
   * @param c query context
   * @param ii input info
   */
  public DBNew(final TYPE t, final Data d, final List<NewInput> in, final QueryContext c,
      final InputInfo ii) {

    super(t, d, ii);
    inputs = in;
    qc = c;
  }

  /**
   * Inserts all documents to be added to a temporary database.
   * @param dt target database
   * @param name name of database
   * @throws QueryException query exception
   */
  final void addDocs(final MemData dt, final String name) throws QueryException {
    md = dt;
    final long ds = inputs.size();
    for(int i = 0; i < ds; i++) {
      md.insert(md.meta.size, -1, data(inputs.get(i), name));
      // clear list to recover memory
      inputs.set(i, null);
    }
    inputs = null;
  }

  /**
   * Creates a {@link DataClip} instance for the specified document.
   * @param ni new database input
   * @param dbname name of database
   * @return database clip
   * @throws QueryException query exception
   */
  private DataClip data(final NewInput ni, final String dbname) throws QueryException {
    // add document node
    final Context ctx = qc.context;
    if(ni.node != null) {
      final MemData mdata = (MemData) ni.node.dbCopy(ctx.prop).data;
      mdata.update(0, Data.DOC, ni.path);
      return new DataClip(mdata);
    }

    // add input
    final IOFile dbpath = ctx.mprop.dbpath(string(ni.dbname));
    final Parser p = new DirParser(ni.io, ctx.prop, dbpath).target(string(ni.path));
    final MemBuilder b = new MemBuilder(dbname, p);
    try {
      return new DataClip(b.build());
    } catch(final IOException ex) {
      throw IOERR.thrw(info, ex);
    }
  }
}
