package org.basex.query.value.array;

import org.basex.*;
import org.basex.core.jobs.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#reverse(Job)}.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class ArrayTest extends SandboxTest {
  /** Interruptible job. */
  static Job job;

  /** Initializes the test. */
  @BeforeAll public static void init() {
    job = new Job() { };
  }

  /** Finalizes the test. */
  @AfterAll public static void close() {
    job = null;
  }
}
