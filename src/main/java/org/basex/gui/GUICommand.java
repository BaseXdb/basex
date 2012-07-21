package org.basex.gui;

import javax.swing.*;

/**
 * This interface defines GUI command methods.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface GUICommand {
  /** Separator. */
  GUICommand EMPTY = new GUICommand() {
    @Override
    public void execute(final GUI gui) { }
    @Override
    public void refresh(final GUI gui, final AbstractButton button) { }
    @Override
    public boolean checked() { return false; }
    @Override
    public String label() { return null; }
    @Override
    public String help() { return null; }
    @Override
    public String key() { return null; } };

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
  String key();
}
