package org.basex.gui.text;

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
              nonEmpty(spans(SearchContext.pattern(pat, true, da), text)), msg);
        }
      }
    }
  }

  /** Lazy quantifiers are never anchored (they would drop matches). */
  @Test public void lazyNotAnchored() {
    assertEquals(List.of("afoo", " bfoo"), subs(".*?foo", true, false, "afoo bfoo"));
    assertFalse(SearchContext.pattern(".*?foo", true, false).pattern().startsWith("^"));
    assertTrue(SearchContext.pattern(".*foo", true, false).pattern().startsWith("^"));
  }

  /** Invalid expressions surface as a {@link PatternSyntaxException}. */
  @Test public void invalidRegex() {
    assertThrows(PatternSyntaxException.class, () -> SearchContext.pattern("(", false, false));
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
   * @param dotall dot matches newline
   * @param text input
   * @return matches
   */
  private static List<String> subs(final String pat, final boolean mcase, final boolean dotall,
      final String text) {
    final List<String> list = new ArrayList<>();
    final Matcher m = SearchContext.pattern(pat, mcase, dotall).matcher(text);
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
   * @param dotall dot matches newline
   * @return pattern
   */
  private static Pattern plain(final String pat, final boolean dotall) {
    int flags = Pattern.MULTILINE;
    if(dotall) flags |= Pattern.DOTALL;
    return Pattern.compile(pat, flags);
  }

  /**
   * Applies a normalized replacement.
   * @param search search string
   * @param replInput raw replacement input
   * @param input text
   * @return result
   */
  private static String repl(final String search, final String replInput, final String input) {
    return SearchContext.pattern(search, true, false).matcher(input).
        replaceAll(SearchBar.normalize(replInput));
  }
}
