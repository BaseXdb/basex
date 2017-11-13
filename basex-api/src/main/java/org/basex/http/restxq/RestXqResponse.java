package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.http.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.scope.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.http.*;

/**
 * This class creates a new HTTP response.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class RestXqResponse {
  /** Function. */
  private final RestXqFunction func;
  /** Query context. */
  private final QueryContext qc;
  /** HTTP connection. */
  private final HTTPConnection conn;

  /** Output stream. */
  private OutputStream out;
  /** Status message. */
  private String message;
  /** Status code. */
  private Integer status;

  /**
   * Constructor.
   * @param func function
   * @param qc query context
   * @param conn HTTP connection
   */
  RestXqResponse(final RestXqFunction func, final QueryContext qc, final HTTPConnection conn) {
    this.func = func;
    this.qc = qc;
    this.conn = conn;
  }

  /**
   * Evaluates the specified function and serializes the result.
   * @param qe query exception (optional)
   * @throws Exception exception (including unexpected ones)
   */
  void create(final QueryException qe) throws Exception {
    // bind variables
    final StaticFunc sf = func.function;
    final Expr[] args = new Expr[sf.params.length];
    func.bind(conn, args, qe, qc);

    // assign function call and http context and register process
    qc.mainModule(MainModule.get(sf, args));
    qc.http(conn);
    qc.jc().type(RESTXQ);

    final String singleton = func.singleton;
    final RestXqSession session = new RestXqSession(conn, singleton, qc);
    String redirect = null, forward = null;

    qc.register(qc.context);
    try {
      // evaluate query
      final Iter iter = qc.iter();
      // handle response element
      final Item first = iter.next();
      if(first instanceof ANode) {
        final ANode node = (ANode) first;
        if(REST_REDIRECT.eq(node)) {
          // send redirect to browser
          final ANode ch = node.children().next();
          if(ch == null || ch.type != NodeType.TXT) throw func.error(NO_VALUE, node.name());
          redirect = string(ch.string()).trim();
        } else if(REST_FORWARD.eq(node)) {
          // server-side forwarding
          final ANode ch = node.children().next();
          if(ch == null || ch.type != NodeType.TXT) throw func.error(NO_VALUE, node.name());
          forward = string(ch.string()).trim();
        } else if(REST_RESPONSE.eq(node)) {
          // custom response
          build(node, iter);
        } else {
          // standard serialization
          serialize(first, iter, false);
        }
      } else if(singleton != null) {
        // cached serialization
        serialize(first, iter, true);
      } else {
        // standard serialization
        serialize(first, iter, false);
      }
    } finally {
      qc.close();
      qc.unregister(qc.context);
      session.close();

      if(redirect != null) {
        conn.redirect(redirect);
      } else if(forward != null) {
        conn.forward(forward);
      } else {
        qc.checkStop();
        finish();
      }
    }
  }

  /**
   * Builds a response element and creates the serialization parameters.
   * @param response response element
   * @param iter result iterator
   * @throws Exception exception (including unexpected ones)
   */
  private void build(final ANode response, final Iter iter) throws Exception {
    // don't allow attributes
    final BasicNodeIter atts = response.attributes();
    final ANode attr = atts.next();
    if(attr != null) throw func.error(UNEXP_NODE, attr);

    // parse response and serialization parameters
    SerializerOptions sp = func.output;
    String cType = null;
    for(final ANode n : response.children()) {
      // process http:response element
      if(HTTP_RESPONSE.eq(n)) {
        // check status and reason
        byte[] sta = null, msg = null;
        for(final ANode a : n.attributes()) {
          final QNm qnm = a.qname();
          if(qnm.eq(Q_STATUS)) sta = a.string();
          else if(qnm.eq(Q_REASON) || qnm.eq(Q_MESSAGE)) msg = a.string();
          else throw func.error(UNEXP_NODE, a);
        }
        if(sta != null) {
          status = toInt(sta);
          message = msg != null ? string(msg) : null;
        }

        for(final ANode c : n.children()) {
          // process http:header elements
          if(HTTP_HEADER.eq(c)) {
            final byte[] nam = c.attribute(Q_NAME);
            final byte[] val = c.attribute(Q_VALUE);
            if(nam != null && val != null) {
              final String key = string(nam), value = string(val);
              if(key.equalsIgnoreCase(HttpText.CONTENT_TYPE)) {
                cType = value;
              } else {
                conn.res.setHeader(key, key.equalsIgnoreCase(HttpText.LOCATION) ?
                  conn.resolve(value) : value);
              }
            }
          } else {
            throw func.error(UNEXP_NODE, c);
          }
        }
      } else if(OUTPUT_SERIAL.eq(n)) {
        // parse output:serialization-parameters
        sp = FuncOptions.serializer(n, func.output, func.function.info);
      } else {
        throw func.error(UNEXP_NODE, n);
      }
    }
    // set content type and serialize data
    if(cType != null) sp.set(SerializerOptions.MEDIA_TYPE, cType);

    final Item first = iter.next();
    if(first != null) checkHead();
    serialize(first, iter, sp, true);
  }

  /**
   * Serializes the first and all remaining items.
   * @param first first item
   * @param iter iterator
   * @param cache cache result
   * @throws Exception exception (including unexpected ones)
   */
  private void serialize(final Item first, final Iter iter, final boolean cache) throws Exception {
    checkHead();
    serialize(first, iter, func.output, cache);
  }

  /**
   * Serializes the first and all remaining items.
   * @param first first item
   * @param iter iterator
   * @param sp serialization parameters
   * @param cache cache result
   * @throws Exception exception (including unexpected ones)
   */
  private void serialize(final Item first, final Iter iter, final SerializerOptions sp,
      final boolean cache) throws Exception {
    conn.sopts(sp);
    conn.initResponse();
    out = cache ? new ArrayOutput() : conn.res.getOutputStream();
    Item item = first;
    try(Serializer ser = Serializer.get(out, sp)) {
      for(; item != null; item = iter.next()) {
        qc.checkStop();
        ser.serialize(item);
      }
    }
  }

  /**
   * Checks if the HEAD method was specified.
   * @throws QueryException query exception
   */
  private void checkHead() throws QueryException {
    if(func.methods.size() == 1 && func.methods.contains(HttpMethod.HEAD.name()))
      throw func.error(HEAD_METHOD);
  }

  /**
   * Finalizes result generation.
   * @throws IOException I/O exception
   */
  private void finish() throws IOException {
    if(status != null) conn.status(status, message);
    if(out instanceof ArrayOutput) {
      final ArrayOutput ao = (ArrayOutput) out;
      if(ao.size() > 0) conn.res.getOutputStream().write(ao.finish());
    }
  }
}
