package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the JSON syntax highlighter ({@link SyntaxJSON}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SyntaxJSONTest {
  /** Default color. */
  private static final Color PLAIN = new Color(1, 0, 0);

  /** Assigns distinctive highlighting colors. */
  @BeforeAll public static void beforeAll() {
    GUIConstants.blue = new Color(2, 0, 0);
    GUIConstants.purple = new Color(4, 0, 0);
    GUIConstants.cyan = new Color(5, 0, 0);
    GUIConstants.brown = new Color(6, 0, 0);
    GUIConstants.red = new Color(7, 0, 0);
  }

  /** Numbers are tokenized into several parts and must be highlighted as a whole. */
  @Test public void numbers() {
    check("{\"a\":-1.5e-3}", "CSSSCNNNNNNNC");
    check("[0,1]", "CNCNC");
  }

  /** Strings, keywords and invalid characters. */
  @Test public void values() {
    check("[true,null]", "CKKKKCKKKKC");
    // escaped quotes do not end a string
    check("[\"a\\\"b\"]", "CSSSSSSC");
    // invalid characters are flagged
    check("[x]", "CRC");
  }

  /**
   * Compares the colors that are assigned to a document with the expected legend.
   * @param json JSON string
   * @param expected expected legend
   */
  private static void check(final String json, final String expected) {
    final TextEditor editor = new TextEditor(null);
    editor.text(Token.token(json));
    final TextIterator iter = new TextIterator(editor);
    final Syntax syntax = new SyntaxJSON();
    syntax.init(PLAIN);

    final StringBuilder sb = new StringBuilder();
    while(iter.moreStrings(1000)) {
      final Color color = syntax.getColor(iter);
      final char ch = color.equals(GUIConstants.blue) ? 'K' :
        color.equals(GUIConstants.purple) ? 'N' : color.equals(GUIConstants.cyan) ? 'C' :
        color.equals(GUIConstants.brown) ? 'S' : color.equals(GUIConstants.red) ? 'R' : '.';
      for(int p = iter.pos(); p < iter.posEnd(); p++) sb.append(ch);
    }
    assertEquals(expected, sb.toString(), json);
  }
}
