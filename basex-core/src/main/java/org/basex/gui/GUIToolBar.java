package org.basex.gui;

import javax.swing.*;

import org.basex.gui.layout.*;

/**
 * This is the toolbar of the main window.
 * The toolbar contents are defined in {@link GUIConstants#TOOLBAR}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class GUIToolBar extends JToolBar {
  /** Toolbar commands. */
  private final GUICommand[] commands;
  /** Reference to main window. */
  private final GUI gui;

  /**
   * Default constructor.
   * @param commands toolbar commands
   * @param gui reference to the main window
   */
  GUIToolBar(final GUICommand[] commands, final GUI gui) {
    setFloatable(false);
    this.commands = commands;
    this.gui = gui;

    for(final GUICommand c : commands) {
      if(c == null) {
        addSeparator();
      } else {
        final AbstractButton button = BaseXButton.command(c, gui);
        button.setFocusable(false);
        add(button);
      }
    }
  }

  /**
   * Refreshes the buttons.
   */
  void refresh() {
    for(int c = 0; c < commands.length; ++c) {
      final GUICommand cmd = commands[c];
      if(cmd != null) {
        final AbstractButton button = (AbstractButton) getComponent(c);
        button.setEnabled(cmd.enabled(gui));
        button.setSelected(cmd.selected(gui));
      }
    }
  }
}
