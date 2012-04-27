package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.io.serial.SerializerProp.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.util.*;

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
   * Binds a value to a global variable.
   * @param n name of variable
   * @param v value to be bound
   * @param t type
   * @throws IOException query exception
   */
  void bind(final String n, final Object v, final String t) throws IOException {
    try {
      qp.bind(n, v, t);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  /**
   * Binds a value to the context item.
   * @param v value to be bound
   * @param t type
   * @throws IOException query exception
   */
  void context(final Object v, final String t) throws IOException {
    try {
      qp.context(v, t);
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
   * @throws IOException I/O Exception
   */
  String options() throws IOException {
    init();
    return options.toString();
  }

  /**
   * Returns {@code true} if the query may perform updates.
   * @return updating flag
   * @throws IOException I/O Exception
   */
  boolean updating() throws IOException {
    init();
    return qp.updating;
  }

  /**
   * Executes the query.
   * @param iter iterative evaluation
   * @param out output stream
   * @param enc encode stream
   * @param full return full type information
   * @throws IOException I/O Exception
   */
  void execute(final boolean iter, final OutputStream out, final boolean enc,
      final boolean full) throws IOException {

    try {
      // parses the query
      init();
      try {
        // registers the process
        ctx.register(qp);

        // create serializer
        final Iter ir = qp.iter();
        final boolean wrap = !options.get(S_WRAP_PREFIX).isEmpty();

        // iterate through results
        final PrintOutput po = PrintOutput.get(enc ? new EncodingOutput(out) : out);
        if(iter && wrap) po.write(1);

        final Serializer ser = Serializer.get(po, options);
        int c = 0;
        for(Item it; (it = ir.next()) != null;) {
          if(iter && !wrap) {
            if(full) {
              po.write(it.xdmInfo());
            } else {
              po.write(it.typeId());
            }
            ser.reset();
          }
          ser.openResult();
          ser.item(it);
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
        tb.addExt(TOTAL_TIME_CC + '%', perf);
        info = tb.toString();

      } catch(final QueryException ex) {
        throw new BaseXException(ex);
      } catch(final ProgressException ex) {
        throw new BaseXException(TIMEOUT_EXCEEDED);
      } finally {
        // unregisters the process
        ctx.unregister(qp);
      }
    } finally {
      // close processor and stop monitoring
      qp.close();
    }
  }

  /**
   * Parses the query and retrieves the serialization options.
   * @throws IOException I/O Exception
   */
  private void init() throws IOException {
    if(options != null) return;
    try {
      qp.parse();
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
    options = qp.ctx.serParams(false);
  }
}
