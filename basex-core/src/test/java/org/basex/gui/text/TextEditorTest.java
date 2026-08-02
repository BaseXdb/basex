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
    assertEquals(6, editor.start());
    assertEquals(4, editor.end());
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

  /** Vertical cursor movement restores the column; tabs are expanded to the indentation. */
  @Test public void lines() {
    // the column is restored in the target line
    assertEquals(5, lines("abc\nxyz", 1, 1));
    assertEquals(1, lines("abc\nxyz", 5, -1));
    // tabs count as indentation: the caret keeps its rendered column
    assertEquals(6, lines("\tab\n\tcd", 2, 1));
    // shorter target line: the caret stops at its end
    assertEquals(6, lines("abcd\nx", 3, 1));
    // first line: the caret moves to the line start; last line: it stays at the text end
    assertEquals(0, lines("abc\nxyz", 1, -1));
    assertEquals(7, lines("abc\nxyz", 5, 1));
  }

  /**
   * Moves the cursor up or down. This is the fallback for text that has not been rendered yet;
   * the editor moves by rendered rows.
   * @param string text
   * @param pos initial cursor position
   * @param l number of lines to move cursor (negative: upwards)
   * @return new cursor position
   */
  private static int lines(final String string, final int pos, final int l) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.lines(l, false);
    return editor.pos();
  }

  /** Horizontal movement collapses an existing selection to its edges. */
  @Test public void charSelection() {
    // shift+right extends the selection
    final TextEditor extend = editor("abcd");
    extend.pos(1);
    extend.nextChar(true);
    assertEquals(1, extend.start());
    assertEquals(2, extend.end());

    // right and left collapse the selection to its end and to its start
    final TextEditor next = editor("abcd");
    next.select(1, 3);
    next.nextChar(false);
    assertEquals(3, next.pos());

    final TextEditor prev = editor("abcd");
    prev.select(1, 3);
    prev.prevChar(false);
    assertEquals(1, prev.pos());
  }

  /** Words and lines are deleted, and line breaks at the caret. */
  @Test public void deleteWordLine() {
    // delete to the end of the line; at the line end, the line break is deleted
    assertEquals("a\ncd", deleteNext("ab\ncd", 1, false));
    assertEquals("abcd", deleteNext("ab\ncd", 2, false));
    // delete to the beginning of the line; at the line start, the line break is deleted
    assertEquals("ab\nd", deletePrev("ab\ncd", 4, false));
    assertEquals("abcd", deletePrev("ab\ncd", 3, false));
    // delete the next and the previous word
    assertEquals("bar", deleteNext("foo bar", 0, true));
    assertEquals("foo ", deletePrev("foo bar", 7, true));
    // beginning and end of the text: nothing is deleted
    assertEquals("ab", deleteNext("ab", 2, false));
    assertEquals("ab", deletePrev("ab", 0, false));
  }

  /**
   * Deletes the word or line following the cursor.
   * @param string text
   * @param pos cursor position
   * @param word word/line flag
   * @return new text
   */
  private static String deleteNext(final String string, final int pos, final boolean word) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.deleteNext(word);
    return Token.string(editor.text());
  }

  /**
   * Deletes the word or line preceding the cursor.
   * @param string text
   * @param pos cursor position
   * @param word word/line flag
   * @return new text
   */
  private static String deletePrev(final String string, final int pos, final boolean word) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.deletePrev(word);
    return Token.string(editor.text());
  }

  /** Lines are moved up and down, starting at the beginning of the line. */
  @Test public void moveLine() {
    assertEquals("b\na\nc", move("a\nb\nc", 2, false));
    assertEquals("b\na\nc", move("a\nb\nc", 0, true));
  }

  /**
   * Moves the current line up or down.
   * @param string text
   * @param pos cursor position
   * @param down down/up flag
   * @return new text
   */
  private static String move(final String string, final int pos, final boolean down) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.move(down);
    return Token.string(editor.text());
  }

  /** A line is selected from its start up to the following line break. */
  @Test public void selectLine() {
    final TextEditor editor = editor("  ab\ncd");
    editor.pos(3);
    editor.selectLine();
    assertEquals(0, editor.start());
    assertEquals(5, editor.end());
  }

  /** Tidying with a single option enabled. */
  @Test public void tidyOptions() {
    final TextEditor trim = editor("a  \nb");
    assertTrue(trim.tidy(true, false));
    assertEquals("a\nb", Token.string(trim.text()));

    final TextEditor nl = editor("a  \nb");
    assertTrue(nl.tidy(false, true));
    assertEquals("a  \nb\n", Token.string(nl.text()));
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

  /** Code completion. */
  @Test public void complete() {
    // the completed string is replaced
    assertEquals("declare %public\ndeclare", complete("declare %pu\ndeclare", 11, "public"));
    // an empty completed string deletes no text
    assertEquals("declare %public\ndeclare", complete("declare %\ndeclare", 9, "public"));
    // an underscore indicates the new cursor position
    assertEquals("declare %rest:path()\ndeclare",
      complete("declare %\ndeclare", 9, "rest:path(_)"));
  }

  /**
   * Completes the string before the specified position.
   * @param string text
   * @param pos cursor position
   * @param value completion value
   * @return new text
   */
  private static String complete(final String string, final int pos, final String value) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.complete(value, editor.completionStart());
    return Token.string(editor.text());
  }

  /** Formatting keeps the caret at its place in the code if no text is selected. */
  @Test public void formatCaret() {
    // the indentation of the second line is reduced to "if(true()) {\n  1\n}"
    final String string = "if(true()) {\n        1\n}";
    assertEquals(0, format(string, 0));
    assertEquals(18, format(string, 24));
    // caret before and after the reindented character
    assertEquals(15, format(string, 21));
    assertEquals(16, format(string, 22));
    // caret in the discarded indentation: it moves to the code that follows
    assertEquals(15, format(string, 13));
    assertEquals(15, format(string, 17));

    // selected text is reselected
    final TextEditor editor = editor(string);
    editor.selectAll();
    editor.format(new SyntaxXQuery());
    assertEquals(0, editor.start());
    assertEquals(18, editor.end());
  }

  /**
   * Formats the text.
   * @param string text
   * @param pos cursor position
   * @return new cursor position
   */
  private static int format(final String string, final int pos) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.format(new SyntaxXQuery());
    return editor.pos();
  }

  /** Tidying keeps the caret at the end of its line. */
  @Test public void tidyCaret() {
    // "a  \nb" is trimmed and gets a final newline
    final TextEditor editor = editor("a  \nb");
    assertTrue(editor.tidy(true, true));
    assertEquals("a\nb\n", Token.string(editor.text()));
    assertFalse(editor.tidy(true, true));

    // caret in the removed whitespace: it stays at the end of the first line
    assertEquals(0, tidy("a  \nb", 0));
    assertEquals(1, tidy("a  \nb", 1));
    assertEquals(1, tidy("a  \nb", 2));
    assertEquals(1, tidy("a  \nb", 3));
    // caret in the second line
    assertEquals(2, tidy("a  \nb", 4));
    assertEquals(3, tidy("a  \nb", 5));
  }

  /**
   * Removes trailing whitespace and appends a final newline.
   * @param string text
   * @param pos cursor position
   * @return new cursor position
   */
  private static int tidy(final String string, final int pos) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.tidy(true, true);
    return editor.pos();
  }

  /** Comments shift the caret if no text is selected. */
  @Test public void commentCaret() {
    // "foo" is enclosed by "(: " and " :)"
    assertEquals(3, comment("foo", 0));
    assertEquals(4, comment("foo", 1));
    assertEquals(6, comment("foo", 3));
    // the comment is removed again
    assertEquals(0, comment("(: foo :)", 3));
    assertEquals(1, comment("(: foo :)", 4));
    // caret in the comment delimiters: it is restricted to the commented text
    assertEquals(0, comment("(: foo :)", 1));
    assertEquals(3, comment("(: foo :)", 9));
    // comment without spaces
    assertEquals(0, comment("(:foo:)", 2));
    assertEquals(2, comment("(:foo:)", 4));
  }

  /** Comments are added with spaces, and removed with and without them. */
  @Test public void commentText() {
    assertEquals("(: foo :)", commented("foo", 0));
    assertEquals("foo", commented("(: foo :)", 3));
    assertEquals("foo", commented("(:foo:)", 2));
  }

  /**
   * (Un)comments the current line.
   * @param string text
   * @param pos cursor position
   * @return new text
   */
  private static String commented(final String string, final int pos) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.comment(new SyntaxXQuery());
    return Token.string(editor.text());
  }

  /**
   * (Un)comments the current line.
   * @param string text
   * @param pos cursor position
   * @return new cursor position
   */
  private static int comment(final String string, final int pos) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.comment(new SyntaxXQuery());
    return editor.pos();
  }

  /** Sorting moves the caret to the new position of its line. */
  @Test public void sortCaret() {
    // the lines are sorted to "a\nb\nc"; the caret keeps its column in its line
    assertEquals(4, sort("c\na\nb", 0));
    assertEquals(5, sort("c\na\nb", 1));
    assertEquals(0, sort("c\na\nb", 2));
    assertEquals(2, sort("c\na\nb", 4));
  }

  /**
   * Sorts the lines of the text.
   * @param string text
   * @param pos cursor position
   * @return new cursor position
   */
  private static int sort(final String string, final int pos) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.sort();
    return editor.pos();
  }

  /**
   * Returns an editor for the specified text.
   * @param string text
   * @return editor
   */
  private static TextEditor editor(final String string) {
    final TextEditor editor = new TextEditor(EditorOptions.DEFAULTS);
    editor.text(Token.token(string));
    return editor;
  }
}
