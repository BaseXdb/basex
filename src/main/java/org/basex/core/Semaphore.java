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
  /** List of waiting processes for writers or reading groups. */
  private final LinkedList<Lock> waiting = new LinkedList<Lock>();
  /** Locking states. */
  private static enum State {
    /** Idle state.   */ IDLE,
    /** Read state.   */ READ,
    /** Write state.  */ WRITE
  }
  /** State of the lock. */
  private State state = State.IDLE;
  /** Number of active readers. */
  private int activeR;

  /**
   * Modifications before executing a process.
   * @param w writing flag
   */
  @SuppressWarnings("fallthrough")
  void before(final boolean w) {
    if(w) {
      final Lock p = new Lock(true);
      synchronized(p) {
        synchronized(this) {
          switch(state) {
            case IDLE:
              state = State.WRITE;
              return;
            default:
              waiting.add(p);
              break;
          }
        }
        try {
          p.wait();
        } catch(final InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    } else {
      Lock l = null;
      synchronized(this) {
        switch(state) {
          case IDLE:
            activeR++;
            return;
          case READ:
            if(waiting.size() == 0) {
              activeR++;
              return;
            }
          default:
          if(waiting.size() > 0 && !waiting.getLast().writer) {
            l = waiting.getLast();
            l.waitingReaders++;
          } else {
            l = new Lock(false);
            waiting.add(l);
          }
          break;
        }
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
      state = State.IDLE;
      notifyNext();
    } else {
      if(--activeR == 0) {
        state = State.IDLE;
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
        state = State.WRITE;
      } else {
        state = State.READ;
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
