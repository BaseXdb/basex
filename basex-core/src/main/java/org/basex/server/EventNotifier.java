package org.basex.server;

/**
 * Notification interface for handling database events.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Roman Raedle
 */
public interface EventNotifier {
  /**
   * Invoked when a database event was fired.
   * @param value event string
   */
  void notify(final String value);
}
