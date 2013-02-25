package org.basex.server;

import static org.basex.core.Text.*;
import static org.basex.io.serial.SerializerProp.*;
import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
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
  final Performance perf = new Performance();
  /** Query info. */
  private final QueryInfo qi = new QueryInfo();
  /** Query string. */
  private final String query;
  /** Database context. */
  private final Context ctx;

  /** Query processor. */
  private QueryProcessor qp;
  /** Serialization options. */
  private SerializerProp options;
  /** Parsing flag. */
  private boolean parsed;
  /** Query info. */
  private String info = "";

  /**
   * Constructor.
   * @param qu query string
   * @param c database context
   */
  QueryListener(final String qu, final Context c) {
    query = qu;
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
      init().bind(n, v, t);
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
      init().context(v, t);
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
    if(options == null) options = parse().ctx.serParams(false);
    return options.toString();
  }

  /**
   * Returns {@code true} if the query may perform updates.
   * @return updating flag
   * @throws IOException I/O Exception
   */
  boolean updating() throws IOException {
    return parse().updating;
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
      try {
        // parses the query and registers the process
        ctx.register(parse());

        // create serializer
        qp.compile();
        qi.cmpl = perf.time();
        final Iter ir = qp.iter();
        qi.evlt = perf.time();
        options();
        final boolean wrap = !options.get(S_WRAP_PREFIX).isEmpty();

        // iterate through results
        final PrintOutput po = PrintOutput.get(enc ? new EncodingOutput(out) : out);
        if(iter && wrap) po.write(1);

        final Serializer ser = Serializer.get(po, full ? null : options);
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
          ser.serialize(it);
          if(iter && !wrap) {
            po.flush();
            out.write(0);
          }
          c++;
        }
        ser.close();
        if(iter && wrap) out.write(0);
        qi.srlz = perf.time();

        // generate query info
        info = qi.toString(qp, po, c, ctx.prop.is(Prop.QUERYINFO));

      } catch(final QueryException ex) {
        throw new BaseXException(ex);
      } catch(final StackOverflowError ex) {
        Util.debug(ex);
        throw new BaseXException(CIRCLDECL.desc);
      } catch(final ProgressException ex) {
        throw new BaseXException(TIMEOUT_EXCEEDED);
      }
    } finally {
      // close processor and unregisters the process
      if(qp != null) {
        qp.close();
        ctx.unregister(qp);
        parsed = false;
        qp = null;
      }
    }
  }

  /**
   * Initializes the query.
   * @return query processor
   * @throws IOException I/O Exception
   */
  private QueryProcessor parse() throws IOException {
    if(!parsed) {
      try {
        perf.time();
        init().parse();
        qi.pars = perf.time();
        parsed = true;
      } catch(final QueryException ex) {
        throw new BaseXException(ex);
      }
    }
    return qp;
  }

  /**
   * Returns an instance of the query processor.
   * @return query processor
   */
  private QueryProcessor init() {
    if(parsed || qp == null) {
      qp = new QueryProcessor(query, ctx);
      parsed = false;
    }
    return qp;
  }
}
