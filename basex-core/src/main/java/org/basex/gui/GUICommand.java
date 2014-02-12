package org.basex.gui;

/**
 * This interface defines GUI command methods.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public interface GUICommand {
  /** Separator. */
  GUICommand SEPARATOR = new GUIPopupCmd(null) {
    @Override public void execute() { }
  };

  /**
   * Executes the command.
   * @param gui reference to the main window
   */
  void execute(final GUI gui);

  /**
   * Checks if the command is currently enabled.
   * @param gui reference to the main window
   * @return result of check
   */
  boolean enabled(final GUI gui);

  /**
   * Checks if the command is currently selected.
   * @param gui reference to the main window
   * @return result of check
   */
  boolean selected(final GUI gui);

  /**
   * Indicates if this is a command that can be turned on and off.
   * @return result of check
   */
  boolean toggle();

  /**
   * Returns the command label.
   * @return command label
   */
  String label();

  /**
   * Returns a shortcut.
   * @return shortcut
   */
  String shortcut();

  /**
   * Returns the command shortcuts.
   * @return command shortcut
   */
  Object shortcuts();
}
