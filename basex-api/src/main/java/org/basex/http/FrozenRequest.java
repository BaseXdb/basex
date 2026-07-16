package org.basex.http;

import java.util.*;

import jakarta.servlet.http.*;

/**
 * Request state with values captured from a live servlet request.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class FrozenRequest implements RequestState {
  /** Headers (names are case-insensitive). */
  private final TreeMap<String, List<String>> headers =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  /** Attributes. */
  private final Map<String, Object> attributes;
  /** Cookies (can be {@code null}). */
  private final Cookie[] cookies;
  /** Method. */
  private final String method;
  /** Query string (can be {@code null}). */
  private final String query;
  /** URL. */
  private final String url;
  /** URI. */
  private final String uri;
  /** Scheme. */
  private final String scheme;
  /** Server name. */
  private final String serverName;
  /** Server port. */
  private final int serverPort;
  /** Context path. */
  private final String contextPath;
  /** Local address. */
  private final String localAddress;
  /** Remote address. */
  private final String remoteAddress;
  /** Remote host name. */
  private final String remoteHostname;
  /** Remote port. */
  private final int remotePort;

  /**
   * Constructor, capturing the values of the supplied state.
   * @param state request state
   */
  FrozenRequest(final RequestState state) {
    method = state.method();
    query = state.query();
    url = state.url();
    uri = state.uri();
    scheme = state.scheme();
    serverName = state.serverName();
    serverPort = state.serverPort();
    contextPath = state.contextPath();
    localAddress = state.localAddress();
    remoteAddress = state.remoteAddress();
    remoteHostname = state.remoteHostname();
    remotePort = state.remotePort();
    for(final String name : state.headerNames()) headers.put(name, state.headers(name));
    cookies = state.cookies();
    attributes = state.attributes();
  }

  @Override
  public String method() {
    return method;
  }

  @Override
  public String query() {
    return query;
  }

  @Override
  public String url() {
    return url;
  }

  @Override
  public String uri() {
    return uri;
  }

  @Override
  public String scheme() {
    return scheme;
  }

  @Override
  public String serverName() {
    return serverName;
  }

  @Override
  public int serverPort() {
    return serverPort;
  }

  @Override
  public String contextPath() {
    return contextPath;
  }

  @Override
  public String localAddress() {
    return localAddress;
  }

  @Override
  public String remoteAddress() {
    return remoteAddress;
  }

  @Override
  public String remoteHostname() {
    return remoteHostname;
  }

  @Override
  public int remotePort() {
    return remotePort;
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
  public Cookie[] cookies() {
    return cookies;
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
  public Map<String, Object> attributes() {
    return attributes;
  }

  @Override
  public void setAttribute(final String name, final Object value) {
    attributes.put(name, value);
  }
}
