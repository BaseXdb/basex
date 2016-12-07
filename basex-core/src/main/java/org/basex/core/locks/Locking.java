package org.basex.core.locks;

import static org.basex.util.Prop.*;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.data.*;
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
 * @author BaseX Team 2005-16, BSD License
 * @author Jens Erat
 */
public final class Locking {
  /** Prefix for internal special locks. */
  public static final String PREFIX = "%";
  /** Special lock identifier for database opened in current context; will be substituted. */
  public static final String CONTEXT = PREFIX + "CONTEXT";
  /** Special lock identifier for administrative commands. */
  public static final String ADMIN = PREFIX + "ADMIN";
  /** Special lock identifier for backup commands. */
  public static final String BACKUP = PREFIX + "BACKUP";
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
   * Exclusive lock - if globally writing,
   * Shared lock    - else
   */
  private final ReentrantReadWriteLock writeAll = new ReentrantReadWriteLock();
  /** Stores one lock for each object used for locking. */
  private final Map<String, ReentrantReadWriteLock> locks = new HashMap<>();
  /** Stores lock usage counters for each object used for locking. */
  private final Map<String, AtomicInteger> lockUsage = new HashMap<>();
  /**
   * Currently running transactions.
   * Used as monitor for atomizing access to {@link #queue}.
   */
  private int transactions;
  /**
   * Queue for transactions waiting.
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

  /** Fair scheduling; prevents starvation, but reduces parallelism. */
  private final boolean fair;
  /** Maximum number of parallel locks. */
  private final int parallel;

  /**
   * Constructor.
   * @param soptions static options
   */
  public Locking(final StaticOptions soptions) {
    fair = soptions.get(StaticOptions.FAIRLOCK);
    parallel = Math.max(soptions.get(StaticOptions.PARALLEL), 1);
  }

  /**
   * Acquires locks for the specified job.
   * @param job job to be queued
   * @param ctx database context of client
   */
  public void acquire(final Job job, final Context ctx) {
    // get touched databases
    final LockResult lr = new LockResult();
    job.databases(lr);
    final Data data = ctx.data();
    final StringList write = prepareLock(lr.write, lr.writeAll, data);
    final StringList read = write == null ? null : prepareLock(lr.read, lr.readAll, data);
    acquire(read, write);
  }

  /**
   * Prepares the string list for locking.
   * @param sl string list
   * @param all lock all databases
   * @param data data reference
   * @return string list, or {@code null} if all databases need to be locked
   */
  private StringList prepareLock(final StringList sl, final boolean all, final Data data) {
    if(all) return null;
    for(int d = 0; d < sl.size(); d++) {
      // replace context reference with real database name, or remove it if no database is open
      if(sl.get(d).equals(Locking.CONTEXT)) {
        if(data == null) sl.remove(d--);
        else sl.set(d, data.meta.name);
      }
    }
    return sl.sort().unique();
  }

  /**
   * Puts read and write locks on the specified databases.
   * @param read names of databases to put read locks on.
   * Global locking is performed if the passed on reference is {@code null}
   * @param write names of databases to put write locks on.
   * Global locking is performed if the passed on reference is {@code null}
   */
  void acquire(final StringList read, final StringList write) {
    final long threadId = Thread.currentThread().getId();
    if(writeLocked.containsKey(threadId) || readLocked.containsKey(threadId))
      throw new IllegalMonitorStateException("Thread already holds one or more locks.");

    // Wait in queue if locking strategy is fair, or if at least one database needs to be locked
    if(fair || write == null || read == null || !write.isEmpty() || !read.isEmpty()) {
      synchronized(queue) { // Guard queue and transaction, monitor for waiting in queue
        queue.add(threadId);
        while(transactions >= parallel || queue.peek() != threadId) {
          try {
            queue.wait();
          } catch(final InterruptedException ex) {
            Thread.currentThread().interrupt();
          }
        }
        final int t = transactions++;
        assert t <= parallel;
        queue.remove(threadId);
      }
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

        //while(localWriters > 0 && (localWriters > 1 || write == null || write.isEmpty())) {

        while(localWriters > 0 &&
            // We're the only writer, allow global read lock anyway
            !(1 == localWriters && !(null == write || write.isEmpty()))) {
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
      writeLocked.put(threadId, writeObjects);
    } else {
      writeObjects = new StringList(0);
    }
    final StringList readObjects;
    if(read != null) {
      readObjects = read.sort().unique();
      readLocked.put(threadId, readObjects);
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
        lock = new ReentrantReadWriteLock(fair);
        locks.put(object, lock);
      }
    }
    return lock;
  }

  /**
   * Removes locks for the specified job.
   */
  public void release() {
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
    if(writeObjects != null && !writeObjects.isEmpty()) {
      synchronized(globalLock) {
        localWriters--;
        globalLock.notifyAll();
      }
    }
    if(readObjects == null) {
      synchronized(globalLock) {
        globalReaders--;
        globalLock.notifyAll();
      }
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
    synchronized(locks) {
      final AtomicInteger usage = lockUsage.get(lock);
      if(usage == null) {
        lockUsage.put(lock, new AtomicInteger(1));
      } else {
        usage.incrementAndGet();
      }
    }
  }

  /**
   * Unsets lock if unused.
   * @param object Object to test
   */
  private void unsetLockIfUnused(final String object) {
    synchronized(locks) {
      final AtomicInteger usage = lockUsage.get(object);
      if(usage.decrementAndGet() == 0) {
        locks.remove(object);
        lockUsage.remove(object);
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
    synchronized(locks) {
      for(final Entry<String, ReentrantReadWriteLock> e : locks.entrySet())
        sb.append(ind + ind + e.getKey() + " -> " + e.getValue() + NL);
    }
    sb.append(ind + "Held write locks by transaction:" + NL);
    for(final Entry<Long, StringList> entry : writeLocked.entrySet())
      sb.append(ind + ind + entry.getKey() + " -> " + entry.getValue() + NL);
    sb.append(ind + "Held read locks by transaction:" + NL);
    for(final Entry<Long, StringList> entry : readLocked.entrySet())
      sb.append(ind + ind + entry.getKey() + " -> " + entry.getValue() + NL);
    return sb.toString();
  }

}
