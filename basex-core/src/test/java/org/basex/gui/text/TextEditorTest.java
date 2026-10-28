package org.basex.gui.text;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;
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

  /** Joins lines. */
  @Test public void joinLines() {
    TextEditor editor = editor("a  \n  b\nc");
    editor.pos(0);
    assertTrue(editor.joinLines());
    assertEquals("a b\nc", Token.string(editor.text()));
    assertEquals(1, editor.pos());

    editor = editor("a\n  b\n\nc\n");
    editor.select(0, 8);
    assertTrue(editor.joinLines());
    assertEquals("a b c\n", Token.string(editor.text()));

    editor = editor("a");
    assertFalse(editor.joinLines());
  }

  /** Splits lines that exceed the line margin. */
  @Test public void splitLines() {
    // default margin: 80 characters
    final Syntax syntax = new SyntaxXQuery();
    syntax.init(java.awt.Color.BLACK);
    final String w = "w".repeat(9), words = String.join(" ", Collections.nCopies(10, w));
    final String seven = String.join(" ", Collections.nCopies(7, w));
    final String three = String.join(" ", Collections.nCopies(3, w));

    TextEditor editor = editor("  " + words + "\nx");
    assertTrue(editor.splitLines(syntax));
    assertEquals("  " + seven + "\n  " + three + "\nx", Token.string(editor.text()));
    // strings and comments are not split, words that exceed the margin are kept
    final String string = "'" + words + "'", comment = "(: " + words + " :)";
    editor = editor("a " + string + ' ' + comment + " b");
    assertTrue(editor.splitLines(syntax));
    assertEquals("a\n" + string + '\n' + comment + "\nb", Token.string(editor.text()));
    // lines that fit are preserved
    editor = editor("a b");
    assertFalse(editor.splitLines(syntax));

    // joining reverts the split
    editor = editor("  " + words);
    editor.splitLines(syntax);
    editor.select(0, editor.size());
    editor.joinLines();
    assertEquals("  " + words, Token.string(editor.text()));
  }

  /** Expands the selection to enclosing names and brackets. */
  @Test public void enclosing() {
    final TextEditor editor = editor("f( a-b, (c) )");
    final Syntax syntax = new SyntaxXQuery();
    syntax.init(java.awt.Color.BLACK);
    editor.pos(4);
    final int[][] ranges = { { 3, 6 }, { 3, 11 }, { 1, 13 }, { 0, 13 } };
    for(final int[] range : ranges) {
      final int[] r = editor.enclosing(syntax);
      assertArrayEquals(range, r);
      editor.select(r[0], r[1]);
    }
    assertNull(editor.enclosing(syntax));
  }

  /** Bookmarks follow the edited text. */
  @Test public void bookmarks() {
    final TextEditor editor = editor("a\nb\nc");
    editor.pos(2);
    editor.toggleBookmark();
    assertTrue(editor.bookmarked(2));
    // a line that is inserted before the bookmarked line
    editor.text(Token.token("a\nx\nb\nc"));
    assertTrue(editor.bookmarked(4));
    assertEquals(4, editor.bookmark(true));
    // a line break that is inserted at the start of the bookmarked line
    editor.text(Token.token("a\nx\n\nb\nc"));
    assertTrue(editor.bookmarked(5));
    // text that is typed in the bookmarked line
    editor.text(Token.token("a\nx\n\nbb\nc"));
    assertTrue(editor.bookmarked(5));
    // next and previous bookmarks wrap around
    editor.pos(0);
    editor.toggleBookmark();
    assertEquals(5, editor.bookmark(true));
    assertEquals(5, editor.bookmark(false));
    editor.pos(5);
    assertEquals(0, editor.bookmark(true));
    assertEquals(0, editor.bookmark(false));
    editor.pos(0);
    editor.toggleBookmark();
    // the bookmarked line is deleted
    editor.text(Token.token("a\nc"));
    assertEquals(-1, editor.bookmark(true));
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
    editor.delete(word, true);
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
    editor.delete(word, false);
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

  /** Typing a bracket or quote surrounds the selected text. */
  @Test public void surround() {
    // brackets and quotes
    assertEquals("a(bc)d", surround("abcd", 1, 3, '('));
    assertEquals("a[bc]d", surround("abcd", 1, 3, '['));
    assertEquals("a{bc}d", surround("abcd", 1, 3, '{'));
    assertEquals("a\"bc\"d", surround("abcd", 1, 3, '"'));
    assertEquals("a'bc'd", surround("abcd", 1, 3, '\''));
    assertEquals("a`bc`d", surround("abcd", 1, 3, '`'));
    // backward selection, multiple lines, whole text
    assertEquals("a(bc)d", surround("abcd", 3, 1, '('));
    assertEquals("(ab\ncd)", surround("ab\ncd", 0, 5, '('));
    // closing brackets and other characters replace the selection
    assertEquals("abcd", surround("abcd", 1, 3, ')'));
    assertEquals("abcd", surround("abcd", 1, 3, 'x'));
    // no selection
    assertEquals("abcd", surround("abcd", 2, 2, '('));
    // single brackets and quotes are replaced
    assertEquals("a\"b", surround("a\"b", 1, 2, '\''));
    assertEquals("a(b", surround("a(b", 1, 2, '('));
    assertEquals("a)b", surround("a)b", 2, 1, '['));
  }

  /** Typing a quote adds a second quote or skips the next one. */
  @Test public void quote() {
    // second quote is added
    assertEquals("\"|\"", type("", 0, '"'));
    assertEquals("(\"|\")", type("()", 1, '"'));
    // no second quote before quotes and names
    assertEquals("\"|'", type("'", 0, '"'));
    assertEquals("\"|a", type("a", 0, '"'));
    // closing quote is skipped
    assertEquals("\"\"|", type("\"\"", 1, '"'));
    assertEquals("\"a\"|", type("\"a\"", 2, '"'));
    // opening quote is not skipped
    assertEquals("\"|\"a\"", type("\"a\"", 0, '"'));
    assertEquals("x \"|\"a\"", type("x \"a\"", 2, '"'));
    assertEquals("(\"|\"a\")", type("(\"a\")", 1, '"'));
    assertEquals("x \"|\"\"", type("x \"\"", 2, '"'));
    assertEquals("\"|\"ü\"", type("\"ü\"", 0, '"'));
    assertEquals("ä\"|", type("ä", 2, '"'));
    // closing quote after whitespace, brackets and backslashes is skipped
    assertEquals("\", \"|", type("\", \"", 3, '"'));
    assertEquals("\"(\"|", type("\"(\"", 2, '"'));
    assertEquals("\"C:\\\"|", type("\"C:\\\"", 4, '"'));
  }

  /** Typing a bracket adds a closing bracket. */
  @Test public void bracketPair() {
    assertEquals("f(|)", type("f", 1, '('));
    assertEquals("f(|), 1", type("f, 1", 1, '('));
    assertEquals("f(|);", type("f;", 1, '('));
    assertEquals("(|a", type("a", 0, '('));
  }

  /** Typing {@code >} closes an opening element. */
  @Test public void closeElement() {
    assertEquals("<a>|</a>", type("<a", 2, '>'));
    assertEquals("<ä>|</ä>", type("<ä", 3, '>'));
    // no element names
    assertEquals("$a<$b>|", type("$a<$b", 5, '>'));
    assertEquals("$a < 1 >|", type("$a < 1 ", 7, '>'));
  }

  /** Deleting an opening quote or bracket removes the closing one. */
  @Test public void deletePair() {
    assertEquals("|", backspace("\"\"", 1));
    assertEquals("(|)", backspace("(\"\")", 2));
    assertEquals("|", backspace("()", 1));
    // the next quote opens a string
    assertEquals("x |\"a\"", backspace("x \"\"a\"", 3));
  }

  /**
   * Deletes the previous character.
   * @param string text
   * @param pos caret position
   * @return new text, with {@code |} marking the caret
   */
  private static String backspace(final String string, final int pos) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.deletePrev();
    return caret(editor);
  }

  /**
   * Types a character.
   * @param string text
   * @param pos caret position
   * @param ch typed character
   * @return new text, with {@code |} marking the caret
   */
  private static String type(final String string, final int pos, final char ch) {
    final TextEditor editor = editor(string);
    editor.pos(pos);
    editor.add(new StringBuilder().append(ch), false);
    return caret(editor);
  }

  /** The caret is placed relative to the insertion point. */
  @Test public void typeCaret() {
    // closing bracket in leading whitespace: the whitespace is removed
    assertEquals("  )|x", type("    )x", 4, ')'));
    // enter replaces the selected text
    final TextEditor editor = editor("f(abc)");
    editor.select(2, 5);
    editor.delete();
    editor.enter(new StringBuilder("\n"));
    assertEquals("f(\n  |\n)", caret(editor));
  }

  /**
   * Returns the text of an editor with {@code |} marking the caret.
   * @param editor editor
   * @return text
   */
  private static String caret(final TextEditor editor) {
    final byte[] text = editor.text();
    final int pos = editor.pos();
    return Token.string(text, 0, pos) + '|' + Token.string(text, pos, text.length - pos);
  }

  /** The text surrounded by a bracket or quote stays selected. */
  @Test public void surroundSelection() {
    final TextEditor editor = editor("abcd");
    editor.select(3, 1);
    assertTrue(editor.surround('('));
    assertEquals(4, editor.start());
    assertEquals(2, editor.end());
    assertEquals(2, editor.pos());
  }

  /**
   * Surrounds the selected text with the specified character.
   * @param string text
   * @param start start of the selection
   * @param end end of the selection
   * @param ch typed character
   * @return new text
   */
  private static String surround(final String string, final int start, final int end,
      final char ch) {
    final TextEditor editor = editor(string);
    editor.select(start, end);
    editor.surround(ch);
    return Token.string(editor.text());
  }

  /** Typing and deleting at multiple carets. */
  @Test public void multipleCarets() {
    final TextEditor editor = editor("ab\ncd\nef");
    editor.pos(1);
    editor.addCaret(4);
    editor.addCaret(7);
    assertTrue(editor.multiple());
    editor.forEach(() -> editor.insert("x"));
    assertEquals("axb\ncxd\nexf", Token.string(editor.text()));
    assertEquals(10, editor.pos());
    editor.forEach(editor::deletePrev);
    editor.forEach(editor::deletePrev);
    assertEquals("b\nd\nf", Token.string(editor.text()));
    editor.forEach(() -> editor.lineEnd(false));
    editor.forEach(() -> editor.insert(";"));
    assertEquals("b;\nd;\nf;", Token.string(editor.text()));
    // carets that meet are merged
    editor.forEach(() -> editor.moveTo(0, false));
    assertFalse(editor.multiple());
  }

  /** Adding carets for the occurrences of the selected text. */
  @Test public void occurrences() {
    final TextEditor editor = editor("$a + $ab + $a");
    editor.pos(1);
    // select word, add the next occurrences, which wrap around
    editor.nextOccurrence();
    assertEquals("a", editor.selected());
    editor.nextOccurrence();
    editor.nextOccurrence();
    editor.nextOccurrence();
    assertEquals(2, editor.carets().length);
    editor.forEach(() -> {
      editor.delete();
      editor.insert("x");
    });
    assertEquals("$x + $xb + $x", Token.string(editor.text()));

    // remove the caret that was added last
    editor.select(1, 2);
    editor.allOccurrences();
    assertEquals(2, editor.carets().length);
    editor.prevOccurrence();
    assertEquals(1, editor.carets().length);
    editor.singleCaret();
    assertFalse(editor.multiple());

    // a text change from outside discards secondary carets
    editor.allOccurrences();
    editor.text(Token.token("x"));
    assertFalse(editor.multiple());
  }

  /** Adding and removing carets in adjacent rows. */
  @Test public void cloneCaret() {
    final TextEditor editor = editor("ab\ncd\nef");
    editor.pos(1);
    editor.cloneCaret(4);
    editor.cloneCaret(7);
    assertEquals(2, editor.carets().length);
    assertEquals(7, editor.pos());
    // moving back removes the primary caret
    editor.cloneCaret(4);
    assertEquals(1, editor.carets().length);
    assertEquals(4, editor.pos());
    // first or last row: no caret is added
    editor.cloneCaret(4);
    assertEquals(1, editor.carets().length);
  }

  /** Copying and pasting at multiple carets. */
  @Test public void copyPaste() {
    // selections are joined in text order
    final TextEditor editor = editor("ab\ncd\nef");
    editor.pos(0);
    editor.cloneCaret(3);
    editor.forEach(editor::selectWord);
    assertEquals("ab\ncd", editor.selectedAll());

    // lines are distributed if their number matches the number of carets
    editor.paste("1\n2");
    assertEquals("1\n2\nef", Token.string(editor.text()));
    editor.paste("x");
    assertEquals("1x\n2x\nef", Token.string(editor.text()));

    // carets without selection contribute empty lines
    final TextEditor column = editor("abc\n\nghi");
    column.columns(new IntList(new int[] { 1, 4, 6 }), new IntList(new int[] { 2, 4, 7 }));
    assertEquals("b\n\nh", column.selectedAll());
    column.paste("x\ny\nz");
    assertEquals("axc\ny\ngzi", Token.string(column.text()));

    // a selection of a secondary caret counts if the primary one has none
    final TextEditor empty = editor("abc\ndef\n");
    empty.columns(new IntList(new int[] { 1, 5, 8 }), new IntList(new int[] { 2, 6, 8 }));
    assertFalse(empty.isSelected());
    assertTrue(empty.anySelected());
  }

  /** Line commands at multiple carets. */
  @Test public void lineCommands() {
    // one caret per line is kept
    final TextEditor editor = editor("ab\ncd\nef");
    editor.pos(0);
    editor.addCaret(1);
    editor.addCaret(3);
    editor.caretPerLine();
    assertEquals(1, editor.carets().length);
    editor.forEach(editor::duplLines);
    assertEquals("ab\nab\ncd\ncd\nef", Token.string(editor.text()));

    final TextEditor comment = editor("a\nb");
    comment.pos(0);
    comment.addCaret(2);
    comment.forEach(() -> comment.comment(new SyntaxXQuery()));
    assertEquals("(: a :)\n(: b :)", Token.string(comment.text()));
  }

  /** A line with two carets is unindented once. */
  @Test public void lineIndent() {
    final TextEditor indent = editor("    ab");
    indent.pos(4);
    indent.addCaret(5);
    indent.forEachLine(() -> indent.indent(new StringBuilder("\t"), true));
    assertEquals("  ab", Token.string(indent.text()));
  }

  /** Edits at a caret that remove the positions of other carets. */
  @Test public void overlappingEdits() {
    final TextEditor editor = editor("abcdef");
    editor.pos(2);
    editor.addCaret(4);
    editor.forEach(() -> editor.delete(false, false));
    assertEquals("ef", Token.string(editor.text()));

    final TextEditor words = editor("ab cd");
    words.pos(4);
    words.addCaret(5);
    words.forEach(() -> words.delete(true, false));
    assertTrue(words.pos() <= words.size());

    // forward deletion removes the position of a caret that has already been processed
    final TextEditor next = editor("xx ab cd ef");
    next.pos(3);
    next.addCaret(4);
    next.forEach(() -> next.delete(true, true));
    assertEquals(3, next.pos());
    for(final TextEditor.Caret c : next.carets()) assertEquals(3, c.pos());

    // deleted text that repeats the remaining text
    final TextEditor repeat = editor("ababab");
    repeat.pos(1);
    repeat.addCaret(4);
    repeat.forEach(() -> repeat.delete(false, false));
    assertEquals("ab", Token.string(repeat.text()));
  }

  /** Column selections. */
  @Test public void columns() {
    final TextEditor editor = editor("abc\ndef\ng");
    editor.columns(new IntList(new int[] { 1, 5, 9 }), new IntList(new int[] { 2, 6, 9 }));
    assertEquals(2, editor.carets().length);
    assertEquals(9, editor.pos());
    editor.forEach(() -> editor.toCase(TextEditor.Case.UPPER));
    assertEquals("aBc\ndEf\ng", Token.string(editor.text()));
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
