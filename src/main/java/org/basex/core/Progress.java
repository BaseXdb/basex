package org.basex.core;

import static org.basex.core.Text.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class is implemented by all kinds of processes.
 * It gives feedback on the current process. Moreover, it allows to
 * interrupt the process by calling the {@link #stop()} method.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Progress {
  /** Info listener. */
  public InfoListener listen;
  /** This flag indicates that a command may perform updates. */
  public boolean updating;

  /** Stopped flag. */
  private boolean stopped;
  /** Timeout thread. */
  private Thread timeout;
  /** Sub progress. */
  private Progress sub;

  /**
   * Returns short information on the current process or sub process.
   * @return header information
   */
  public final String title() {
    return sub != null ? sub.title() : tit();
  }

  /**
   * Returns detailed information on the current process or sub process.
   * Can be overwritten to give more detailed information.
   * @return information in detail
   */
  public final String detail() {
    return sub != null ? sub.detail() : det();
  }

  /**
   * Returns a progress value from the interval {@code [0, 1]}.
   * @return header information
   */
  public final double progress() {
    return sub != null ? sub.progress() : prog();
  }

  /**
   * Attaches the specified info listener.
   * @param il info listener
   */
  public final void listen(final InfoListener il) {
    if(sub != null) sub.listen(il);
    listen = il;
  }

  /**
   * Sets a new sub progress.
   * @param <P> progress type
   * @param prog progress
   * @return passed on progress reference
   */
  protected final <P extends Progress> P progress(final P prog) {
    sub = prog;
    sub.listen = listen;
    if(stopped) sub.stop();
    return prog;
  }

  /**
   * Stops a process or sub process.
   */
  public final void stop() {
    if(sub != null) sub.stop();
    stopped = true;
    stopTimeout();
  }

  /**
   * Checks if the progress was interrupted; if yes, sends a runtime exception.
   */
  public final void checkStop() {
    if(stopped) throw new ProgressException();
  }

  /**
   * Aborts a failed or interrupted progress.
   */
  protected void abort() {
    if(sub != null) sub.abort();
  }

  /**
   * Starts a timeout thread.
   * @param sec seconds to wait; deactivated if set to 0
   */
  public final void startTimeout(final long sec) {
    if(sec == 0) return;

    timeout = new Thread() {
      @Override
      public void run() {
        Performance.sleep(sec * 1000);
        Progress.this.stop();
      }
    };
    timeout.start();
  }

  /**
   * Stops the timeout thread.
   */
  public final void stopTimeout() {
    if(timeout != null) {
      timeout.interrupt();
      timeout = null;
    }
  }

  /**
   * Adds the names of the databases that will be touched by the process.
   * An empty string indicates that the currently opened database will be
   * touched.
   * @param db databases
   * @return {@code false} if databases cannot be statically determined
   */
  @SuppressWarnings("unused")
  protected boolean databases(final StringList db) {
    return false;
  }

  /**
   * Returns short information on this process.
   * Can be overwritten to give more detailed information.
   * @return header information
   */
  protected String tit() {
    return PLEASE_WAIT_D;
  }

  /**
   * Returns short information on this process.
   * @return header information
   */
  protected String det() {
    return PLEASE_WAIT_D;
  }

  /**
   * Returns a progress value (0 - 1).
   * Can be overwritten to give more detailed information.
   * @return header information
   */
  protected double prog() {
    return 0;
  }
}
