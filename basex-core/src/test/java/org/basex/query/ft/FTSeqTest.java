package org.basex.query.ft;

import org.junit.jupiter.api.*;

/**
 * Re-runs the full-text queries of {@link FTTest} without a full-text index, i.e. with sequential
 * evaluation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTSeqTest extends FTTest {
  /** Re-creates the database without a full-text index. */
  @BeforeAll public static void beforeSeq() {
    createDB(false);
  }
}
