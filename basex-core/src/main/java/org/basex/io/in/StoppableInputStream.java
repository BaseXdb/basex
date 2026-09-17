package org.basex.io.in;

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.basex.core.jobs.*;
import org.basex.util.*;

/**
 * Input stream wrapper whose blocking reads are aborted if the job is stopped or a timeout passes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoppableInputStream extends FilterInputStream {
  /** Scheduler for closing stalled streams. */
  private static final ScheduledExecutorService SCHEDULER =
    Executors.newSingleThreadScheduledExecutor(runnable -> {
      final Thread thread = new Thread(runnable, "basex-read-timeout");
      thread.setDaemon(true);
      return thread;
    });

  /** Timeout for a single blocking read in nanoseconds ({@code 0}: no timeout). */
  private final long timeout;
  /** Start time of the current read in nanoseconds ({@code 0}: no read in progress). */
  private volatile long started;
  /** Indicates that a check for a stalled read is scheduled. */
  private final AtomicBoolean watched = new AtomicBoolean();
  /** Indicates that the stream was closed because the timeout passed. */
  private volatile boolean timedOut;

  /**
   * Constructor.
   * @param in input stream to wrap
   */
  public StoppableInputStream(final InputStream in) {
    this(in, null);
  }

  /**
   * Constructor.
   * @param in input stream to wrap
   * @param timeout timeout for a single blocking read (can be {@code null})
   */
  public StoppableInputStream(final InputStream in, final Duration timeout) {
    super(in);
    this.timeout = timeout != null ? timeout.toNanos() : 0;
  }

  @Override
  public int read() throws IOException {
    return read(in::read);
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    return read(() -> in.read(b, off, len));
  }

  /**
   * Runs a read operation.
   * @param op read operation
   * @return result
   * @throws IOException I/O exception
   */
  private int read(final Job.Stoppable<Integer> op) throws IOException {
    if(timeout > 0) {
      // odd value, never 0
      started = System.nanoTime() | 1;
      watch(timeout);
    }
    try {
      return Job.run(op);
    } catch(final InterruptedException ex) {
      final InterruptedIOException io = new InterruptedIOException();
      io.initCause(ex);
      throw io;
    } catch(final IOException ex) {
      if(!timedOut) throw ex;
      final SocketTimeoutException ste = new SocketTimeoutException("Read timed out");
      ste.initCause(ex);
      throw ste;
    } finally {
      if(timeout > 0) started = 0;
    }
  }

  /**
   * Schedules a check for a stalled read unless one is already scheduled.
   * @param delay delay in nanoseconds
   */
  private void watch(final long delay) {
    if(!watched.get() && watched.compareAndSet(false, true)) {
      SCHEDULER.schedule(this::check, delay, TimeUnit.NANOSECONDS);
    }
  }

  /**
   * Closes the stream if the current read has exceeded the timeout, or checks again later.
   */
  private void check() {
    watched.set(false);
    final long start = started;
    // no read in progress: the next read schedules a new check
    if(start == 0) return;
    final long remaining = start + timeout - System.nanoTime();
    if(remaining > 0) {
      watch(remaining);
    } else {
      timedOut = true;
      try {
        in.close();
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    }
  }
}
