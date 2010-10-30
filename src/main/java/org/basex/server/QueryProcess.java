package org.basex.server;

import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * Container for processes executing a query with iterative results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
final class QueryProcess extends Progress {
  /** Query processor. */
  private final QueryProcessor qp;
  /** Database context. */
  private final Context ctx;
  /** Query string. */
  public String query;

  /** Print output. */
  private PrintOutput out;
  /** Serializer. */
  private XMLSerializer xml;
  /** Monitored flag. */
  private boolean monitored;
  /** Iterator. */
  private Iter iter;
  /** Current item. */
  private Item item;

  /**
   * Constructor.
   * @param qu query string
   * @param po output stream
   * @param c database context
   * @throws QueryException query exception
   */
  QueryProcess(final String qu, final PrintOutput po, final Context c)
      throws QueryException {

    query = qu;
    qp = new QueryProcessor(qu, c);
    try {
      qp.parse();
    } catch(final QueryException ex) {
      try { qp.close(); } catch(final Exception e) { }
      throw ex;
    }
    out = po;
    ctx = c;
    startTimeout(ctx.prop.num(Prop.TIMEOUT));
  }

  /**
   * Binds an object to a global variable.
   * @param n name of variable
   * @param o object to be bound
   * @param t type
   * @throws QueryException query exception
   */
  public void bind(final String n, final String o, final String t)
      throws QueryException {
    qp.bind(n, o, t);
  }

  /**
   * Initializes the iterative evaluation.
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  public void init() throws IOException, QueryException {
    monitored = true;
    ctx.lock.before(qp.ctx.updating);
    xml = qp.getSerializer(out);
    iter = qp.iter();
  }

  /**
   * Serializes the next item and tests if more items can be returned.
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  void next() throws IOException, QueryException {
    if(xml == null) init();
    if(stopped) SERVERTIMEOUT.thrw(null);

    item = iter.next();
    if(item != null) {
      xml.init();
      xml.openResult();
      item.serialize(xml);
      xml.closeResult();
    }
  }

  /**
   * Closes the query process.
   * @param forced forced close
   * @throws IOException I/O exception
   */
  void close(final boolean forced) throws IOException {
    if(xml != null && !forced) xml.close();
    qp.stopTimeout();
    qp.close();
    if(monitored) ctx.lock.after(qp.ctx.updating);
  }
}
