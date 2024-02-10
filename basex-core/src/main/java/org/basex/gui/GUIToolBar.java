package org.basex.gui;

import javax.swing.*;

import org.basex.gui.layout.*;

/**
 * This is the toolbar of the main window.
 * The toolbar contents are defined in {@link GUIConstants#TOOLBAR}.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class GUIToolBar extends BaseXToolBar {
  /** Toolbar commands. */
  private final GUICommand[] commands;
  /** Reference to the main window. */
  private final GUI gui;

  /**
   * Default constructor.
   * @param commands toolbar commands
   * @param gui reference to the main window
   */
  GUIToolBar(final GUICommand[] commands, final GUI gui) {
    this.commands = commands;
    this.gui = gui;

    for(final GUICommand c : commands) {
      if(c == null) {
        addSeparator();
      } else {
        add(BaseXButton.command(c, gui));
      }
    }
  }

  /**
   * Refreshes the buttons.
   */
  void refresh() {
    final int cl = commands.length;
    for(int c = 0; c < cl; ++c) {
      final GUICommand cmd = commands[c];
      if(cmd != null) {
        final AbstractButton button = (AbstractButton) getComponent(c);
        button.setEnabled(cmd.enabled(gui));
        button.setSelected(cmd.selected(gui));
      }
    }
  }
}
