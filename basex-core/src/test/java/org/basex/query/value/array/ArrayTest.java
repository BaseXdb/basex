package org.basex.query.value.array;

import org.basex.*;
import org.basex.query.*;
import org.junit.jupiter.api.*;

/**
 * Tests for {@link XQArray#reverse(QueryContext)}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public abstract class ArrayTest extends SandboxTest {
  /** Query context. */
  static QueryContext qc;

  /** Initializes the test. */
  @BeforeAll public static void init() {
    qc = new QueryContext(context);
  }

  /** Finalizes the test. */
  @AfterAll public static void close() {
    qc = null;
  }
}
