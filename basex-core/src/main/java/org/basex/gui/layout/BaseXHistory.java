package org.basex.gui.layout;

import org.basex.gui.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * This class remembers previous text inputs of a GUI component.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BaseXHistory {
  /** Maximum number of history entries. */
  private static final int MAX = 12;
  /** GUI reference. */
  private final GUI gui;
  /** History option. */
  private final StringsOption history;

  /**
   * Constructor.
   * @param main main window
   * @param option option
   */
  public BaseXHistory(final GUI main, final StringsOption option) {
    history = option;
    gui = main;
  }

  /**
   * Stores the current history.
   * @param input new input
   */
  public void store(final String input) {
    if(input == null) return;
    final StringList sl = new StringList(MAX).add(input);
    for(final String s : gui.gopts.get(history)) {
      if(sl.size() < MAX &&  !input.equals(s)) sl.add(s);
    }
    gui.gopts.set(history, sl.toArray());
  }
}
