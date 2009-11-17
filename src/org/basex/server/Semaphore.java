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
  private ArrayList<Object> waitingW = new ArrayList<Object>();
  /** Number of waiting readers. */
  private int waitingR;
  /** Number of active writers. */
  private boolean activeW;
  /** Number of active readers. */
  private int activeR;

  /**
   * Modifications before executing a process.
   * @param up updating flag
   */
  public void before(final boolean up) {
    if(up) {
      final Object monitor = new Object();
      synchronized(monitor) {
        synchronized(this) {
          if(waitingW.size() == 0 && activeR == 0 && !activeW) {
            activeW = true;
            return;
          }
          waitingW.add(monitor);
        }
        try {
          monitor.wait();
        } catch(final InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    } else {
      synchronized(this) {
        if(!activeW && waitingW.size() == 0) {
          ++activeR;
          return;
        }
        ++waitingR;
        try {
          wait();
        } catch(final InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  /**
   * Modifications after executing a process.
   * @param up updating flag
   */
  public void after(final boolean up) {
    if(up) {
      activeW = false;
      if(waitingR > 0) notifyReaders();
      else notifyWriter();
    } else {
      if(--activeR == 0) notifyWriter();
    }
  }

  /**
   * Notifies the readers.
   */
  private synchronized void notifyReaders() {
    notifyAll();
    activeR = waitingR;
    waitingR = 0;
  }

  /**
   * Notifies the writers.
   */
  private synchronized void notifyWriter() {
    if(waitingW.size() > 0) {
      final Object eldest = waitingW.remove(0);
      synchronized(eldest) {
        eldest.notify();
      }
      activeW = true;
    }
  }
}
