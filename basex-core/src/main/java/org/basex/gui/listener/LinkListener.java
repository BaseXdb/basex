package org.basex.gui.listener;

/**
 * Listener interface for handling link events.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface LinkListener {
  /**
   * Invoked when a link is pressed.
   * @param link pressed link
   */
  void linkClicked(String link);
}
