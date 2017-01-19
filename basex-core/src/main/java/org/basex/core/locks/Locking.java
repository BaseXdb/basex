package org.basex.core.locks;

import static org.basex.util.Prop.*;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import org.basex.core.*;
import org.basex.core.jobs.*;

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
 * @author BaseX Team 2005-16, BSD License
 * @author Jens Erat
 */
public final class Locking {
  /** Prefix for internal special locks. */
  public static final String PREFIX = "%";
  /** Prefix for user defined locks. */
  public static final String USER_PREFIX = "+";
  /** Prefix for locks in Java modules. */
  public static final String MODULE_PREFIX = "&";

  /** Special lock identifier for database opened in current context; will be substituted. */
  public static final String CONTEXT = PREFIX + "CONTEXT";
  /** Special lock identifier for collection available via current context; will be substituted. */
  public static final String COLLECTION = PREFIX + "COLLECTION";
  /** Special lock identifier for user commands. */
  public static final String USER = PREFIX + "USER";
  /** Special lock identifier for backup commands. */
  public static final String BACKUP = PREFIX + "BACKUP";
  /** Special lock identifier for repository commands. */
  public static final String REPO = PREFIX + "REPO";

  /** Fair ordering policy; prevents starvation, but reduces parallelism. */
  private final boolean fair;
  /** Maximum number of parallel jobs. */
  private final int parallel;

  /** Write locks assigned to threads. */
  private final ConcurrentMap<Long, LockList> writeLocked = new ConcurrentHashMap<>();
  /** Read locks assigned to threads. */
  private final ConcurrentMap<Long, LockList> readLocked = new ConcurrentHashMap<>();
  /** Lock queue. */
  private final LockQueue queue;

  /** Global lock: exclusive lock for global writes, shared lock otherwise. */
  private final ReentrantReadWriteLock globalLock;
  /** Stores one lock for each lock string. */
  private final Map<String, LocalLock> localLocks = new HashMap<>();

  /** Number of running local writers. Guarded by {@link #globalLock}. */
  private int localWriters;
  /** Number of running global readers. Guarded by {@link #globalLock}. */
  private int globalReaders;
  /** Number of currently running jobs. */
  private int jobs;

  /**
   * Constructor.
   * @param soptions static options
   */
  public Locking(final StaticOptions soptions) {
    fair = soptions.get(StaticOptions.FAIRLOCK);
    parallel = Math.max(soptions.get(StaticOptions.PARALLEL), 1);
    globalLock = new ReentrantReadWriteLock(fair);
    queue = fair ? new FairLockQueue() : new NonfairLockQueue();
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
    final Locks locks = job.job().locks;
    locks.finish(ctx);

    try {
      acquire(locks.reads, locks.writes);
    } catch(final InterruptedException ex) {
      Thread.currentThread().interrupt();
      job.stop();
    }

    // before running the job, check if it has been stopped
    job.checkStop();
  }

  /**
   * Puts read and write locks for the specified lock lists.
   * The lists must have been prepared for locking (see {@link Locks#finish(Context)}).
   * @param reads read locks
   * @param writes write locks
   * @throws InterruptedException interrupted exception
   */
  void acquire(final LockList reads, final LockList writes) throws InterruptedException {
    final Long id = Thread.currentThread().getId();
    final boolean write = writes.locking(), read = reads.locking(), lock = read || write;

    // one thread can only hold a single lock
    if(writeLocked.containsKey(id) || readLocked.containsKey(id))
      throw new IllegalMonitorStateException("Thread already holds locks.");
    writeLocked.put(id, writes);
    readLocked.put(id, reads);

    // queue job if the job limit has been reached
    synchronized(queue) {
      if(jobs >= parallel) queue.wait(id, read, write);
      jobs++;
    }

    // apply exclusive lock (global write), or shared lock otherwise
    if(lock) (writes.global() ? globalLock.writeLock() : globalLock.readLock()).lock();

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
    final LockList reads = readLocked.remove(id), writes = writeLocked.remove(id);
    final boolean lock = reads.locking() || writes.locking();

    // release all local locks
    for(final String read : reads) unpin(read).readLock().unlock();
    for(final String write : writes) unpin(write).writeLock().unlock();

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
    if(lock) (writes.global() ? globalLock.writeLock() : globalLock.readLock()).unlock();

    // allow next queued job to resume
    synchronized(queue) {
      jobs--;
      queue.notifyAll();
    }
  }

  /**
   * Pins a lock string. Creates a new lock if necessary.
   * @param string lock string
   * @return lock
   */
  private LocalLock pin(final String string) {
    synchronized(localLocks) {
      LocalLock lock = localLocks.get(string);
      if(lock == null) {
        lock = new LocalLock(fair);
        localLocks.put(string, lock);
      }
      lock.pin();
      return lock;
    }
  }

  /**
   * Unpins a lock string. Removes a lock if pin count is zero.
   * @param string lock string
   * @return lock
   */
  private LocalLock unpin(final String string) {
    synchronized(localLocks) {
      final LocalLock lock = localLocks.get(string);
      if(lock.unpin() == 0) {
        localLocks.remove(lock);
      }
      return lock;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(NL);
    sb.append("Locking" + NL);
    final String ind = "| ";
    sb.append(ind + "Running jobs: " + jobs + NL);
    sb.append(ind + queue + NL);
    sb.append(ind + "Held locks by object:" + NL);
    synchronized(localLocks) {
      for(final Entry<String, LocalLock> e : localLocks.entrySet())
        sb.append(ind + ind + e.getKey() + " -> " + e.getValue() + NL);
    }
    sb.append(ind + "Held write locks by job:" + NL);
    for(final Entry<Long, LockList> entry : writeLocked.entrySet())
      sb.append(ind + ind + entry.getKey() + " -> " + entry.getValue() + NL);
    sb.append(ind + "Held read locks by job:" + NL);
    for(final Entry<Long, LockList> entry : readLocked.entrySet())
      sb.append(ind + ind + entry.getKey() + " -> " + entry.getValue() + NL);
    return sb.toString();
  }
}
