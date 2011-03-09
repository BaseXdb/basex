package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Format parser for integers and dates.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FormatParser extends FormatUtil {
  /** With pattern: ","  min-width ("-" max-width)?. */
  static final Pattern WIDTH = Pattern.compile("(\\*|\\d+)(-(\\*|\\d+))?");

  /** Input information. */
  public final InputInfo input;
  /** Input to be parsed. */
  public final byte[] in;
  /** Default modifier. */
  public final byte[] def;
  /** Date flag. */
  public final boolean date;

  /** Case. */
  Case cs;
  /** Presentation modifier in lower-case. */
  byte[] pres;
  /** Ordinal suffix; {@code null} if not specified. */
  byte[] ordinal;
  /** Minimum width. */
  int min;
  /** Maximum width. */
  int max = Integer.MAX_VALUE;

  /**
   * Constructor.
   * @param ii input info
   * @param i input
   * @param df (valid) presentation modifier.
   * '1' is used if no default modifier is specified.
   */
  public FormatParser(final InputInfo ii, final byte[] i, final byte[] df) {
    input = ii;
    in = i;
    date = df != null;
    def = date ? df : ONE;
  }

  /**
   * Parses the input string.
   * @return success flag
   * @throws QueryException query exception
   */
  public boolean parse() throws QueryException {
    // no marker specified - use default settings
    byte[] pm = in.length != 0 ? mod() : def;
    if(pm == null) return false;

    if(date) {
      // extract and check width modifier
      final int w = lastIndexOf(pm, ',');
      if(w != -1) {
        final byte[] wd = substring(pm, w + 1);
        pm = substring(pm, 0, w);

        final Matcher match = WIDTH.matcher(string(wd));
        if(!match.find()) return false;

        int m = toInt(match.group(1));
        if(m != Integer.MIN_VALUE) min = m;
        final String mc = match.group(3);
        m = mc != null ? toInt(mc) : Integer.MIN_VALUE;
        if(m != Integer.MIN_VALUE) max = m;
      }
    }

    // choose first character and case
    cs = pm.length > cl(pm, 0) ? Case.STANDARD :
      (ch(pm, 0) & ' ') != 0 ? Case.LOWER : Case.UPPER;
    pres = lc(pm);
    return true;
  }

  /**
   * Returns a presentation modifier.
   * @return presentation modifier
   * @throws QueryException query exception
   */
  private byte[] mod() throws QueryException {
    final int l = in.length;
    // final presentation modifier
    byte[] pm = null;
    // current offset
    int pos = cl(in, 0);

    // proposed presentation modifier
    int ch = ch(in, 0);
    if(sequence(ch) != null) {
      // Latin, Greek and other alphabets
    } else if(ch >= '\u2460' && ch <= '\u249b') {
      // circled, parenthesized or full stop digits
    } else if(ch == KANJI[1]) {
      // Japanese numbering
    } else if((ch | ' ') == 'i') {
      // Roman sequence
    } else if((ch | ' ') == 'w' || (ch | ' ') == 'n' && date) {
      // word-wise output (incl. title-case check)
      if((ch & ' ') == 0 && ch(in, pos) == (ch | ' ')) pos += cl(in, pos);
    } else {
      // mandatory-digit-sign
      int z = -1;
      boolean group = false;
      for(pos = 0; pos < l; pos += cl(in, pos)) {
        ch = ch(in, pos);

        if(z == -1) {
          z = zeroes(ch);
          if(z != -1 || ch == '#') {
          } else if(Character.isLetter(ch)) {
            pm = def;
            pos += cl(in, pos);
            break;
          } else if(pos == 0) {
            GROUPSTART.thrw(input, in);
          }
        } else {
          if(Character.isLetter(ch)) {
            pm = substring(in, 0, pos);
            break;
          } else if(ch >= z && ch <= z + 9) {
          } else if(zeroes(ch) != -1) {
            MANSAME.thrw(input, in);
          } else if(ch == '#') {
            OPTAFTER.thrw(input, in);
          } else {
            if(group) GROUPADJ.thrw(input, in);
            group = true;
            continue;
          }
        }
        group = false;
      }
      if(z == -1) NOMAND.thrw(input, in);
      if(group) GROUPEND.thrw(input, in);
      if(pm == null) pm = substring(in, 0, pos);
    }

    // extract primary format token from the original string if proposed
    // is to be adopted as final character
    if(pm == null) pm = substring(in, 0, pos);

    // check for optional format modifier
    if(pos < l) {
      if(ch(in, pos) == 'o') {
        final TokenBuilder tb = new TokenBuilder();
        if(ch(in, ++pos) == '(') {
          while(ch(in, ++pos) != ')') {
            // ordinal isn't closed by a parenthesis
            if(pos == l) ORDCLOSED.thrw(input, in);
            tb.add(ch(in, pos));
          }
          ++pos;
        }
        ordinal = tb.finish();
      } else if(ch(in, pos) == 't') {
        // traditional numbering (ignored)
        ++pos;
      }

      // check for optional format modifier
      if(pos < l) {
        // invalid remaining input
        if(ch(in, pos) != ',') PICCOMP.thrw(input, in);
        pm = concat(pm, substring(in, pos));
      }
    }
    return pm;
  }
}
