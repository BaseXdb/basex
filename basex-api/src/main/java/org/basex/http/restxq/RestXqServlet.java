package org.basex.http.restxq;

import static org.basex.http.HTTPText.*;
import static org.basex.http.restxq.RestXqText.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * <p>This servlet receives and processes REST requests.
 * The evaluated code is defined in XQuery modules, which are located in the web server's
 * root directory (specified by the {@code HTTPPATH} option), and decorated with RESTXQ
 * annotations.</p>
 *
 * <p>The implementation is based on Adam Retter's paper presented at XMLPrague 2012,
 * titled "RESTful XQuery - Standardised XQuery 3.0 Annotations for REST".</p>
 *
 * @author BaseX Team 2005-16, BSD License
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

    // select function that closest matches the request
    RestXqFunction func = rxm.find(conn, null);
    if(func == null) throw HTTPCode.NO_XQUERY.get();

    try {
      // process function
      func.process(conn, null);
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
    Object value = null;
    if(session != null) {
      if((http.path() + '/').contains('/' + DBA + '/')) value = session.getAttribute(DBA);
      if(!(value instanceof Str)) value = session.getAttribute("id");
    }
    return value instanceof Str ? ((Str) value).toJava() : super.username(http);
  }
}
