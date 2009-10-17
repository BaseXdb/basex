package org.basex.util;

import java.util.ArrayList;

import org.basex.server.ServerSession;

/**
 * Managing of incoming processes.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class Semaphore {

  /** Counts active readers. */
  protected int activeReaders = 0;
  /** Boolean if a writer process is there. */
  protected boolean writerPresent = false;
  /** List for all active sessions. */
  protected ArrayList<ServerSession> list = new ArrayList<ServerSession>();

  /**
   * Checks write condition.
   * @return boolean if writer can start
   */
  public boolean writeCondition() {
    return activeReaders == 0 && !writerPresent;
  }

  /**
   * Checks read condition.
   * @return boolean if reader can start
   */
  public boolean readCondition() {
    return !writerPresent;
  }

  /**
   * Starts a read process.
   * @param s ServerSession
   */
  public synchronized void startRead(final ServerSession s) {
    list.add(s);
    while(!readCondition())
      try {
        wait();
      } catch(InterruptedException ex) {}
    ++activeReaders;
  }

  /**
   * Stops a read process.
   * @param s ServerSession
   */
  public synchronized void stopRead(final ServerSession s) {
    list.remove(s);
    --activeReaders;
    notifyAll();
  }

  /**
   * Starts a write process.
   * @param s ServerSession
   */
  public synchronized void startWrite(final ServerSession s) {
    list.add(s);
    while(!writeCondition() && list.indexOf(s) != 0)
      try {
        wait();
      } catch(InterruptedException ex) {}
    writerPresent = true;
  }

  /**
   * Stops a write process.
   * @param s ServerSession
   */
  public synchronized void stopWrite(final ServerSession s) {
    list.remove(s);
    writerPresent = false;
    notifyAll();
  }
  
}
