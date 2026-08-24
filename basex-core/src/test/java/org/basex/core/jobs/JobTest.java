package org.basex.core.jobs;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.junit.jupiter.api.Test;

/**
 * Tests stop-state propagation and the interruption of blocking operations in the {@link Job}
 * parent/child hierarchy.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobTest {
  /** Stopping a job propagates to a child that is present at stop time. */
  @Test public void stopActiveChild() {
    final Job parent = job(), child = job();
    parent.pushJob(child);
    parent.stop();
    assertTrue(child.stopped(), "active child should be stopped");
  }

  /** A child registered after the parent was stopped must inherit the stopped state. */
  @Test public void stopLateChild() {
    final Job parent = job();
    parent.stop();
    final Job child = job();
    parent.pushJob(child);
    assertTrue(child.stopped(), "child registered after stop should inherit the stopped state");
  }

  /** All children must stay reachable for subsequent stops. */
  @Test public void popJobByIdentity() {
    final Job parent = job();
    final Job first = job(), second = job();
    parent.pushJob(first);
    parent.pushJob(second);
    // the first child closes before the second one
    parent.popJob(first);
    parent.stop();
    assertTrue(second.stopped(), "remaining child should still be reachable for stop");
  }

  /**
   * A stoppable operation returns its result if the job is not stopped.
   * @throws Exception exception
   */
  @Test public void stoppableReturnsResult() throws Exception {
    assertEquals("ok", job().runStoppable(() -> "ok"));
  }

  /**
   * Without a registered job, a static stoppable operation runs unwrapped.
   * @throws Exception exception
   */
  @Test public void runStoppableWithoutJob() throws Exception {
    assertEquals("ok", Job.run(() -> "ok"));
  }

  /** A job that is already stopped must not run the operation. */
  @Test public void stoppableSkipsIfStopped() {
    final Job job = job();
    job.stop();
    final AtomicBoolean ran = new AtomicBoolean();
    assertThrows(JobException.class, () -> job.runStoppable(() -> {
      ran.set(true);
      return null;
    }));
    assertFalse(ran.get(), "operation must not run when the job is already stopped");
  }

  /**
   * Stopping a job interrupts a blocking operation and surfaces a {@link JobException}.
   * @throws Exception exception
   */
  @Test public void stopInterruptsBlockingOperation() throws Exception {
    final Job job = job();
    final CountDownLatch started = new CountDownLatch(1);
    final AtomicReference<Throwable> caught = new AtomicReference<>();
    final Thread worker = new Thread(() -> {
      try {
        job.runStoppable(() -> {
          started.countDown();
          // block until interrupted by the stop
          Thread.sleep(60_000);
          return null;
        });
      } catch(final Throwable th) {
        caught.set(th);
      }
    });
    worker.start();
    assertTrue(started.await(5, TimeUnit.SECONDS), "operation should have started");
    job.stop();
    worker.join(5_000);
    assertFalse(worker.isAlive(), "blocking operation should have been interrupted");
    assertInstanceOf(JobException.class, caught.get(),
        "interrupt should surface as a JobException");
  }

  /** A job that is nested in another one on the same thread runs with the locks of its parent. */
  @Test public void nestedRegistration() {
    final Context ctx = new Context();
    try {
      // registration and its counterpart are paired as they are for web requests, so that a
      // failure leaves no job behind and reports itself instead of blocking the closing context
      final Job outer = job();
      try {
        outer.register(ctx);
        final Job inner = job();
        try {
          inner.register(ctx);
        } finally {
          inner.unregister(ctx);
        }
      } finally {
        outer.unregister(ctx);
      }
      assertTrue(ctx.jobs.active.isEmpty(), "every job should have left the pool");
    } finally {
      ctx.close();
    }
  }

  /** A job that fails while acquiring its locks must not keep its run slot. */
  @Test public void registrationFails() {
    final Context ctx = new Context();
    try {
      // locks are collected by traversing the query: a stack overflow must not block the pool
      final Job job = new Job() {
        @Override public void addLocks() {
          throw new StackOverflowError();
        }
      };
      assertThrows(StackOverflowError.class, () -> job.register(ctx));
      assertTrue(ctx.jobs.active.isEmpty(), "failed job should have left the pool");
    } finally {
      // a wedged job would make the pool wait for it forever
      ctx.jobs.active.clear();
      ctx.close();
    }
  }

  /** A job that fails after acquiring its locks must give them back. */
  @Test public void registrationFailsWithLocks() {
    final Context ctx = new Context();
    try {
      // non-admin jobs schedule a timeout, which is rejected once the pool has been closed
      ctx.user(new User("tester"));
      ctx.soptions.set(StaticOptions.TIMEOUT, 5);
      ctx.jobs.close();
      assertThrows(RejectedExecutionException.class, () -> job().register(ctx));
      assertTrue(ctx.jobs.active.isEmpty(), "failed job should have left the pool");
      assertNull(ctx.locking.held(), "failed job should have released its locks");
    } finally {
      ctx.jobs.active.clear();
      ctx.close();
    }
  }

  /** Unregistering a job that was never registered must do nothing. */
  @Test public void unregisterWithoutRegister() {
    final Context ctx = new Context();
    try {
      // a web response gives up its job in a finally, even if it never got as far as running it
      job().unregister(ctx);
      assertTrue(ctx.jobs.active.isEmpty(), "no job should have been touched");
    } finally {
      ctx.close();
    }
  }

  /**
   * Creates a minimal job instance.
   * @return job
   */
  private static Job job() {
    return new Job() { };
  }
}
