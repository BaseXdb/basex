package org.basex.gui.listener;

import javax.swing.event.*;

/**
 * Listener interface for inserted and removed text.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@FunctionalInterface
public interface DocumentChangeListener extends DocumentListener {
  /**
   * Invoked when text has been inserted or removed.
   * @param e document event
   */
  void changed(DocumentEvent e);

  @Override
  default void insertUpdate(final DocumentEvent e) {
    changed(e);
  }

  @Override
  default void removeUpdate(final DocumentEvent e) {
    changed(e);
  }

  @Override
  default void changedUpdate(final DocumentEvent e) { }
}
