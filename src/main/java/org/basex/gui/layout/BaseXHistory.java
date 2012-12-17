package org.basex.gui.layout;

import org.basex.gui.*;
import org.basex.util.list.*;

/**
 * This class remembers previous text inputs of a GUI component.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXHistory {
  /** Maximum number of history entries. */
  private static final int MAX = 12;
  /** GUI reference. */
  final GUI gui;
  /** History object. */
  final Object[] history;

  /**
   * Constructor.
   * @param main main window
   * @param option option
   */
  public BaseXHistory(final GUI main, final Object[] option) {
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
    for(final String s : gui.gprop.strings(history)) {
      if(sl.size() < MAX &&  !input.equals(s)) sl.add(s);
    }
    gui.gprop.set(history, sl.toArray());
  }
}
