package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for class {@link FreeSlots}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FreeSlotsTest {
  /** Adjacent slots are merged, the smallest sufficient slot is chosen, remainders are kept. */
  @Test public void slots() {
    final FreeSlots free = new FreeSlots();
    assertEquals(100, free.get(10, 100));

    free.add(10, 20);
    free.add(10, 40);
    free.add(10, 30);
    free.add(5, 60);
    assertEquals(60, free.get(5, 100));
    assertEquals(20, free.get(25, 100));
    assertEquals(45, free.get(5, 100));
    assertEquals(100, free.get(1, 100));

    free.add(5, 45);
    free.add(25, 20);
    assertEquals(20, free.get(30, 100));
  }

  /** Allocations are checked against a byte map of the file. */
  @Test public void random() {
    final Random rnd = new Random(0);
    final FreeSlots free = new FreeSlots();
    final boolean[] used = new boolean[1 << 20];
    final List<long[]> allocated = new ArrayList<>();
    long length = 0;

    for(int i = 0; i < 20000; i++) {
      if(allocated.isEmpty() || rnd.nextBoolean()) {
        final int size = 1 + rnd.nextInt(rnd.nextBoolean() ? 8 : 200);
        final long offset = free.get(size, length);
        if(offset == length) {
          // no free slot: the file must not contain a sufficient free run
          assertTrue(maxRun(used, length) < size);
          length += size;
          assertTrue(length <= used.length, "File too large");
        }
        for(long o = offset; o < offset + size; o++) {
          assertFalse(used[(int) o], "Slot is already used: " + o);
          used[(int) o] = true;
        }
        allocated.add(new long[] { offset, size });
      } else {
        final long[] slot = allocated.remove(rnd.nextInt(allocated.size()));
        free.add((int) slot[1], slot[0]);
        for(long o = slot[0]; o < slot[0] + slot[1]; o++) used[(int) o] = false;
      }
    }
  }

  /**
   * Returns the length of the longest run of free bytes.
   * @param used byte map
   * @param length file length
   * @return length
   */
  private static int maxRun(final boolean[] used, final long length) {
    int max = 0, run = 0;
    for(int o = 0; o < length; o++) {
      run = used[o] ? 0 : run + 1;
      max = Math.max(max, run);
    }
    return max;
  }
}
