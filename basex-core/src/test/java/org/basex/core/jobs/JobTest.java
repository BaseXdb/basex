package org.basex.core.jobs;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests stop-state propagation in the {@link Job} parent/child hierarchy.
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
   * Creates a minimal job instance.
   * @return job
   */
  private static Job job() {
    return new Job() { };
  }
}
