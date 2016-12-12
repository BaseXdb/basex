package org.basex.server;

/**
 * Client info.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public interface ClientInfo {
  /**
   * Returns the host and port of a client.
   * @return string representation
   */
  String address();

  /**
   * Returns the name of the current user.
   * @return user name
   */
  String user();
}
