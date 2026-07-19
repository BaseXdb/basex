package org.basex.gui.text;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * Tests the incremental line-offset cache: an incremental update after an edit must produce the
 * same lines as a rebuild from scratch. Lines are modelled without a renderer, using one row per
 * newline-separated line, a fixed row height, and a highlighter state that flips on each
 * {@code '#'} (so state-changing edits can be exercised).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TextLineCacheTest {
  /** Row height. */
  private static final int ROW_H = 12;
  /** Text width the cache is built for. */
  private static final int WIDTH = 200;
  /** Border offset the cache is built for. */
  private static final int OFFSET = 8;

  /** Insertion within a line. */
  @Test public void insertMidLine() {
    check("line one\nline two\nline three", "line one\nline XXtwo\nline three");
  }

  /** Insertion at the very beginning. */
  @Test public void insertAtStart() {
    check("abc\ndef\nghi", "Xabc\ndef\nghi");
  }

  /** Insertion at the very end. */
  @Test public void insertAtEnd() {
    check("abc\ndef\nghi", "abc\ndef\nghiX");
  }

  /** Appending a trailing newline (adds an empty last line). */
  @Test public void appendNewline() {
    check("abc\ndef", "abc\ndef\n");
  }

  /** Inserting a newline splits one line into two. */
  @Test public void insertNewlineSplitsLine() {
    check("abc\ndefghi\njkl", "abc\ndef\nghi\njkl");
  }

  /** Deletion within a line. */
  @Test public void deleteWithinLine() {
    check("abcdef\nghi", "abf\nghi");
  }

  /** Deleting a newline merges two lines into one. */
  @Test public void deleteNewlineMergesLines() {
    check("abc\ndef\nghi", "abcdef\nghi");
  }

  /** Deleting the whole text. */
  @Test public void deleteAll() {
    check("abc\ndef", "");
  }

  /** Inserting into an empty text. */
  @Test public void insertIntoEmpty() {
    check("", "hello\nworld");
  }

  /** A change spanning several lines (first and last differ, no reuse until the end). */
  @Test public void multiRegion() {
    check("foo\nfoo\nfoo", "bar\nfoo\nbar");
  }

  /** A state change that propagates to the end of the document (no convergence). */
  @Test public void stateChangePropagates() {
    check("a\nb\nc\nd", "#a\nb\nc\nd");
  }

  /** An edit on the last line. */
  @Test public void editOnLastLine() {
    check("a\nb\nc", "a\nb\ncc");
  }

  /** An edit on the first of many lines, reusing the long tail. */
  @Test public void editOnFirstOfMany() {
    check("aaaa\nb\nc\nd\ne", "aXaaa\nb\nc\nd\ne");
  }

  /** No incremental update without a prior build. */
  @Test public void noPriorBuild() {
    assertEquals(-1, new TextLineCache().beginUpdate(token("abc"), WIDTH, OFFSET));
  }

  /** No incremental update for unchanged text. */
  @Test public void identicalText() {
    final TextLineCache cache = new TextLineCache();
    buildFull(cache, token("abc\ndef"));
    assertEquals(-1, cache.beginUpdate(token("abc\ndef"), WIDTH, OFFSET));
  }

  /** A width change forces a full rebuild. */
  @Test public void widthChangeForcesRebuild() {
    final TextLineCache cache = new TextLineCache();
    buildFull(cache, token("abc\ndef"));
    assertEquals(-1, cache.beginUpdate(token("abc\nXef"), WIDTH + 1, OFFSET));
  }

  /** An offset change forces a full rebuild. */
  @Test public void offsetChangeForcesRebuild() {
    final TextLineCache cache = new TextLineCache();
    buildFull(cache, token("abc\ndef"));
    assertEquals(-1, cache.beginUpdate(token("abc\nXef"), WIDTH, OFFSET + 1));
  }

  /**
   * Builds a cache for the old text, incrementally updates it to the new text, and asserts the
   * result equals a rebuild from scratch.
   * @param oldText old text
   * @param newText new text
   */
  private static void check(final String oldText, final String newText) {
    final TextLineCache cache = new TextLineCache();
    buildFull(cache, token(oldText));
    incremental(cache, token(newText));
    assertLayout(cache, token(newText));
  }

  /**
   * Builds the cache from scratch, as the renderer does on a full layout.
   * @param cache cache
   * @param text text
   */
  private static void buildFull(final TextLineCache cache, final byte[] text) {
    cache.reset();
    final int[] pos = starts(text);
    for(int i = 0; i < pos.length; i++) cache.add(i * ROW_H, pos[i], state(text, pos[i]));
    cache.finish(text, WIDTH, OFFSET, endY(pos.length));
  }

  /**
   * Applies an incremental update, driving {@link TextLineCache} as the renderer does: resume at
   * the returned line, then feed the new lines, splicing in the reusable tail on convergence.
   * @param cache cache (built for the previous text)
   * @param text new text
   */
  private static void incremental(final TextLineCache cache, final byte[] text) {
    final int r0 = cache.beginUpdate(text, WIDTH, OFFSET);
    assertTrue(r0 >= 0, "expected an incremental update");
    final int[] pos = starts(text);
    // resume at the first changed line, reusing its cached start (position, y and state)
    cache.add(cache.startY(), cache.startPos(), cache.startState());
    int end = -1;
    for(int i = r0 + 1; i < pos.length; i++) {
      final int[] st = state(text, pos[i]);
      if(cache.splice(pos[i], i * ROW_H, st)) { end = cache.endY(); break; }
      cache.add(i * ROW_H, pos[i], st);
    }
    cache.finish(text, WIDTH, OFFSET, end < 0 ? endY(pos.length) : end);
  }

  /**
   * Asserts that the cache holds exactly the lines of a full layout of the text.
   * @param cache cache
   * @param text text
   */
  private static void assertLayout(final TextLineCache cache, final byte[] text) {
    final int[] pos = starts(text);
    assertEquals(pos.length, cache.size(), "line count");
    for(int i = 0; i < pos.length; i++) {
      assertEquals(pos[i], cache.pos(i), "position of line " + i);
      assertEquals(i * ROW_H, cache.y(i), "y of line " + i);
      assertArrayEquals(state(text, pos[i]), cache.state(i), "state of line " + i);
    }
    assertEquals(endY(pos.length), cache.endY(), "total height");
  }

  /**
   * Returns the start position of every line (one per newline, plus a leading and trailing one).
   * @param text text
   * @return line-start positions
   */
  private static int[] starts(final byte[] text) {
    final IntList pos = new IntList().add(0);
    for(int i = 0; i < text.length; i++) {
      if(text[i] == '\n') pos.add(i + 1);
    }
    return pos.finish();
  }

  /**
   * Returns the modelled highlighter state at a position: the parity of the preceding {@code '#'}.
   * @param text text
   * @param end position
   * @return state
   */
  private static int[] state(final byte[] text, final int end) {
    int c = 0;
    for(int i = 0; i < end; i++) {
      if(text[i] == '#') c++;
    }
    return new int[] { c & 1 };
  }

  /**
   * Returns the total height (y of the last line) for the given line count.
   * @param lines number of lines
   * @return height
   */
  private static int endY(final int lines) {
    return (lines - 1) * ROW_H;
  }
}
