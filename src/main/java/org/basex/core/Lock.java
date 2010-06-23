package org.basex.core;

import java.util.LinkedList;

/**
 * Management of executing read/write processes. Multiple readers, single
 * writers (readers/writer lock).
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public final class Lock {
  /** List of waiting processes for writers or reading groups. */
  private final LinkedList<Resource> waiting = new LinkedList<Resource>();

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
      final Resource lx = new Resource(true);
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
      final Resource ls = new Resource(false);
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
   * Notifies all waiting readers.
   */
  private synchronized void notifyReaders() {
    while(waiting.size() > 0) {
      if(waiting.getFirst().writer) break;
      Resource l = waiting.removeFirst();
      synchronized(l) {
        l.valid = true;
        l.notify();
      }
    }
  }

  /**
   * Notifies a waiting writer.
   */
  private synchronized void notifyWriter() {
    if(waiting.size() > 0) {
      Resource l = waiting.removeFirst();
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
  private static class Resource {
    /** Writer flag. */
    boolean writer;
    /** Flag if lock can start. */
    boolean valid;

    /**
     * Standard constructor.
     * @param w writing flag
     */
    Resource(final boolean w) {
      writer = w;
    }
  }
}
