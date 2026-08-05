package org.basex.gui.layout;

import javax.swing.*;

/**
 * This class can be overwritten to define code snippets that are to be evaluated
 * after all pending events.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param <A> argument type
 */
public abstract class GUICode<A> {
  /** Counter. */
  private int counter;

  /**
   * Code to be run.
   * @param arg argument (can be {@code null})
   */
  public abstract void execute(A arg);

  /**
   * Invokes the specified thread after all other threads.
   */
  public final void invokeLater() {
    invokeLater(null);
  }

  /**
   * Invokes the specified thread after all other threads.
   * @param arg optional argument
   */
  public final void invokeLater(final A arg) {
    final int c = ++counter;
    SwingUtilities.invokeLater(() -> {
      if(c == counter) execute(arg);
    });
  }
}
