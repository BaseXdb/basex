package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the XML syntax highlighter ({@link SyntaxXML}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXMLTest {
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

  /** Elements, attributes and text. */
  @Test public void elements() {
    check("<a>x</a>", "KKK.KKKK");
    check("<a b=\"1\">x</a>", "KK.NKSSSK.KKKK");
    check("<a b='1' c='2'/>", "KK.NKSSS.NKSSSKK");
    check("<a:b x:y=\"1\"/>", "KKKK.NNNKSSSKK");
    // text is no code: reserved words and quotes are plain
    check("<a>for don't</a>", "KKK.........KKKK");
  }

  /** Entity and character references. */
  @Test public void entities() {
    check("<a>&amp;</a>", "KKKNNNNNKKKK");
    check("<a b=\"&#65;\">", "KK.NKSNNNNNSK");
  }

  /** Comments, CDATA sections, processing instructions and doctype declarations. */
  @Test public void sections() {
    check("<!-- c --><a/>", "CCCCCCCCCCKKKK");
    check("<a><![CDATA[<b>]]></a>", "KKKCCCCCCCCCCCCCCCKKKK");
    check("<?xml version=\"1.0\"?><a/>", "CCCCCCCCCCCCCCCCCCCCCKKKK");
    check("<!DOCTYPE html><a/>", "CCCCCCCCCCCCCCCKKKK");
  }

  /**
   * Compares the colors that are assigned to a document with the expected legend.
   * @param xml XML string
   * @param expected expected legend
   */
  private static void check(final String xml, final String expected) {
    final TextEditor editor = new TextEditor(null);
    editor.text(Token.token(xml));
    final TextIterator iter = new TextIterator(editor);
    final Syntax syntax = new SyntaxXML();
    syntax.init(PLAIN);

    final StringBuilder sb = new StringBuilder();
    while(iter.moreStrings(1000)) {
      final Color color = syntax.getColor(iter);
      final char ch = color.equals(GUIConstants.blue) ? 'K' :
        color.equals(GUIConstants.purple) ? 'N' : color.equals(GUIConstants.cyan) ? 'C' :
        color.equals(GUIConstants.brown) ? 'S' : '.';
      for(int p = iter.pos(); p < iter.posEnd(); p++) sb.append(ch);
    }
    assertEquals(expected, sb.toString(), xml);
  }
}
