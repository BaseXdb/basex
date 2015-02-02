package org.basex.query.util.regex;

/**
 * A character literal.
 *
 * @author BaseX Team 2005-15, BSD License
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
  StringBuilder toRegEx(final StringBuilder sb) {
    return sb.append(escape(codepoint));
  }

  /**
   * Escapes the given code point for a regular expression.
   * @param cp code point
   * @return string representation
   */
  public static String escape(final int cp) {
    switch(cp) {
      case '\t': return "\\t";
      case '\r': return "\\r";
      case '\n': return "\\n";
      case '\\':
      case '|':
      case '.':
      case '?':
      case '*':
      case '+':
      case '(':
      case ')':
      case '{':
      case '}':
      case '$':
      case '-':
      case '[':
      case ']':
      case '^':
        return "\\" + (char) cp;
      default:
        if(cp < 128 && !Character.isISOControl(cp)) return String.valueOf((char) cp);
        if(cp < 0x10000) return String.format("\\u%04x", cp);
        return String.valueOf(Character.toChars(cp));
    }
  }
}
