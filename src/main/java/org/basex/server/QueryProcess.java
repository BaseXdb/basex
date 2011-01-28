package org.basex.server;

import static org.basex.core.Text.*;
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
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Container for processes executing a query with iterative results.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
final class QueryProcess extends Progress {
  /** Performance. */
  private final Performance p = new Performance();
  /** Query processor. */
  private final QueryProcessor qp;
  /** Database context. */
  private final Context ctx;
  /** Print output. */
  private final PrintOutput out;

  /** Query info. */
  private TokenBuilder info;
  /** Query results. */
  private int hits;

  /** Serializer. */
  private XMLSerializer xml;
  /** Monitored flag. */
  private boolean monitored;
  /** Iterator. */
  private Iter iter;
  /** Closed. */
  private boolean closed;

  /**
   * Constructor.
   * @param qu query string
   * @param po output stream
   * @param c database context
   * @throws QueryException query exception
   */
  QueryProcess(final String qu, final PrintOutput po, final Context c)
      throws QueryException {

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
  void bind(final String n, final String o, final String t)
      throws QueryException {
    qp.bind(n, o, t);
  }

  /**
   * Initializes the iterative evaluation.
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  void init() throws IOException, QueryException {
    monitored = true;
    ctx.register(qp.ctx.updating);
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
    xml.init();
    final Item it = iter.next();
    if(it != null) next(it);
  }

  /**
   * Evaluates the complete query.
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  void execute() throws IOException, QueryException {
    if(xml == null) init();
    Item it;
    while((it = iter.next()) != null) next(it);
    close(false);
  }

  /**
   * Returns the query info.
   * @throws IOException Exception
   */
  void info() throws IOException {
    initInfo();
    out.print(info.finish());
  }

  /**
   * Closes the query process.
   * @param forced forced close
   * @throws IOException I/O exception
   */
  void close(final boolean forced) throws IOException {
    if(closed) return;
    if(xml != null && !forced) xml.close();
    qp.stopTimeout();
    qp.close();
    if(monitored) ctx.unregister(qp.ctx.updating);
    initInfo();
    closed = true;
  }

  /**
   * Serializes the specified item.
   * @param it item to be serialized
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  private void next(final Item it) throws IOException, QueryException {
    if(stopped) SERVERTIME.thrw(null);
    xml.openResult();
    it.serialize(xml);
    xml.closeResult();
    hits++;
  }

  /**
   * Initializes the query info.
   */
  private void initInfo() {
    if(info == null) {
      final int up = qp.updates();
      info = new TokenBuilder();
      info.addExt(QUERYHITS + "% %" + NL, hits, hits == 1 ? VALHIT : VALHITS);
      info.addExt(QUERYUPDATED + "% %" + NL, up, up == 1 ? VALHIT : VALHITS);
      info.addExt(QUERYTOTAL + "%", p);
    }
  }
}
