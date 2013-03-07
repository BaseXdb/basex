package org.basex.core;

import static org.basex.core.Prop.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import org.basex.util.list.*;

/**
 * Manage read and write locks on arbitrary strings. Maximum of {@link MainProp#PARALLEL}
 * concurrent transactions are allowed, further will be queued.
 *
 * This class prevents locking deadlocks by sorting all all strings
 *
 * Locks can only be released and downgraded by the same thread which acquired it.
 *
 * Locking methods are not synchronized to each other. The user must make sure not to call
 * them in parallel by the same thread (it is fine to call arbitrary locking methods by
 * different threads at the same time).
 *
 * This locking can be deactivated by setting {@link MainProp#GLOBALLOCK} to {@code true}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Jens Erat
 */
public final class DBLocking implements Locking {
  /** Fair scheduling; prevents starvation, but reduces parallelism. */
  private static final boolean FAIR = true;

  /** Prefix for internal special locks. */
  private static final String PREFIX = "%";
  /** Special lock identifier for admin commands. */
  public static final String ADMIN = PREFIX + "ADMIN";
  /** Special lock identifier for backup commands. */
  public static final String BACKUP = PREFIX + "BACKUP";
  /** Special lock identifier for event commands. */
  public static final String EVENT = PREFIX + "EVENT";
  /** Special lock identifier for repository commands. */
  public static final String REPO = PREFIX + "REPO";
  /** Prefix for user defined locks. */
  public static final String USER_PREFIX = "+";

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
  private final ConcurrentMap<Long, StringList> writeLocked =
      new ConcurrentHashMap<Long, StringList>();
  /**
   * Stores a list of objects each transaction has read-locked. Null means lock
   * everything, an empty array lock nothing.
   */
  private final ConcurrentMap<Long, StringList> readLocked =
      new ConcurrentHashMap<Long, StringList>();
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
    final Long thread = Thread.currentThread().getId();
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
      // global write locking
      if(null != write && !write.isEmpty()) {
        while(globalReaders > 0) {
          try {
            globalLock.wait();
          } catch(final InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
        localWriters++;
      }
      // global read locking
      if(null == read) {
        while(localWriters > 0) {
          try {
            globalLock.wait();
          } catch(final InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
        globalReaders++;
      }
    }

    // Local locking
    final StringList writeObjects;
    if(null != write) {
      writeObjects = write.sort(true, true).unique();
      writeLocked.put(thread, writeObjects);
    } else {
      writeObjects = new StringList(0);
    }
    final StringList readObjects;
    if(null != read) {
      readObjects = read.sort(true, true).unique();
      readLocked.put(thread, readObjects);
    } else {
      readObjects = new StringList(0);
    }

    // Use pattern similar to merge sort
    int w = 0, r = 0;
    while(r < readObjects.size() || w < writeObjects.size()) {
      // Look what token comes earlier in alphabet, prefer writing against reading
      if(w < writeObjects.size() && (r >= readObjects.size()
          || writeObjects.get(w).compareTo(readObjects.get(r)) <= 0))
        getOrCreateLock(writeObjects.get(w++)).writeLock().lock();
      else
        // Read lock only if not global write locking; otherwise no lock downgrading from
        // global write lock is possible
        if(null != write) getOrCreateLock(readObjects.get(r++)).readLock().lock();
    }
  }

  /**
   * Only keeps given write locks, downgrades the others to read locks.
   * @param downgrade Write locks to keep
   */
  @Override
  public void downgrade(final StringList downgrade) {
    final Long thread = Thread.currentThread().getId();
    if(null == downgrade)
      throw new IllegalMonitorStateException("Cannot downgrade to global write lock.");
    downgrade.sort(true, true).unique();

    // Fetch current locking status
    final StringList writeObjects = writeLocked.remove(thread);
    final StringList readObjects = readLocked.remove(thread);
    final StringList newWriteObjects = new StringList();
    final StringList newReadObjects = new StringList();
    if(null != readObjects) newReadObjects.add(readObjects);

    if(null != writeObjects) {
      if(!writeObjects.containsAll(downgrade)) throw new IllegalMonitorStateException(
          "Cannot downgrade write lock not acquired.");

      // Perform downgrades
      for(final String object : writeObjects) {
        if(downgrade.contains(object)) {
          newWriteObjects.add(object);
        } else {
          final ReentrantReadWriteLock lock = getOrCreateLock(object);
          assert 1 == lock.getWriteHoldCount() : "Unexpected write lock count: "
              + lock.getWriteHoldCount();
          lock.readLock().lock();
          newReadObjects.add(object);
          lock.writeLock().unlock();
        }
      }
    }

    // Downgrade from global write lock to global read lock
    if(writeAll.isWriteLocked()) {
      // Fetch not yet claimed read locks before releasing global write lock
      for(final String object : readObjects) {
        getOrCreateLock(object).readLock().lock();
      }
      writeAll.readLock().lock();
      writeAll.writeLock().unlock();

      synchronized(globalLock) {
        if(!downgrade.isEmpty())
          localWriters++;
        globalReaders++;
        globalLock.notifyAll();
      }
    }

    // Write back new locking lists
    writeLocked.put(thread, newWriteObjects);
    readLocked.put(thread, newReadObjects);
  }

  /**
   * Gets or creates lock on object.
   * @param object to fetch lock for
   * @return lock on object
   */
  private ReentrantReadWriteLock getOrCreateLock(final String object) {
    ReentrantReadWriteLock lock;
    synchronized(locks) { // Make sure each object lock is a singleton
      lock = locks.get(object);
      if(null == lock) { // Create lock if needed
        lock = new ReentrantReadWriteLock(FAIR);
        locks.put(object, lock);
      }
    }
    return lock;
  }

  @Override
  public void release(final Progress pr) {
    // Release all write locks
    final Long thread = Thread.currentThread().getId();
    final StringList writeObjects = writeLocked.remove(thread);
    if(null != writeObjects) for(final String object : writeObjects) {
      final ReentrantReadWriteLock lock = getOrCreateLock(object);
      assert 1 == lock.getWriteHoldCount() : "Unexpected write lock count: "
          + lock.getWriteHoldCount();
      lock.writeLock().unlock();
    }

    // Release all read locks
    final StringList readObjects = readLocked.remove(thread);
    if(!writeAll.isWriteLocked() && null != readObjects)
      for(final String object : readObjects) {
        getOrCreateLock(object).readLock().unlock();
      }

    // Release global locks
    (writeAll.isWriteLocked() ? writeAll.writeLock() : writeAll.readLock()).unlock();
    if(null != writeObjects && !writeObjects.isEmpty()) synchronized(globalLock) {
      localWriters--;
      globalLock.notifyAll();
    }
    if(null == readObjects) synchronized(globalLock) {
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
