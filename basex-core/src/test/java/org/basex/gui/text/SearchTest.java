package org.basex.gui.text;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.regex.*;

import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the GUI editor regular-expression search and replace engine
 * ({@link SearchContext#pattern} and {@link SearchBar#normalize}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SearchTest {
  /** Case-insensitive search must not corrupt case-sensitive metacharacters. */
  @Test public void caseInsensitiveMetachars() {
    assertEquals(List.of("a", "b"), subs("\\D", false, false, "a1b2"));
    assertEquals(List.of("1", "2"), subs("\\d", false, false, "a1b2"));
    assertEquals(List.of("a", "b"), subs("\\D", true, false, "a1b2"));
    assertEquals(List.of("a", "B", "c", "D", "e"), subs("[A-F]", false, false, "aBcDeZ"));
    assertEquals(List.of("a.b"), subs("\\Qa.b\\E", false, false, "xa.by a1b"));
  }

  /** Case-insensitive search folds non-ASCII characters. */
  @Test public void caseInsensitiveUnicode() {
    assertEquals(List.of("É"), subs("é", false, false, "É"));
    assertEquals(List.of("Ä", "ä"), subs("ä", false, false, "Ää"));
    assertEquals(List.of("Σ"), subs("σ", false, false, "Σ"));
    assertEquals(List.of(), subs("é", true, false, "É"));
  }

  /** Default mode: ^/$ anchor per line, . stops at newline, literal \n matches. */
  @Test public void multiLineDefault() {
    assertEquals(List.of("b"), subs("^b", true, false, "abc\nbcd\nxb"));
    assertEquals(List.of("c", "c"), subs("c$", true, false, "abc\nxc\ny"));
    assertEquals(List.of("a", "b"), subs(".", true, false, "a\nb"));
    assertEquals(List.of("a\nb"), subs("a\\nb", true, false, "a\nb"));
  }

  /** Dot-all mode: . matches newlines. */
  @Test public void dotAll() {
    assertEquals(List.of("a", "\n", "b"), subs(".", true, true, "a\nb"));
    assertEquals(List.of(), subs("a.b", true, false, "a\nb"));
    assertEquals(List.of("a\nb"), subs("a.b", true, true, "a\nb"));
  }

  /** Greedy leading-wildcard anchoring preserves every non-empty match. */
  @Test public void greedyAnchoringPreservesNonEmpty() {
    final String[] pats = { ".*foo|bar", ".+a|b", ".*x|.*y", "(.*)|foo", ".*|foo", ".*", ".+",
        "(.+)bar|baz", ".*(a|b)c", "(.*a)?b", "(.*a|b)", "(.*)b\\1", "(.*a)*b", "(.+a)?b" };
    final String[] texts = { "bar afoo\nzbar", "xyz", "abc\ndef", "", "foo\nbarbaz", "zazbzc",
        "xb", "xbx yby" };
    for(final String pat : pats) {
      for(final boolean da : new boolean[] { false, true }) {
        for(final String text : texts) {
          final String msg = "pat=" + pat + " dotall=" + da + " text=" + text.replace("\n", "\\n");
          assertEquals(nonEmpty(spans(plain(pat, da), text)),
              nonEmpty(spans(SearchContext.pattern(pat, true, false, true, da), text)), msg);
        }
      }
    }
  }

  /** Lazy quantifiers are never anchored (they would drop matches). */
  @Test public void lazyNotAnchored() {
    assertEquals(List.of("afoo", " bfoo"), subs(".*?foo", true, false, "afoo bfoo"));
    assertFalse(SearchContext.pattern(".*?foo", true, false, true, false).pattern().
        startsWith("^"));
    assertTrue(SearchContext.pattern(".*foo", true, false, true, false).pattern().
        startsWith("^"));
  }

  /** Invalid expressions surface as a {@link PatternSyntaxException}. */
  @Test public void invalidRegex() {
    assertThrows(PatternSyntaxException.class,
        () -> SearchContext.pattern("(", false, false, true, false));
    // a literal search is quoted, so it can never be invalid
    assertDoesNotThrow(() -> SearchContext.pattern("(", false, false, false, false));
  }

  /** A literal search never interprets metacharacters. */
  @Test public void literalSearch() {
    final byte[] text = token("a.c abc");
    assertEquals(List.of("1:2"), hits(literal(".", true), text));
    assertEquals(List.of("0:3"), hits(literal("a.c", true), text));
    assertEquals(List.of("0:3", "4:7"), hits(regex("a.c"), text));
    // a literal replacement is quoted as well: $ and \ stay literal
    assertEquals("a$1c abc", replRange(literal(".", true), Matcher.quoteReplacement("$1"),
        "a.c abc", 0, 7, false));
    // case-insensitive matching folds non-ASCII characters
    assertEquals(List.of("0:2"), hits(literal("é", false), token("É")));
    // hit offsets stay byte offsets, also for supplementary characters
    assertEquals(List.of("1:5"), hits(literal("𝄞", true), token("a𝄞b")));
  }

  /** Whole-word boundaries follow Character.isLetterOrDigit: '_' is no word character. */
  @Test public void wholeWord() {
    assertEquals(List.of("7:10"), hits(word("bar"), token("foobar bar")));
    assertEquals(List.of("0:3"), hits(word("bar"), token("bar_")));
    assertEquals(List.of(), hits(word("bar"), token("bar2")));
    assertEquals(List.of("0:3"), hits(word("bar"), token("bar")));
  }

  /** Whole-word boundaries treat a supplementary character as a single code point. */
  @Test public void wholeWordSupplementary() {
    // U+10400: letter, U+1D7CE: digit, U+1D11E: symbol (no word character)
    for(final int cp : new int[] { 0x10400, 0x1D7CE }) {
      assertEquals(List.of(), hits(word("bar"), token(cps(cp) + "bar")), () -> hex(cp));
      assertEquals(List.of(), hits(word("bar"), token("bar" + cps(cp))), () -> hex(cp));
    }
    assertEquals(List.of("4:7"), hits(word("bar"), token(cps(0x1D11E) + "bar")));
    assertEquals(List.of("0:3"), hits(word("bar"), token("bar" + cps(0x1D11E))));
  }

  /** Whole word also applies to regular expressions, and binds to the whole expression. */
  @Test public void wholeWordRegex() {
    assertEquals(List.of("7:10"), hits(wordRegex("ba."), token("foobar bar")));
    // the boundaries must not bind to the branches of a top-level alternation
    assertEquals(List.of("7:10"), hits(wordRegex("bar|foo"), token("foobar foo")));
    assertEquals(List.of("0:3", "4:7", "8:11"), hits(wordRegex("bar|foo"), token("foo bar foo")));
    // anchors and back-references keep working inside the group
    assertEquals(List.of("4:7"), hits(wordRegex("ba.$"), token("foo bar")));
    assertEquals(List.of("5:8"), hits(wordRegex("(b)a\\1"), token("xbab bab")));
  }

  /** F3 navigation must step through zero-width hits (e.g. empty-line matches for ".*"). */
  @Test public void zeroWidthNavigation() {
    // three line hits; the middle one (4,4) is zero-width, as an empty line yields for '^.*'
    final IntList st = new IntList(), en = new IntList();
    st.add(0); st.add(4); st.add(5);
    en.add(3); en.add(4); en.add(8);
    final TextEditor ed = new TextEditor(null);
    ed.searchResults = new IntList[] { st, en };
    ed.pos(0);
    ed.jump(SearchBar.SearchDir.CURRENT, false);
    final IntList visited = new IntList();
    for(int i = 0; i < 4; i++) {
      ed.jump(SearchBar.SearchDir.FORWARD, true);
      visited.add(ed.searchIndex());
    }
    // first F3 selects the current hit (0), then advances past the zero-width hit, then wraps
    assertArrayEquals(new int[] { 0, 1, 2, 0 }, visited.finish());
  }

  /** After a replacement, the caret lands behind the new text: the hit ahead must be selected. */
  @Test public void replaceNextSelectsHitAhead() {
    // "OOOOOOOOO", searched for "O": one hit per character
    final IntList st = new IntList(), en = new IntList();
    for(int i = 0; i < 9; i++) {
      st.add(i);
      en.add(i + 1);
    }
    final TextEditor ed = new TextEditor(null);
    ed.searchResults = new IntList[] { st, en };
    // caret behind the first, just replaced hit
    ed.pos(1);
    ed.jump(SearchBar.SearchDir.CURRENT, true);
    assertEquals(1, ed.searchIndex());
  }

  /** Replace Next targets the hit at the caret, not the hit that was jumped to last. */
  @Test public void caretHit() {
    // two hits, spanning the offsets 2-5 and 10-13
    final IntList st = new IntList(), en = new IntList();
    st.add(2); st.add(10);
    en.add(5); en.add(13);
    final TextEditor ed = new TextEditor(null);
    ed.searchResults = new IntList[] { st, en };

    // caret before, at the start of, and inside the first hit
    for(final int p : new int[] { 0, 2, 3 }) {
      ed.pos(p);
      assertEquals(0, ed.caretHit());
    }
    // caret at the end of the first hit: the second one is current
    ed.pos(5);
    assertEquals(1, ed.caretHit());
    // caret behind the last hit: wrap to the first one
    ed.pos(13);
    assertEquals(0, ed.caretHit());

    // no hits
    ed.searchResults = new IntList[] { new IntList(), new IntList() };
    assertEquals(-1, ed.caretHit());
  }

  /** Replace All is restricted to a selection, unless the selection is one of the hits. */
  @Test public void replaceInSelection() {
    // no selection: the whole text is replaced
    assertEquals("XX\nXX\nXX", replaceAll(null));
    // a selected hit is no range: Replace All must not be reduced to a single hit
    assertEquals("XX\nXX\nXX", replaceAll(new int[] { 0, 1 }));
    assertEquals("XX\nXX\nXX", replaceAll(new int[] { 4, 5 }));
    // any other selection restricts the replacement, also within a single line
    assertEquals("XX\naa\naa", replaceAll(new int[] { 0, 2 }));
    assertEquals("aX\naa\naa", replaceAll(new int[] { 1, 3 }));
    assertEquals("XX\nXX\naa", replaceAll(new int[] { 0, 5 }));
    // a reversed selection is adopted as well
    assertEquals("aa\nXX\nXX", replaceAll(new int[] { 8, 3 }));
  }

  /** A scoped replacement returns and re-selects the new range of the selection. */
  @Test public void replaceInSelectionRange() {
    final TextEditor ed = editor(new int[] { 0, 5 });
    final ReplaceContext rc = new ReplaceContext("XY");
    // "aa\naa" grows from 5 to 9 bytes
    assertArrayEquals(new int[] { 0, 9 }, ed.replace(rc));
    assertEquals("XYXY\nXYXY\naa", string(rc.text));
    // without a selection, no range is returned
    assertNull(editor(null).replace(new ReplaceContext("XY")));
  }

  /** Only the replaced hits are encoded anew; all other bytes are copied as they are. */
  @Test public void replacePreservesBytes() {
    // 0xE9 is no valid UTF-8 sequence: decoding and encoding it would yield a U+FFFD (0xEFBFBD)
    final byte[] txt = { 'f', 'o', 'o', (byte) 0xE9 };
    final ReplaceContext rc = new ReplaceContext("bar");
    rc.replace(literal("foo", true), txt, string(txt), 0, txt.length);
    assertArrayEquals(new byte[] { 'b', 'a', 'r', (byte) 0xE9 }, rc.text);
  }

  /** A replacement counts the hits it has replaced. */
  @Test public void replacementCount() {
    assertEquals(6, count(literal("a", true), "X", "aa\naa\naa", 0, 8, false));
    assertEquals(2, count(literal("a", true), "X", "aa\naa\naa", 3, 5, false));
    assertEquals(1, count(literal("a", true), "X", "aa\naa\naa", 3, 5, true));
    assertEquals(3, count(regex("^aa$"), "X", "aa\naa\naa", 0, 8, false));
    assertEquals(1, count(regex("^aa$"), "X", "aa\naa\naa", 3, 5, false));
    assertEquals(0, count(literal("z", true), "X", "aa", 0, 2, false));
  }

  /** Replacements are restricted to the range, but anchors and boundaries see the whole text. */
  @Test public void replaceRangeContext() {
    // ^ must not match at the start of the range, which is no line start
    assertEquals("foo\nXar", replRange(regex("^."), "X", "foo\nbar", 1, 7, false));
    // $ must not match at the end of the range
    assertEquals("foX\nbar", replRange(regex(".$"), "X", "foo\nbar", 0, 6, false));
    // \b must not match at the start of the range, which is inside a word
    assertEquals("foobar X", replRange(regex("\\bba\\w"), "X", "foobar bar", 3, 10, false));
  }

  /** A single replacement may rely on lookaround into the text surrounding the hit. */
  @Test public void replaceHitContext() {
    assertEquals("Xb", replRange(regex("a(?=b)"), "X", "ab", 0, 1, true));
    assertEquals("aX", replRange(regex("(?<=a)b"), "X", "ab", 1, 2, true));
  }

  /** Literal replacements splice every hit of the range, and only those. */
  @Test public void replaceLiteral() {
    assertEquals("XXX", replRange(literal("o", true), "X", "ooo", 0, 3, false));
    assertEquals("Xoo", replRange(literal("o", true), "X", "ooo", 0, 3, true));
    assertEquals("oXo", replRange(literal("o", true), "X", "ooo", 1, 2, false));
    assertEquals("aXc", replRange(literal("B", false), "X", "abc", 0, 3, false));
    assertEquals("abc", replRange(literal("B", true), "X", "abc", 0, 3, false));
  }

  /** Whole-word replacements do not treat the start of the range as a word boundary. */
  @Test public void replaceWordBoundary() {
    assertEquals("foobar\nX", replRange(word("bar"), "X", "foobar\nbar", 3, 10, false));
  }

  /** Replacement normalization yields a valid Java replacement with the documented grammar. */
  @Test public void replacementNormalization() {
    assertEquals("host.user", repl("(\\w+)@(\\w+)", "$2.$1", "user@host"));
    assertEquals("user-host", repl("(\\w+)@(\\w+)", "\\1-\\2", "user@host"));
    assertEquals("user", repl("(\\w+)@(\\w+)", "${1}", "user@host"));
    assertEquals("$5.00", repl("X", "\\$5.00", "X"));
    assertEquals("a\nb", repl("-", "a\\nb", "-"));
    assertEquals("\t", repl("X", "\\t", "X"));
    assertEquals("\\", repl("X", "\\\\", "X"));
    assertEquals("abc\\", repl("X", "abc\\", "X"));
    assertEquals("$", repl("X", "$", "X"));
    assertEquals("$x", repl("X", "$x", "X"));
    assertEquals("\\d", repl("X", "\\d", "X"));
  }

  // HELPERS ======================================================================================

  /**
   * Returns the matched substrings.
   * @param pat search string
   * @param mcase match case
   * @param dotall dot matches all
   * @param text input
   * @return matches
   */
  private static List<String> subs(final String pat, final boolean mcase, final boolean dotall,
      final String text) {
    final List<String> list = new ArrayList<>();
    final Matcher m = SearchContext.pattern(pat, mcase, false, true, dotall).matcher(text);
    while(m.find()) list.add(text.substring(m.start(), m.end()));
    return list;
  }

  /**
   * Returns the match spans as {@code start:end} strings.
   * @param p pattern
   * @param text input
   * @return spans
   */
  private static List<String> spans(final Pattern p, final String text) {
    final List<String> list = new ArrayList<>();
    final Matcher m = p.matcher(text);
    while(m.find()) list.add(m.start() + ":" + m.end());
    return list;
  }

  /**
   * Filters out zero-width spans.
   * @param spans spans
   * @return non-empty spans
   */
  private static List<String> nonEmpty(final List<String> spans) {
    final List<String> list = new ArrayList<>();
    for(final String s : spans) {
      final int c = s.indexOf(':');
      if(!s.substring(0, c).equals(s.substring(c + 1))) list.add(s);
    }
    return list;
  }

  /**
   * Compiles an unanchored reference pattern with the same flags.
   * @param pat search string
   * @param dotall dot matches all
   * @return pattern
   */
  private static Pattern plain(final String pat, final boolean dotall) {
    int flags = Pattern.MULTILINE;
    if(dotall) flags |= Pattern.DOTALL;
    return Pattern.compile(pat, flags);
  }

  /**
   * Returns a code point as string.
   * @param cp code point
   * @return string
   */
  private static String cps(final int cp) {
    return new String(Character.toChars(cp));
  }

  /**
   * Returns a code point in hexadecimal notation.
   * @param cp code point
   * @return string
   */
  private static String hex(final int cp) {
    return "U+" + Integer.toHexString(cp);
  }

  /**
   * Returns a case-sensitive regular-expression search context.
   * @param search search string
   * @return context
   */
  private static SearchContext regex(final String search) {
    return new SearchContext(null, search, true, false, true, false);
  }

  /**
   * Returns a literal search context.
   * @param search search string
   * @param mcase match case
   * @return context
   */
  private static SearchContext literal(final String search, final boolean mcase) {
    return new SearchContext(null, search, mcase, false, false, false);
  }

  /**
   * Returns a case-sensitive whole-word search context.
   * @param search search string
   * @return context
   */
  private static SearchContext word(final String search) {
    return new SearchContext(null, search, true, true, false, false);
  }

  /**
   * Returns a case-sensitive whole-word regular-expression search context.
   * @param search search string
   * @return context
   */
  private static SearchContext wordRegex(final String search) {
    return new SearchContext(null, search, true, true, true, false);
  }

  /**
   * Returns the hits of a search as {@code start:end} strings.
   * @param sc search context
   * @param text text to be searched
   * @return hits
   */
  private static List<String> hits(final SearchContext sc, final byte[] text) {
    final IntList[] results = sc.search(text, string(text));
    final List<String> list = new ArrayList<>();
    final int rs = results[0].size();
    for(int r = 0; r < rs; r++) list.add(results[0].get(r) + ":" + results[1].get(r));
    return list;
  }

  /**
   * Replaces a byte range of the input and returns the number of replacements.
   * @param sc search context
   * @param rplc replacement
   * @param input text
   * @param start start offset of the range
   * @param end end offset of the range
   * @param single replace only the first hit in the range
   * @return number of replaced hits
   */
  private static int count(final SearchContext sc, final String rplc, final String input,
      final int start, final int end, final boolean single) {
    final ReplaceContext rc = new ReplaceContext(rplc, single);
    rc.replace(sc, token(input), input, start, end);
    return rc.count;
  }

  /**
   * Returns an editor that holds the text "aa\naa\naa" and searches for "a".
   * @param select selection offsets (can be {@code null})
   * @return editor
   */
  private static TextEditor editor(final int[] select) {
    final TextEditor ed = new TextEditor(null);
    ed.text(token("aa\naa\naa"));
    ed.searchContext = literal("a", true);
    // the hits of "a": every character but the two newlines
    final IntList st = new IntList(), en = new IntList();
    for(final int p : new int[] { 0, 1, 3, 4, 6, 7 }) {
      st.add(p);
      en.add(p + 1);
    }
    ed.searchResults = new IntList[] { st, en };
    if(select != null) ed.select(select[0], select[1]);
    return ed;
  }

  /**
   * Replaces all hits of "a" with "X" in the text "aa\naa\naa".
   * @param select selection offsets (can be {@code null})
   * @return resulting text
   */
  private static String replaceAll(final int[] select) {
    final ReplaceContext rc = new ReplaceContext("X");
    editor(select).replace(rc);
    return string(rc.text);
  }

  /**
   * Replaces a byte range of the input.
   * @param sc search context
   * @param rplc replacement
   * @param input text
   * @param start start offset of the range
   * @param end end offset of the range
   * @param single replace only the first hit in the range
   * @return result
   */
  private static String replRange(final SearchContext sc, final String rplc, final String input,
      final int start, final int end, final boolean single) {
    final ReplaceContext rc = new ReplaceContext(rplc, single);
    rc.replace(sc, token(input), input, start, end);
    return string(rc.text);
  }

  /**
   * Applies a normalized replacement.
   * @param search search string
   * @param replInput raw replacement input
   * @param input text
   * @return result
   */
  private static String repl(final String search, final String replInput, final String input) {
    return SearchContext.pattern(search, true, false, true, false).matcher(input).
        replaceAll(SearchBar.normalize(replInput));
  }
}
