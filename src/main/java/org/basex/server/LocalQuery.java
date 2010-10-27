package org.basex.server;

import java.io.IOException;
import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.io.ArrayOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * This class defines all methods for iteratively evaluating queries with the
 * local architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class LocalQuery extends Query {
  /** Query processor. */
  private final QueryProcessor qp;
  /** Local session. */
  private final Context ctx;
  /** Monitored flag. */
  private boolean monitored;
  /** Query iterator. */
  private Iter iter;
  /** Next item. */
  private Item item;

  /**
   * Standard constructor.
   * @param query query to be run
   * @param context database context
   */
  public LocalQuery(final String query, final Context context) {
    qp = new QueryProcessor(query, context);
    ctx = context;
  }

  @Override
  public void bind(final String n, final Object o) throws BaseXException {
    try {
      qp.bind(n, o);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public boolean more() throws BaseXException {
    try {
      if(iter == null) {
        qp.parse();
        monitored = true;
        ctx.lock.before(qp.ctx.updating);
        iter = qp.iter();
      }
      item = iter.next();
      return item != null;
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public void next(final OutputStream out) throws BaseXException {
    try {
      item.serialize(qp.getSerializer(out));
    } catch(final Exception ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public String next() throws BaseXException {
    final ArrayOutput out = new ArrayOutput();
    next(out);
    return out.toString();
  }

  @Override
  public void close() throws BaseXException {
    try {
      qp.close();
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    } finally {
      if(monitored) ctx.lock.after(qp.ctx.updating);
    }
  }
}
