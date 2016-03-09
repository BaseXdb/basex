package org.basex.core;

import java.util.*;

import org.basex.core.locks.*;

/**
 * This class is implemented by all kinds of processes.
 * It gives feedback on the current process. A process can be canceled by calling {@link #stop()}.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class Proc {
  /** State of process. */
  public enum State {
    /** OK.      */ OK,
    /** Stopped. */ STOPPED,
    /** Timeout. */ TIMEOUT,
    /** Memory.  */ MEMORY;
  }

  /** Listener, reacting on process information. */
  public InfoListener listen;
  /** This flag indicates that a command may perform updates. */
  public boolean updating;
  /** Stopped flag. */
  public State state = State.OK;

  /** Indicates if a process is currently registered. */
  protected boolean registered;

  /** Timer. */
  private Timer timer;
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
  public final <P extends Proc> P proc(final P proc) {
    sub = proc;
    if(proc != null) {
      proc.listen = listen;
      proc.registered = registered;
      proc.proc(sub.sub);
      if(state != State.OK) proc.state(state);
    }
    return proc;
  }

  /**
   * Stops a process or sub process.
   */
  public final void stop() {
    state(State.STOPPED);
  }

  /**
   * Stops a process because of a timeout.
   */
  public final void timeout() {
    state(State.TIMEOUT);
  }

  /**
   * Stops a process because a memory limit was exceeded.
   */
  public final void memory() {
    state(State.MEMORY);
  }

  /**
   * Sets a new process state.
   * @param st new state
   */
  final void state(final State st) {
    if(sub != null) sub.state(st);
    state = st;
    stopTimeout();
  }

  /**
   * Checks if the process was interrupted; if yes, sends a runtime exception.
   */
  public final void checkStop() {
    if(state != State.OK) throw new ProcException();
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

    timer = new Timer(true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() { timeout(); }
    }, ms);
  }

  /**
   * Stops the timeout thread.
   */
  public final void stopTimeout() {
    if(timer != null) {
      timer.cancel();
      timer = null;
    }
  }

  /**
   * Adds the names of the databases that may be touched by the process.
   * @param lr container for lock result to pass around
   */
  public void databases(final LockResult lr) {
    // default (worst case): lock all databases
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
    return Text.PLEASE_WAIT_D;
  }

  /**
   * Returns short information on this process.
   * @return header information
   */
  protected String det() {
    return Text.PLEASE_WAIT_D;
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
