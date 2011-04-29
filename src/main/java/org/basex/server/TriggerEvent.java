package org.basex.server;

/**
 * TriggerNotification interface.
 *
 * @author Workgroup HCI, University of Konstanz 2005-10, ISC License
 * @author Roman Raedle
 */
public interface TriggerEvent {

  /**
   * Updates data.
   * @param data data string
   */
  void update(final String data);
}
