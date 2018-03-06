package org.basex.server;

/**
 * Client info.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public interface ClientInfo {
  /**
   * Returns the host and port of a client.
   * @return string representation
   */
  String clientAddress();

  /**
   * Returns the name of the current client.
   * @return user name
   */
  String clientName();
}
