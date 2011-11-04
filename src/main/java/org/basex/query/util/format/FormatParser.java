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
  private final InputInfo input;
  /** Input to be parsed. */
  private final byte[] pic;
  /** Default modifier. */
  private final byte[] def;
  /** Extended format (used for dates). */
  private final boolean ext;

  /** Case. */
  Case cs;
  /** Primary format token. */
  byte[] primary;
  /** Primary format or mandatory digit. */
  int digit;
  /** Ordinal suffix; {@code null} if not specified. */
  byte[] ordinal;
  /** Minimum width. */
  int min;
  /** Maximum width. */
  int max = Integer.MAX_VALUE;

  /**
   * Constructor.
   * @param ii input info
   * @param p info picture
   * @param df default presentation modifier (may be {@code null})
   * @throws QueryException query exception
   */
  public FormatParser(final InputInfo ii, final byte[] p, final byte[] df)
      throws QueryException {

    input = ii;
    pic = p.length != 0 ? p : df;
    ext = df != null;
    def = ext ? df : ONE;

    byte[] pm = mod();
    if(ext) {
      // extract and check width modifier
      final int w = lastIndexOf(pm, ',');
      if(w != -1) {
        final byte[] wd = substring(pm, w + 1);
        pm = substring(pm, 0, w);

        final Matcher m = WIDTH.matcher(string(wd));
        if(!m.find()) PICDATE.thrw(ii, wd);

        int i = toInt(m.group(1));
        if(i != Integer.MIN_VALUE) min = i;
        final String mc = m.group(3);
        i = mc != null ? toInt(mc) : Integer.MIN_VALUE;
        if(i != Integer.MIN_VALUE) max = i;
      }
    }
    // choose first character and case
    if(pm.length == 0) pm = ONE;
    cs = cl(pm, 0) < pm.length ? Case.STANDARD :
      (ch(pm, 0) & ' ') == 0 ? Case.UPPER : Case.LOWER;
    primary = lc(pm);
    if(digit == 0) digit = ch(primary, 0);
  }

  /**
   * Returns a presentation modifier.
   * @return presentation modifier
   * @throws QueryException query exception
   */
  private byte[] mod() throws QueryException {
    final int l = pic.length;
    // final presentation modifier
    byte[] pm = null;
    // current offset
    int pos = cl(pic, 0);

    // proposed presentation modifier
    int ch = ch(pic, 0);
    if(sequence(ch) != null) {
      // Latin, Greek and other alphabets
    } else if(ch >= '\u2460' && ch <= '\u249b') {
      // circled, parenthesized or full stop digits
    } else if(ch == KANJI[1]) {
      // Japanese numbering
    } else if((ch | ' ') == 'i') {
      // Roman sequence
    } else if((ch | ' ') == 'w' || (ch | ' ') == 'n' && ext) {
      // word-wise output (incl. title-case check)
      if((ch & ' ') == 0 && ch(pic, pos) == (ch | ' ')) pos += cl(pic, pos);
    } else if(ch == ',') {
      // width modifier
      pos = 0;
    } else {
      // mandatory-digit-sign
      int z = -1;
      boolean group = false;
      for(pos = 0; pos < l; pos += cl(pic, pos)) {
        ch = ch(pic, pos);
        if(z == -1) {
          z = zeroes(ch);
          if(z != -1) {
            digit = z;
            group = false;
          } else if(ch == '#') {
            group = false;
          } else if(Character.isLetter(ch)) {
            pm = def;
            pos += cl(pic, pos);
            break;
          } else {
            if(pos == 0) GROUPSTART.thrw(input, pic);
            if(group) GROUPADJ.thrw(input, pic);
            group = true;
          }
        } else {
          if(Character.isLetter(ch)) {
            pm = substring(pic, 0, pos);
            break;
          } else if(ch >= z && ch <= z + 9) {
            group = false;
          } else {
            if(zeroes(ch) != -1) DIFFMAND.thrw(input, pic);
            if(ch == '#') OPTAFTER.thrw(input, pic);
            if(group) GROUPADJ.thrw(input, pic);
            group = true;
          }
        }
      }
      if(z == -1) NOMAND.thrw(input, pic);
      if(group) GROUPEND.thrw(input, pic);
    }

    // if necessary, extract primary format token from the original string
    if(pm == null) pm = substring(pic, 0, pos);

    // check for optional format modifier
    if(pos < l) {
      if(ch(pic, pos) == 'o') {
        final TokenBuilder tb = new TokenBuilder();
        if(ch(pic, ++pos) == '(') {
          while(ch(pic, ++pos) != ')') {
            // ordinal isn't closed by a parenthesis
            if(pos == l) ORDCLOSED.thrw(input, pic);
            tb.add(ch(pic, pos));
          }
          ++pos;
        }
        ordinal = tb.finish();
      } else if(ch(pic, pos) == 't') {
        // traditional numbering (ignored)
        ++pos;
      }

      // check for optional format modifier
      if(pos < l) {
        // invalid remaining input
        if(ch(pic, pos) != ',') PICCOMP.thrw(input, pic);
        pm = concat(pm, substring(pic, pos));
      }
    }
    return pm;
  }
}
