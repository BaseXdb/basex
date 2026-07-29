package org.basex.gui.listener;

/**
 * Listener interface for handling text edits.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@FunctionalInterface
public interface EditListener {
  /**
   * Invoked when the text was changed by an edit command.
   */
  void edited();
}
