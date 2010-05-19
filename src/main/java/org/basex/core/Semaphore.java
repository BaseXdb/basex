package org.basex.core;

import java.util.LinkedList;

/**
 * Management of executing read/write processes. Multiple readers, single
 * writers.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
final class Semaphore {
  /** List of waiting processes for writers or reading groups. */
  private final LinkedList<Lock> waiting = new LinkedList<Lock>();

  /** States of locking. */
  private static enum State {
    /** Idle state. */ IDLE,
    /** Read state. */ READ,
    /** Write state. */ WRITE
  }

  /** State of the lock. */
  private State state = State.IDLE;
  /** Number of active readers. */
  private int activeR;

  /**
   * Modifications before executing a process.
   * @param w writing flag
   */
  void before(final boolean w) {
    if(w) {
      // exclusive lock
      final Lock lx = new Lock(true);
      synchronized(lx) {
        synchronized(this) {
          if(state == State.IDLE) {
            state = State.WRITE;
            return;
          }
          waiting.add(lx);
        }
        try {
          lx.wait();
          state = State.WRITE;
        } catch(final InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    } else {
      // shared lock
      Lock ls = null;
      synchronized(this) {
        if(state != State.WRITE && waiting.size() == 0) {
          state = State.READ;
          activeR++;
          return;
        }
        if(waiting.size() > 0 && !waiting.getLast().writer) {
            ls = waiting.getLast();
            ls.waitingReaders++;
          } else {
            ls = new Lock(false);
            waiting.add(ls);
        }
      }
      synchronized(ls) {
        try {
          ls.wait();
          if(activeR == 0) {
            state = State.READ;
            activeR = ls.waitingReaders;
          }
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
      notifyNext();
    } else {
      if(--activeR == 0) {
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
    } else {
      state = State.IDLE;
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
