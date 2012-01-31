package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.io.serial.SerializerProp.*;

import java.io.IOException;
import java.io.OutputStream;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.io.out.EncodingOutput;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerProp;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * Server-side query session in the client-server architecture.
 *
 * @author BaseX Team 2005-12, BSD License
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

  /** Query info. */
  private String info = "";
  /** Serialization options. */
  private SerializerProp options;

  /**
   * Constructor.
   * @param qu query string
   * @param c database context
   */
  QueryListener(final String qu, final Context c) {
    qp = new QueryProcessor(qu, c);
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
   * Returns the query info.
   * @return query info
   */
  String info() {
    return info;
  }

  /**
   * Returns the serialization options.
   * @return serialization options
   * @throws IOException Exception
   */
  String options() throws IOException {
    init();
    return options.toString();
  }

  /**
   * Executes the query.
   * @param iter iterative evaluation
   * @param out output stream
   * @param enc encode stream
   * @throws IOException Exception
   */
  void execute(final boolean iter, final OutputStream out, final boolean enc)
      throws IOException {

    boolean mon = false;
    try {
      init();
      ctx.register(qp.ctx.updating);
      mon = true;

      // create serializer
      final Iter ir = qp.iter();
      final boolean wrap = !options.get(S_WRAP_PREFIX).isEmpty();

      // iterate through results
      final PrintOutput po =
          PrintOutput.get(enc ? new EncodingOutput(out) : out);
      if(iter && wrap) po.write(1);

      final Serializer ser = Serializer.get(po, options);
      int c = 0;
      for(Item it; (it = ir.next()) != null;) {
        if(iter && !wrap) {
          po.write(it.type.id());
          ser.reset();
        }
        ser.openResult();
        it.serialize(ser);
        ser.closeResult();
        if(iter && !wrap) {
          po.flush();
          out.write(0);
        }
        c++;
      }
      ser.close();
      if(iter && wrap) out.write(0);

      // generate query info
      final int up = qp.updates();
      final TokenBuilder tb = new TokenBuilder();
      tb.addExt(HITS_X_CC + "% %" + NL, c, c == 1 ? ITEM : ITEMS);
      tb.addExt(UPDATED_CC + "% %" + NL, up, up == 1 ? ITEM : ITEMS);
      tb.addExt(TOTAL_TIME_CC + "%", perf);
      info = tb.toString();

    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    } finally {
      try { qp.close(); } catch(final QueryException ex) { }
      if(mon) ctx.unregister(qp.ctx.updating);
    }
  }

  /**
   * Parses the query and retrieves the serialization options.
   * @throws IOException Exception
   */
  private void init() throws IOException {
    if(options != null) return;
    try {
      qp.parse();
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
    options = qp.ctx.serProp(false);
  }
}
