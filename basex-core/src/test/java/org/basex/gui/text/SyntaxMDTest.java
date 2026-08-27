package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.util.*;

import org.basex.gui.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the Markdown syntax highlighter {@link SyntaxMD}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SyntaxMDTest {
  /** Color of text that is not highlighted. */
  private static final Color PLAIN = new Color(0);
  /** Highlighting colors, in the order of {@link #CODES}. */
  private static final ArrayList<Color> COLORS = new ArrayList<>();
  /** Characters that represent the highlighting colors. */
  private static final String CODES = ".bpco";

  /** Assigns distinguishable highlighting colors. */
  @BeforeAll public static void beforeAll() {
    GUIConstants.blue = new Color(1);
    GUIConstants.purple = new Color(2);
    GUIConstants.cyan = new Color(3);
    GUIConstants.brown = new Color(4);
    Collections.addAll(COLORS, PLAIN, GUIConstants.blue, GUIConstants.purple, GUIConstants.cyan,
      GUIConstants.brown);
  }

  /** ATX headings are highlighted up to the end of the line. */
  @Test public void atxHeading() {
    check("# Heading", "bbbbbbbbb");
    check("###### x", "bbbbbbbb");
    check("### *x*\ny", "bbbbbbb", ".");
    // seven hashes and hashes that are followed by text are no heading
    check("####### x", ".........");
    check("#hashtag", "........");
  }

  /** Underlined headings are only recognized below a paragraph. */
  @Test public void setextHeading() {
    check("Text\n===", "....", "bbb");
    check("Text\n---", "....", "bbb");
    // without a preceding paragraph, dashes are a thematic break
    check("---", "ccc");
    check("\n===", "", "...");
  }

  /** Thematic breaks consist of at least three dashes, asterisks or underscores. */
  @Test public void thematicBreak() {
    check("***", "ccc");
    check("___", "ccc");
    check("* * *", "ccccc");
    // two characters are too few
    check("**", "..");
  }

  /** Block quote and list markers are highlighted, their content is not. */
  @Test public void marker() {
    check("> quote", "c......");
    check("- item", "c.....");
    check("+ item", "c.....");
    check("* item", "c.....");
    check("12. item", "ccc.....");
    check("1) item", "cc.....");
    // markers can be nested
    check("> - item", "c.c.....");
    check("  - - item", "..c.c.....");
    // dashes and numbers inside a line are text
    check("a - b", ".....");
    check("2024 was", "........");
  }

  /** Fenced code blocks span several lines. */
  @Test public void codeBlock() {
    check("```xq\n1\n```", "ooooo", "o", "ooo");
    check("~~~\n*x*\n~~~", "ooo", "ooo", "ooo");
    // a block is not closed by the other fence character
    check("```\n~~~\nx", "ooo", "ooo", "o");
    // an unclosed block extends to the end of the text
    check("```\n# x", "ooo", "ooo");
  }

  /** Code spans are closed by a backtick string of the same length. */
  @Test public void codeSpan() {
    check("`code`", "oooooo");
    check("a `b` c", "..ooo..");
    // a single backtick does not close a doubled one
    check("``a`b``", "ooooooo");
    // code spans do not cross line breaks
    check("`a\nb", "oo", ".");
  }

  /** Strikethrough is delimited by one or two tildes. */
  @Test public void strikethrough() {
    check("~x~", "ppp");
    check("~~x~~", "ppppp");
    // three tildes at the start of a line open a code block
    check("~~~\nx", "ooo", "o");
    // strikethrough does not cross line breaks
    check("~~a\nb", "ppp", ".");
  }

  /** Bare URLs are highlighted, trailing punctuation is not. */
  @Test public void autolink() {
    check("see https://x", "....bbbbbbbbb");
    check("www.basex.org", "bbbbbbbbbbbbb");
    check("(https://x)", ".bbbbbbbbb.");
    // punctuation inside a link is part of it, punctuation at its end is not
    check("https://basex.org.", "bbbbbbbbbbbbbbbbb.");
    // words that merely start with the same letters are text
    check("a howto b", ".........");
  }

  /** Table delimiters are only highlighted below a delimiter row. */
  @Test public void table() {
    check("| a | b |\n|---|---|\n| 1 | 2 |", "c...c...c", "c...c...c", "c...c...c");
    // a table is closed by a blank line
    check("| a |\n| - |\n\n| b |", "c...c", "c...c", "", ".....");
    // without a delimiter row, a pipe is text
    check("a | b", ".....");
  }

  /** Emphasis is delimited by asterisks and underscores. */
  @Test public void emphasis() {
    check("*x*", "ppp");
    check("**x**", "ppppp");
    check("***x***", "ppppppp");
    check("_x_", "ppp");
    // delimiters that are followed or preceded by whitespace do not emphasize
    check("a * b", ".....");
    check("2 * 3 * 4", ".........");
    // underscores do not emphasize inside a word
    check("snake_case_x", "............");
    // emphasis does not cross line breaks
    check("*a\nb", "pp", ".");
  }

  /** Links, images, autolinks and HTML tags are highlighted. */
  @Test public void link() {
    check("[label](url)", "bbbbbbbppppp");
    check("![alt](url)", "bbbbbbppppp");
    check("[a] (b)", "bbb....");
    check("<https://x>", "bbbbbbbbbbb");
    check("<b>x</b>", "bbb.bbbb");
    // inline markup is highlighted inside labels
    check("[a `b`]", "bbbooob");
  }

  /** Escapes and entity references. */
  @Test public void misc() {
    check("a\\*b*", ".p...");
    check("&amp;", "ppppp");
  }

  /**
   * Compares the highlighting of a Markdown string with the expected colors.
   * @param markdown Markdown string
   * @param expected one string per line, with one character per color: {@code .} plain,
   *   {@code b} blue, {@code p} purple, {@code c} cyan, {@code o} brown
   */
  private static void check(final String markdown, final String... expected) {
    final byte[] text = Token.token(markdown);
    final Syntax syntax = new SyntaxMD();
    syntax.init(PLAIN);

    final StringBuilder sb = new StringBuilder();
    for(int p = 0; p < text.length;) {
      final int cl = Token.cl(text, p);
      final Color color = syntax.color(text, p, p + cl);
      sb.append(Token.cp(text, p) == '\n' ? '\n' : CODES.charAt(COLORS.indexOf(color)));
      p += cl;
    }
    assertEquals(String.join("\n", expected), sb.toString(), markdown);
  }
}
