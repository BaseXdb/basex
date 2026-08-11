package org.basex.http;

import java.util.*;
import java.util.function.*;

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
   * @return local address (can be {@code null})
   */
  String localAddress();

  /**
   * Returns the address of the client.
   * @return remote address (can be {@code null})
   */
  String remoteAddress();

  /**
   * Returns the host name of the client.
   * @return remote host name (can be {@code null})
   */
  String remoteHostname();

  /**
   * Returns the port of the client.
   * @return remote port (can be {@code -1})
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
   * @return cookies or {@code null}
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
    return access(session, s -> s.getAttribute(name), null);
  }

  /**
   * Returns the ID of a session.
   * @param session HTTP session (can be {@code null})
   * @return ID, or {@code null} if the session is not available
   */
  static String id(final HttpSession session) {
    return access(session, HttpSession::getId, null);
  }

  /**
   * Returns the time when a session was created.
   * @param session HTTP session (can be {@code null})
   * @return time in milliseconds, or {@code -1} if the session is not available
   */
  static long created(final HttpSession session) {
    return access(session, HttpSession::getCreationTime, -1L);
  }

  /**
   * Returns the time when a session was last accessed.
   * @param session HTTP session (can be {@code null})
   * @return time in milliseconds, or {@code -1} if the session is not available
   */
  static long accessed(final HttpSession session) {
    return access(session, HttpSession::getLastAccessedTime, -1L);
  }

  /**
   * Returns the names of the attributes of a session.
   * @param session HTTP session (can be {@code null})
   * @return names, or {@code null} if the session is not available
   */
  static String[] attributeNames(final HttpSession session) {
    return access(session, s -> Collections.list(s.getAttributeNames()).toArray(String[]::new),
        null);
  }

  /**
   * Assigns a session attribute.
   * @param session HTTP session (can be {@code null})
   * @param name name of the attribute
   * @param value value to be assigned
   * @return success flag
   */
  static boolean attribute(final HttpSession session, final String name, final Object value) {
    return access(session, s -> {
      s.setAttribute(name, value);
      return true;
    }, false);
  }

  /**
   * Removes a session attribute.
   * @param session HTTP session (can be {@code null})
   * @param name name of the attribute
   * @return success flag
   */
  static boolean remove(final HttpSession session, final String name) {
    return access(session, s -> {
      s.removeAttribute(name);
      return true;
    }, false);
  }

  /**
   * Invalidates a session.
   * @param session HTTP session (can be {@code null})
   * @return success flag
   */
  static boolean invalidate(final HttpSession session) {
    return access(session, s -> {
      s.invalidate();
      return true;
    }, false);
  }

  /**
   * Accesses a session. A container rejects every access to a session it has dropped, which can
   * happen at any time and to a session that was listed a moment ago.
   * @param <T> result type
   * @param session HTTP session (can be {@code null})
   * @param function access to the session
   * @param fallback result if the session is not available
   * @return result of the access, or the fallback
   */
  private static <T> T access(final HttpSession session, final Function<HttpSession, T> function,
      final T fallback) {
    if(session != null) {
      try {
        return function.apply(session);
      } catch(final NullPointerException | IllegalStateException ex) {
        // Jetty 12
        // - getSession: _coreRequest may be null for propagated request instances
        // - checkValidForRead: Invalid for read
        Util.debug(ex);
      }
    }
    return fallback;
  }
}
