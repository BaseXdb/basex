package org.basex.core;

import java.util.LinkedList;

/**
 * Management of executing read/write processes.
 * Multiple readers, single writers.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
final class Semaphore {
  /** List of monitors for locking objects. */
  private final LinkedList<Lock> waiting = new LinkedList<Lock>();
  /** State of the semaphore: 0 = idle, 1 = read lock, 2 = write lock. */
  private int state;
  /** Number of active readers. */
  private int activeR;

  /**
   * Modifications before executing a process.
   * @param w writing flag
   */
  void before(final boolean w) {
    if(w) {
      final Lock l = new Lock(true);
      synchronized(l) {
        synchronized(this) {
          if(state == 0) {
            state = 2;
            return;
          }
          waiting.add(l);
        }
        try {
          l.wait();
        } catch(final InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    } else {
      synchronized(this) {
        if(state < 2 && waiting.size() == 0) {
          state = 1;
          activeR++;
          return;
        }
      }
      Lock l = null;
      if(waiting.size() > 0 && !waiting.getLast().writer) {
        l = waiting.getLast();
        l.waitingReaders++;
      } else {
        l = new Lock(false);
        waiting.add(l);
      }
      synchronized(l) {
        try {
          l.wait();
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
  synchronized void after(final boolean w) {
    if(w) {
      state = 0;
      notifyNext();
    } else {
      if(--activeR == 0) {
        state = 0;
        notifyNext();
      }
    }
  }

  /**
   * Notifies the next processes in line.
   */
  private synchronized void notifyNext() {
    if(waiting.size() > 0) {
      final Lock eldest = waiting.remove(0);
      synchronized(eldest) {
        eldest.notifyAll();
      }
      if(eldest.writer) {
        state = 2;
      } else {
        state = 1;
        activeR = eldest.waitingReaders;
      }
    }
  }

  /**
   * Inner class for a locking object.
   *
   * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
   * @author Andreas Weiler
   */
  private static class Lock {
    /** Writer flag. */
    boolean writer;
    /** Number of readers. */
    int waitingReaders = 1;

    /**
     * Standard constructor.
     * @param w writing flag
     */
    Lock(final boolean w) {
      writer = w;
    }
  }
}
