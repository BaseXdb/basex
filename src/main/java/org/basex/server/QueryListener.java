package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;

import org.basex.core.MainProp;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.XMLSerializer;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Server-side query session in the client-server architecture.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 * @author Christian Gruen
 */
final class QueryListener extends Progress {
  /** Performance. */
  private final Performance perf = new Performance();
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
  QueryListener(final String qu, final PrintOutput po, final Context c)
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
    startTimeout(ctx.mprop.num(MainProp.TIMEOUT));
  }

  /**
   * Binds an object to a global variable.
   * @param n name of variable
   * @param o object to be bound
   * @param t type
   * @throws QueryException query exception
   */
  void bind(final String n, final Object o, final String t)
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
   * @return {@code true} if a new item was serialized
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  boolean next() throws IOException, QueryException {
    if(xml == null) init();
    xml.init();
    final Item it = iter.next();
    if(it == null) return false;
    next(it);
    return true;
  }

  /**
   * Evaluates the complete query.
   * @throws IOException Exception
   * @throws QueryException query exception
   */
  void execute() throws IOException, QueryException {
    if(xml == null) init();
    for(Item it; (it = iter.next()) != null;) next(it);
    close(false);
  }

  /**
   * Prints the query info.
   * @throws IOException Exception
   */
  void printInfo() throws IOException {
    out.print(info());
  }

  /**
   * Returns the query info.
   * @return query info
   */
  byte[] info() {
    initInfo();
    return info.finish();
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
      info.addExt(QUERYTOTAL + "%", perf);
    }
  }
}
