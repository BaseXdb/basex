package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.util.hash.*;
import org.junit.jupiter.api.*;
import org.opentest4j.*;

/**
 * WaekTokenSet test.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Gunther Rademacher
 */
public class WeakTokenSetTest {
  /** The prefix for assertion messages, showing random seed for reproducing potential failures. */
  private static String msgPrefix;
  /** Random value generator. */
  private static Random random;

  /** Initialize. */
  @BeforeAll
  public static void init() {
    random = new Random();
    final long seed = random.nextLong();
    random.setSeed(seed);
    msgPrefix = "Using seed " + seed + "L: ";
  }

  /** Verify WeakTokenSet behavior with respect to garbage collection. */
  @RepeatedTest(value = 8)
  public void testGarbageCollection() {
    try {
      final List<String> strings = randomStrings();
      final Map<String, byte[]> strongRefs = new HashMap<>();
      final WeakTokenSet wts = new WeakTokenSet();
      int size = 0;
      for(final String string : strings) {
        final byte[] key = Token.token(string);
        assertEquals(key, wts.put(key), msgPrefix + "key should not have been found");
        strongRefs.put(string, key);

        final byte[] eqKey = Token.token(string);
        assertNotEquals(key, eqKey, msgPrefix + "keys should be different objects");
        assertEquals(key, wts.put(eqKey), msgPrefix + "key should have been found");
        assertEquals(++size, wts.size(), msgPrefix + "unexpected size");
      }
      long capacity = 1;
      while(capacity < size + 1) capacity <<= 1;

      // remove some strong references, verify those get garbage collected in the set's references
      Collections.shuffle(strings, random);
      final int count = random.nextInt(strings.size() + 1);
      for (int i = 0; i < count; ++i) {
        assertNotNull(strongRefs.remove(strings.get(i)), msgPrefix + "unexpected removal failure");
      }
      System.gc();
      Thread.sleep(2); // allow some time for gc'ed references to appear in reference queue
      for(String string : strings) {
        final byte[] key = Token.token(string);
        if(strongRefs.containsKey(string)) {
          final byte[] stored = strongRefs.get(string);
          assertEquals(stored, wts.put(key), msgPrefix + "key should have survived gc");
        } else {
          assertEquals(key, wts.put(key), msgPrefix + "key should have been gc'ed");
          assertTrue(capacity - 1 >= wts.size(), msgPrefix + "unexpected size" + wts.size());
        }
      }

    } catch(AssertionFailedError e) {
      throw e;
    } catch(Throwable t) {
      throw new AssertionFailedError(msgPrefix + "caught " + t.getClass().getSimpleName(), t);
    }
  }

  /**
   * Create an array of 4-33 distinct random ASCII strings of length 1-16.
   * @return random string array
   */
  private List<String> randomStrings() {
    final int maxLength = 1 + random.nextInt(16);
    final int count = (int) Math.pow(2, 3 + random.nextInt(3)) - 2 + random.nextInt(4);
    final Set<String> strings = new LinkedHashSet<>();
    do {
      final int length = 1 + random.nextInt(maxLength);
      final String asciiString = random.ints(length, ' ', 127).
          collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).
          toString();
      strings.add(asciiString);
    }
    while(strings.size() < count);
    return new ArrayList<>(strings);
  }
}