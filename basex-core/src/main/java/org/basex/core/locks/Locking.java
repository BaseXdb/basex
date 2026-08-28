package org.basex.core.locks;

import static org.basex.util.Prop.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Read and write locks on arbitrary strings.
 *
 * A maximum of {@link StaticOptions#PARALLEL} concurrent locking jobs is allowed.
 *
 * (Non-)fair locking can be adjusted via the {@link StaticOptions#FAIRLOCK} option.
 *
 * This class prevents locking deadlocks by sorting all strings.
 *
 * Locks can only be released by the same thread which acquired it.
 *
 * Locking methods are not synchronized to each other. The user must make sure not to call them in
 * parallel by the same thread (it is fine to call arbitrary locking methods by different threads at
 * the same time).
 *
 * @author BaseX Team, BSD License
 * @author Jens Erat
 */
public final class Locking {
  /** Prefix for internal special locks. */
  public static final String INTERNAL_PREFIX = "internal:";
  /** Prefix for query locks. */
  public static final String BASEX_PREFIX = "basex:";

  /** Special lock identifier for database opened in current context; will be substituted. */
  public static final String CONTEXT = INTERNAL_PREFIX + "context";
  /** Special lock identifier for collection available via current context; will be substituted. */
  public static final String COLLECTION = INTERNAL_PREFIX + "collection";
  /** Special lock identifier for user commands. */
  public static final String USER = INTERNAL_PREFIX + "user";
  /** Special lock identifier for repository commands. */
  public static final String REPO = INTERNAL_PREFIX + "repo";
  /** Prefix for locks on the backups of a database. */
  private static final String BACKUP_PREFIX = INTERNAL_PREFIX + "backup:";

  /** Fair ordering policy; prevents starvation, but reduces parallelism. */
  private final boolean fair;

  /** Locks assigned to threads. */
  private final ConcurrentMap<Long, Locks> locked = new ConcurrentHashMap<>();
  /** Lock queue. */
  private final LockQueue queue;

  /** Global lock: exclusive lock for global writes, shared lock otherwise. */
  private final ReentrantReadWriteLock globalLocks;
  /** Stores one lock for each lock string. */
  private final Map<String, LocalReadWriteLock> localLocks = new HashMap<>();
  /** Lock for queuing local writes and global reads. */
  private final ReentrantLock globalLock = new ReentrantLock();
  /** Condition for signaling completion of local writes and global reads. */
  private final Condition globalCond = globalLock.newCondition();

  /** Number of running local writers. */
  private int localWriters;
  /** Number of running global readers. */
  private int globalReaders;

  /**
   * Constructor.
   * @param soptions static options
   */
  public Locking(final StaticOptions soptions) {
    fair = soptions.get(StaticOptions.FAIRLOCK);
    globalLocks = new ReentrantReadWriteLock(fair);
    final int parallel = Math.max(soptions.get(StaticOptions.PARALLEL), 1);
    queue = fair ? new FairLockQueue(parallel) : new NonfairLockQueue(parallel);
  }

  /**
   * Acquires locks for the specified job.
   * @param job job to be queued
   * @param ctx database context of client
   */
  public void acquire(final Job job, final Context ctx) {
    // collect lock strings
    job.addLocks();
    // prepare lock strings and acquire locks
    try {
      acquire(job.jc().locks.finish(ctx), !job.inheritsSlot());
    } catch(final InterruptedException ex) {
      throw Util.notExpected("Thread was interrupted: %", ex);
    }
  }

  /**
   * Puts read and write locks for the specified lock lists.
   * The lists must have been prepared for locking (see {@link Locks#finish(Context)}).
   * @param locks locks
   * @throws InterruptedException interrupted exception
   */
  void acquire(final Locks locks) throws InterruptedException {
    acquire(locks, true);
  }

  /**
   * Puts read and write locks for the specified lock lists.
   * The lists must have been prepared for locking (see {@link Locks#finish(Context)}).
   * @param locks locks
   * @param slot occupy a run slot in the lock queue
   * @throws InterruptedException interrupted exception
   */
  private void acquire(final Locks locks, final boolean slot) throws InterruptedException {
    // one thread can only hold a single lock
    final Long id = Thread.currentThread().threadId();
    if(locked.containsKey(id)) throw new IllegalMonitorStateException("Thread holds locks: " + id);
    locked.put(id, locks);

    // queue job if the job limit has been reached (only locking jobs count towards the limit)
    final LockList reads = locks.reads, writes = locks.writes;
    final boolean write = writes.locking(), read = reads.locking(), lock = read || write;
    boolean global = false;
    locks.slot = false;
    try {
      // a job that inherits the run slot of a blocked caller must not occupy a second one
      if(lock && slot) {
        queue.acquire(id, read, write);
        locks.slot = true;
      }
      if(lock) {
        // apply exclusive lock (global write), or shared lock otherwise
        (writes.global() ? globalLocks.writeLock() : globalLocks.readLock()).lock();
        global = true;
      }

      globalLock.lock();
      try {
        final boolean localWrite = writes.local(), globalRead = reads.global();
        if(localWrite && globalRead) {
          // job is both local writer and global reader
          while(localWriters > 0 || globalReaders > 0) globalCond.await();
          localWriters++;
          globalReaders++;
        } else if(localWrite) {
          // local write lock: wait for completion of global readers
          while(globalReaders > 0) globalCond.await();
          localWriters++;
        } else if(globalRead) {
          // global read lock: wait for completion of local writers
          while(localWriters > 0) globalCond.await();
          globalReaders++;
        }
      } finally {
        globalLock.unlock();
      }
    } catch(final InterruptedException ex) {
      // an interrupted acquisition holds nothing: give back what was reserved for it
      locked.remove(id);
      if(global) (writes.global() ? globalLocks.writeLock() : globalLocks.readLock()).unlock();
      if(locks.slot) queue.release();
      throw ex;
    }

    // assign locks in sorted order (to ensure that write locks will be assigned first)
    int w = 0, r = 0;
    final int rs = reads.size(), ws = writes.size();
    while(r < rs || w < ws) {
      if(w < ws && (r == rs || writes.get(w).compareTo(reads.get(r)) <= 0)) {
        pin(writes.get(w++)).writeLock().lock();
      } else {
        pin(reads.get(r++)).readLock().lock();
      }
    }
  }

  /**
   * Releases the locks that are held by the calling thread.
   */
  public void release() {
    final Long id = Thread.currentThread().threadId();
    final Locks locks = locked.remove(id);
    final LockList reads = locks.reads, writes = locks.writes;
    final boolean lock = reads.locking() || writes.locking();

    // release all local locks
    for(final String string : reads) unpin(string, false);
    for(final String string : writes) unpin(string, true);

    // allow next global reader and local writer to resume
    final boolean globalRead = reads.global(), localWrite = writes.local();
    if(globalRead || localWrite) {
      globalLock.lock();
      try {
        if(globalRead) globalReaders--;
        if(localWrite) localWriters--;
        globalCond.signalAll();
      } finally {
        globalLock.unlock();
      }
    }

    // release exclusive lock (global write), or shared lock otherwise
    if(lock) (writes.global() ? globalLocks.writeLock() : globalLocks.readLock()).unlock();

    // allow next queued job to resume (jobs without a run slot were never counted)
    if(locks.slot) queue.release();
  }

  /**
   * Returns the locks currently held by the calling thread.
   * @return locks, or {@code null} if the thread holds no locks
   */
  public Locks held() {
    return locked.get(Thread.currentThread().threadId());
  }

  /**
   * Pins a lock string. Creates a new lock if necessary.
   * @param string lock string
   * @return lock
   */
  private LocalReadWriteLock pin(final String string) {
    synchronized(localLocks) {
      final LocalReadWriteLock lock = localLocks.computeIfAbsent(string,
          k -> new LocalReadWriteLock(fair));
      lock.pin();
      return lock;
    }
  }

  /**
   * Releases a lock string and removes the lock if its pin count drops to zero.
   * @param string lock string
   * @param write release write lock
   */
  private void unpin(final String string, final boolean write) {
    synchronized(localLocks) {
      // the lock must be released before it is dropped: otherwise, another job could pin a new
      // instance for the same string and enter while this job still holds the old one
      final LocalReadWriteLock lock = localLocks.get(string);
      (write ? lock.writeLock() : lock.readLock()).unlock();
      if(lock.unpin()) localLocks.remove(string);
    }
  }

  /**
   * Indicates if a lock key refers to a database.
   * @param lock lock key
   * @return result of check
   */
  public static boolean database(final String lock) {
    return !lock.startsWith(INTERNAL_PREFIX) || Strings.eq(lock, CONTEXT, COLLECTION);
  }

  /**
   * Returns the lock key for the backups of a database.
   * @param db name of the database
   * @return lock key
   */
  public static String backup(final String db) {
    return BACKUP_PREFIX + db;
  }

  /**
   * Returns query lock keys.
   * @param string string with lock keys
   * @return locks
   */
  public static String[] queryLocks(final byte[] string) {
    final StringList list = new StringList();
    for(final byte[] lock : split(string, ',')) {
      list.add(BASEX_PREFIX + string(lock).trim());
    }
    if(list.isEmpty()) list.add(BASEX_PREFIX);
    return list.finish();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(NL).append("Locking").append(NL);
    final String in = "| ";
    sb.append(in).append(queue).append(NL);
    sb.append(in).append("Held locks by object:").append(NL);
    synchronized(localLocks) {
      localLocks.forEach((key, value) ->
        sb.append(in).append(in).append(key).append(" -> ").append(value).append(NL));
    }
    sb.append(in).append("Held locks by job:").append(NL);
    locked.forEach((key, value) ->
      sb.append(in).append(in).append(key).append(" -> ").append(value).append(NL));
    return sb.toString();
  }
}
