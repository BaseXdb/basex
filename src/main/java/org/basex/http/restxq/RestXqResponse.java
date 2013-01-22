package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.Expr.Use;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * This class creates a new HTTP response.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqResponse {
  /** QName. */
  private static final QNm Q_STATUS = new QNm(STATUS);
  /** QName. */
  private static final QNm Q_REASON = new QNm(REASON);
  /** QName. */
  private static final QNm Q_MESSAGE = new QNm(MESSAGE);
  /** QName. */
  private static final QNm Q_NAME = new QNm(NAME);
  /** QName. */
  private static final QNm Q_VALUE = new QNm(VALUE);

  /** Serializer node test. */
  private static final ExtTest OUTPUT_SERIAL = new ExtTest(NodeType.ELM,
      FuncParams.Q_SPARAM);
  /** HTTP Response test. */
  private static final ExtTest HTTP_RESPONSE = new ExtTest(NodeType.ELM,
      new QNm(RESPONSE, QueryText.HTTPURI));
  /** RESTXQ Response test. */
  private static final ExtTest RESTXQ_RESPONSE = new ExtTest(NodeType.ELM,
      new QNm(RESPONSE, QueryText.RESTXQURI));
  /** RESTXQ Redirect test. */
  private static final ExtTest RESTXQ_REDIRECT = new ExtTest(NodeType.ELM,
      new QNm(REDIRECT, QueryText.RESTXQURI));
  /** RESTXQ Forward test. */
  private static final ExtTest RESTXQ_FORWARD = new ExtTest(NodeType.ELM,
      new QNm(FORWARD, QueryText.RESTXQURI));
  /** HTTP Header test. */
  private static final ExtTest HTTP_HEADER = new ExtTest(NodeType.ELM,
      new QNm(HEADER, QueryText.HTTPURI));

  /** Function to be evaluated. */
  private final RestXqFunction function;
  /** Query context. */
  private final QueryContext qc;
  /** HTTP context. */
  private final HTTPContext http;

  /**
   * Constructor.
   * @param rxf function to be evaluated
   * @param ctx query context
   * @param hc HTTP context
   */
  RestXqResponse(final RestXqFunction rxf, final QueryContext ctx, final HTTPContext hc) {
    function = rxf;
    qc = ctx;
    http = hc;
  }

  /**
   * Evaluates the specified function and creates a response.
   * @throws Exception exception
   */
  void create() throws Exception {
    // wrap function with a function call
    final UserFunc uf = function.function;
    final BaseFuncCall bfc = new BaseFuncCall(null, uf.name, uf.args);
    bfc.init(uf);

    // bind variables
    function.bind(http);

    // compile and evaluate function
    String redirect = null;
    String forward = null;
    try {
      // assign local updating flag
      qc.updating = bfc.uses(Use.UPD);
      qc.context(http, null);
      qc.context.register(qc);

      // set database options
      final StringList o = qc.dbOptions;
      for(int s = 0; s < o.size(); s += 2) qc.context.prop.set(o.get(s), o.get(s + 1));

      Value result = qc.value(bfc.compile(qc));
      final Value update = qc.update();
      if(update != null) result = update;

      // handle response element
      final ValueIter iter = result.iter();
      Item item = iter.next();
      ANode resp = null;
      if(item != null && item.type.isNode()) {
        final ANode node = (ANode) item;
        // send redirect to browser
        if(RESTXQ_REDIRECT.eq(node)) {
          final ANode ch = node.children().next();
          if(ch == null || ch.type != NodeType.TXT) function.error(NO_VALUE, node.name());
          redirect = string(trim(ch.string()));
          return;
        }
        // server-side forwarding
        if(RESTXQ_FORWARD.eq(node)) {
          final ANode ch = node.children().next();
          if(ch == null || ch.type != NodeType.TXT) function.error(NO_VALUE, node.name());
          forward = string(trim(ch.string()));
          return;
        }
        if(RESTXQ_RESPONSE.eq(node)) {
          resp = node;
          item = iter.next();
        }
      }

      // HEAD method may only return a single response element
      if(function.methods.size() == 1 && function.methods.contains(HTTPMethod.HEAD)) {
        if(resp == null || item != null) function.error(HEAD_METHOD);
      }

      // get serializer, initialize response and serialize result
      final SerializerProp sp = process(resp);
      http.initResponse(sp);
      final Serializer ser = Serializer.get(http.res.getOutputStream(), sp);
      for(; item != null; item = iter.next()) ser.serialize(item);
      ser.close();

    } finally {
      qc.close();
      qc.context.unregister(qc);

      if(redirect != null) {
        http.res.sendRedirect(redirect);
      } else if(forward != null) {
        http.req.getRequestDispatcher(forward).forward(http.req, http.res);
      }
    }
  }

  /**
   * Processes the response element and creates the serialization parameters.
   * @param response response element
   * @return serialization properties
   * @throws Exception exception (also unexpected ones)
   */
  private SerializerProp process(final ANode response) throws Exception {
    SerializerProp sp = function.output;

    if(response != null) {
      // don't allow attributes
      for(final ANode a : response.attributes()) function.error(UNEXP_NODE, a);

      String cType = null;
      for(final ANode n : response.children()) {
        // process http:response element
        if(HTTP_RESPONSE.eq(n)) {
          // check status and reason
          byte[] sta = null;
          byte[] msg = null;
          for(final ANode a : n.attributes()) {
            final QNm qnm = a.qname();
            if(qnm.eq(Q_STATUS)) sta = a.string();
            else if(qnm.eq(Q_REASON) || qnm.eq(Q_MESSAGE)) msg = a.string();
            else function.error(UNEXP_NODE, a);
          }
          if(sta != null) http.status(toInt(sta), msg != null ? string(msg) : null);

          for(final ANode c : n.children()) {
            // process http:header elements
            if(HTTP_HEADER.eq(c)) {
              final byte[] nam = c.attribute(Q_NAME);
              final byte[] val = c.attribute(Q_VALUE);
              if(nam != null && val != null) {
                final String key = string(nam);
                final String value = string(val);
                if(key.equals(MimeTypes.CONTENT_TYPE)) {
                  cType = value;
                } else {
                  http.res.setHeader(key, value);
                }
              }
            } else {
              function.error(UNEXP_NODE, c);
            }
          }
        } else if(OUTPUT_SERIAL.eq(n)) {
          // process output:serialization-parameters
          sp = FuncParams.serializerProp(n);
        } else {
          function.error(UNEXP_NODE, n);
        }
      }
      // set content type
      if(cType != null) {
        if(sp == null) sp = new SerializerProp(function.output.toString());
        sp.set(SerializerProp.S_MEDIA_TYPE, cType);
      }
    }
    return sp;
  }
}
