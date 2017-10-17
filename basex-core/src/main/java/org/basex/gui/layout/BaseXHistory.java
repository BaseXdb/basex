package org.basex.gui.layout;

import org.basex.gui.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * This class remembers previous text inputs of a GUI component.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class BaseXHistory {
  /** Maximum number of history entries. */
  public static final int MAX = 18;
  /** Maximum number of compact history. */
  public static final int MAXCOMPACT = 7;

  /** GUI reference. */
  private final GUI gui;
  /** History option. */
  private final StringsOption history;
  /** History index. */
  private int index;

  /**
   * Constructor.
   * @param gui reference to the main window
   * @param history history values
   */
  public BaseXHistory(final GUI gui, final StringsOption history) {
    this.gui = gui;
    this.history = history;
  }

  /**
   * Stores the current history.
   * @param input new input
   */
  public void store(final String input) {
    if(input == null) return;
    final StringList list = new StringList(MAX).add(input);
    for(final String value : values()) {
      if(list.size() < MAX && !input.equals(value)) list.add(value);
    }
    gui.gopts.set(history, list.finish());
    index = 0;
  }

  /**
   * Returns a history value.
   * @param next next/previous entry
   * @return entry, or {@code null} if history is empty
   */
  public String get(final boolean next) {
    final String[] list = values();
    if(list.length == 0) return null;
    index = next ? Math.min(list.length - 1, index + 1) : Math.max(0, index - 1);
    return list[index];
  }

  /**
   * Returns all values.
   * @return history values
   */
  public String[] values() {
    return gui.gopts.get(history);
  }
}
