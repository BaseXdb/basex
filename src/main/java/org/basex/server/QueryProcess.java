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
  }

  /**
   * Constructor.
   * @param out output
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  void init(final PrintOutput out) throws IOException, QueryException {
    qp.parse();
    startTimeout(ctx.prop.num(Prop.TIMEOUT));
    monitored = true;
    ctx.lock.before(qp.ctx.updating);
    iter = qp.iter();
    item = iter.next();
    // don't serialize potential initialization parameters
    xml = qp.getSerializer(new ArrayOutput()).out(out);
  }

  /**
   * Serializes the next item and tests if more items can be returned.
   * @return result of check
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  boolean next() throws IOException, QueryException {
    final boolean more = item != null;
    if(more) {
      if(stopped) SERVERTIMEOUT.thrw(null);
      // item found: send {ITEM}
      xml.init();
      item.serialize(xml);
      item = iter.next();
    }
    return !more;
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
