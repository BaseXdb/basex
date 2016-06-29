package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.http.*;

/**
 * This class creates a new HTTP response.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class RestXqResponse {
  /** Private constructor. */
  private RestXqResponse() { }

  /**
   * Evaluates the specified function and creates a response.
   * @param function function to be evaluated
   * @param qc query context
   * @param http HTTP context
   * @param error optional query error
   * @throws Exception exception (including unexpected ones)
   */
  static void create(final RestXqFunction function, final QueryContext qc,
      final HTTPContext http, final QueryException error) throws Exception {

    // bind variables
    final StaticFunc sf = function.function;
    final Expr[] args = new Expr[sf.args.length];
    function.bind(http, args, error, qc);

    // assign function call and http context and register process
    qc.mainModule(MainModule.get(sf, args));
    qc.http(http);
    qc.job().type(RESTXQ);
    qc.register(qc.context);

    final RestXqSession session = new RestXqSession(http, function.key, qc);
    String redirect = null, forward = null;
    RestXqRespBuilder response = null;
    try {
      // evaluate query
      final Iter iter = qc.iter();
      Item item = iter.next();

      // handle response element
      if(item instanceof ANode) {
        final ANode node = (ANode) item;
        // send redirect to browser
        if(REST_REDIRECT.eq(node)) {
          final ANode ch = node.children().next();
          if(ch == null || ch.type != NodeType.TXT) throw function.error(NO_VALUE, node.name());
          redirect = string(ch.string()).trim();
          return;
        }
        // server-side forwarding
        if(REST_FORWARD.eq(node)) {
          final ANode ch = node.children().next();
          if(ch == null || ch.type != NodeType.TXT) throw function.error(NO_VALUE, node.name());
          forward = string(ch.string()).trim();
          return;
        }
        if(REST_RESPONSE.eq(node)) {
          response = new RestXqRespBuilder();
          response.build(node, function, iter, http);
          return;
        }
      }

      // HEAD method must return a single response element
      if(function.methods.size() == 1 && function.methods.contains(HttpMethod.HEAD.name()))
        throw function.error(HEAD_METHOD);

      // serialize result
      final SerializerOptions sp = function.output;
      http.sopts(sp);
      http.initResponse();
      try(final Serializer ser = Serializer.get(http.res.getOutputStream(), sp)) {
        for(; item != null; item = iter.next()) ser.serialize(item);
      }

    } finally {
      qc.close();
      qc.unregister(qc.context);
      session.close();

      if(redirect != null) {
        http.redirect(redirect);
      } else if(forward != null) {
        http.forward(forward);
      } else if(response != null) {
        if(response.status != 0) http.status(response.status, response.message, response.error);
        final byte[] out = response.cache.finish();
        if(out.length != 0) http.res.getOutputStream().write(out);
      }
    }
  }
}
