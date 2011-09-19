package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.io.serial.SerializerProp.*;
import static org.basex.query.util.Err.*;

import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.core.Progress;
import org.basex.io.out.ArrayOutput;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerProp;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.list.TokenList;

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
  private String info = "";
  /** Serialization options. */
  private String options = "";

  /** Cached results. */
  private TokenList cache;
  /** Result iterator. */
  private int pos;

  /**
   * Constructor.
   * @param qu query string
   * @param po output stream
   * @param c database context
   */
  QueryListener(final String qu, final PrintOutput po, final Context c) {
    qp = new QueryProcessor(qu, c);
    out = po;
    ctx = c;
  }

  /**
   * Binds an object to a global variable.
   * @param n name of variable
   * @param o object to be bound
   * @param t type
   * @throws IOException query exception
   */
  void bind(final String n, final Object o, final String t) throws IOException {
    try {
      qp.bind(n, o, t);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  /**
   * Serializes the next item and tests if more items can be returned.
   * @return {@code true} if a new item was serialized
   * @throws IOException Exception
   */
  boolean next() throws IOException {
    if(cache == null) execute(true);
    if(pos == cache.size()) return false;
    out.print(cache.get(pos));
    cache.set(pos++, null);
    return true;
  }

  /**
   * Evaluates the complete query.
   * @throws IOException Exception
   */
  void execute() throws IOException {
    execute(false);
  }

  /**
   * Returns the query info.
   * @return query info
   */
  String info() {
    return info;
  }

  /**
   * Returns the serialization options.
   * @return serialization options
   */
  String options() {
    return options;
  }

  /**
   * Closes the query.
   */
  void close() {
    if(cache != null) cache = null;
  }

  /**
   * Initializes the iterative evaluation.
   * @param caching cache results
   * @throws IOException Exception
   */
  private void execute(final boolean caching) throws IOException {
    startTimeout(ctx.mprop.num(MainProp.TIMEOUT));
    boolean mon = false;
    try {
      qp.parse();
      ctx.register(qp.ctx.updating);
      mon = true;

      // create serializer
      final Iter iter = qp.iter();
      final SerializerProp sprop = qp.ctx.serProp(false);
      final boolean wrap = !sprop.get(S_WRAP_PREFIX).isEmpty();
      options = qp.ctx.serProp(false).toString();

      // prepare caching
      ArrayOutput ao = new ArrayOutput();
      cache = new TokenList();

      // iterate through results
      final Serializer ser = Serializer.get(caching ? ao : out, sprop);
      int c = 0;
      for(Item it; (it = iter.next()) != null;) {
        if(stopped) SERVERTIME.thrw(null);
        if(caching && !wrap) ser.reset();
        ser.openResult();
        it.serialize(ser);
        ser.closeResult();
        if(caching && !wrap) {
          cache.add(ao.toArray());
          ao.reset();
        }
        c++;
      }
      ser.close();
      if(caching && ao.size() != 0) {
        cache.add(ao.toArray());
        ao.reset();
      }

      // generate query info
      final int up = qp.updates();
      final TokenBuilder tb = new TokenBuilder();
      tb.addExt(QUERYHITS + "% %" + NL, c, c == 1 ? VALHIT : VALHITS);
      tb.addExt(QUERYUPDATED + "% %" + NL, up, up == 1 ? VALHIT : VALHITS);
      tb.addExt(QUERYTOTAL + "%", perf);
      info = tb.toString();

    } catch(final QueryException ex) {
      close();
      throw new BaseXException(ex);
    } finally {
      try { qp.close(); } catch(final IOException ex) { }
      if(mon) ctx.unregister(qp.ctx.updating);
      qp.stopTimeout();
    }
  }
}
