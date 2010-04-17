package org.basex.core;

import java.util.LinkedList;

/**
 * Management of executing read/write processes.
 * Multiple readers, single writers.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
final class SemaphoreNew {
  
  /** List of monitors for locking objects. */
  private final LinkedList<Lock> waiting = new LinkedList<Lock>();
  /** Value for an active writer. */
  private boolean activeW;
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
          if(waiting.size() == 0 && activeR == 0 && !activeW) {
            activeW = true;
            return;
          }
          waiting.add(l);
        try {
          l.wait();
        } catch(final InterruptedException ex) {
          ex.printStackTrace();
        }
      }
    } else {
      Lock l = new Lock(false);
      synchronized(l) {
          if(!activeW && waiting.size() == 0) {
            activeR++;
            return;
          }
          if(waiting.size() > 0) {
            if(!waiting.getLast().isWriter()) {
              l = waiting.getLast();
              l.inc();
            } else {
              waiting.add(l);
            }
          } else {
            waiting.add(l);
          }
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
      activeW = false;
      notifyNext();
    } else {
      if(--activeR == 0) notifyNext();
    }
  }
  
  /**
   * Notifies the next processes in line.
   */
  private synchronized void notifyNext() {
    if(waiting.size() > 0) {
      final Lock eldest = waiting.remove(0);
      synchronized(eldest) { eldest.notifyAll(); }
      if(eldest.isWriter()) {
      activeW = true;
      } else {
        activeR = eldest.number;
      }
    }
  }
  
  /**
   * Inner class for a locking object
   *
   * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
   * @author Andreas Weiler
   */
  class Lock {
    /** Writer flag. */
    private boolean writer;
    /** Number of readers. */
    public int number = 1;
    
    /**
     * Standard constructor.
     * @param w writing flag
     */
    public Lock(final boolean w) {
      this.writer = w;
    }
    
    /**
     * Increment the number.
     */
    public void inc() {
      number++;
    }
    
    /**
     * Returns the writer value.
     * @return boolean writing flag
     */
    public boolean isWriter() {
      return writer;
    }
  }
}
