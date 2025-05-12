package org.basex.gui.listener;

/**
 * Listener interface for handling link events.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@FunctionalInterface
public interface LinkListener {
  /**
   * Invoked when a link is pressed.
   * @param link pressed link
   */
  void linkClicked(String link);
}
