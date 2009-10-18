package org.basex.server;

import java.util.ArrayList;

/**
 * Management of executing read/write processes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class Semaphore {
  /** List of monitors for waiting writers. */
  private ArrayList<Object> waitingWriters = new ArrayList<Object>();
  /** Number of waiting readers. */
  private int waitingReaders;
  /** Number of active writers. */
  private boolean activeWriter;
  /** Number of active readers. */
  private int activeReaders;

  /**
   * Modifications before read.
   */
  public void beforeRead() {
    synchronized(this) {
      if(!activeWriter && waitingWriters.size() == 0) {
        ++activeReaders;
        return;
      }
      ++waitingReaders;
      try {
        wait();
      } catch(final InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Modifications before write.
   */
  public void beforeWrite() {
    final Object monitor = new Object();
    synchronized(monitor) {
      synchronized(this) {
        if(waitingWriters.size() == 0 && activeReaders == 0 && !activeWriter) {
          activeWriter = true;
          return;
        }
        waitingWriters.add(monitor);
      }
      try {
        monitor.wait();
      } catch(final InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }

  /**
   * Notifies the readers.
   */
  private synchronized void notifyReaders() {
    notifyAll();
    activeReaders = waitingReaders;
    waitingReaders = 0;
  }

  /**
   * Notifies the writers.
   */
  private synchronized void notifyWriter() {
    if(waitingWriters.size() > 0) {
      final Object eldest = waitingWriters.remove(0);
      synchronized(eldest) {
        eldest.notify();
      }
      activeWriter = true;
    }
  }

  /**
   * After read process.
   */
  public synchronized void afterRead() {
    if(--activeReaders == 0) notifyWriter();
  }

  /**
   * After writer process.
   */
  public synchronized void afterWrite() {
    activeWriter = false;
    if(waitingReaders > 0) notifyReaders();
    else notifyWriter();
  }
}
