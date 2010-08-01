package org.basex.server;

import static org.basex.core.Text.*;
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
  /** Output stream reference. */
  private final PrintOutput out;
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
   * @param o output
   * @param c database context
   */
  QueryProcess(final String q, final PrintOutput o, final Context c) {
    query = q;
    qp = new QueryProcessor(q, c);
    out = o;
    ctx = c;
  }

  /**
   * Constructor.
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  void init() throws IOException, QueryException {
    qp.parse();
    if(!qp.ctx.updating) startTimeout(ctx.prop.num(Prop.TIMEOUT));
    monitored = true;
    ctx.lock.before(qp.ctx.updating);
    iter = qp.iter();
    item = iter.next();
    xml = qp.getSerializer(out);
  }

  /**
   * Serializes the next item and tests if more items can be returned.
   * @return result of check
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  boolean next() throws IOException, QueryException {
    if(stopped) throw new QueryException(SERVERTIMEOUT);

    final boolean more = item != null;
    if(more) {
      // item found: send {ITEM}
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
    if(xml != null) xml.close();
    qp.close();
    if(monitored) ctx.lock.after(qp.ctx.updating);
  }
}
