package org.basex.core;

import static org.basex.core.Text.*;

import org.basex.util.*;

/**
 * This class is implemented by all kinds of processes.
 * It gives feedback on the current process. Moreover, it allows to
 * interrupt the process by calling the {@link #stop()} method.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class Proc {
  /** Listener, reacting on process information. */
  public InfoListener listen;
  /** This flag indicates that a command may perform updates. */
  public boolean updating;
  /** Indicates if a process is currently registered. */
  boolean registered;

  /** Stopped flag. */
  private boolean stopped;
  /** Timeout thread. */
  private Thread timeout;
  /** Sub process. */
  private Proc sub;

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
   * Sets a new sub process.
   * @param <P> process type
   * @param proc process
   * @return passed on process reference
   */
  protected final <P extends Proc> P proc(final P proc) {
    sub = proc;
    if(proc != null) {
      proc.listen = listen;
      proc.registered = registered;
      proc.proc(sub.sub);
      if(stopped) proc.stop();
    }
    return proc;
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
   * Checks if the process was interrupted; if yes, sends a runtime exception.
   */
  public final void checkStop() {
    if(stopped) throw new ProcException();
  }

  /**
   * Aborts a failed or interrupted process.
   */
  protected void abort() {
    if(sub != null) sub.abort();
  }

  /**
   * Starts a timeout thread.
   * @param ms milliseconds to wait; deactivated if set to 0
   */
  public final void startTimeout(final long ms) {
    if(ms == 0) return;

    timeout = new Thread() {
      @Override
      public void run() {
        Performance.sleep(ms);
        Proc.this.stop();
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
   * Adds the names of the databases that may be touched by the process.
   * @param lr container for lock result to pass around
   */
  public void databases(final LockResult lr) {
    lr.writeAll = true;
  }

  /**
   * Checks if the process is registered.
   * @return result of check
   */
  public final boolean registered() {
    return sub != null ? sub.registered() : registered;
  }

  /**
   * Sets the registered state.
   * @param reg registered flag
   */
  public final void registered(final boolean reg) {
    if(sub != null) sub.registered(reg);
    registered = reg;
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
