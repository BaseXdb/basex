package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Format parser for integers and dates.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
abstract class FormatParser extends FormatUtil {
  /** Input information. */
  private final InputInfo info;

  /** Case. */
  Case cs;
  /** Primary format token. */
  byte[] primary;
  /** First character of format token, or mandatory digit. */
  int first = -1;
  /** Cardinal vs. ordinal flag. */
  boolean ordinal;
  /** Format modifier; {@code null} if not specified. */
  byte[] modifier;
  /** Traditional modifier. */
  boolean trad;
  /** Minimum width. */
  int min;
  /** Maximum width. */
  int max = Integer.MAX_VALUE;
  /** Radix. */
  int radix = 10;
  /** Locale. */
  Locale locale;
  /** Whether to use ICU with this format. */
  boolean useIcu;

  /**
   * Constructor for formatting integers.
   * @param info input info (can be {@code null})
   */
  FormatParser(final InputInfo info) {
    this.info = info;
  }

  /**
   * Parses and returns the presentation modifier.
   * @param pic picture
   * @param def default token
   * @param date date flag
   * @return presentation modifier
   * @throws QueryException query exception
   */
  byte[] presentation(final byte[] pic, final byte[] def, final boolean date)
      throws QueryException {

    // find primary format
    final TokenParser tp = new TokenParser(pic);
    int ch = tp.next();
    // check single character
    if(tp.more()) {
      // Word output (title case)
      if(ch == 'W' && tp.consume('w')) return pic;
      // Textual output (title case)
      if(date && ch == 'N' && tp.consume('n')) return pic;
    } else if(
      sequence(ch) != null || // Latin, Greek and other alphabets
      ch == 'i' || ch == 'I' || // Roman sequences (lower/upper case)
      ch == 'w' || ch == 'W' || // Word output (lower/upper case)
      date && (ch == 'n' || ch == 'N') || // Textual output
      ch == '\u2460' || ch == '\u2474' || ch == '\u2488' || // circled, parenthesized, full stop
      ch == KANJI[1] // Japanese numbering
    ) {
      return pic;
    }

    // find digit of decimal-digit-pattern
    tp.reset();
    while(first == -1 && tp.more()) first = zeroes(tp.next());
    // no digit found: return default primary token
    if(first == -1) return def;

    // flags for mandatory-digit-sign and group-separator-sign
    tp.reset();
    boolean gss = true;
    boolean mds = false;
    while(tp.more()) {
      ch = tp.next();
      final int d = zeroes(ch);
      if(d != -1) {
        // mandatory-digit-sign
        if(first != d) throw DIFFMAND_X.get(info, pic);
        mds = true;
        gss = false;
      } else if(ch == '#') {
        // optional-digit-sign
        if(mds) throw OPTAFTER_X.get(info, pic);
        gss = false;
      } else if(!Character.isLetter(ch)) {
        // grouping-separator-sign
        if(gss) throw INVGROUP_X.get(info, pic);
        gss = true;
      } else {
        // any other letter: return default primary token
        throw INVDDPATTERN_X.get(info, pic);
      }
    }
    if(gss) throw INVGROUP_X.get(info, pic);
    return pic;
  }

  /**
   * Checks if a character is a valid digit.
   * @param ch character
   * @param zero zero character
   * @return result of check
   */
  public boolean digit(final int ch, final int zero) {
    return ch >= zero && ch <= zero + 9;
  }

  /**
   * Finishes format parsing.
   * @param pres presentation string
   */
  void finish(final byte[] pres) {
    // skip correction of case if modifier has more than one codepoint (Ww)
    final int cp = ch(pres, 0);
    cs = radix == 10 && (cl(pres, 0) < pres.length || Token.digit(cp)) ? Case.STANDARD :
      (cp & ' ') == 0 ? Case.UPPER : Case.LOWER;
    primary = lc(pres);
    if(first == -1) first = ch(primary, 0);
  }
}
