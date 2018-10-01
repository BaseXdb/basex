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
   * @return address of client
   */
  String clientAddress();

  /**
   * Returns the name of the current client.
   * @return name of client
   */
  String clientName();
}
