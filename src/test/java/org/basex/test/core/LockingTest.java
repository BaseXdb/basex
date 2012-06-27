package org.basex.test.core;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.concurrent.*;
import org.basex.core.*;
import org.basex.test.*;
import org.junit.*;

/**
 * Tests for {@link org.basex.core.Locking}.
 * @author jenserat
 */
public final class LockingTest extends SandboxTest {
  /** How many milliseconds to wait for threads to finish. */
  private static final long WAIT = 100L;
  /** Main properties, used to read parallel transactions limit. */
  private final MainProp mprop = new Context().mprop;
  /** Locking instance used for testing. */
  Locking locks = new Locking(mprop);
  /** Objects used for locking. */
  private Integer[] objects = new Integer[1];

  /**
   * Test preparations: create objects for locking.
   */
  @Before
  public void before() {
    for(int i = 0; i < objects.length; i++) {
      objects[i] = new Integer(i);
    }
  }

  /**
   * Test for concurrent writes.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void writeWriteTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    LockTester<Integer> thread1 = new LockTester<Integer>(null, true, objects, sync);
    LockTester<Integer> thread2 = new LockTester<Integer>(sync, true, objects, test);

    thread1.start();
    thread2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    thread1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    thread2.release();
  }

  /**
   * Fetch read lock, then write lock.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void writeReadTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    LockTester<Integer> thread1 = new LockTester<Integer>(null, true, objects, sync);
    LockTester<Integer> thread2 = new LockTester<Integer>(sync, false, objects, test);

    thread1.start();
    thread2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    thread1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    thread2.release();
  }

  /**
   * Fetch read lock, then write lock.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void readWriteTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    LockTester<Integer> thread1 = new LockTester<Integer>(null, false, objects, sync);
    LockTester<Integer> thread2 = new LockTester<Integer>(sync, true, objects, test);

    thread1.start();
    thread2.start();
    assertFalse("Thread 2 shouldn't be able to acquire lock yet.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    thread1.release();
    assertTrue("Thread 2 should be able to acquire lock now.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    thread2.release();
  }

  /**
   * Fetch read lock, then write lock.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void deadLockTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test1 = new CountDownLatch(1),
        test2 = new CountDownLatch(1);

    Integer[] obj1 = new Integer[] { 1, 2, 3};
    Integer[] obj2 = new Integer[] { obj1[2], obj1[0] }; // 3, 1
    Integer[] obj0 = new Integer[] { obj1[1] };          // 2

    // Block 2
    LockTester<Integer> thread0 = new LockTester<Integer>(null, true, obj0, sync);
    // Fetches 1, pauses on 2 (which is hold by thread0), later on fetch 3
    LockTester<Integer> thread1 = new LockTester<Integer>(sync, true, obj1, test1);
    // Fetches 3, then 2 (will pause) - later on fetch 1 (deadlock when not rearranged)
    LockTester<Integer> thread2 = new LockTester<Integer>(sync, true, obj2, test2);

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
    if (test1.await(0, TimeUnit.MILLISECONDS)) {
      thread1.release();
      first = true;
    }
    if (test2.await(0, TimeUnit.MILLISECONDS)) thread2.release();
    assertTrue("Both threads should be finished now.",
        test1.await(WAIT, TimeUnit.MILLISECONDS)
        && test2.await(WAIT, TimeUnit.MILLISECONDS));

    if (first) thread2.release();
    else thread1.release();
  }

  /**
   * Fetch two read locks.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void readReadTest() throws InterruptedException {
    final CountDownLatch sync = new CountDownLatch(1), test = new CountDownLatch(1);
    LockTester<Integer> thread1 = new LockTester<Integer>(null, false, objects, sync);
    LockTester<Integer> thread2 = new LockTester<Integer>(sync, false, objects, test);

    thread1.start();
    thread2.start();
    assertTrue("Thread 2 should be able to acquire lock.",
        test.await(WAIT, TimeUnit.MILLISECONDS));
    thread1.release();
    thread2.release();
  }

  /**
   * Test parallel transaction limit.
   * @throws InterruptedException Got interrupted.
   */
  @Test
  public void parallelTransactionLimitTest() throws InterruptedException {
    CountDownLatch latch =
        new CountDownLatch(Math.max(mprop.num(MainProp.PARALLEL), 1));
    // Container for (maximum number allowed transactions) + 1 testers
    @SuppressWarnings("unchecked")
    // Generic array construction not allowed
    final LockTester<Integer>[] testers = (LockTester<Integer>[]) Array.newInstance(
        LockTester.class, (int) (latch.getCount() + 1));

    // Start maximum number of allowed transactions
    for(int i = 0; i < testers.length - 1; i++) {
      testers[i] = new LockTester<Integer>(null, false, objects, latch);
      testers[i].start();
    }
    assertTrue("Couldn't start maximum allowed number of parallel transactions!",
        latch.await(WAIT, TimeUnit.MILLISECONDS));

    // Start one more transaction
    CountDownLatch latch2 = new CountDownLatch(1);
    testers[testers.length - 1] = new LockTester<Integer>(null, false, objects, latch2);
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
   * Default implementation for setting locks and latches.
   * @param <T> Object Array to put locks on
   */
  private class LockTester<T extends Object & Comparable<? super T>>
      extends Thread {
    /** Latch to await before locking. */
    private final CountDownLatch await;
    /** Latch to count down after locking. */
    private final CountDownLatch countDown;
    /** Shall we fetch write locks? */
    private final boolean write;
    /** Array of objects to put locks onto. */
    private final Comparable<? extends T>[] objectsArray;
    /** Flag indicating to release locks after being notified. */
    private volatile boolean requestRelease;

    /**
     * Setup locking thread. Call {@code start} to lock, notify the thread to unlock.
     * @param a Latch to await
     * @param w Fetch write lock?
     * @param o Object array to put locks on
     * @param c Latch to count down after receiving locks
     */
    LockTester(final CountDownLatch a, final boolean w,
        final Comparable<? extends T>[] o, final CountDownLatch c) {
      await = a;
      write = w;
      objectsArray = o;
      countDown = c;
    }

    @Override
    public synchronized void run() {
      // Await latch if set
      if(null != await) {
        try {
          if(!await.await(WAIT, TimeUnit.MILLISECONDS)) fail("Latch timed out.");
        } catch(InterruptedException e) {
          throw new RuntimeException("Unexpectedly interrupted.");
        }
      }

      // Fetch lock if objects are set
      if(null != objectsArray) locks.acquire(write, objectsArray);

      // We hold the lock, count down
      if(null != countDown) countDown.countDown();

      // Wait until we're asked to release the lock
      try {
        while(!requestRelease)
          wait();
      } catch(InterruptedException e) {
        throw new RuntimeException("Unexpectedly interrupted.");
      }
      locks.release();
    }

    /**
     * Release all locks tester owns.
     */
    public synchronized void release() {
      requestRelease = true;
      notify();
    }
  }
}
