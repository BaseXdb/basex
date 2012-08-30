package org.basex.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

import org.basex.util.*;
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
  /** Stores one lock for each object ever used for locking. */
  private final Map<String, ReentrantReadWriteLock> locks =
      new HashMap<String, ReentrantReadWriteLock>();
  /**
   * Currently running transactions.
   *
   * Used as monitor for atomizing access to
   * {@link DBLocking#transactions} and {@link DBLocking#queue}
   */
  private final AtomicInteger transactions = new AtomicInteger(0);
  /**
   * Queue for transactions waiting.
   *
   * Used as monitor for waiting threads in queue.
   */
  private final ConcurrentLinkedQueue<Thread> queue = new ConcurrentLinkedQueue<Thread>();
  /** Stores a list of objects each transaction has locked. */
  private final ConcurrentMap<Thread, Object[]> locked
      = new ConcurrentHashMap<Thread, Object[]>();
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
  public void acquire(final Progress pr, final StringList db) {
    // No databases specified: lock globally
    if(db == null) Util.notimplemented("Global locks in DBLocking not implemented yet.");

    final Thread thread = Thread.currentThread();
    if(locked.containsKey(thread))
      throw new IllegalMonitorStateException("Thread already holds one or more locks.");

    // [JE] synchronization of concurrent objects (transactions, queue) may be
    //  superfluous/incorrect. it could suffice to use ordinary LinkedList and int.

    // Wait in queue if necessary
    synchronized(queue) {
      queue.add(thread);
      boolean result;
      synchronized(transactions) {
        result = transactions.get() >= Math.max(mprop.num(MainProp.PARALLEL), 1)
            || queue.peek() != thread;
      }
      while(result) {
        try {
          queue.wait();
        } catch(final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        synchronized(transactions) {
          result = transactions.get() >= Math.max(mprop.num(MainProp.PARALLEL), 1)
              || queue.peek() != thread;
        }
      }
      synchronized(transactions) {
        final int t = transactions.incrementAndGet();
        assert t <= Math.max(mprop.num(MainProp.PARALLEL), 1);
        queue.remove(thread);
      }
    }

    // Sort entries and remove duplicates to prevent deadlocks
    final String[] objects = db.sort(true, true).unique().toArray();

    // Store for unlocking later
    locked.put(thread, objects);

    // Finally lock objects
    for(final String object : objects) {
      ReentrantReadWriteLock lock = locks.get(object);
      if(null == lock) {
        lock = new ReentrantReadWriteLock();
        locks.put(object, lock);
      }
      (pr.updating ? lock.writeLock() : lock.readLock()).lock();
    }
  }

  @Override
  public void release(final Progress pr) {
    final Object[] objects = locked.remove(Thread.currentThread());
    if(null == objects)
      throw new IllegalMonitorStateException("No locks held by current thread");

    // Unlock all locks, no matter if read or write lock
    for(final Object object : objects) {
      final ReentrantReadWriteLock lock = locks.get(object);
      if(lock.isWriteLockedByCurrentThread())
        lock.writeLock().unlock();
      else
        lock.readLock().unlock();
    }

    // Allow another transaction to run
    transactions.decrementAndGet();
    synchronized(queue) {
      queue.notifyAll();
    }
  }

  /**
   * Present current locking status. Not to be seen as a programming API but only for
   * debugging purposes.
   */
  @Override
  public String toString() {
    final String nl = System.getProperty("line.separator");
    final String ind = "| ";
    final StringBuilder sb = new StringBuilder(nl);
    sb.append("Locking" + nl);
    sb.append(ind + "Transactions running: " + transactions.get() + nl);
    sb.append(ind + "Transaction queue: " + queue + nl);
    sb.append(ind + "Held locks by object:" + nl);
    for(final Object object : locks.keySet())
      sb.append(ind + ind + object + " -> " + locks.get(object) + nl);
    sb.append(ind + "Held locks by transaction:" + nl);
    for(final Thread thread : locked.keySet())
      sb.append(ind + ind + thread + " -> " + locked.get(thread) + nl);
    return sb.toString();
  }

}
