package org.basex.core.locks;

import static org.basex.util.Prop.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import org.basex.core.*;
import org.basex.util.list.*;

/**
 * Manage read and write locks on arbitrary strings. Maximum of
 * {@link StaticOptions#PARALLEL} concurrent transactions are allowed,
 * further will be queued.
 *
 * This class prevents locking deadlocks by sorting all all strings
 *
 * Locks can only be released by the same thread which acquired it.
 *
 * Locking methods are not synchronized to each other. The user must make sure not to call
 * them in parallel by the same thread (it is fine to call arbitrary locking methods by
 * different threads at the same time).
 *
 * This locking can be deactivated by setting {@link StaticOptions#GLOBALLOCK} to
 * {@code true}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Jens Erat
 */
public final class DBLocking implements Locking {
  /** Fair scheduling; prevents starvation, but reduces parallelism. */
  private static final boolean FAIR = true;

  /** Prefix for internal special locks. */
  private static final String PREFIX = "%";
  /** Special lock identifier for collection available via current context; will be substituted. */
  public static final String COLL = PREFIX + "COLL";
  /** Special lock identifier for database opened in current context; will be substituted. */
  public static final String CTX = PREFIX + "CTX";
  /** Special lock identifier for administrative commands. */
  public static final String ADMIN = PREFIX + "ADMIN";
  /** Special lock identifier for backup commands. */
  public static final String BACKUP = PREFIX + "BACKUP";
  /** Special lock identifier for event commands. */
  public static final String EVENT = PREFIX + "EVENT";
  /** Special lock identifier for repository commands. */
  public static final String REPO = PREFIX + "REPO";
  /** Prefix for user defined locks. */
  public static final String USER_PREFIX = "+";
  /** Prefix for locks in Java modules. */
  public static final String MODULE_PREFIX = "&";

  /** Lock for running thread counters. */
  private final Object globalLock = new Object();
  /** Number of running local writers. Guarded by {@code globalLock}. */
  private int localWriters;
  /** Number of running local writers. Guarded by {@code globalLock}. */
  private int globalReaders;
  /**
   * Lock for global write locking.
   *
   * Exclusive lock - if globally writing,
   * Shared lock    - else
   */
  private final ReentrantReadWriteLock writeAll = new ReentrantReadWriteLock();
  /** Stores one lock for each object used for locking. */
  private final Map<String, ReentrantReadWriteLock> locks = new HashMap<>();
  /** Stores lock usage counters for each object used for locking. */
  private final Map<String, Integer> lockUsage = new HashMap<>();
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
  private final Queue<Long> queue = new LinkedList<>();
  /**
   * Stores a list of objects each transaction has write-locked.
   * Null means lock everything, an empty array lock nothing.
   */
  private final ConcurrentMap<Long, StringList> writeLocked = new ConcurrentHashMap<>();
  /**
   * Stores a list of objects each transaction has read-locked. Null means lock
   * everything, an empty array lock nothing.
   */
  private final ConcurrentMap<Long, StringList> readLocked = new ConcurrentHashMap<>();
  /** Static options. */
  private final StaticOptions sopts;

  /**
   * Initialize new Locking instance.
   * @param sopts static options
   */
  public DBLocking(final StaticOptions sopts) {
    this.sopts = sopts;
  }

  @Override
  public void acquire(final Proc pr, final StringList read, final StringList write) {
    final long thread = Thread.currentThread().getId();
    if(writeLocked.containsKey(thread) || readLocked.containsKey(thread))
      throw new IllegalMonitorStateException("Thread already holds one or more locks.");

    // Wait in queue if necessary
    synchronized(queue) { // Guard queue and transaction, monitor for waiting in queue
      queue.add(thread);
      while(transactions >= Math.max(sopts.get(StaticOptions.PARALLEL), 1)
          || queue.peek() != thread) {
        try {
          queue.wait();
        } catch(final InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
      final int t = transactions++;
      assert t <= Math.max(sopts.get(StaticOptions.PARALLEL), 1);
      queue.remove(thread);
    }

    // Global write lock if write StringList is not set
    (write == null ? writeAll.writeLock() : writeAll.readLock()).lock();

    synchronized(globalLock) {
      // local write locking
      if(write != null && !write.isEmpty()) {
        while(globalReaders > 0) {
          try {
            globalLock.wait();
          } catch(final InterruptedException ex) {
            Thread.currentThread().interrupt();
          }
        }
        localWriters++;
      }
      // global read locking
      if(read == null) {
        while(localWriters > 0) {
          try {
            globalLock.wait();
          } catch(final InterruptedException ex) {
            Thread.currentThread().interrupt();
          }
        }
        globalReaders++;
      }
    }

    // Local locking
    final StringList writeObjects;
    if(write != null) {
      writeObjects = write.sort().unique();
      writeLocked.put(thread, writeObjects);
    } else {
      writeObjects = new StringList(0);
    }
    final StringList readObjects;
    if(read != null) {
      readObjects = read.sort().unique();
      readLocked.put(thread, readObjects);
    } else {
      readObjects = new StringList(0);
    }

    // Use pattern similar to merge sort
    int w = 0, r = 0;
    final int rs = readObjects.size(), ws = writeObjects.size();
    while(r < rs || w < ws) {
      // Look what token comes earlier in alphabet, prefer writing against reading
      if(w < ws && (r >= rs || writeObjects.get(w).compareTo(readObjects.get(r)) <= 0)) {
        final String writeObject = writeObjects.get(w++);
        setLockUsed(writeObject);
        getOrCreateLock(writeObject).writeLock().lock();
      } else
      // Read lock only if not global write locking; otherwise no lock downgrading from
      // global write lock is possible
      if(write != null) {
        final String readObject = readObjects.get(r++);
        setLockUsed(readObject);
        getOrCreateLock(readObject).readLock().lock();
      }
    }
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
      if(lock == null) { // Create lock if needed
        lock = new ReentrantReadWriteLock(FAIR);
        locks.put(object, lock);
      }
    }
    return lock;
  }

  @Override
  public void release(final Proc pr) {
    // Release all write locks
    final Long thread = Thread.currentThread().getId();
    final StringList writeObjects = writeLocked.remove(thread);
    if(writeObjects != null) for(final String object : writeObjects) {
      final ReentrantReadWriteLock lock = getOrCreateLock(object);
      assert lock.getWriteHoldCount() == 1 : "Unexpected write lock count: "
          + lock.getWriteHoldCount();
      lock.writeLock().unlock();
      unsetLockIfUnused(object);
    }

    // Release all read locks
    final StringList readObjects = readLocked.remove(thread);
    if(!writeAll.isWriteLocked() && readObjects != null)
      for(final String object : readObjects) {
        getOrCreateLock(object).readLock().unlock();
        unsetLockIfUnused(object);
      }

    // Release global locks
    (writeAll.isWriteLocked() ? writeAll.writeLock() : writeAll.readLock()).unlock();
    if(writeObjects != null && !writeObjects.isEmpty()) synchronized(globalLock) {
      localWriters--;
      globalLock.notifyAll();
    }
    if(readObjects == null) synchronized(globalLock) {
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
   * Marks a lock as used.
   * @param lock Lock to set used
   */
  private void setLockUsed(final String lock) {
    synchronized(lockUsage) {
      Integer usage = lockUsage.get(lock);
      if(usage == null) usage = 0;
      lockUsage.put(lock, ++usage);
    }
  }

  /**
   * Unsets lock if unused.
   * @param object Object to test
   */
  private void unsetLockIfUnused(final String object) {
    synchronized(lockUsage) {
      Integer usage = lockUsage.get(object);
      assert usage != null;
      if(--usage == 0) {
        locks.remove(object);
        lockUsage.remove(object);
      } else {
        lockUsage.put(object, usage);
      }
    }
  }

  /**
   * Present current locking status. Not to be seen as a programming API but only for
   * debugging purposes.
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(NL);
    sb.append("Locking" + NL);
    final String ind = "| ";
    sb.append(ind + "Transactions running: " + transactions + NL);
    sb.append(ind + "Transaction queue: " + queue + NL);
    sb.append(ind + "Held locks by object:" + NL);
    for(final Entry<String, ReentrantReadWriteLock> e : locks.entrySet())
      sb.append(ind + ind + e.getKey() + " -> " + e.getValue() + NL);
    sb.append(ind + "Held write locks by transaction:" + NL);
    for(final Long thread : writeLocked.keySet())
      sb.append(ind + ind + thread + " -> " + writeLocked.get(thread) + NL);
    sb.append(ind + "Held read locks by transaction:" + NL);
    for(final Long thread : readLocked.keySet())
      sb.append(ind + ind + thread + " -> " + readLocked.get(thread) + NL);
    return sb.toString();
  }

}
