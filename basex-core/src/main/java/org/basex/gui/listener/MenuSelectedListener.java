package org.basex.gui.listener;

import javax.swing.event.*;

/**
 * Listener interface for selected menus.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@FunctionalInterface
public interface MenuSelectedListener extends MenuListener {
  @Override
  default void menuDeselected(final MenuEvent e) { }

  @Override
  default void menuCanceled(final MenuEvent e) { }
}
