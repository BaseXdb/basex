package org.basex.core;

import static org.basex.core.Prop.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import org.basex.util.list.*;

/**
 * Manage read and write locks on arbitrary objects. Maximum of {@link MainProp#PARALLEL}
 * concurrent transactions are allowed, further will be queued.
 *
 * This class prevents locking deadlocks by sorting all Objects to put locks on what
 * requires them to have be {@link Comparable}.
 *
 * Locks can only be released by the same thread which acquired it.
 *
 * This locking can be activated by setting {@link MainProp#DBLOCKING} to {@code true}.
 * It will get the default implementation in future versions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Jens Erat
 */
public final class DBLocking implements ILocking {
  /** Lock for running thread counters. */
  private final Object globalLock = new Object();
  /** Number of running local writers. Guarded by {@code globalLock}. */
  private int localWriters;
  /** Number of running local writers. Guarded by {@code globalLock}. */
  private int globalReaders;
  /**
   * Lock for global write locking.
   *
   * Exclusive lock - if globally writing
   * Shared lock    - else
   */
  private final ReentrantReadWriteLock writeAll = new ReentrantReadWriteLock();
  /** Stores one lock for each object ever used for locking. */
  private final Map<String, ReentrantReadWriteLock> locks =
      new HashMap<String, ReentrantReadWriteLock>();
  /**
   * Currently running transactions.
   * Used as monitor for atomizing access to {@link #queue}.
   */
  private int transactions;
  /**
   * Queue for transactions waiting.
   *
   * Used as monitor for waiting threads in queue.
   */
  private final Queue<Long> queue = new LinkedList<Long>();
  /**
   * Stores a list of objects each transaction has write-locked.
   * Null means lock everything, an empty array lock nothing.
   */
  private final ConcurrentMap<Long, String[]> writeLocked =
      new ConcurrentHashMap<Long, String[]>();
  /**
   * Stores a list of objects each transaction has read-locked. Null means lock
   * everything, an empty array lock nothing.
   */
  private final ConcurrentMap<Long, String[]> readLocked =
      new ConcurrentHashMap<Long, String[]>();
  /** BaseX database context. */
  private final MainProp mprop;

  /**
   * Initialize new Locking instance.
   * @param mp Main properties, used to read parallel transactions limit.
   */
  public DBLocking(final MainProp mp) {
    mprop = mp;
  }

  @Override
  public void acquire(final Progress pr, final StringList read, final StringList write) {
    final long thread = Thread.currentThread().getId();
    if(writeLocked.containsKey(thread) || readLocked.containsKey(thread))
      throw new IllegalMonitorStateException("Thread already holds one or more locks.");

    // Wait in queue if necessary
    synchronized(queue) { // Guard queue and transaction, monitor for waiting in queue
      queue.add(thread);
      while(transactions >= Math.max(mprop.num(MainProp.PARALLEL), 1)
          || queue.peek() != thread) {
        try {
          queue.wait();
        } catch(final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
      final int t = transactions++;
      assert t <= Math.max(mprop.num(MainProp.PARALLEL), 1);
      queue.remove(thread);
    }

    // Global write lock if write StringList is not set
    if(null == write) writeAll.writeLock().lock();
    else writeAll.readLock().lock();

    synchronized(globalLock) {
      // local write locking
      if(null != write && !write.isEmpty()) {
        while(globalReaders > 0) {
          try {
            globalLock.wait();
          } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
        localWriters++;
      } else {
        // global read locking
        if(null == read) {
          while(localWriters > 0) {
            try {
              globalLock.wait();
            } catch(InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
          globalReaders++;
        }
      }
    }

    // Local write locking
    if (null != write) {
      // Sort entries and remove duplicates to prevent deadlocks
      final String[] writeObjects = write.sort(true, true).unique().toArray();
      // Store for unlocking later
      writeLocked.put(thread, writeObjects);
      // Finally lock objects
      for(final String object : writeObjects) {
        ReentrantReadWriteLock lock;
        synchronized(locks) { // Make sure each object lock is a singleton
          lock = locks.get(object);
          if(null == lock) {
            lock = new ReentrantReadWriteLock();
            locks.put(object, lock);
          }
        }
        lock.writeLock().lock();
      }
    }

    // Local read locking, same again
    if (null != read) {
      final String[] readObjects = read.sort(true, true).unique().toArray();
      readLocked.put(thread, readObjects);
      for(final String object : readObjects) {
        ReentrantReadWriteLock lock;
        synchronized(locks) {
          lock = locks.get(object);
          if(null == lock) {
            lock = new ReentrantReadWriteLock();
            locks.put(object, lock);
          }
        }
        lock.readLock().lock();
      }
    }
  }

  @Override
  public void release(final Progress pr) {
    // Release all write locks
    final String[] writeObjects = writeLocked.remove(Thread.currentThread().getId());
    if(null != writeObjects) for(final String object : writeObjects) {
      final ReentrantReadWriteLock lock = locks.get(object);
      assert 1 == lock.getWriteHoldCount() : "Unexpected write lock count: "
          + lock.getWriteHoldCount();
      lock.writeLock().unlock();
    }

    // Release all read locks
    final Object[] readObjects = readLocked.remove(Thread.currentThread().getId());
    if(null != readObjects) for(final Object object : readObjects) {
      final ReentrantReadWriteLock lock = locks.get(object);
      assert 1 == lock.getReadHoldCount() : "Unexpected read lock count: "
          + lock.getReadHoldCount();
      lock.readLock().unlock();
    }

    // Release global locks
    (writeAll.isWriteLocked() ? writeAll.writeLock() : writeAll.readLock()).unlock();
    if(null != writeObjects && 0 != writeObjects.length) synchronized(globalLock) {
      localWriters--;
      globalLock.notifyAll();
    } else if(null == readObjects) synchronized(globalLock) {
      globalReaders--;
      globalLock.notifyAll();
    }

    // Allow another transaction to run
    synchronized(queue) {
      transactions--;
      queue.notifyAll();
    }
  }

  /**
   * Present current locking status. Not to be seen as a programming API but only for
   * debugging purposes.
   */
  @Override
  public String toString() {
    final String ind = "| ";
    final StringBuilder sb = new StringBuilder(NL);
    sb.append("Locking" + NL);
    sb.append(ind + "Transactions running: " + transactions + NL);
    sb.append(ind + "Transaction queue: " + queue + NL);
    sb.append(ind + "Held locks by object:" + NL);
    for(final Object object : locks.keySet())
      sb.append(ind + ind + object + " -> " + locks.get(object) + NL);
    sb.append(ind + "Held write locks by transaction:" + NL);
    for(final Long thread : writeLocked.keySet())
      sb.append(ind + ind + thread + " -> " + writeLocked.get(thread) + NL);
    sb.append(ind + "Held read locks by transaction:" + NL);
    for(final Long thread : readLocked.keySet())
      sb.append(ind + ind + thread + " -> " + readLocked.get(thread) + NL);
    return sb.toString();
  }

}
