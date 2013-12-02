package org.basex.gui;

import java.awt.*;

import javax.swing.*;

/**
 * This interface defines GUI command methods.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public interface GUICmd {
  /** Shortcut string for meta key. */
  String META = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ==
      Event.META_MASK ? "meta" : "ctrl";

  /**
   * Executes the command.
   * @param gui reference to the main window
   */
  void execute(final GUI gui);

  /**
   * Enables or disables the specified button, depending on the command properties.
   * @param gui reference to the main window
   * @param button button to be modified
   */
  void refresh(final GUI gui, final AbstractButton button);

  /**
   * Tests if this command includes a menu checkbox.
   * @return result of check
   */
  boolean checked();

  /**
   * Returns the command label.
   * @return command label
   */
  String label();

  /**
   * Returns the command help.
   * @return command help
   */
  String help();

  /**
   * Returns the command shortcut.
   * @return command shortcut
   */
  Object key();
}
