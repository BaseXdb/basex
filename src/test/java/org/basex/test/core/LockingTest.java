package org.basex.test.core;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.*;
import org.basex.core.*;
import org.basex.test.*;
import org.basex.util.list.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

/**
 * Tests for {@link org.basex.core.DBLocking}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Jens Erat
 */
@RunWith(Parameterized.class)
public final class LockingTest extends SandboxTest {
  /** How often should tests be repeated? */
  private static final int REPEAT = 1;

  /**
   * Enable repeated running of test to track down synchronization issues.
   * @return Collection of empty object arrays
   */
  @Parameters
  public static Collection<Object[]> generateParams() {
    List<Object[]> params = new ArrayList<Object[]>();
    for(int i = 1; i <= REPEAT; i++) {
      params.add(new Object[0]);
    }
    return params;
  }

  /** How many milliseconds to wait for threads to finish. */
  private static final long WAIT = 100L;
  /** Main properties, used to read parallel transactions limit. */
  private final MainProp mprop = new Context().mprop;
  /** Locking instance used for testing. */
  DBLocking locks = new DBLocking(mprop);
  /** Objects used for locking. */
  private final String[] objects = new String[1];
  /** Empty string array for convenience. */
  private static final String[] NONE = new String[0];

  /**
   * Test preparations: create objects for locking.
   */
  @Before
  public void before() {
    for(int i = 0; i < objects.length; i++) {
      objects[i] = Integer.toString(i);
    }
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
   * Fetch read lock, then write lock.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void deadLockTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test1 = new CountDownLatch(1),
        test2 = new CountDownLatch(1);

    final String[] obj1 = new String[] { "1", "2", "3" };
    final String[] obj2 = new String[] { obj1[2], obj1[0] }; // 3, 1
    final String[] obj0 = new String[] { obj1[1] };          // 2

    // Block 2
    final LockTester thread0 = new LockTester(null, NONE, obj0, sync);
    // Fetches 1, pauses on 2 (which is hold by thread0), later on fetch 3
    final LockTester thread1 = new LockTester(sync, NONE, obj1, test1);
    // Fetches 3, then 2 (will pause) - later on fetch 1 (deadlock when not rearranged)
    final LockTester thread2 = new LockTester(sync, NONE, obj2, test2);

    thread0.start();
    thread1.start();
    thread2.start();

    // Hope thread 1&2 will fetch their first locks... Can't check this.
    Thread.sleep(WAIT);

    // Release 2
    thread0.release();

    assertTrue("One of the threads should be able to fetch its second lock.",
        test1.await(WAIT, TimeUnit.MILLISECONDS)
        ^ test2.await(WAIT, TimeUnit.MILLISECONDS));

    boolean first = false;
    if(test1.await(0, TimeUnit.MILLISECONDS)) {
      thread1.release();
      first = true;
    }
    if(test2.await(0, TimeUnit.MILLISECONDS)) thread2.release();
    assertTrue("Both threads should be finished now.",
        test1.await(WAIT, TimeUnit.MILLISECONDS)
        && test2.await(WAIT, TimeUnit.MILLISECONDS));

    if(first) thread2.release();
    else thread1.release();
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
        new CountDownLatch(Math.max(mprop.num(MainProp.PARALLEL), 1));
    // Container for (maximum number allowed transactions) + 1 testers
    final LockTester[] testers = (LockTester[]) Array.newInstance(
        LockTester.class, (int) (latch.getCount() + 1));

    // Start maximum number of allowed transactions
    for(int i = 0; i < testers.length - 1; i++) {
      testers[i] = new LockTester(null, objects, NONE, latch);
      testers[i].start();
    }
    assertTrue("Couldn't start maximum allowed number of parallel transactions!",
        latch.await(WAIT, TimeUnit.MILLISECONDS));

    // Start one more transaction
    final CountDownLatch latch2 = new CountDownLatch(1);
    testers[testers.length - 1] = new LockTester(null, objects, NONE, latch2);
    testers[testers.length - 1].start();
    assertFalse("Shouldn't be able to start another parallel transaction yet!",
        latch2.await(WAIT, TimeUnit.MILLISECONDS));

    // Stop first transaction
    testers[0].release();
    assertTrue("New transaction should have started!",
        latch2.await(WAIT, TimeUnit.MILLISECONDS));

    // Stop all other transactions
    for(int i = 1; i < testers.length; i++) {
      testers[i].release();
    }
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
   * Default implementation for setting locks and latches.
   */
  private class LockTester extends Thread {
    /** Latch to await before locking. */
    private final CountDownLatch await;
    /** Latch to count down after locking. */
    private final CountDownLatch countDown;
    /** Shall we fetch write locks? */
    private final boolean writing;
    /** Array of objects to put read locks onto. */
    private final String[] readObjects;
    /** Array of objects to put write locks onto. */
    private final String[] writeObjects;
    /** Flag indicating to release locks after being notified. */
    private volatile boolean requestRelease;

    /**
     * Setup locking thread. Call {@code start} to lock, notify the thread to unlock.
     * @param a Latch to await
     * @param r Strings to put read lock on
     * @param w Strings to put write lock on
     * @param c Latch to count down after receiving locks
     */
    LockTester(final CountDownLatch a, final String[] r, final String[] w,
        final CountDownLatch c) {
      await = a;
      writing = w != null && w.length != 0;
      readObjects = r;
      writeObjects = w;
      countDown = c;
    }

    @Override
    public synchronized void run() {
      // Await latch if set
      if(null != await) {
        try {
          if(!await.await(WAIT, TimeUnit.MILLISECONDS)) fail("Latch timed out.");
        } catch(final InterruptedException e) {
          throw new RuntimeException("Unexpectedly interrupted.");
        }
      }

      // Fetch lock if objects are set
      final Command cmd = new Cmd(writing);
      locks.acquire(cmd,
            null != readObjects ? new StringList().add(readObjects) : null,
            null != writeObjects ? new StringList().add(writeObjects) : null);

      // We hold the lock, count down
      if(null != countDown) countDown.countDown();

      // Wait until we're asked to release the lock
      try {
        while(!requestRelease)
          wait();
      } catch(final InterruptedException e) {
        throw new RuntimeException("Unexpectedly interrupted.");
      }
      locks.release(cmd);
    }

    /**
     * Release all locks tester owns. [@code release} gets called by other threads, so it
     * cannot release locks directly (the thread holding the lock must do this). Set flag
     * in object that lock should be released and wake up all threads.
     */
    public synchronized void release() {
      requestRelease = true;
      notifyAll();
    }
  }

  /** Dummy command. */
  private static class Cmd extends Command {
    /**
     * Constructor.
     * @param w write flag
     */
    Cmd(final boolean w) {
      super(Perm.NONE);
      updating = w;
    }

    @Override
    protected boolean run() {
      return true;
    }
  }
}
