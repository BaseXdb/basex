package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.*;
import java.util.*;

import org.basex.util.*;

/**
 * This class defines syntax highlighting for Javascript files.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SyntaxJS extends Syntax {
  /** Keywords. */
  private static final HashSet<String> KEYWORDS = new HashSet<>();

  /** Mode: code. */
  private static final int CODE = 0;
  /** Mode: double-quoted string. */
  private static final int STRING_D = 1;
  /** Mode: single-quoted string. */
  private static final int STRING_S = 2;
  /** Mode: template literal. */
  private static final int TEMPLATE = 3;
  /** Mode: line comment. */
  private static final int LINE = 4;
  /** Mode: block comment. */
  private static final int BLOCK = 5;
  /** Mode: regular expression. */
  private static final int REGEX = 6;
  /** Mode: character class of a regular expression. */
  private static final int CLASS = 7;

  /** Keywords that end an operand: they are followed by a division, not by a regular expression. */
  private static final HashSet<String> OPERANDS = new HashSet<>(Arrays.asList(
    "super", "this"));

  // initialize keywords
  static {
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Lexical_grammar#Keywords
    Collections.addAll(KEYWORDS,
      "await", "break", "case", "catch", "class", "const", "continue", "debugger", "default",
      "delete", "do", "else", "enum", "export", "extends", "finally", "for", "function", "if",
      "implements", "import", "in", "instanceof", "interface", "let", "new", "package", "private",
      "protected", "public", "return", "static", "super", "switch", "this", "throw", "try",
      "typeof", "var", "void", "while", "with", "yield"
    );
  }

  @Override
  boolean code(final int mode) {
    return mode == CODE;
  }

  @Override
  Color color(final int mode) {
    return switch(mode) {
      case STRING_D, STRING_S, TEMPLATE, REGEX, CLASS -> brown;
      case LINE, BLOCK -> cyan;
      default -> plain;
    };
  }

  @Override
  Color mode(final byte[] text, final int pos, final int end, final int ch, final int mode) {
    return switch(mode) {
      case LINE -> {
        if(ch == '\n') close(0);
        yield cyan;
      }
      case BLOCK -> {
        if(ch == '*' && cp(text, pos + 1) == '/') close(1);
        yield cyan;
      }
      case STRING_D, STRING_S -> {
        if(ch == '\\') state[SKIP] = 1;
        else if(ch == (mode == STRING_D ? '"' : '\'')) close(0);
        yield brown;
      }
      case TEMPLATE -> {
        if(ch == '\\') {
          state[SKIP] = 1;
        } else if(ch == '`') {
          close(0);
        } else if(ch == '$' && cp(text, pos + 1) == '{') {
          // substitution: '${ ... }'
          enter(CODE, 1);
        }
        yield brown;
      }
      case REGEX -> {
        // a character class may contain the delimiter: '/[/]/'
        if(ch == '\\') state[SKIP] = 1;
        else if(ch == '[') enter(CLASS, 0);
        // a regular expression is limited to a single line
        else if(ch == '/' || ch == '\n') close(0);
        yield brown;
      }
      case CLASS -> {
        if(ch == '\\') state[SKIP] = 1;
        else if(ch == ']' || ch == '\n') close(0);
        yield brown;
      }
      default -> {
        if(ch == '/') {
          final int next = cp(text, pos + 1);
          if(next == '/' || next == '*') {
            enter(next == '/' ? LINE : BLOCK, 1);
            yield cyan;
          }
          if(!operand(text, pos)) {
            enter(REGEX, 0);
            yield brown;
          }
          yield plain;
        }
        if(ch == '"' || ch == '\'') {
          enter(ch == '"' ? STRING_D : STRING_S, 0);
          yield brown;
        }
        if(ch == '`') {
          enter(TEMPLATE, 0);
          yield brown;
        }
        if(ch == '{') {
          enter(CODE, 0);
          yield plain;
        }
        if(ch == '}') {
          close(0);
          yield plain;
        }
        if(Token.digit(ch)) yield purple;
        if(!XMLToken.isNCStartChar(ch)) yield plain;
        yield KEYWORDS.contains(string(text, pos, end - pos)) ? blue : plain;
      }
    };
  }

  @Override
  boolean operandName(final byte[] text, final int pos) {
    // identifiers and numbers end an operand; keywords that expect an expression do not
    final int start = nameStart(text, pos);
    final String name = string(text, start, pos + cl(text, pos) - start);
    return !KEYWORDS.contains(name) || OPERANDS.contains(name);
  }

  @Override
  public byte[] commentOpen() {
    return XMLToken.JSCOMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XMLToken.JSCOMM_C;
  }
}
