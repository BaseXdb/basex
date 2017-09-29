package org.basex.gui.listener;

import java.awt.event.*;

/**
 * Listener interface for handling mouse clicks.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public interface MouseEnteredListener extends MouseListener {
  @Override
  default void mouseClicked(MouseEvent e) { }

  @Override
  default void mouseExited(MouseEvent e) { }

  @Override
  default void mousePressed(MouseEvent e) { }

  @Override
  default void mouseReleased(MouseEvent e) { }
}
