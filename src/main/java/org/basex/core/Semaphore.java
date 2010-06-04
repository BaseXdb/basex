package org.basex.core;

import java.util.LinkedList;

/**
 * Management of executing read/write processes. Multiple readers, single
 * writers.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class Semaphore {
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
  public void before(final boolean w) {
    if(w) {
      if(state == State.IDLE) {
          state = State.WRITE;
          return;
      }
      // exclusive lock
      final Lock lx = new Lock(true);
      synchronized(lx) {
        waiting.add(lx);
        while(!lx.valid) {
          try {
            lx.wait();
          } catch(InterruptedException e) {
            e.printStackTrace();
          }
        }
        state = State.WRITE;
        }
    } else {
      if(state != State.WRITE && waiting.size() == 0) {
          state = State.READ;
          activeR++;
          return;
        }
      // shared lock
      final Lock ls = new Lock(false);
      synchronized(ls) {
        waiting.add(ls);
        while(!ls.valid) {
          try {
            ls.wait();
          } catch(final InterruptedException ex) {
            ex.printStackTrace();
          }
        }
        activeR++;
        state = State.READ;
      }
    }
  }

  /**
   * Modifications after executing a process.
   * @param w writing flag
   */
  public synchronized void after(final boolean w) {
    if(w) {
      if(waiting.size() > 0 && !waiting.getFirst().writer) {
        notifyReaders();
      } else {
        notifyWriter();
      }
    } else {
      if(--activeR == 0) {
          notifyWriter();
      }
    }
  }

  /**
   * Notify readers.
   */
  protected synchronized void notifyReaders() {
    while(waiting.size() > 0) {
      if(waiting.getFirst().writer) break;
      Lock l = waiting.removeFirst();
      synchronized(l) {
        l.valid = true;
        l.notify();
      }
    }
  }

  /**
   * Notify writers.
   */
  protected synchronized void notifyWriter() {
    if(waiting.size() > 0) {
      Lock l = waiting.removeFirst();
      synchronized(l) {
        l.valid = true;
        l.notify();
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
    /** Flag if lock can start. */
    boolean valid;

    /**
     * Standard constructor.
     * @param w writing flag
     */
    Lock(final boolean w) {
      writer = w;
    }
  }
}
