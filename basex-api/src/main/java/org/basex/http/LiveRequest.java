package org.basex.http;

import java.util.*;

import jakarta.servlet.http.*;

import org.basex.util.*;

/**
 * Request state that is backed by a live servlet request.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class LiveRequest implements RequestState {
  /** HTTP servlet request. */
  private final HttpServletRequest request;

  /**
   * Constructor.
   * @param request HTTP servlet request
   */
  LiveRequest(final HttpServletRequest request) {
    this.request = request;
  }

  @Override
  public String method() {
    return request.getMethod();
  }

  @Override
  public String query() {
    return request.getQueryString();
  }

  @Override
  public String url() {
    return request.getRequestURL().toString();
  }

  @Override
  public String uri() {
    return request.getRequestURI();
  }

  @Override
  public String scheme() {
    return request.getScheme();
  }

  @Override
  public String serverName() {
    return request.getServerName();
  }

  @Override
  public int serverPort() {
    return request.getServerPort();
  }

  @Override
  public String contextPath() {
    return request.getContextPath();
  }

  @Override
  public String localAddress() {
    return request.getLocalAddr();
  }

  @Override
  public String remoteAddress() {
    return request.getRemoteAddr();
  }

  @Override
  public String remoteHostname() {
    return request.getRemoteHost();
  }

  @Override
  public int remotePort() {
    return request.getRemotePort();
  }

  @Override
  public List<String> headers(final String name) {
    return Collections.list(request.getHeaders(name));
  }

  @Override
  public List<String> headerNames() {
    return Collections.list(request.getHeaderNames());
  }

  @Override
  public Cookie[] cookies() {
    return request.getCookies();
  }

  @Override
  public HttpSession session(final boolean create) {
    try {
      return request.getSession(create);
    } catch(final NullPointerException ex) {
      // Jetty 12, getSession: _coreRequest may be null for propagated request instances
      Util.debug(ex);
      return null;
    }
  }

  @Override
  public Object attribute(final String name) {
    try {
      return request.getAttribute(name);
    } catch(final NullPointerException | IllegalStateException ex) {
      // Tomcat: https://github.com/spring-projects/spring-boot/issues/36763
      // Jetty 12.1: org.eclipse.jetty.ee11.servlet.ServletApiRequest.getAttribute (line 915)
      Util.debug(ex);
      return null;
    }
  }

  @Override
  public Map<String, Object> attributes() {
    final Map<String, Object> map = new HashMap<>();
    try {
      for(final String name : Collections.list(request.getAttributeNames())) {
        map.put(name, attribute(name));
      }
    } catch(final NullPointerException | IllegalStateException ex) {
      // see attribute(String)
      Util.debug(ex);
    }
    return map;
  }

  @Override
  public void setAttribute(final String name, final Object value) {
    request.setAttribute(name, value);
  }
}
