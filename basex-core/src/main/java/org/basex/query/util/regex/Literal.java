package org.basex.query.util.regex;

/**
 * A character literal.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class Literal extends RegExp {
  /** Code point. */
  private final int codepoint;

  /**
   * Constructor.
   * @param codepoint Unicode code point
   */
  public Literal(final int codepoint) {
    this.codepoint = codepoint;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append(escape(codepoint));
  }

  /**
   * Escapes the given code point for a regular expression.
   * @param cp code point
   * @return string representation
   */
  public static String escape(final int cp) {
    return switch(cp) {
      case '\t' -> "\\t";
      case '\r' -> "\\r";
      case '\n' -> "\\n";
      case '\\', '|', '.', '?', '*', '+', '(', ')', '{', '}', '$', '-', '[', ']', '^' ->
        "\\" + (char) cp;
      default -> {
        if(cp < 128 && !Character.isISOControl(cp)) yield String.valueOf((char) cp);
        if(cp < 0x10000) yield String.format("\\u%04x", cp);
        yield String.valueOf(Character.toChars(cp));
      }
    };
  }
}
