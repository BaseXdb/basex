package org.basex.gui.layout;

import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * This class remembers previous text inputs of a GUI component.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXHistory {
  /** Maximum number of history entries. */
  public static final int MAX = 18;
  /** Maximum number of compact history. */
  public static final int MAXCOMPACT = 7;

  /** Options. */
  private final Options options;
  /** History option. */
  private final StringsOption history;
  /** History index. */
  private int index;

  /**
   * Constructor.
   * @param history history values
   * @param options options
   */
  public BaseXHistory(final StringsOption history, final Options options) {
    this.options = options;
    this.history = history;
  }

  /**
   * Adds the specified value on top of history and stores the history in the options.
   * Empty values will be ignored.
   * @param input new input
   */
  public void add(final String input) {
    if(input.isEmpty()) return;
    final StringList list = new StringList(MAX).add(input);
    for(final String value : values()) {
      if(list.size() < MAX && !input.equals(value)) list.add(value);
    }
    options.set(history, list.finish());
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
    return options.get(history);
  }
}
