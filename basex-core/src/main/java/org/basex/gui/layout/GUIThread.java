package org.basex.gui.layout;

import javax.swing.*;

/**
 * GUI thread, which will be invoked as daemon via {@link SwingUtilities#invokeLater(Runnable)}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class GUIThread implements Runnable {
  /**
   * Starts this thread.
   */
  public void start() {
    invoke(new Runnable() {
      @Override
      public void run() {
        final Thread t = new Thread(GUIThread.this);
        t.setDaemon(true);
        t.start();
      }
    });
  }

  /**
   * Invokes the thread in the same thread.
   */
  public void invoke() {
    invoke(this);
  }

  /**
   * Invokes the specified runnable in the same thread.
   * @param run runnable
   */
  private void invoke(final Runnable run) {
    SwingUtilities.invokeLater(run);
  }
}
