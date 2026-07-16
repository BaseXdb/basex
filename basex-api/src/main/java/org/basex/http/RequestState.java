package org.basex.http;

import java.util.*;

import jakarta.servlet.http.*;

import org.basex.util.*;
import org.basex.util.http.*;

/**
 * State of an HTTP or WebSocket request, backed by a live servlet request or captured values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface RequestState {
  /** Forwarding headers. */
  String[] FORWARDING_HEADERS = { "X-Forwarded-For", "Proxy-Client-IP",
      "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR" };

  /**
   * Returns the request method.
   * @return method
   */
  String method();

  /**
   * Returns the query string.
   * @return query string (can be {@code null})
   */
  String query();

  /**
   * Returns the request URL.
   * @return URL
   */
  String url();

  /**
   * Returns the request URI.
   * @return URI
   */
  String uri();

  /**
   * Returns the scheme of the request.
   * @return scheme
   */
  String scheme();

  /**
   * Returns the host name of the addressed server.
   * @return host name
   */
  String serverName();

  /**
   * Returns the port of the addressed server.
   * @return port
   */
  int serverPort();

  /**
   * Returns the context path.
   * @return context path
   */
  String contextPath();

  /**
   * Returns the local address of the server.
   * @return local address
   */
  String localAddress();

  /**
   * Returns the address of the client.
   * @return remote address
   */
  String remoteAddress();

  /**
   * Returns the host name of the client.
   * @return remote host name
   */
  String remoteHostname();

  /**
   * Returns the port of the client.
   * @return remote port
   */
  int remotePort();

  /**
   * Returns the values of a header.
   * @param name name of the header
   * @return values
   */
  List<String> headers(String name);

  /**
   * Returns the names of all headers.
   * @return names
   */
  List<String> headerNames();

  /**
   * Returns all cookies.
   * @return cookies, or {@code null} if none were sent
   */
  Cookie[] cookies();

  /**
   * Returns the content type of the request as media type.
   * @return media type
   */
  default MediaType mediaType() {
    final List<String> values = headers(HTTPText.CONTENT_TYPE);
    return values.isEmpty() ? MediaType.ALL_ALL : new MediaType(values.get(0));
  }

  /**
   * Returns the original client address, resolving forwarding headers.
   * @return client address
   */
  default String originalAddress() {
    for(final String header : FORWARDING_HEADERS) {
      final List<String> values = headers(header);
      final String value = values.isEmpty() ? null : values.get(0);
      // header found: test last (most reliable) part first
      if(value != null && !value.isEmpty()) {
        String ip = null;
        final String[] entries = value.split("\\s*,\\s*");
        for(int e = entries.length; --e >= 0 && entries[e].matches("^\\[?[:.\\d]+\\]?$");) {
          ip = entries[e];
        }
        if(ip != null) return ip;
      }
    }
    return remoteAddress();
  }

  /**
   * Returns the HTTP session of the request.
   * @param create create session if none exists
   * @return session, or {@code null} if it does not exist or cannot be created
   */
  HttpSession session(boolean create);

  /**
   * Returns a request attribute.
   * @param name name of the attribute
   * @return value, or {@code null} if it does not exist or cannot be retrieved
   */
  Object attribute(String name);

  /**
   * Returns all request attributes.
   * @return map
   */
  Map<String, Object> attributes();

  /**
   * Sets a request attribute.
   * @param name name of the attribute
   * @param value value
   */
  void setAttribute(String name, Object value);

  /**
   * Returns a session attribute.
   * @param session HTTP session (can be {@code null})
   * @param name name of the attribute
   * @return value, or {@code null} if it does not exist or cannot be retrieved
   */
  static Object attribute(final HttpSession session, final String name) {
    if(session != null) {
      try {
        return session.getAttribute(name);
      } catch(final NullPointerException | IllegalStateException ex) {
        // Jetty 12
        // - getSession: _coreRequest may be null for propagated request instances
        // - checkValidForRead: Invalid for read
        Util.debug(ex);
      }
    }
    return null;
  }
}
