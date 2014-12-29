package org.basex.gui;

import org.basex.gui.layout.*;

/**
 * This class provides a default implementation for GUI popup commands.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class GUIPopupCmd implements GUICommand {
  /** Shortcut. */
  private final BaseXKeys[] shortcut;
  /** Label. */
  private final String label;

  /**
   * Constructor.
   * @param label label
   * @param shortcut shortcut
   */
  protected GUIPopupCmd(final String label, final BaseXKeys... shortcut) {
    this.label = label;
    this.shortcut = shortcut;
  }

  /**
   * Executes the popup command.
   */
  public abstract void execute();

  @Override
  public final void execute(final GUI main) {
    if(enabled(main)) execute();
  }

  @Override
  public boolean toggle() {
    return false;
  }

  @Override
  public final String label() {
    return label;
  }

  @Override
  public final String shortCut() {
    return null;
  }

  @Override
  public final BaseXKeys[] shortcuts() {
    return shortcut;
  }

  @Override
  public boolean enabled(final GUI main) {
    return true;
  }

  @Override
  public boolean selected(final GUI main) {
    return false;
  }
}
