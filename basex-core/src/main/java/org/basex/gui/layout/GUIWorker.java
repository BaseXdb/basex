package org.basex.gui.layout;

import javax.swing.*;

import org.basex.util.*;

/**
 * Simplified version of the {@link SwingWorker} class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @param <T> result type
 */
public abstract class GUIWorker<T> {
  /**
   * Background task.
   * @return result
   * @throws Exception exception
   */
  protected abstract T doInBackground() throws Exception;

  /**
   * Result handling task.
   * @param t result value
   */
  protected abstract void done(T t);

  /**
   * Executes this worker.
   */
  public final void execute() {
    new SwingWorker<T, T>() {
      @Override
      protected T doInBackground() throws Exception {
        return GUIWorker.this.doInBackground();
      }
      @Override
      protected void done() {
        try {
          final T t = get();
          if(t != null) GUIWorker.this.done(t);
        } catch(final Exception ex) {
          if(!(ex.getCause() instanceof InterruptedException)) Util.stack(ex);
        }
      }
    }.execute();
  }
}
