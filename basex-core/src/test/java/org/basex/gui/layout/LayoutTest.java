package org.basex.gui.layout;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Layout tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LayoutTest {
  /** Target pixel size of the anchored panel. */
  private static final int TARGET = 250;
  /** Minimum pixel size of the anchored panel and its neighbor. */
  private static final int MIN = 100;
  /** Allowed rounding tolerance. */
  private static final double DELTA = 1e-9;

  /** Tests fixed size. */
  @Test public void fixedSize() {
    for(final int size : new int[] { 600, 1000, 2000, 4000 }) {
      final double px = BaseXSplit.anchorFraction(TARGET, MIN, size) * size;
      assertEquals(TARGET, px, DELTA, "size " + size);
    }
  }

  /** Tests absorption. */
  @Test public void absorbsGrowth() {
    final double small = BaseXSplit.anchorFraction(TARGET, MIN, 1000) * 1000;
    final double large = BaseXSplit.anchorFraction(TARGET, MIN, 3000) * 3000;
    assertEquals(small, large, DELTA);
  }

  /** Test shrinking. */
  @Test public void shrinkNeighbor() {
    final int size = 300;
    final double px = BaseXSplit.anchorFraction(TARGET, MIN, size) * size;
    assertEquals(size - MIN, px, DELTA);
  }

  /** Tests minimum size. */
  @Test public void floor() {
    final double px = BaseXSplit.anchorFraction(TARGET, MIN, 150) * 150;
    assertEquals(MIN, px, DELTA);
  }

  /** Tests degeneration. */
  @Test public void degenerate() {
    assertEquals(1.0, BaseXSplit.anchorFraction(TARGET, MIN, 80), DELTA);
    assertEquals(0.0, BaseXSplit.anchorFraction(TARGET, MIN, 0), DELTA);
  }
}
