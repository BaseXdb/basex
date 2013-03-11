package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Parser for formatting integers in dates and times.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DateFormat extends FormatParser {
  /** With pattern: ","  min-width ("-" max-width)?. */
  private static final Pattern WIDTH =
      Pattern.compile("(\\*|\\d+)(-(\\*|\\d+))?");

  /** Presentation modifier. */
  byte[] pres;

  /**
   * Constructor.
   * @param p info picture
   * @param df default presentation modifier ({@code null} for formatting integers)
   * @param ii input info
   * @throws QueryException query exception
   */
  public DateFormat(final byte[] p, final byte[] df, final InputInfo ii)
      throws QueryException {

    super(ii);

    final byte[] pic = p;
    int cp = picture(pic, df);
    if(cp < pic.length) cp = format(pic, cp);

    // check for optional format modifier
    if(cp < pic.length) {
      // invalid remaining input
      if(ch(pic, cp) != ',') PICCOMP.thrw(info, pic);
      pres = concat(pres, substring(pic, cp));
    }

    // extract and check width modifier
    final int w = lastIndexOf(pres, ',');
    if(w != -1) {
      final byte[] wd = substring(pres, w + 1);
      pres = substring(pres, 0, w);

      final Matcher m = WIDTH.matcher(string(wd));
      if(!m.find()) PICDATE.thrw(ii, wd);

      int i = toInt(m.group(1));
      if(i != Integer.MIN_VALUE) min = i;
      final String mc = m.group(3);
      i = mc != null ? toInt(mc) : Integer.MIN_VALUE;
      if(i != Integer.MIN_VALUE) max = i;
    }

    // choose first character and case
    if(pres.length == 0) pres = ONE;
    cs = cl(pres, 0) < pres.length ? Case.STANDARD :
      (ch(pres, 0) & ' ') == 0 ? Case.UPPER : Case.LOWER;
    primary = lc(pres);
    if(digit == -1) digit = ch(primary, 0);
  }

  /**
   * Parses the integer picture.
   * @param pic picture
   * @param def default format
   * @return offset to last parsed character
   * @throws QueryException query exception
   */
  protected int picture(final byte[] pic, final byte[] def) throws QueryException {
    final int l = pic.length;
    if(l == 0) {
      pres = def;
      return 0;
    }

    // final presentation modifier
    byte[] pm = null;
    // current offset
    int cp = cl(pic, 0);

    // proposed presentation modifier
    int ch = ch(pic, 0);
    if(sequence(ch) != null) {
      // Latin, Greek and other alphabets
    } else if(ch == '\u2460' || ch == '\u2474' || ch == '\u2488') {
      // circled, parenthesized or full stop digits
    } else if(ch == KANJI[1]) {
      // Japanese numbering
    } else if((ch | ' ') == 'i') {
      // Roman sequence
    } else if((ch | ' ') == 'w' || (ch | ' ') == 'n') {
      // word-wise output (incl. title-case check)
      if((ch & ' ') == 0 && ch(pic, cp) == (ch | ' ')) cp += cl(pic, cp);
    } else if(ch == ',') {
      // width modifier
      cp = 0;
    } else {
      // mandatory-digit-sign
      int z = -1;
      boolean group = false;
      for(cp = 0; cp < l; cp += cl(pic, cp)) {
        ch = ch(pic, cp);
        if(z == -1) {
          z = zeroes(ch);
          if(z != -1) {
            digit = z;
            group = false;
          } else if(ch == '#') {
            group = false;
          } else if(Character.isLetter(ch)) {
            pm = def;
            cp += cl(pic, cp);
            break;
          } else {
            if(cp == 0) GROUPSTART.thrw(info, pic);
            if(group) INVGROUP.thrw(info, pic);
            group = true;
          }
        } else {
          if(Character.isLetter(ch)) {
            pm = substring(pic, 0, cp);
            break;
          } else if(ch >= z && ch <= z + 9) {
            group = false;
          } else {
            if(zeroes(ch) != -1) DIFFMAND.thrw(info, pic);
            if(ch == '#') OPTAFTER.thrw(info, pic);
            if(group) INVGROUP.thrw(info, pic);
            group = true;
          }
        }
      }
      if(z == -1) NOMAND.thrw(info, pic);
      if(group) INVGROUP.thrw(info, pic);
    }

    // if necessary, extract primary format token from the original string
    if(pm == null) pm = substring(pic, 0, cp);

    pres = pm;
    return cp;
  }

  /**
   * Parses the format modifier.
   * @param format picture
   * @param p current position
   * @return new position
   * @throws QueryException query exception
   */
  protected int format(final byte[] format, final int p) throws QueryException {
    int cp = p;
    final int l = format.length;
    if(ch(format, cp) == 'o') {
      final TokenBuilder tb = new TokenBuilder();
      if(ch(format, ++cp) == '(') {
        while(ch(format, ++cp) != ')') {
          // ordinal isn't closed by a parenthesis
          if(cp == l) INVORDINAL.thrw(info, format);
          tb.add(ch(format, cp));
        }
        ++cp;
      }
      ordinal = tb.finish();
    } else if(ch(format, cp) == 'c') {
      // cardinal numbering (default)
      ++cp;
    } else if(ch(format, cp) == 't' || ch(format, cp) == 'a') {
      // alphabetical or traditional numbering (ignored)
      ++cp;
    }
    return cp;
  }
}
