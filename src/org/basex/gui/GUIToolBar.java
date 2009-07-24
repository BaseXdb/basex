package org.basex.gui;

import javax.swing.JToolBar;
import org.basex.gui.layout.BaseXButton;

/**
 * This is the toolbar of the main window.
 * The toolbar contents are defined in {@link GUIConstants#TOOLBAR}.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class GUIToolBar extends JToolBar {
  /** Toolbar commands. */
  private final GUICommand[] cmd;
  /** Reference to main window. */
  final GUI gui;

  /**
   * Default Constructor.
   * @param tb toolbar commands
   * @param main reference to the main window
   */
  public GUIToolBar(final GUICommand[] tb, final GUI main) {
    setFloatable(false);
    cmd = tb;
    gui = main;

    for(final GUICommand c : cmd) {
      if(c == null) {
        addSeparator();
      } else {
        final BaseXButton button = BaseXButton.command(c, gui);
        button.setFocusable(false);
        add(button);
      }
    }
  }

  /**
   * Refresh buttons.
   */
  public void refresh() {
    for(int b = 0; b < cmd.length; b++) {
      if(cmd[b] != null) cmd[b].refresh(gui, (BaseXButton) getComponent(b));
    }
  }
}
