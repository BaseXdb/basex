package org.basex.http;

import jakarta.servlet.http.*;

/**
 * Provides the HTTP session of a WebSocket connection.
 *
 * @author BaseX Team, BSD License
 */
public interface WsSession {
  /**
   * Returns the HTTP session.
   * @return session, or {@code null} if none is available
   */
  HttpSession session();
}
