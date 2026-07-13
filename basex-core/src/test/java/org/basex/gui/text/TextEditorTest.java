package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the GUI editor cursor movements ({@link TextEditor}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TextEditorTest {
  /** Home key toggles between the first non-whitespace character and the line start. */
  @Test public void lineStart() {
    // cursor inside the text, indentation, and at both target positions
    assertEquals(4, lineStart("    foo", 6));
    assertEquals(4, lineStart("    foo", 7));
    assertEquals(0, lineStart("    foo", 4));
    assertEquals(4, lineStart("    foo", 0));
    assertEquals(4, lineStart("    foo", 2));
    assertEquals(4, lineStart("    foo", 1));

    // lines without indentation
    assertEquals(0, lineStart("foo", 2));
    assertEquals(0, lineStart("foo", 0));

    // second line
    assertEquals(7, lineStart("ab\n    cd", 8));
    assertEquals(3, lineStart("ab\n    cd", 7));
    assertEquals(7, lineStart("ab\n    cd", 3));
    assertEquals(7, lineStart("ab\n    cd", 5));

    // whitespace-only and empty lines
    assertEquals(2, lineStart("  \nx", 1));
    assertEquals(0, lineStart("  \nx", 2));
    assertEquals(2, lineStart("a\n\nb", 2));
  }

  /** Home key extends an existing selection. */
  @Test public void lineStartSelect() {
    final TextEditor editor = editor("    foo");
    editor.pos(6);
    editor.lineStart(true);
    assertEquals(4, editor.pos());
    assertEquals(6, editor.start);
    assertEquals(4, editor.end);
  }

  /**
   * Moves the cursor to the beginning of the line.
   * @param string text
   * @param pos initial cursor position
   * @return new cursor position
   */
  private static int lineStart(final String string, final int pos) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.lineStart(false);
    return editor.pos();
  }

  /** Jumps to a matching bracket; brackets that are no code are ignored. */
  @Test public void bracket() {
    assertEquals(5, bracket("(1, 2)", 0));
    assertEquals(0, bracket("(1, 2)", 5));
    // the closing bracket of the string is no code
    assertEquals(7, bracket("(1, \")\")", 0));
    assertEquals(0, bracket("(1, \")\")", 7));
    // the bracket in the comment is no code
    assertEquals(10, bracket("( (: ) :) )", 0));
    // brackets in element content are literal text: no bracket to jump to
    assertEquals(3, bracket("<a>(x)</a>", 3));
    assertEquals(3, bracket("<a>(x)</a>", 3, new SyntaxXML()));
    // the caret is on no bracket: jump to the enclosing one
    assertEquals(0, bracket("(1, 2)", 1));
    // the caret is on a bracket that is no code: the enclosing bracket is taken at the caret,
    // not at the end of the text (an unclosed bracket must not attract the caret)
    assertEquals(3, bracket("(: ( :)\nlocal:f(", 3));
    assertEquals(0, bracket("((: ( :)\n1)", 4));
  }

  /**
   * Returns the position of the bracket that matches the one at the specified position.
   * @param string text
   * @param pos cursor position
   * @return new cursor position
   */
  private static int bracket(final String string, final int pos) {
    return bracket(string, pos, new SyntaxXQuery());
  }

  /**
   * Returns the position of the bracket that matches the one at the specified position.
   * @param string text
   * @param pos cursor position
   * @param syntax syntax highlighter
   * @return new cursor position
   */
  private static int bracket(final String string, final int pos, final Syntax syntax) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    return editor.bracket(syntax);
  }

  /**
   * Returns an editor for the specified text.
   * @param string text
   * @return editor
   */
  private static TextEditor editor(final String string) {
    final TextEditor editor = new TextEditor(null);
    editor.text(Token.token(string));
    return editor;
  }
}
