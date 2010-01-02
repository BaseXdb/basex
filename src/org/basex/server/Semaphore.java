package org.basex.server;

import java.util.ArrayList;

import org.basex.core.Context;
import org.basex.core.Proc;
import org.basex.core.User;

/**
 * Management of executing read/write processes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class Semaphore {
  /** List of monitors for waiting writers. */
  private final ArrayList<Object> waitingW = new ArrayList<Object>();
  /** Number of waiting readers. */
  private int waitingR;
  /** Number of active writers. */
  private boolean activeW;
  /** Number of active readers. */
  private int activeR;

  /**
   * Checks if the specified process is a writer.
   * @param pr process
   * @param ctx database context
   * @return result of check
   */
  public boolean writing(final Proc pr, final Context ctx) {
    return (pr.flags & (User.CREATE | User.WRITE)) != 0 || pr.updating(ctx);
  }

  /**
   * Modifications before executing a process.
   * @param w writing flag
   */
  public void before(final boolean w) {
    if(w) {
      final Object o = new Object();
      synchronized(o) {
        synchronized(this) {
          if(waitingW.size() == 0 && activeR == 0 && !activeW) {
            activeW = true;
            return;
          }
          waitingW.add(o);
        }
        try {
          o.wait();
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
   * @param w writing flag
   */
  public synchronized void after(final boolean w) {
    if(w) {
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
