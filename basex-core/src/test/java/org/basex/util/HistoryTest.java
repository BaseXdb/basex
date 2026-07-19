package org.basex.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the record-based undo {@link History}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HistoryTest {
  /** Reconstruction of arbitrary states via undo and redo. */
  @Test public void undoRedo() {
    final History h = new History(Token.token(""));
    final byte[] a = Token.token("abc");
    final byte[] b = Token.token("aXbc");
    final byte[] c = Token.token("aXc");
    // three independent edits (insert, insert in middle, delete)
    h.store(a, 0, 3);
    h.store(b, 1, 2);
    h.store(c, 2, 2);
    assertTrue(h.last());

    // walk all the way back
    assertArrayEquals(b, h.prev());
    assertArrayEquals(a, h.prev());
    assertArrayEquals(Token.token(""), h.prev());
    assertTrue(h.first());
    assertNull(h.prev());

    // walk forward again
    assertArrayEquals(a, h.next());
    assertArrayEquals(b, h.next());
    assertArrayEquals(c, h.next());
    assertTrue(h.last());
    assertNull(h.next());
  }

  /** Caret positions travel with the states. */
  @Test public void caret() {
    final History h = new History(Token.token("x"));
    // multi-character edits: no typing merge, so each is its own step
    h.store(Token.token("xAB"), 1, 3);
    h.store(Token.token("xABCD"), 3, 5);
    assertEquals(5, h.caret());
    h.prev();
    assertEquals(3, h.caret());
    h.prev();
    assertEquals(1, h.caret());
    h.next();
    assertEquals(3, h.caret());
  }

  /** Consecutive single-character typing collapses into one undo step. */
  @Test public void mergeTyping() {
    final History h = new History(Token.token(""));
    // simulate typing "abc" one character at a time (oc + 1 == nc, growing)
    h.store(Token.token("a"), 0, 1);
    h.store(Token.token("ab"), 1, 2);
    h.store(Token.token("abc"), 2, 3);
    // a single undo removes the whole run
    assertArrayEquals(Token.token(""), h.prev());
    assertTrue(h.first());
    assertArrayEquals(Token.token("abc"), h.next());
    assertTrue(h.last());
  }

  /** A non-consecutive edit is not merged into the previous typing run. */
  @Test public void noMerge() {
    final History h = new History(Token.token(""));
    h.store(Token.token("a"), 0, 1);
    // caret jumps to the front (oc != previous caret): new step, not a merge
    h.store(Token.token("Xa"), 0, 1);
    assertArrayEquals(Token.token("a"), h.prev());
    assertArrayEquals(Token.token(""), h.prev());
    assertTrue(h.first());
  }

  /** Storing a new state truncates the redo branch. */
  @Test public void branchTruncation() {
    final History h = new History(Token.token(""));
    h.store(Token.token("a"), 0, 1);
    h.store(Token.token("Xa"), 0, 1);
    h.prev();
    assertFalse(h.last());
    // a fresh edit from the undone state drops the "Xa" redo entry
    h.store(Token.token("aY"), 1, 2);
    assertTrue(h.last());
    assertNull(h.next());
    assertArrayEquals(Token.token("a"), h.prev());
  }

  /** Identical or unchanged text is ignored. */
  @Test public void noChange() {
    final History h = new History(Token.token("abc"));
    final byte[] same = Token.token("abc");
    h.store(same, 0, 0);
    assertTrue(h.first());
    assertTrue(h.last());
    assertNull(h.prev());
  }

  /** Save position tracks the modification state. */
  @Test public void modified() {
    final History h = new History(Token.token("a"));
    assertFalse(h.modified());
    h.store(Token.token("ab"), 1, 2);
    assertTrue(h.modified());
    h.save();
    assertFalse(h.modified());
    h.prev();
    assertTrue(h.modified());
    h.next();
    assertFalse(h.modified());
  }

  /** Inactive history (no editable text) never reconstructs or reports changes. */
  @Test public void inactive() {
    final History h = new History(null);
    assertFalse(h.active());
    assertTrue(h.first());
    assertTrue(h.last());
    assertNull(h.prev());
    assertNull(h.next());
    h.store(Token.token("x"), 0, 1);
    assertFalse(h.modified());
  }

  /** Typing after a deletion is not folded into the deletion step (the length guard). */
  @Test public void deletionNoMerge() {
    final History h = new History(Token.token(""));
    h.store(Token.token("abc"), 0, 3);
    // delete the last character
    h.store(Token.token("ab"), 3, 2);
    // retype it consecutively (oc + 1 == nc): must stay a separate step, not merge
    h.store(Token.token("abc"), 2, 3);
    assertArrayEquals(Token.token("ab"), h.prev());
    assertArrayEquals(Token.token("abc"), h.prev());
    assertArrayEquals(Token.token(""), h.prev());
  }

  /** A saved step is never mutated by a subsequent merge. */
  @Test public void saveBlocksMerge() {
    final History h = new History(Token.token(""));
    h.store(Token.token("a"), 0, 1);
    h.save();
    // typing continues consecutively, but the saved step must stay a distinct entry
    h.store(Token.token("ab"), 1, 2);
    assertTrue(h.modified());
    assertArrayEquals(Token.token("a"), h.prev());
    assertFalse(h.modified());
    assertArrayEquals(Token.token(""), h.prev());
    assertTrue(h.modified());
  }

  /**
   * Typing a character identical to the one following the caret splits the run into a separate
   * undo step (ambiguous diff), but still reconstructs every state exactly.
   */
  @Test public void mergeSplitsOnAdjacentIdentical() {
    final History h = new History(Token.token("c"));
    h.store(Token.token("ac"), 0, 1);
    // type 'c' before the existing 'c': the insertion is attributed past the run
    h.store(Token.token("acc"), 1, 2);
    assertArrayEquals(Token.token("ac"), h.prev());
    assertArrayEquals(Token.token("c"), h.prev());
    assertTrue(h.first());
  }

  /** Deep, randomized edit sequences reconstruct exactly on full undo and redo. */
  @Test public void randomizedRoundTrip() {
    final Random rnd = new Random(42);
    final ArrayList<byte[]> states = new ArrayList<>();
    byte[] cur = Token.token("");
    states.add(cur);
    final History h = new History(cur);
    for(int i = 0; i < 500; i++) {
      final int len = cur.length;
      final int from = len == 0 ? 0 : rnd.nextInt(len + 1);
      final int del = len == 0 ? 0 : rnd.nextInt(len - from + 1);
      final byte[] ins = new byte[rnd.nextInt(5)];
      // small alphabet: forces prefix/suffix adjacency collisions
      for(int j = 0; j < ins.length; j++) ins[j] = (byte) ('a' + rnd.nextInt(3));
      final byte[] next = new byte[len - del + ins.length];
      System.arraycopy(cur, 0, next, 0, from);
      System.arraycopy(ins, 0, next, from, ins.length);
      System.arraycopy(cur, from + del, next, from + ins.length, len - from - del);
      if(Token.eq(next, cur)) continue;
      cur = next;
      states.add(cur);
      // nc == oc: never triggers the typing merge, so each edit is its own step
      h.store(cur, from, from);
    }
    // walk all the way back, then all the way forward
    for(int i = states.size() - 1; i > 0; i--) {
      assertArrayEquals(states.get(i - 1), h.prev(), "undo to state " + (i - 1));
    }
    assertTrue(h.first());
    for(int i = 1; i < states.size(); i++) {
      assertArrayEquals(states.get(i), h.next(), "redo to state " + i);
    }
    assertTrue(h.last());
  }

  /** Beyond the entry cap, the oldest steps drop but the retained window reconstructs exactly. */
  @Test public void entryCap() {
    final ArrayList<byte[]> states = new ArrayList<>();
    byte[] cur = Token.token("a");
    states.add(cur);
    final History h = new History(cur);
    final int count = 5000;
    for(int i = 0; i < count; i++) {
      cur = Token.token(String.valueOf((char) ('a' + i % 26)));
      states.add(cur);
      h.store(cur, 0, 0);
    }
    // undo as far as the retained history allows
    int depth = 0;
    while(!h.first()) {
      assertArrayEquals(states.get(states.size() - 1 - depth - 1), h.prev(), "undo depth " + depth);
      depth++;
    }
    // trimming happened (not everything retained) but a deep window survived
    assertTrue(depth > 0 && depth < count, "unexpected retained depth: " + depth);
  }

  /** Edits at the very end and start of large texts round-trip correctly. */
  @Test public void largeEdges() {
    final byte[] base = new byte[100_000];
    java.util.Arrays.fill(base, (byte) '.');
    final History h = new History(base);

    // append at the end
    final byte[] end = java.util.Arrays.copyOf(base, base.length + 1);
    end[base.length] = 'Z';
    h.store(end, base.length, base.length + 1);
    // insert at the start
    final byte[] start = new byte[end.length + 1];
    start[0] = 'A';
    System.arraycopy(end, 0, start, 1, end.length);
    h.store(start, 0, 1);

    assertArrayEquals(end, h.prev());
    assertArrayEquals(base, h.prev());
    assertArrayEquals(end, h.next());
    assertArrayEquals(start, h.next());
  }
}
