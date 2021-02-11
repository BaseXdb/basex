package org.basex.gui.listener;

import java.awt.event.*;

/**
 * Listener interface for released keys.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface KeyTypedListener extends KeyListener {
  @Override
  default void keyPressed(final KeyEvent e) { }

  @Override
  default void keyReleased(final KeyEvent e) { }
}
