package org.basex.query.util.format;

import static org.basex.util.Token.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple format parser for integers and dates.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FormatParser {
  /** With pattern: ","  min-width ("-" max-width)?. */
  private static final Pattern WIDTH =
    Pattern.compile("(\\*|\\d+)(-(\\*|\\d+))?");

  /** Cases. */
  public enum Case {
    /** Lower case. */ LOWER,
    /** Upper case. */ UPPER,
    /** Standard.   */ STANDARD;
  }

  /** Case. */
  public final Case cs;
  /** Presentation modifier in lower-case. */
  public final String pres;
  /** Variation modifier. */
  public String var;
  /** Error occurred while parsing. */
  public boolean error;
  /** Ordinal suffix; {@code null} if not specified. */
  public String ord;
  /** Minimum width. */
  public int min;
  /** Maximum width. */
  public int max = Integer.MAX_VALUE;

  /**
   * Constructor, parsing the input string. The {@link #error} variable is
   * set to true if the input is invalid.
   * @param in marker input
   * @param p (valid) presentation modifier
   * @param ext extended flag, allowing width modifier
   */
  public FormatParser(final String in, final String p, final boolean ext) {
    // no marker specified - use default settings
    String pm = p;

    final int l = in.length();
    int s = -1;

    // find presentation modifier
    if(l != 0) {
      final int ch = cp(in, 0);
      final int cu = ch & 0xFFFFFFDF;
      if(cu == 'A' || cu == 'I' || cu == '\u0391') {
        s = 1;
      } else if(cu == 'W' || cu == 'N') {
        s = cp(in, 1) == (ch | 0x20) ? 2 : 1;
      } else if(ch >= '\u2460' && ch <= '\u249b') {
        s = 1;
      } else if(ch == '#' || ch >= '0' && ch <= '9' ||
          ch >= '\u0660' && ch <= '\u0669') {
        s = decimal(in, ch & 0xFFFFFFF0);
        // invalid decimal parsing
        error = s == -1;
      } else {
        error = true;
      }
      if(s != -1) pm = in.substring(0, s);
    }
    if(s == -1) s = l;

    // find format modifier
    if(s < l) {
      if(cp(in, s) == 'o') {
        final StringBuilder sb = new StringBuilder();
        if(cp(in, ++s) == '(') {
          while(cp(in, ++s) != ')') {
            // ordinal isn't closed by a parenthesis
            error = s == l;
            if(error) break;
            sb.append((char) cp(in, s));
          }
          ++s;
        }
        ord = sb.toString();
      } else if(cp(in, s) == 't') {
        // traditional numbering (ignored)
        ++s;
      }
    }

    // find remaining modifier
    if(s < l) {
      if(cp(in, s) == ',') {
        pm += in.substring(s);
      } else {
        // invalid remaining input
        error = true;
      }
    }

    if(ext) {
      // extract and check width modifier
      final int w = pm.lastIndexOf(',');
      if(w != -1) {
        final String wd = pm.substring(w + 1);
        pm = pm.substring(0, w);

        final Matcher match = WIDTH.matcher(wd);
        if(match.find()) {
          int m = toInt(match.group(1));
          if(m != Integer.MIN_VALUE) min = m;
          final String mc = match.group(3);
          m = mc != null ? toInt(mc) : Integer.MIN_VALUE;
          if(m != Integer.MIN_VALUE) max = m;
        } else {
          error = true;
        }
      }
    }

    // choose first character and case
    cs = pm.length() > 1 ? Case.STANDARD :
      (cp(pm, 0) & 0x20) != 0 ? Case.LOWER : Case.UPPER;
    pres = pm.toLowerCase();
  }

  /**
   * Parses a decimal-digit-pattern.
   * @param in input
   * @param ba base char
   * @return end position, or {@code -1} for error
   */
  private int decimal(final String in, final int ba) {
    int s = -1;
    final int l = in.length();
    boolean d = false;
    boolean g = false;
    while(++s < l) {
      final int ch = cp(in, s);
      if(Character.isLetter(ch)) break;

      if(ch == '#') {
        // optional after decimal sign
        if(d) return -1;
        g = false;
      } else if(ch == '*') {
        g = false;
      } else if(ch >= ba && ch <= ba + 9) {
        d = true;
        g = false;
      } else {
        // adjacent grouping separators
        if(g) return -1;
        g = true;
      }
    }
    // no decimal, or ending with grouping separator
    if(!d || g) return -1;
    return s;
  }
}
