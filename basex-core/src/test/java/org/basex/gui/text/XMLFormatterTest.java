package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the code {@link Formatter}, driven by the XML syntax.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XMLFormatterTest {
  /** Indentation of a single level. */
  private static final byte[] SPACES = Token.token("  ");
  /** Line margin. */
  private static final int MARGIN = 80;

  /** Elements are indented by their nesting depth. */
  @Test public void indent() {
    format("<a>\n<b>x</b>\n</a>", "<a>\n  <b>x</b>\n</a>");
    // wrong indentation is corrected
    format("<a>\n      <b/>\n   <c/>\n</a>", "<a>\n  <b/>\n  <c/>\n</a>");
    // empty elements open no level
    format("<a>\n<b>\n<c/>\n</b>\n</a>", "<a>\n  <b>\n    <c/>\n  </b>\n</a>");
    // compact markup is left alone
    format("<a><b/></a>", "<a><b/></a>");
  }

  /** Only the whitespace between tags is indented. */
  @Test public void text() {
    // text is significant
    format("<a>\n  text\n</a>", "<a>\n  text\n</a>");
    format("<a>x\n  <b/>\n</a>", "<a>x\n  <b/>\n</a>");
    // brackets in text are no code
    format("<a>\n<b>{ ( }</b>\n</a>", "<a>\n  <b>{ ( }</b>\n</a>");
    // comments, sections and instructions are adopted unchanged
    format("<?xml version='1.0'?>\n<a>\n<!--  c  -->\n<![CDATA[ < ]]>\n</a>",
           "<?xml version='1.0'?>\n<a>\n  <!--  c  -->\n  <![CDATA[ < ]]>\n</a>");
  }

  /** Tags: whitespace is collapsed, attributes in new lines are indented. */
  @Test public void tags() {
    format("<a  b='1'   c='2'/>", "<a b='1' c='2'/>");
    format("<a\nb='1'\nc='2'>x</a>", "<a\n  b='1'\n  c='2'>x</a>");
    // whitespace in attribute values is significant
    format("<a b='1  2'/>", "<a b='1  2'/>");
  }

  /** Preserved whitespace is significant. */
  @Test public void preserve() {
    format("<a xml:space='preserve'>\n      <b/>\n</a>",
           "<a xml:space='preserve'>\n      <b/>\n</a>");
    // the document is still formatted: only its boundary whitespace is preserved
    format("<a xml:space='preserve'>\n<b  c='1'/>\n</a>",
           "<a xml:space='preserve'>\n<b c='1'/>\n</a>");
  }

  /**
   * Compares formatted XML with the expected result, and checks that formatting is idempotent.
   * @param xml XML string
   * @param expected expected result
   */
  private static void format(final String xml, final String expected) {
    final Syntax syntax = new SyntaxXML();
    final byte[] formatted = syntax.format(Token.token(xml), SPACES, MARGIN);
    assertEquals(expected, Token.string(formatted), xml);
    assertEquals(expected, Token.string(syntax.format(formatted, SPACES, MARGIN)),
      "reformatted: " + xml);
  }
}
