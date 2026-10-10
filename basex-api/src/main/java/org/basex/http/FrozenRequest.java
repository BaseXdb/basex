package org.basex.http;

import java.util.*;

import jakarta.servlet.http.*;

/**
 * Request state with values captured from a live servlet request.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 *
 * @param method method
 * @param query query string (can be {@code null})
 * @param url URL
 * @param uri URI
 * @param scheme scheme
 * @param serverName server name
 * @param serverPort server port
 * @param contextPath context path
 * @param localAddress local address
 * @param remoteAddress remote address
 * @param remoteHostname remote host name
 * @param remotePort remote port
 * @param headers headers (names are case-insensitive)
 * @param cookies cookies (can be {@code null})
 * @param attributes attributes
 */
record FrozenRequest(String method, String query, String url, String uri, String scheme,
    String serverName, int serverPort, String contextPath, String localAddress,
    String remoteAddress, String remoteHostname, int remotePort,
    TreeMap<String, List<String>> headers, Cookie[] cookies, Map<String, Object> attributes)
    implements RequestState {

  /**
   * Captures the values of the supplied state.
   * @param state request state
   * @return frozen request
   */
  static FrozenRequest of(final RequestState state) {
    final TreeMap<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for(final String name : state.headerNames()) headers.put(name, state.headers(name));
    return new FrozenRequest(state.method(), state.query(), state.url(), state.uri(),
        state.scheme(), state.serverName(), state.serverPort(), state.contextPath(),
        state.localAddress(), state.remoteAddress(), state.remoteHostname(), state.remotePort(),
        headers, state.cookies(), state.attributes());
  }

  @Override
  public List<String> headers(final String name) {
    final List<String> values = headers.get(name);
    return values != null ? values : List.of();
  }

  @Override
  public List<String> headerNames() {
    return List.copyOf(headers.keySet());
  }

  @Override
  public HttpSession session(final boolean create) {
    return null;
  }

  @Override
  public Object attribute(final String name) {
    return attributes.get(name);
  }

  @Override
  public void setAttribute(final String name, final Object value) {
    attributes.put(name, value);
  }
}
