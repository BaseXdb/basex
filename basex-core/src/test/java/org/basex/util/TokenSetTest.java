package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

/**
 * Token set tests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class TokenSetTest {
  /** Token lists. */
  private static final TokenList[] LISTS = new TokenList[4];
  /** Token set. */
  private final TokenSet set = new TokenSet();

  /** Initializes the tests. */
  @BeforeAll public static void initAll() {
    int s = 10;
    LISTS[0] = new TokenList(s);
    for(int i = 0; i < s; i++) LISTS[0].add(Token.token(i));

    s = 100;
    LISTS[1] = new TokenList(s);
    for(int i = 0; i < s; i++) LISTS[1].add(Token.token(i));

    s = 1000;
    LISTS[2] = new TokenList(s);
    for(int i = 0; i < s; i++) LISTS[2].add(Token.token(i));

    s = 100000;
    LISTS[3] = new TokenList(s);
    for(int i = 0; i < s; i++) LISTS[3].add(new byte[] {
        (byte) (i >>> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i });
  }

  /**
   * Tests added tokens.
   * @param l index of list
   */
  @ParameterizedTest
  @ValueSource(ints = { 0, 1, 2, 3 })
  public void add(final int l) {
    final TokenList list = LISTS[l];
    for(final byte[] token : list) assertTrue(set.add(token), "Token was already indexed.");
    assertEquals(list.size(), set.size());
    for(final byte[] token : list) assertTrue(set.contains(token), "Token is missing.");
  }

  /**
   * Tests removed tokens.
   * @param l index of list
   */
  @ParameterizedTest
  @ValueSource(ints = { 0, 1, 2, 3 })
  public void delete(final int l) {
    final TokenList list = LISTS[l];
    for(final byte[] token : list) assertTrue(set.add(token), "Token was already indexed.");
    for(final byte[] token : list) assertTrue(set.remove(token) != 0, "Token was not removed.");
    for(final byte[] token : list) assertFalse(set.contains(token), "Token still exists.");
  }

  /**
   * Tests removed tokens.
   * @param l index of list
   */
  @ParameterizedTest
  @ValueSource(ints = { 0, 1, 2, 3 })
  public void addDelete(final int l) {
    final TokenList list = LISTS[l];
    for(final byte[] token : list) assertTrue(set.add(token), "Token was already indexed.");
    for(final byte[] token : list) assertTrue(set.contains(token), "Token is missing.");
    for(final byte[] token : list) assertTrue(set.remove(token) != 0, "Token was not removed.");
    for(final byte[] token : list) assertFalse(set.contains(token), "Token still exists.");
    for(final byte[] token : list) assertTrue(set.add(token), "Token was already indexed.");
    for(final byte[] token : list) assertTrue(set.contains(token), "Token is missing.");
    for(final byte[] token : list) assertTrue(set.remove(token) != 0, "Token was not removed.");
    for(final byte[] token : list) assertFalse(set.contains(token), "Token still exists.");
  }
}
