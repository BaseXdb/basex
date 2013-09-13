package org.basex.gui;

import javax.swing.*;

/**
 * This class provides a default implementation for GUI commands.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class GUIBaseCmd implements GUICmd {
  /** Separator. */
  public static final GUICmd SEPARATOR = new GUIBaseCmd() {
    @Override
    public void execute(final GUI gui) { }
    @Override
    public String label() { return null; }
  };

  @Override
  public boolean checked() {
    return false;
  }
  @Override
  public String help() {
    return null;
  }
  @Override
  public String key() {
    return null;
  }
  @Override
  public void refresh(final GUI main, final AbstractButton button) {
  }
}
