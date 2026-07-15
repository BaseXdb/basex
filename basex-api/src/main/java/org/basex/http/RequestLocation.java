package org.basex.http;

/**
 * Location of an HTTP or WebSocket request. For WebSocket connections, the values are captured
 * during the handshake, before the servlet request is recycled.
 *
 * @author BaseX Team, BSD License
 * @param query query string (can be {@code null})
 * @param url request URL
 * @param uri request URI
 */
public record RequestLocation(String query, String url, String uri) {
}
