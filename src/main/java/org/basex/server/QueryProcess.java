package org.basex.server;

import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.XMLSerializer;
import org.basex.io.ArrayOutput;
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
   * @param q query string
   * @param c database context
   */
  QueryProcess(final String q, final Context c) {
    query = q;
    qp = new QueryProcessor(q, c);
    ctx = c;
    startTimeout(ctx.prop.num(Prop.TIMEOUT));
  }

  /**
   * Binds an object to a global variable.
   * @param n name of variable
   * @param o object to be bound
   * @throws QueryException query exception
   */
  public void bind(final String n, final Object o) throws QueryException {
    qp.bind(n, o);
  }

  /**
   * Serializes the next item and tests if more items can be returned.
   * @param out output
   * @return result of check
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  boolean next(final PrintOutput out) throws IOException, QueryException {
    if(xml == null) {
      qp.parse();
      monitored = true;
      ctx.lock.before(qp.ctx.updating);
      iter = qp.iter();
      xml = qp.getSerializer(new ArrayOutput()).out(out);
    }
    if(stopped) SERVERTIMEOUT.thrw(null);
    
    item = iter.next();
    if(item == null) return false;
    
    // item found: send {ITEM}
    xml.init();
    item.serialize(xml);
    return true;
  }

  /**
   * Closes the query process.
   * @throws IOException I/O exception
   */
  void close() throws IOException {
    qp.stopTimeout();
    if(xml != null) xml.out(new ArrayOutput()).close();
    qp.close();
    if(monitored) ctx.lock.after(qp.ctx.updating);
  }
}
