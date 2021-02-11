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
 * @author BaseX Team 2005-21, BSD License
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
  /** Special lock identifier for backup commands. */
  public static final String BACKUP = INTERNAL_PREFIX + "backup";
  /** Special lock identifier for repository commands. */
  public static final String REPO = INTERNAL_PREFIX + "repo";

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
  /** Lock object for queuing local writes and global reads. */
  private final Object globalLock = new Object();

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
    final Locks locks = job.jc().locks;
    locks.finish(ctx);
    try {
      acquire(locks);
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
    // one thread can only hold a single lock
    final Long id = Thread.currentThread().getId();
    if(locked.containsKey(id)) throw new IllegalMonitorStateException("Thread holds locks: " + id);
    locked.put(id, locks);

    // queue job if the job limit has been reached
    final LockList reads = locks.reads, writes = locks.writes;
    final boolean write = writes.locking(), read = reads.locking(), lock = read || write;
    queue.acquire(id, read, write);

    // apply exclusive lock (global write), or shared lock otherwise
    if(lock) (writes.global() ? globalLocks.writeLock() : globalLocks.readLock()).lock();

    synchronized(globalLock) {
      // local write locks: wait for completion of global readers
      if(writes.local()) {
        while(globalReaders > 0) globalLock.wait();
        localWriters++;
      }
      // global read lock: wait for completion of local writers (excluding the current job)
      if(reads.global()) {
        while(localWriters > 1 || localWriters == 1 && !writes.local()) globalLock.wait();
        globalReaders++;
      }
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
   * Removes locks for the specified job, all in reverse order.
   */
  public void release() {
    final Long id = Thread.currentThread().getId();
    final Locks locks = locked.remove(id);
    final LockList reads = locks.reads, writes = locks.writes;
    final boolean lock = reads.locking() || writes.locking();

    // release all local locks
    for(final String string : reads) unpin(string).readLock().unlock();
    for(final String string : writes) unpin(string).writeLock().unlock();

    // allow next global reader to resume
    synchronized(globalLock) {
      if(reads.global()) {
        globalReaders--;
        globalLock.notifyAll();
      }
    }

    // allow next local writer to resume
    synchronized(globalLock) {
      if(writes.local()) {
        localWriters--;
        globalLock.notifyAll();
      }
    }

    // release exclusive lock (global write), or shared lock otherwise
    if(lock) (writes.global() ? globalLocks.writeLock() : globalLocks.readLock()).unlock();

    // allow next queued job to resume
    queue.release();
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
   * Unpins a lock string. Removes a lock if pin count is zero.
   * @param string lock string
   * @return lock
   */
  private LocalReadWriteLock unpin(final String string) {
    synchronized(localLocks) {
      final LocalReadWriteLock lock = localLocks.get(string);
      if(lock.unpin()) localLocks.remove(string);
      return lock;
    }
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
