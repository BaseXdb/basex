package org.basex.http.restxq;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.http.web.WebText.*;
import static org.basex.util.Token.*;

import java.io.*;

import javax.servlet.*;

import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * This class creates a new HTTP response.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class RestXqResponse extends WebResponse {
  /** HTTP connection. */
  private final HTTPConnection conn;

  /** Function. */
  private RestXqFunction func;
  /** Status message. */
  private String message;
  /** Status code. */
  private Integer status;

  /**
   * Constructor.
   * @param conn HTTP connection
   */
  RestXqResponse(final HTTPConnection conn) {
    super(conn.context);
    this.conn = conn;
  }

  @Override
  protected void init(final WebFunction function) throws QueryException, IOException {
    final Performance perf = new Performance();
    func = new RestXqFunction(function.function, qc, function.module);
    ctx.setExternal(conn.requestCtx);
    qc.jc().type(RESTXQ);
    func.parse(ctx);
    qc.info.parsing = perf.ns();
  }

  @Override
  protected void bind(final Expr[] args, final Object data) throws QueryException, IOException {
    func.bind(args, data, conn, qc);
  }

  @Override
  public Response serialize(final boolean body) throws QueryException, IOException,
      ServletException {

    final String id = func.singleton;
    final RestXqSingleton singleton = id != null ? new RestXqSingleton(conn, id, qc) : null;
    final ArrayOutput cache = id != null ? new ArrayOutput() : null;
    String forward = null;
    boolean response;

    qc.register(ctx);
    final Performance perf = qc.jc().performance;
    final QueryInfo qi = qc.info;
    try {
      qc.compile();
      qi.compiling = perf.ns();

      // evaluate query
      final Iter iter = qc.iter();
      qi.evaluating = perf.ns();
      Item item = iter.next();
      response = item != null;

      SerializerOptions so = func.output;
      boolean head = true;

      // handle special cases
      if(item != null && item.type == NodeType.ELEMENT) {
        final ANode node = (ANode) item;
        if(REST_FORWARD.matches(node)) {
          // server-side forwarding
          final ANode ch = node.childIter().next();
          if(ch == null || ch.type != NodeType.TEXT) throw func.error(NO_VALUE_X, node.name());
          forward = string(ch.string()).trim();
          item = iter.next();
        } else if(REST_RESPONSE.matches(node)) {
          // custom response
          so = build(node);
          item = iter.next();
          head = item != null;
        }
      }
      if(head && func.methods.size() == 1 && func.methods.contains(HttpMethod.HEAD.name()))
        throw func.error(HEAD_METHOD);

      // initialize serializer
      conn.sopts(so);
      conn.initResponse();
      if(cache == null) conn.timing(qi);

      if(status != null) {
        final int s = status;
        final StringBuilder msg = new StringBuilder();
        if(message != null) msg.append(message);
        if(s == 302) {
          if(msg.length() != 0) msg.append("; ");
          msg.append(HttpText.LOCATION + ": ").append(conn.response.getHeader(HttpText.LOCATION));
        }
        conn.log(s, msg.toString());
        conn.status(s, message, null);
      }

      // serialize result
      if(item != null && body) {
        final OutputStream out = cache != null ? cache : conn.response.getOutputStream();
        try(Serializer ser = Serializer.get(out, so)) {
          for(; item != null; item = qc.next(iter)) ser.serialize(item);
        }
      }

    } finally {
      qc.close();
      qi.serializing = perf.ns();
      if(cache != null) conn.timing(qi);

      qc.unregister(ctx);
      if(singleton != null) singleton.unregister();

      if(forward != null) {
        conn.forward(forward);
      } else {
        qc.checkStop();
      }
    }

    // write cached result
    if(cache != null) {
      final int size = (int) cache.size();
      if(size > 0) conn.response.getOutputStream().write(cache.buffer(), 0, size);
    }

    return status != null || forward != null ? Response.CUSTOM :
      response ? Response.STANDARD : Response.NONE;
  }

  /**
   * Builds a response element and creates the serialization parameters.
   * @param response response element
   * @return serialization parameters
   * @throws QueryException query exception (including unexpected ones)
   */
  private SerializerOptions build(final ANode response) throws QueryException {
    // don't allow attributes
    final BasicNodeIter atts = response.attributeIter();
    final ANode attr = atts.next();
    if(attr != null) throw func.error(UNEXP_NODE_X, attr);

    // parse response and serialization parameters
    SerializerOptions sp = func.output;
    String cType = null;
    for(final ANode node : response.childIter()) {
      // process http:response element
      if(HTTP_RESPONSE.matches(node)) {
        // check status and reason
        byte[] sta = null, msg = null;
        for(final ANode a : node.attributeIter()) {
          final QNm qnm = a.qname();
          if(qnm.eq(Q_STATUS)) sta = a.string();
          else if(qnm.eq(Q_REASON) || qnm.eq(Q_MESSAGE)) msg = a.string();
          else throw func.error(UNEXP_NODE_X, a);
        }
        if(sta != null) {
          status = toInt(sta);
          message = msg != null ? string(msg) : null;
        }

        for(final ANode c : node.childIter()) {
          // process http:header elements
          if(HTTP_HEADER.matches(c)) {
            final byte[] nam = c.attribute(Q_NAME);
            final byte[] val = c.attribute(Q_VALUE);
            if(nam != null && val != null) {
              final String key = string(nam), value = string(val);
              if(key.equalsIgnoreCase(HttpText.CONTENT_TYPE)) {
                cType = value;
              } else {
                conn.response.setHeader(key, key.equalsIgnoreCase(HttpText.LOCATION) ?
                  conn.resolve(value) : value);
              }
            }
          } else {
            throw func.error(UNEXP_NODE_X, c);
          }
        }
      } else if(OUTPUT_SERIAL.matches(node)) {
        // parse output:serialization-parameters
        sp = FuncOptions.serializer(node, func.output, func.function.info);
      } else {
        throw func.error(UNEXP_NODE_X, node);
      }
    }
    if(status == null) status = SC_OK;

    // set content type and serialize data
    if(cType != null) sp.set(SerializerOptions.MEDIA_TYPE, cType);
    return sp;
  }
}
