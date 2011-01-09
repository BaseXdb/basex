package org.basex.core;

import java.util.LinkedList;
import org.basex.util.Util;

/**
 * Management of executing read/write processes. Multiple readers, single
 * writers (readers/writer lock).
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Andreas Weiler
 */
public final class Lock {
  /** Flag for skipping all locking tests. */
  private static final boolean SKIP = false;
  /** List of waiting processes for writers or reading groups. */
  private final LinkedList<Resource> waiting = new LinkedList<Resource>();

  /** States of locking. */
  private static enum State {
    /** Idle state. */  IDLE,
    /** Read state. */  READ,
    /** Write state. */ WRITE
  }

  /** State of the lock. */
  private State state = State.IDLE;
  /** Number of active readers. */
  private int activeR;

  /**
   * Modifications before executing a command.
   * @param w writing flag
   */
  public void before(final boolean w) {
    if(SKIP) return;

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
          } catch(final InterruptedException ex) {
            Util.stack(ex);
          }
        }
        state = State.WRITE;
      }
    } else {
      synchronized(this) {
        if(state != State.WRITE && waiting.size() == 0) {
          state = State.READ;
          ++activeR;
          return;
        }
      }
      // shared lock
      final Resource ls = new Resource(false);
      synchronized(ls) {
        waiting.add(ls);
        while(!ls.valid) {
          try {
            ls.wait();
          } catch(final InterruptedException ex) {
            Util.stack(ex);
          }
        }
        ++activeR;
        state = State.READ;
      }
    }
  }

  /**
   * Modifications after executing a command.
   * @param w writing flag
   */
  public synchronized void after(final boolean w) {
    if(SKIP) return;

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
  private void notifyReaders() {
    while(waiting.size() > 0) {
      if(waiting.getFirst().writer) break;
      final Resource l = waiting.removeFirst();
      synchronized(l) {
        l.valid = true;
        l.notify();
      }
    }
  }

  /**
   * Notifies a waiting writer.
   */
  private void notifyWriter() {
    if(waiting.size() > 0) {
      final Resource l = waiting.removeFirst();
      synchronized(l) {
        l.valid = true;
        l.notify();
      }
    } else {
      state = State.IDLE;
    }
  }

  /** Inner class for a locking object. */
  private static class Resource {
    /** Writer flag. */
    final boolean writer;
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
