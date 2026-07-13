package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Code formatter: indents lines by their bracket nesting, collapses whitespace, and wraps
 * expressions that exceed the line margin.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class Formatter {
  /** Length of an expression that spans several lines. */
  private static final int MULTILINE = -1;
  /** Character that separates the operands of a list (JSON: no colon). */
  private static final int LIST_SEPARATOR = ',';

  /**
   * State of a bracketed expression that is currently open.
   * @param broken the expression is placed on several lines
   * @param separated all operands of the expression are placed on separate lines
   * @param indent indentation of the line with the opening bracket
   */
  private record Bracket(boolean broken, boolean separated, Syntax.Indent indent) { }

  /** Syntax highlighter: supplies the modes of the formatted text. */
  private final Syntax syntax;
  /** Separators: preceded by no space, followed by a single space. */
  private final String separators;
  /** Brackets of lists: if a list is broken, all its operands are placed on separate lines. */
  private final String lists;
  /** Indentation of a single level. */
  private final byte[] spaces;
  /** Line margin ({@code 0}: expressions will not be wrapped). */
  private final int margin;
  /** Formatted text. */
  private final TokenBuilder tb = new TokenBuilder();

  /** Indentation of the first line: it is retained if a text fragment is formatted. */
  private byte[] base = EMPTY;
  /** Start of the current line in the formatted text. */
  private int line;
  /** Nesting depth of the brackets. */
  private int level;
  /** Indicates if a line break must be inserted before the next character. */
  private boolean wrap;
  /** Start of the whitespace that precedes the current character ({@code -1}: none). */
  private int wsStart = -1;
  /** Number of line breaks in that whitespace. */
  private int wsLines;
  /** Indicates if that whitespace occurs in element content. */
  private boolean wsContent;
  /** Last character that was not whitespace. */
  private int lastChar;
  /** Position after the last character that was not whitespace ({@code -1}: no previous line). */
  private int last = -1;
  /** Indicates if the last character that was not whitespace was code. */
  private boolean lastCode;
  /** Mode of that character (it indicates if a line ends with code, a string, a comment, a tag). */
  private int lastMode = -1;
  /** Indicates if the last non-whitespace character closed a tag or an enclosed expression. */
  private boolean lastContent;
  /** Indicates if boundary whitespace may be indented. */
  private boolean boundary;
  /** Indentation of the last line. */
  private Syntax.Indent lastIndent = Syntax.Indent.NONE;

  /**
   * Constructor.
   * @param syntax syntax highlighter
   * @param spaces indentation of a single level
   * @param margin line margin ({@code 0}: expressions will not be wrapped)
   */
  Formatter(final Syntax syntax, final byte[] spaces, final int margin) {
    this.syntax = syntax;
    this.spaces = spaces;
    this.margin = margin;
    separators = syntax.separators();
    lists = syntax.lists();
  }

  /**
   * Formats the specified text.
   * @param text text to be formatted
   * @return formatted text
   */
  byte[] format(final byte[] text) {
    final BoolList direct = new BoolList();
    final IntList lengths = lengths(text, direct);
    syntax.reset();
    base = base(text);
    boundary = syntax.boundarySpace(text);

    final ArrayDeque<Bracket> brackets = new ArrayDeque<>();
    final BoolList elements = new BoolList();
    final int tl = text.length;
    int index = 0;
    for(int p = 0; p < tl;) {
      final int cl = cl(text, p), ch = cp(text, p);
      syntax.color(text, p, p + cl);

      // whitespace in code and in element content: discarded, replaced by a line break, or adopted
      final boolean code = syntax.code(), content = syntax.content();
      if(ws(ch) && (code || content)) {
        if(wsStart == -1) {
          wsStart = p;
          wsContent = content;
        }
        if(ch == '\n') wsLines++;
        p += cl;
        continue;
      }

      // closing bracket or end tag: leave the current level
      boolean brk = false;
      Syntax.Indent close = null;
      if(closing(ch) && !brackets.isEmpty()) {
        // the closing bracket is indented like the line that contains the opening one
        final Bracket bracket = brackets.pop();
        brk = bracket.broken();
        close = bracket.indent();
        if(brk) level = Math.max(0, level - 1 - close.extra());
      } else if(boundary && syntax.elementClose() && !elements.isEmpty()) {
        if(elements.pop() && level > 0) level--;
      }
      whitespace(text, p, brk, close, ch);

      // the expression continues after the closing bracket: restore the state of its first line
      if(close != null) lastIndent = close;

      // opening bracket or start tag: enter a new level
      final int column = tb.size() - line;
      add(text, p, p + cl);
      if(opening(ch)) {
        // an expression is broken if its own operands span several lines, or if it is too long
        final boolean known = index < lengths.size();
        final int length = known ? lengths.get(index) : 0;
        wrap = known && direct.get(index) ||
          margin > 0 && length > 0 && column + length > margin;
        index++;
        brackets.push(new Bracket(wrap, wrap && lists.indexOf(ch) != -1, lastIndent));
        // the indentation of the line with the opening bracket is adopted by the enclosed lines
        if(wrap) {
          level += 1 + lastIndent.extra();
          lastIndent = Syntax.Indent.NONE;
        }
      } else if(ch == LIST_SEPARATOR && code && lastIndent.separates() && !brackets.isEmpty() &&
          brackets.peek().separated()) {
        wrap = true;
      } else if(boundary && syntax.elementOpen(text, p)) {
        // the content of an element is only indented if it starts in the next line
        final boolean indent = newline(text, p + cl);
        elements.add(indent);
        if(indent) level++;
      }
      lastChar = ch;
      last = p + cl;
      lastCode = code;
      lastMode = syntax.modeBefore();
      lastContent = syntax.contentStart();
      p += cl;
    }

    // trailing whitespace: retain line breaks, discard spaces
    lineBreaks(wsLines);
    return tb.finish();
  }

  /**
   * Returns the lengths of all bracketed expressions, in the order of their opening brackets.
   * @param text text
   * @param direct assigns for every expression if its own operands are followed by a line break
   * @return lengths ({@link #MULTILINE}, or {@code 0} if the bracket has no counterpart)
   */
  private IntList lengths(final byte[] text, final BoolList direct) {
    syntax.reset();

    // stack: indexes of the lengths, and positions of the opening brackets
    final IntList lengths = new IntList(), indexes = new IntList(), positions = new IntList();
    final int tl = text.length;
    int pending = -1;
    for(int p = 0; p < tl;) {
      final int cl = cl(text, p), ch = cp(text, p);
      syntax.color(text, p, p + cl);
      final boolean closes = closing(ch) && !indexes.isEmpty();
      if(ch == '\n' && syntax.code()) {
        for(int i = indexes.size() - 1; i >= 0; i--) lengths.set(indexes.get(i), MULTILINE);
        // a line break in a tag is nested in a constructor: it breaks no enclosing expression
        if(!indexes.isEmpty() && !syntax.tag()) pending = indexes.peek();
      } else if(!ws(ch)) {
        // a line break that is only followed by the closing bracket separates no operands
        if(pending != -1) {
          if(!closes || indexes.peek() != pending) direct.set(pending, true);
          pending = -1;
        }
        if(opening(ch)) {
          indexes.add(lengths.size());
          positions.add(p);
          lengths.add(0);
          direct.add(false);
        } else if(closes) {
          final int index = indexes.pop(), start = positions.pop();
          if(lengths.get(index) != MULTILINE) lengths.set(index, p - start);
        }
      }
      p += cl;
    }
    return lengths;
  }

  /**
   * Checks if a character opens a bracketed expression in code.
   * @param ch character
   * @return result of check
   */
  private boolean opening(final int ch) {
    return Syntax.OPENING.indexOf(ch) != -1 && syntax.codeAfter();
  }

  /**
   * Checks if a character closes a bracketed expression in code.
   * @param ch character
   * @return result of check
   */
  private boolean closing(final int ch) {
    return Syntax.CLOSING.indexOf(ch) != -1 && syntax.codeBefore();
  }

  /**
   * Adds the whitespace that precedes a character.
   * @param text text
   * @param end end of the whitespace
   * @param brk enforce a line break (the character closes a wrapped expression)
   * @param close indentation of the opening bracket if the character closes an expression
   *   ({@code null} if it does not)
   * @param ch character that follows the whitespace
   */
  private void whitespace(final byte[] text, final int end, final boolean brk,
      final Syntax.Indent close, final int ch) {
    if(wsContent) {
      // boundary whitespace is indented if it spans lines; other text is adopted unchanged
      if(boundary && wsLines > 0 && lastContent && syntax.contentEnd()) {
        lineBreaks(wsLines);
        indent(0);
        lastIndent = Syntax.Indent.NONE;
      } else if(wsStart != -1) {
        add(text, wsStart, end);
      }
    } else if(wsLines > 0) {
      // the indentation rules of the syntax are only applied if the line opens no expression
      lineBreaks(wsLines);
      final Syntax.Indent ind = close != null ? close :
        syntax.indent(text, end, last, lastMode, wsLines, lastIndent);
      indent(ind.extra());
      // the attributes of a tag are indented, but the line with the tag name stays the reference
      if(!syntax.tag()) lastIndent = ind;
    } else if(wrap || brk) {
      // line breaks are only inserted if the character is not the first one of a line
      if(tb.size() > line) {
        add('\n');
        indent(brk ? close.extra() : 0);
      }
    } else if(tb.size() == line) {
      // indentation of the first line: it is retained, but it may start a clause
      if(wsStart != -1) add(text, wsStart, end);
      lastIndent = syntax.indent(text, end, -1, -1, 0, Syntax.Indent.NONE);
    } else if(!empty(ch) && separators.indexOf(ch) == -1 && (wsStart != -1 ||
        lastCode && separators.indexOf(lastChar) != -1)) {
      // whitespace is collapsed, and a single space is added after a separator
      add(' ');
    }
    wrap = false;
    wsStart = -1;
    wsLines = 0;
    wsContent = false;
  }

  /**
   * Checks if a character closes the bracket that was opened by the last one.
   * @param ch character
   * @return result of check
   */
  private boolean empty(final int ch) {
    final int opening = Syntax.OPENING.indexOf(lastChar);
    return opening != -1 && Syntax.CLOSING.indexOf(ch) == opening;
  }

  /**
   * Adds line breaks; at most one empty line is retained.
   * @param count number of line breaks
   */
  private void lineBreaks(final int count) {
    for(int c = Math.min(count, 2); c > 0; c--) add('\n');
  }

  /**
   * Indents the current line.
   * @param extra additional levels
   */
  private void indent(final int extra) {
    add(base, 0, base.length);
    for(int l = level + extra; l > 0; l--) add(spaces, 0, spaces.length);
  }

  /**
   * Checks if a line break follows the specified position, preceded by whitespace only.
   * @param text text
   * @param pos position
   * @return result of check
   */
  private static boolean newline(final byte[] text, final int pos) {
    final int tl = text.length;
    for(int p = pos; p < tl && ws(text[p]); p++) {
      if(text[p] == '\n') return true;
    }
    return false;
  }

  /**
   * Returns the indentation of the first line of a text.
   * @param text text
   * @return indentation
   */
  private static byte[] base(final byte[] text) {
    final int tl = text.length;
    int p = 0;
    while(p < tl && (text[p] == ' ' || text[p] == '\t')) p++;
    return p < tl && text[p] != '\n' ? substring(text, 0, p) : EMPTY;
  }

  /**
   * Adds a character.
   * @param ch character
   */
  private void add(final int ch) {
    tb.addByte((byte) ch);
    if(ch == '\n') line = tb.size();
  }

  /**
   * Adds a range of characters.
   * @param text text
   * @param start start position
   * @param end end position
   */
  private void add(final byte[] text, final int start, final int end) {
    for(int p = start; p < end; p++) add(text[p]);
  }
}
