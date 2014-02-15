package org.basex.gui.layout;

import javax.swing.*;

/**
 * This class can be overwritten to define code snippets that are to be evaluated
 * after all pending events.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class GUICode {
  /** Counter. */
  private int counter;

  /**
   * Code to be run.
   * @param arg argument (may be {@code null})
   */
  public abstract void execute(final Object arg);

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
  public final void invokeLater(final Object arg) {
    final int c = ++counter;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if(c == counter) execute(arg);
      }
    });
  }
}
