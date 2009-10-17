package org.basex.server;

import java.util.ArrayList;

/**
 * Management of executing read/write processes.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class Semaphore {
  
  /** Number of active readers. */
  protected int activeReaders = 0;
  /** Number of active writers. */
  protected int activeWriters = 0;
  /** Number of waiting readers. */
  protected int waitingReaders = 0;
  /** Monitor for all waiting readers. */
  protected Object waitingReaderMonitor = this;
  /** List of monitors for waiting writers. */
  protected ArrayList<Object> waitingWriterMonitors = new ArrayList<Object>();

  /**
   * Checks if a reader can start.
   * @return boolean if reader can start
   */
  protected boolean allowReader() {
    return activeWriters == 0 && waitingWriterMonitors.size() == 0;
  }
  
  /**
   * Checks if a writer can start.
   * @return boolean if writer can start.
   */
  protected boolean allowWriter() {
    return waitingWriterMonitors.size() == 0 && activeReaders == 0
        && activeWriters == 0;
  }
  
  /**
   * Modifications before read.
   */
  public void beforeRead() {
    synchronized(waitingReaderMonitor) {
      synchronized(this) {
        if(allowReader()) {
          ++activeReaders;
          return;
        }
        ++waitingReaders;
      }
      try {
        waitingReaderMonitor.wait();
      } catch(InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  /**
   * Modifications before write.
   */
  public void beforeWrite() {
    Object monitor = new Object();
    synchronized(monitor) {
      synchronized(this) {
        if(allowWriter()) {
          ++activeWriters;
          return;
        }
        waitingWriterMonitors.add(monitor);
      }
      try {
        monitor.wait();
      } catch(InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  /**
   * Notify readers.
   */
  protected synchronized void notifyReaders() {
    synchronized(waitingReaderMonitor) {
      waitingReaderMonitor.notifyAll();
    }
    activeReaders = waitingReaders;
    waitingReaders = 0;
  }
  
  /**
   * Notify writers.
   */
  protected synchronized void notifyWriter() {
    if(waitingWriterMonitors.size() > 0) {
      Object eldest = waitingWriterMonitors.get(0);
      waitingWriterMonitors.remove(0);
      synchronized(eldest) {
        eldest.notify();
      }
      ++activeWriters;
    }
  }

  /**
   * After read process.
   */
  public synchronized void afterRead() {
    if(--activeReaders == 0) notifyWriter();
  }
  
  /**
   * After writer process.
   */
  public synchronized void afterWrite() {
    --activeWriters;
    if(waitingReaders > 0)
    notifyReaders();
    else notifyWriter();
  }
}
