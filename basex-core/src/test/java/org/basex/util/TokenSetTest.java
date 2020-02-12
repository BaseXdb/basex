package org.basex.util;

import static org.junit.Assert.*;

import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.junit.*;

/**
 * Token set tests.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public class TokenSetTest {
  /** Number of tests. */
  private static final int SIZE = 100000;
  /** Token list. */
  private static final TokenList LIST = new TokenList(SIZE);
  /** Token set. */
  private final TokenSet set = new TokenSet();

  /** Initializes the tests. */
  @BeforeClass public static void init() {
    for(int i = 0; i < SIZE; i++) LIST.add(new byte[] {
        (byte) (i >>> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i });
  }

  /** Initializes a single test. */
  @Before public void initTest() {
    for(final byte[] token : LIST) set.add(token);
  }

  /** Tests added tokens. */
  @Test public void add() {
    assertEquals(SIZE, set.size());
    for(final byte[] token : LIST) assertTrue("Token is missing.", set.contains(token));
  }

  /** Tests removed tokens. */
  @Test public void delete() {
    for(final byte[] token : LIST) set.remove(token);
    for(final byte[] token : LIST) assertFalse("Token exists.", set.contains(token));
  }

  /** Tests removed tokens. */
  @Test public void addDelete() {
    for(final byte[] token : LIST) set.add(token);
    for(final byte[] token : LIST) assertTrue("Token is missing.", set.contains(token));
    for(final byte[] token : LIST) set.remove(token);
    for(final byte[] token : LIST) assertFalse("Token exists.", set.contains(token));
    for(final byte[] token : LIST) set.add(token);
    for(final byte[] token : LIST) assertTrue("Token is missing.", set.contains(token));
    for(final byte[] token : LIST) set.remove(token);
    for(final byte[] token : LIST) assertFalse("Token exists.", set.contains(token));
  }
}
