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
 * Contains helper methods for adding documents.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class DBNew {
  /** Query context. */
  private final QueryContext qc;
  /** Input info. */
  private final InputInfo info;

  /** Inputs to add. */
  List<NewInput> inputs;
  /** Insertion sequence. */
  Data md;

  /**
   * Constructor.
   * @param qc query context
   * @param inputs input
   * @param info input info
   */
  DBNew(final QueryContext qc, final List<NewInput> inputs, final InputInfo info) {
    this.qc = qc;
    this.inputs = inputs;
    this.info = info;
  }

  /**
   * Inserts all documents to be added to a temporary database.
   * @param dt target database
   * @param name name of database
   * @throws QueryException query exception
   */
  void addDocs(final MemData dt, final String name) throws QueryException {
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
      final MemData mdata = (MemData) ni.node.dbCopy(ctx.options).data;
      mdata.update(0, Data.DOC, ni.path);
      return new DataClip(mdata);
    }

    // add input
    final IOFile dbpath = ctx.globalopts.dbpath(string(ni.dbname));
    try {
      final Parser p = new DirParser(ni.io, ctx.options, dbpath).target(string(ni.path));
      final MemBuilder b = new MemBuilder(dbname, p);
      return new DataClip(b.build());
    } catch(final IOException ex) {
      throw IOERR.get(info, ex);
    }
  }
}
