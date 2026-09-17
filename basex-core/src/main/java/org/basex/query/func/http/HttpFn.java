package org.basex.query.func.http;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Functions of the HTTP Client Module 2.0.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class HttpFn extends StandardFunc {
  /**
   * Resolves a relative URI against the static base URI.
   * @param href supplied URI
   * @return resolved URI
   */
  private String resolve(final String href) {
    final Uri uri = Uri.get(href);
    if(uri.isAbsolute() || !uri.isValid()) return href;
    final Uri base = sc().baseURI();
    return base.isValid() && base.isAbsolute() ? Token.string(base.resolve(uri).string()) : href;
  }

  /**
   * Sends an HTTP request and returns the response.
   * @param method HTTP method
   * @param body body expression (can be {@code null})
   * @param options options expression
   * @param qc query context
   * @return response record
   * @throws QueryException query exception
   */
  final XQMap send(final String method, final Expr body, final Expr options,
      final QueryContext qc) throws QueryException {

    final String href = resolve(toString(arg(0), qc));
    final RequestOptions ropts = toOptions(options, new RequestOptions(), qc);
    try {
      final Request request = ropts.request(method, info);
      if(body != null) ropts.body(body.value(qc), request, qc, info);
      return new Client(info, qc.context.options).send(href, request, qc, definition);
    } catch(final QueryException ex) {
      throw Client.error(ex, info);
    }
  }
}
