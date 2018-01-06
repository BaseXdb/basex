package org.basex.query.value.array;

import org.basex.*;
import org.basex.query.*;
import org.junit.*;

/**
 * Tests for {@link Array#reverse(QueryContext)}.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Leo Woerteler
 */
public class ArrayTest extends SandboxTest {
  /** Query context. */
  static QueryContext qc;

  /** Initializes the test. */
  @BeforeClass public static void init() {
    qc = new QueryContext(context);
  }

  /** Finalizes the test. */
  @AfterClass public static void close() {
    qc = null;
  }
}
