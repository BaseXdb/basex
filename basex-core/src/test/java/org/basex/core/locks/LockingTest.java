package org.basex.core.locks;

import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

/**
 * Locking tests.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Jens Erat
 */
@RunWith(Parameterized.class)
public final class LockingTest extends SandboxTest {
  /** How often should tests be repeated? */
  private static final int REPEAT = 1;
  /** How many milliseconds to wait for threads to finish. */
  private static final long WAIT = 100L;
  /** Number of threads used in fuzzing test. */
  private static final int FUZZING_THREADS = 5;
  /** Repeated locking events each thread should trigger. */
  private static final int FUZZING_REPEATS = 10;
  /** How long each lock should be hold before releasing it and fetching the next. */
  private static final int HOLD_TIME = 10;

  /**
   * Enable repeated running of test to track down synchronization issues.
   * @return Collection of empty object arrays
   */
  @Parameters
  public static Collection<Object[]> generateParams() {
    final List<Object[]> params = new ArrayList<>();
    for(int i = 1; i <= REPEAT; i++) {
      params.add(new Object[0]);
    }
    return params;
  }

  /** Locking instance used for testing. */
  private final Locking locks = new Locking(context.soptions);
  /** Objects used for locking. */
  private final String[] objects = { "0", "1", "2", "3", "4" };
  /** Empty string array for convenience. */
  private static final String[] NONE = { };

  /**
   * Single thread acquires both global read lock and a single write lock.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void singleThreadGlobalReadLocalWriteTest() throws InterruptedException {
    final CountDownLatch test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, null, objects, test);

    th1.start();
    assertTrue("Thread should be able to acquire lock.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
  }

  /**
   * Test for concurrent writes.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void writeWriteTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, NONE, objects, sync);
    final LockTester th2 = new LockTester(sync, NONE, objects, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
  }

  /**
   * Fetch write lock, then read lock.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void writeReadTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, NONE, objects, sync);
    final LockTester th2 = new LockTester(sync, objects, NONE, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
  }

  /**
   * Fetch read lock, then write lock.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void readWriteTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, objects, NONE, sync);
    final LockTester th2 = new LockTester(sync, NONE, objects, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
  }

  /**
   * Fetch two read locks.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void readReadTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, objects, NONE, sync);
    final LockTester th2 = new LockTester(sync, objects, NONE, test);

    th1.start();
    th2.start();
    assertTrue("Thread 2 should be able to acquire lock.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    th2.release();
  }

  /**
   * Test parallel transaction limit.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void parallelTransactionLimitTest() throws InterruptedException {
    final CountDownLatch latch =
        new CountDownLatch(Math.max(context.soptions.get(StaticOptions.PARALLEL), 1));
    // Container for(maximum number allowed transactions) + 1 testers
    final int tl = (int) latch.getCount() + 1;
    final LockTester[] testers = new LockTester[tl];

    // Start maximum number of allowed transactions
    for(int t = 0; t < tl - 1; t++) {
      testers[t] = new LockTester(null, objects, NONE, latch);
      testers[t].start();
    }
    assertTrue("Couldn't start maximum allowed number of parallel transactions!",
        latch.await(WAIT, TimeUnit.MILLISECONDS));

    // Start one more transaction
    final CountDownLatch latch2 = new CountDownLatch(1);
    testers[tl - 1] = new LockTester(null, objects, NONE, latch2);
    testers[tl - 1].start();
    assertFalse("Shouldn't be able to start another parallel transaction yet!",
        latch2.await(WAIT, TimeUnit.MILLISECONDS));

    // Stop first transaction
    testers[0].release();
    assertTrue("New transaction should have started!",
        latch2.await(WAIT, TimeUnit.MILLISECONDS));

    // Stop all other transactions
    for(int t = 1; t < tl; t++) testers[t].release();
  }

  /**
   * Global locking test.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void globalWriteLocalWriteLockingTest() throws InterruptedException {
    final CountDownLatch sync1 = new CountDownLatch(1), sync2 = new CountDownLatch(1),
        test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, NONE, objects, sync1);
    final LockTester th2 = new LockTester(sync1, NONE, null, sync2);
    final LockTester th3 = new LockTester(sync2, NONE, objects, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        sync2.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        sync2.await(WAIT, TimeUnit.MILLISECONDS));
    th3.start();
    assertFalse("Thread 3 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
    assertTrue("Thread 3 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th3.release();
  }

  /**
   * Global locking test.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void globalWriteLocalReadLockingTest() throws InterruptedException {
    final CountDownLatch sync1 = new CountDownLatch(1), sync2 = new CountDownLatch(1),
        test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, objects, NONE, sync1);
    final LockTester th2 = new LockTester(sync1, NONE, null, sync2);
    final LockTester th3 = new LockTester(sync2, objects, NONE, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        sync2.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        sync2.await(WAIT, TimeUnit.MILLISECONDS));
    th3.start();
    assertFalse("Thread 3 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
    assertTrue("Thread 3 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th3.release();
  }

  /**
   * Global locking test.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void globalReadLocalWriteLockingTest() throws InterruptedException {
    final CountDownLatch sync1 = new CountDownLatch(1), sync2 = new CountDownLatch(1),
        test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, NONE, objects, sync1);
    final LockTester th2 = new LockTester(sync1, null, NONE, sync2);
    final LockTester th3 = new LockTester(sync2, NONE, objects, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        sync2.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        sync2.await(WAIT, TimeUnit.MILLISECONDS));
    th3.start();
    assertFalse("Thread 3 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
    assertTrue("Thread 3 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th3.release();
  }

  /**
   * Global locking test.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void globalReadLocalReadLockingTest() throws InterruptedException {
    final CountDownLatch sync1 = new CountDownLatch(1), sync2 = new CountDownLatch(1),
        test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, objects, NONE, sync1);
    final LockTester th2 = new LockTester(sync1, null, NONE, sync2);
    final LockTester th3 = new LockTester(sync2, objects, NONE, test);

    th1.start();
    th2.start();
    assertTrue("Thread 2 should be able to acquire lock.",
        sync2.await(WAIT, TimeUnit.MILLISECONDS));
    th3.start();
    assertTrue("Thread 3 should be able to acquire lock.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    th2.release();
    th3.release();
  }

  /**
   * Simultaneous read/write lock test.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void simultaneousReadWriteTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, objects, objects, sync);
    final LockTester th2 = new LockTester(sync, objects, NONE, test);
    final LockTester th3 = new LockTester(sync, NONE, objects, test);

    th1.start();
    th2.start();
    th3.start();
    assertFalse("Threads 2 & 3 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Threads 2 & 3 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
    th3.release();
  }

  /**
   * Another simultaneous read/write lock test.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void simultaneousReadWriteTestSingleReadFirst() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, objects, NONE, sync);
    final LockTester th2 = new LockTester(sync, objects, objects, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
  }

  /**
   * Another simultaneous read/write lock test.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void simultaneousReadWriteTestSingleWriteFirst() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    final LockTester th1 = new LockTester(null, NONE, objects, sync);
    final LockTester th2 = new LockTester(sync, objects, objects, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
  }

  /**
   * Locks downgrading, the other thread is reader.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void downgradeOtherReadTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);

    assertTrue("Increase {@code objects.length}!", objects.length > 1);
    final String[] release = Arrays.copyOf(objects, 1);

    final LockTester th1 = new LockTester(null, NONE, objects, sync);
    final LockTester th2 = new LockTester(sync, release, NONE, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    th2.release();
  }

  /**
   * Locks downgrading, the other thread is writer.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void downgradeOtherWriteTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);

    assertTrue("Increase {@code objects.length}!", objects.length > 1);
    final String[] release = Arrays.copyOf(objects, 1);

    final LockTester th1 = new LockTester(null, NONE, objects, sync);
    final LockTester th2 = new LockTester(sync, NONE, release, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
  }

  /**
   * Downgrades from global write lock, other fetches local writes locks.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void downgradeGlobalWriteLockTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);

    final LockTester th1 = new LockTester(null, NONE, null, sync);
    final LockTester th2 = new LockTester(sync, NONE, objects, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
  }

  /**
   * Downgrades from global write lock, other fetches local writes locks.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void downgradeToNoWriteLocksTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);

    final LockTester th1 = new LockTester(null, NONE, objects, sync);
    final LockTester th2 = new LockTester(sync, null, NONE, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    th2.release();
  }

  /**
   * Locks downgrading holding read locks.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void downgradeHoldingReadLocksTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);

    assertTrue("Increase {@code objects.length}!", objects.length > 1);
    final String[] release = Arrays.copyOf(objects, 1);
    final String[] keep = Arrays.copyOfRange(objects, 1, objects.length);

    final LockTester th1 = new LockTester(null, objects, release, sync);
    final LockTester th2 = new LockTester(sync, NONE, keep, test);

    th1.start();
    th2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    th2.release();
  }

  /**
   * Forces a deadlock.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void deadlockTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test2 = new CountDownLatch(1),
        test3 = new CountDownLatch(1);

    final LockTester th1 = new LockTester(null, NONE, new String[] { "3" }, sync);
    final LockTester th2 = new LockTester(
        sync, new String[] { "2" }, new String[] { "1", "3" }, test2);
    final LockTester th3 = new LockTester(
        sync, new String[] { "1" }, new String[] { "2", "3" }, test3);

    th1.start();
    // Make sure thread 1 has wl(3)
    Thread.sleep(WAIT);
    th2.start();
    th3.start();
    th1.release();
    assertTrue(
        "One of the threads should be able to aquire its locks now.",
        test2.await(WAIT, TimeUnit.MILLISECONDS)
            || test3.await(WAIT, TimeUnit.MILLISECONDS));
    boolean which = false;
    if(test2.getCount() == 0) {
      th2.release();
      which = true;
    } else th3.release();
    assertTrue(
        "The other thread should be able to acquire its locks now.",
        test2.await(WAIT, TimeUnit.MILLISECONDS)
            && test3.await(WAIT, TimeUnit.MILLISECONDS));
    if(which) th3.release();
    else th2.release();
  }

  /**
   * Fuzzing test, watch for deadlocks. Uses multiple threads in parallel which all fetch
   * random locks, hold them for a while, release them and fetch the next one.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void fuzzingTest() throws InterruptedException {
    assertTrue("Increase {@code objects.length}!", objects.length > 1);

    final Thread[] threads = new Thread[FUZZING_THREADS];
    final CountDownLatch allDone = new CountDownLatch(FUZZING_THREADS * FUZZING_REPEATS);
    for(int i = 0; i < FUZZING_THREADS; i++) {
      threads[i] = new Thread() {
        private String[] randomSubset(final String[] set, final boolean nullAllowed) {
          if(nullAllowed && Math.random() * set.length == 0) return null;

          final int start = (int) (Math.random() * set.length);
          final int end = (int) (Math.random() * (set.length - start)) + start;
          return Arrays.copyOfRange(set, start, end);
        }

        @Override
        public void run() {
          for(int j = 0; j < FUZZING_REPEATS; j++) {
            final CountDownLatch latch = new CountDownLatch(1);
            final String[] read = randomSubset(objects, true);
            final String[] write = randomSubset(objects, true);
            final LockTester th = new LockTester(null, read, write, latch);
            th.start();
            try {
              Thread.sleep(HOLD_TIME);
              if(!latch.await(FUZZING_THREADS * HOLD_TIME + WAIT, TimeUnit.MILLISECONDS))
                throw new RuntimeException("Looks like thread is stuck in a deadlock.");
            } catch(final InterruptedException e) {
              throw new RuntimeException(e);
            }
            th.release();
            allDone.countDown();
          }
        }
      };
      threads[i].start();
    }
    assertTrue("Looks like thread is stuck in a deadlock.",
        allDone.await(FUZZING_THREADS * FUZZING_REPEATS * HOLD_TIME,
            TimeUnit.MILLISECONDS));
  }

  /**
   * Default implementation for setting locks and latches.
   */
  private class LockTester extends Thread {
    /** Latch to await before locking. */
    private final CountDownLatch await;
    /** Latch to count down after locking. */
    private final CountDownLatch countDown;
    /** Array of objects to put read locks onto (can be {@code null}). */
    private final LockList readObjects = new LockList();
    /** Array of objects to put write locks onto (can be {@code null}). */
    private final LockList writeObjects = new LockList();
    /** Flag indicating to release locks after being notified. */
    private volatile boolean requestRelease;

    /**
     * Setup locking thread. Call {@code start} to lock, notify the thread to unlock.
     * @param await latch to await (can be {@code null})
     * @param reads strings to put read lock on (can be {@code null})
     * @param writes strings to put write lock on (can be {@code null})
     * @param countDown latch to count down after receiving locks
     */
    LockTester(final CountDownLatch await, final String[] reads, final String[] writes,
        final CountDownLatch countDown) {

      this.await = await;
      this.countDown = countDown;
      if(reads == null) {
        readObjects.addGlobal();
      } else {
        for(final String read : reads) readObjects.add(read);
      }
      if(writes == null) {
        writeObjects.addGlobal();
      } else {
        for(final String write : writes) writeObjects.add(write);
      }
    }

    @Override
    public void run() {
      // await latch if set
      if(await != null) {
        try {
          if(!await.await(WAIT, TimeUnit.MILLISECONDS)) fail("Latch timed out.");
        } catch(final InterruptedException e) {
          throw new RuntimeException("Unexpectedly interrupted.");
        }
      }

      // fetch lock if objects are set
      try {
        locks.acquire(readObjects, writeObjects);

        // we hold the lock, count down
        if(countDown != null) countDown.countDown();

        // wait until we're asked to release the lock
        synchronized(this) {
          while(!requestRelease) wait();
        }

        locks.release();
      } catch(final InterruptedException ex) {
        throw new RuntimeException("Unexpectedly interrupted.");
      }
    }

    /**
     * Releases all locks tester owns. {@code release} gets called by other threads, so it
     * cannot release locks directly (the thread holding the lock must do this). Set flag
     * in object that lock should be released and wake up all threads.
     */
    public synchronized void release() {
      requestRelease = true;
      notifyAll();
    }
  }
}
