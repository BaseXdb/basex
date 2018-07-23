package org.basex.http.restxq;

import static org.basex.http.web.WebText.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * <p>This servlet receives and processes REST requests.
 * The evaluated code is defined in XQuery modules, which are located in the web server's
 * root directory (specified by the {@code HTTPPATH} option), and decorated with RESTXQ
 * annotations.</p>
 *
 * <p>The implementation is based on Adam Retter's paper presented at XMLPrague 2012,
 * titled "RESTful XQuery - Standardised XQuery 3.0 Annotations for REST".</p>
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class RestXqServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPConnection conn) throws Exception {
    // no trailing slash: send redirect
    if(conn.req.getPathInfo() == null) {
      conn.redirect("/");
      return;
    }

    // analyze input path
    final RestXqModules rxm = RestXqModules.get(conn.context);

    // initialize RESTXQ
    if(conn.path().equals('/' + INIT)) {
      rxm.init();
      return;
    }

    // select the closest match for this request
    RestXqFunction func = rxm.find(conn, null);
    if(func == null) throw HTTPCode.NO_XQUERY.get();

    try {
      boolean valid = true;
      for(final RestXqFunction check : rxm.checks(conn)) {
        if(!check.process(conn, func)) {
          valid = false;
          break;
        }
      }

      // process function
      if(valid) func.process(conn, null);
    } catch(final QueryException ex) {
      // run optional error function
      func = rxm.find(conn, ex.qname());
      if(func == null) throw ex;
      func.process(conn, ex);
    }
  }

  @Override
  public String username(final HTTPConnection http) {
    // try to retrieve session id (DBA, global one)
    final HttpSession session = http.req.getSession(false);
    byte[] value = null;
    if(session != null) {
      value = token(session.getAttribute("id"));
      if((http.path() + '/').contains('/' + DBA + '/')) value = token(session.getAttribute(DBA));
    }
    return value != null ? Token.string(Token.normalize(value)) : super.username(http);
  }

  /**
   * Tries to convert the specified session value to a string.
   * @param value value
   * @return return string or {@code null}
   */
  private static byte[] token(final Object value) {
    if(value instanceof Str) return ((Str) value).string();
    if(value instanceof Atm) return ((Atm) value).string(null);
    return null;
  }
}
