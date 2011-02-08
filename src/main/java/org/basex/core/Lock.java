package org.basex.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  private final List<Resource> list =
    Collections.synchronizedList(new ArrayList<Resource>());
  /** Server Context. */
  private final Context ctx;

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
   * Default constructor.
   * @param c context
   */
  public Lock(final Context c) {
    ctx = c;
  }

  /**
   * Modifications before executing a command.
   * @param w writing flag
   */
  public void register(final boolean w) {
    if(SKIP) return;
    if(w) {
      synchronized(this) {
        if(state == State.IDLE) {
          state = State.WRITE;
          return;
        }
      }
      // exclusive lock
      final Resource lx = new Resource(false);
      synchronized(lx) {
        list.add(lx);
        while(lx.locked) {
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
        final int p = Math.max(ctx.prop.num(Prop.PARALLEL), 1);
        if(state != State.WRITE && list.size() == 0 && activeR < p) {
          state = State.READ;
          ++activeR;
          return;
        }
      }

      // shared lock
      final Resource ls = new Resource(true);
      synchronized(ls) {
        list.add(ls);
        while(ls.locked) {
          try {
            ls.wait();
          } catch(final InterruptedException ex) {
            Util.stack(ex);
          }
        }
        state = State.READ;
        synchronized(this) {
          ++activeR;
        }
      }
    }
  }

  /**
   * Modifications after executing a command.
   * @param w writing flag
   */
  public synchronized void unregister(final boolean w) {
    if(SKIP) return;

    if(!w) --activeR;

    if(list.size() > 0) {
      if(list.get(0).reader) {
        notifyReaders();
      } else {
        notifyNext();
      }
    } else {
      if(activeR == 0) state = State.IDLE;
    }
  }

  /**
   * Notifies all waiting readers.
   */
  private void notifyReaders() {
    final int p = Math.max(ctx.prop.num(Prop.PARALLEL), 1);
    int c = activeR;
    do {
      notifyNext();
    } while(++c < p && list.size() > 0 && list.get(0).reader);
  }

  /**
   * Notifies the next process.
   */
  private void notifyNext() {
    final Resource l = list.remove(0);
    synchronized(l) {
      l.locked = false;
      l.notifyAll();
    }
  }

  /** Inner class for a locking object. */
  private static final class Resource {
    /** Reader flag. */
    final boolean reader;
    /** Flag if lock can start. */
    boolean locked = true;

    /**
     * Standard constructor.
     * @param r reader flag
     */
    Resource(final boolean r) {
      reader = r;
    }
  }
}
