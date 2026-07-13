package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the Javascript syntax highlighter ({@link SyntaxJS}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SyntaxJSTest {
  /** Default color. */
  private static final Color PLAIN = new Color(1, 0, 0);

  /** Assigns distinctive highlighting colors. */
  @BeforeAll public static void beforeAll() {
    GUIConstants.blue = new Color(2, 0, 0);
    GUIConstants.green = new Color(3, 0, 0);
    GUIConstants.purple = new Color(4, 0, 0);
    GUIConstants.cyan = new Color(5, 0, 0);
    GUIConstants.brown = new Color(6, 0, 0);
  }

  /** Regular expressions. */
  @Test public void regex() {
    // quotes in a character class do not open a string literal
    check("if (/[\\'\\\"]/.test(ch)) { return \"x\"; }",
          "KK..SSSSSSSS.............KKKKKK.SSS...");
    // the delimiter may occur in a character class
    check("/[/]/.test(x)", "SSSSS........");
    // an escaped delimiter does not end the expression
    check("/a\\/b/", "SSSSSS");
    // flags follow the expression
    check("/a/gi", "SSS..");
  }

  /** A slash that follows an operand is a division. */
  @Test public void division() {
    check("a / b", ".....");
    check("1 / 2", "N...N");
    check("f(x) / 2", ".......N");
    check("this / 2", "KKKK...N");
    // a keyword that expects an operand is followed by a regular expression
    check("return /a/", "KKKKKK.SSS");
  }

  /** Strings, comments and template literals. */
  @Test public void basics() {
    check("// c", "CCCC");
    check("/* c */1", "CCCCCCCN");
    check("'a\\'b'", "SSSSSS");
    check("`a${1}b`", "SSS.N.SS");
    check("var x = 1;", "KKK.....N.");
  }

  /**
   * Compares the colors that are assigned to a script with the expected legend.
   * @param js script
   * @param expected expected legend
   */
  private static void check(final String js, final String expected) {
    final TextEditor editor = new TextEditor(null);
    editor.text(Token.token(js));
    final TextIterator iter = new TextIterator(editor);
    final Syntax syntax = new SyntaxJS();
    syntax.init(PLAIN);

    final StringBuilder sb = new StringBuilder();
    while(iter.moreStrings(1000)) {
      final Color color = syntax.getColor(iter);
      final char ch = color.equals(GUIConstants.blue) ? 'K' :
        color.equals(GUIConstants.purple) ? 'N' : color.equals(GUIConstants.cyan) ? 'C' :
        color.equals(GUIConstants.brown) ? 'S' : '.';
      for(int p = iter.pos(); p < iter.posEnd(); p++) sb.append(ch);
    }
    assertEquals(expected, sb.toString(), js);
  }
}
