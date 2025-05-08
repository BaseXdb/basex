package org.basex.gui.listener;

import java.awt.event.*;

/**
 * Listener interface for released keys.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@FunctionalInterface
public interface KeyReleasedListener extends KeyListener {
  @Override
  default void keyTyped(final KeyEvent e) { }

  @Override
  default void keyPressed(final KeyEvent e) { }
}
