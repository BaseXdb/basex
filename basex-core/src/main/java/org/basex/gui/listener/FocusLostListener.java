package org.basex.gui.listener;

import java.awt.event.*;

/**
 * Listener interface for lost focus.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface FocusLostListener extends FocusListener {
  @Override
  default void focusGained(final FocusEvent e) { }
}
