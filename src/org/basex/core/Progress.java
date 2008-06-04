package org.basex.core;

/**
 * This class is implemented by all kinds of processes.
 * It gives feedback on the current process. Moreover, it allows to
 * interrupt the process by calling the {@link #stop} method.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Progress {
  /** Stopped flag. */
  private boolean stopped;
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
   * Returns short information on this process.
   * @return header information
   */
  protected abstract String tit();
  
  /**
   * Returns detailed information on the current process or sub process.
   * @return information in detail
   */
  public final String detail() {
    return sub != null ? sub.detail() : det();
  }

  /**
   * Returns detailed information on this progress.
   * @return information in detail
   */
  protected abstract String det();
  
  /**
   * Returns a progress value (0 - 1).
   * @return header information
   */
  public final double progress() {
    return sub != null ? sub.progress() : prog();
  }

  /**
   * Returns progress information.
   * @return header information
   */
  protected abstract double prog();
  
  /**
   * Sets a new sub progress.
   * @param prog progress
   */
  public final void progress(final Progress prog) {
    sub = prog;
    if(stopped) sub.stop();
  }

  /**
   * Stops a process or sub process.
   */
  public final void stop() {
    if(sub != null) sub.stop();
    stopped = true;
  }

  /**
   * Checks if the progress was interrupted; if yes, sends a runtime exception.
   */
  public final void checkStop() {
    if(stopped) throw new ProgressException();
  }
}
