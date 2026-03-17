package org.basex.http.restxq;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.basex.http.web.WebText.*;
import static org.basex.util.Token.*;

import java.io.*;

import jakarta.servlet.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.http.*;

/**
 * This class creates a new HTTP response.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class RestXqResponse extends WebResponse {
  /** HTTP connection. */
  private final HTTPConnection conn;

  /** Singleton. */
  private RestXqSingleton singleton;
  /** Query was registered. */
  private boolean registered;
  /** Function. */
  private RestXqFunction func;
  /** Status message. */
  private String message;
  /** Status code. */
  private Integer status;
  /** Forwarding. */
  String forward;

  /**
   * Constructor.
   * @param conn HTTP connection
   */
  RestXqResponse(final HTTPConnection conn) {
    super(conn.context);
    this.conn = conn;
  }

  @Override
  protected Expr[] init(final WebFunction function, final Object data)
      throws QueryException, IOException {

    qc = function.module.qc(ctx);
    qc.jc().type(RESTXQ);
    ctx.setExternal(conn.requestCtx);

    func = new RestXqFunction(function.function, function.module, qc);
    final MainOptions mopts = new MainOptions(ctx.options);
    func.parseAnnotations(mopts);

    if(func.singleton != null) {
      singleton = new RestXqSingleton(conn, func.singleton, qc);
    }
    return func.bind(data, conn, qc, mopts);
  }

  @Override
  public Response serialize(final boolean body) throws QueryException, IOException {
    final ArrayOutput cache = singleton != null ? new ArrayOutput() : null;
    boolean response;

    qc.register(ctx);
    registered = true;
    try {
      qc.optimize();

      // evaluate query
      final Iter iter = qc.iter();
      Item item = iter.next();
      response = item != null;

      SerializerOptions so = func.sopts;
      boolean head = true;

      // handle special cases
      if(item != null && item.type == NodeType.ELEMENT) {
        final GNode node = (XNode) item;
        if(T_REST_FORWARD.matches(node)) {
          // server-side forwarding
          final GNode ch = node.childIter().next();
          if(ch == null || ch.type != NodeType.TEXT) throw func.error(NO_VALUE_X, node.name());
          forward = string(ch.string()).trim();
          item = iter.next();
        } else if(T_REST_RESPONSE.matches(node)) {
          // custom response
          so = build(node);
          item = iter.next();
          head = item != null;
        }
      }
      if(head && func.methods.size() == 1 && func.methods.contains(Method.HEAD.name()))
        throw func.error(HEAD_METHOD);

      // initialize serializer
      conn.sopts(so);
      conn.initResponse();
      if(cache == null) conn.timing(qc.info);

      if(status != null) {
        final int s = status;
        final StringBuilder msg = new StringBuilder();
        if(message != null) msg.append(message);
        if(s == 302) {
          if(!msg.isEmpty()) msg.append("; ");
          msg.append(HTTPText.LOCATION + ": ").append(conn.response.getHeader(HTTPText.LOCATION));
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
      if(cache != null) conn.timing(qc.info);
    }

    // write cached result
    if(cache != null) {
      final int size = (int) cache.size();
      if(size > 0) conn.response.getOutputStream().write(cache.buffer(), 0, size);
    }

    return status != null || forward != null ? Response.CUSTOM :
      response ? Response.STANDARD : Response.NONE;
  }

  @Override
  public void finish() throws IOException, ServletException {
    if(qc != null) {
      qc.close();
      if(registered) qc.unregister(ctx);
    }
    if(singleton != null) singleton.unregister();
    if(forward != null) {
      conn.log(SC_NO_CONTENT, "");
      conn.forward(forward);
    }
  }

  /**
   * Builds a response element and creates the serialization parameters.
   * @param response response element
   * @return serialization parameters
   * @throws QueryException query exception (including unexpected ones)
   */
  private SerializerOptions build(final GNode response) throws QueryException {
    // don't allow attributes
    final BasicNodeIter atts = response.attributeIter();
    final GNode attr = atts.next();
    if(attr != null) throw func.error(UNEXP_NODE_X, attr);

    // parse response and serialization parameters
    final SerializerOptions sopts = func.sopts;
    String cType = null;
    for(final GNode node : response.childIter()) {
      // process http:response element
      if(T_HTTP_RESPONSE.matches(node)) {
        // check status and reason
        byte[] sta = null, msg = null;
        for(final GNode a : node.attributeIter()) {
          final QNm qnm = a.qname();
          if(qnm.eq(Q_STATUS)) sta = a.string();
          else if(qnm.eq(Q_REASON) || qnm.eq(Q_MESSAGE)) msg = a.string();
          else throw func.error(UNEXP_NODE_X, a);
        }
        if(sta != null) {
          status = toInt(sta);
          message = msg != null ? string(msg) : null;
        }

        for(final GNode gchild : node.childIter()) {
          final XNode child = (XNode) gchild;
          // process http:header elements
          if(T_HTTP_HEADER.matches(child)) {
            final byte[] name = child.attribute(Q_NAME), value = child.attribute(Q_VALUE);
            if(name != null && value != null) {
              final String n = string(name), v = string(value);
              if(n.equalsIgnoreCase(HTTPText.CONTENT_TYPE)) {
                cType = v;
              } else {
                conn.response.setHeader(n, n.equalsIgnoreCase(HTTPText.LOCATION) ?
                  conn.resolve(v) : v);
              }
            }
          } else {
            throw func.error(UNEXP_NODE_X, child);
          }
        }
      } else if(T_OUTPUT_SERIAL.matches(node)) {
        // parse output:serialization-parameters
        sopts.assign(node, func.function.info);
      } else {
        throw func.error(UNEXP_NODE_X, node);
      }
    }
    if(status == null) status = SC_OK;

    // set content type and serialize data
    if(cType != null) sopts.set(SerializerOptions.MEDIA_TYPE, cType);
    return sopts;
  }
}
