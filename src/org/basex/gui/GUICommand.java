package org.basex.gui;

import javax.swing.AbstractButton;

/**
 * This interface defines GUI command methods.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public interface GUICommand {
  /**
   * Executes the command.
   * @param gui reference to the main window
   */
  void execute(final GUI gui);

  /**
   * Enables or disables the specified button,
   * depending on the command properties.
   * @param gui reference to the main window
   * @param button button to be modified
   */
  void refresh(final GUI gui, final AbstractButton button);

  /**
   * Returns if this command includes a menu checkbox.
   * @return result of check
   */
  boolean checked();

  /**
   * Returns the command entry.
   * @return command entry
   */
  String desc();

  /**
   * Returns the command help.
   * @return command help
   */
  String help();

  /**
   * Returns the command shortcut.
   * @return command shortcut
   */
  String key();
}
