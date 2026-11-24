package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Parser for formatting integers in dates and times.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DateFormat extends FormatParser {
  /** With pattern: ","  min-width ("-" max-width)?. */
  private static final Pattern WIDTH = Pattern.compile("^(\\*|\\d+)(-(\\*|\\d+))?$");
  /** Components that can be output as names. */
  private static final byte[] NAMES = token("MFPCE");
  /** Token: n. */
  private static final byte[] N = cpToken('n');
  /** Token: 1. */
  private static final byte[] ONE = cpToken('1');
  /** Presentation modifier: two digits. */
  private static final byte[] TWO_DIGITS = token("01");
  /** Presentation modifier: timezone. */
  private static final byte[] ZONE_DIGITS = token("01:01");

  /** Variable marker. */
  final byte[] marker;
  /** Component. */
  final int comp;
  /** Number of mandatory digit signs. */
  final int mandatory;
  /** Number of optional digit signs. */
  final int optional;
  /** Modulus for reducing years to the requested number of digits ({@code 0}: none). */
  long yearModulus;

  /**
   * Constructor.
   * @param marker variable marker
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  DateFormat(final byte[] marker, final InputInfo info) throws QueryException {
    super(info);
    this.marker = marker;
    comp = ch(marker, 0);
    final byte[] def = switch(comp) {
      case 'Y', 'M', 'D', 'd', 'W', 'w', 'H', 'h', 'f' -> ONE;
      case 'F', 'P', 'C', 'E' -> N;
      case 'm', 's' -> TWO_DIGITS;
      case 'Z', 'z' -> ZONE_DIGITS;
      default -> throw INVCOMPSPEC_X.get(info, marker);
    };

    // split variable marker
    final byte[] picture = substring(marker, 1);
    final int comma = lastIndexOf(picture, ',');
    byte[] pres = comma == -1 ? picture : substring(picture, 0, comma);
    // extract second presentation modifier: numeral type (c, o), followed by style (a, t)
    int pl = pres.length;
    if(pl > 1 && (pres[pl - 1] == 'a' || pres[pl - 1] == 't')) trad = pres[--pl] == 't';
    if(pl > 1 && (pres[pl - 1] == 'c' || pres[pl - 1] == 'o')) {
      if(pres[--pl] == 'o') numType = NumeralType.ORDINAL;
    }
    pres = substring(pres, 0, pl);

    // choose first character and case
    try {
      finish(pres.length == 0 ? def : presentation(pres, def, true, comp == 'f'));
    } catch(final QueryException ex) {
      throw INVFDPATTERN_X.get(info, ex.getLocalizedMessage()).cause(ex);
    }

    // check width modifier
    if(comma != -1) {
      final byte[] width = substring(picture, comma + 1);
      final Matcher m = WIDTH.matcher(string(width));
      if(!m.find()) throw PICDATE_X.get(info, width);
      final String mn = m.group(1), mx = m.group(3);
      if(!mn.equals("*")) {
        min = Strings.toInt(mn);
        if(min < 1) throw PICDATE_X.get(info, width);
      }
      if(mx != null && !mx.equals("*")) {
        // maximum widths beyond the integer range are unbounded
        max = Strings.toInt(mx);
        if(max == Integer.MIN_VALUE) max = Integer.MAX_VALUE;
        if(max < 1 || max < min) throw PICDATE_X.get(info, width);
      }
    }

    // no name available, or fractional seconds without digits: decimal output
    final boolean zone = comp == 'Z' || comp == 'z', names = first == 'n' && contains(NAMES, comp);
    if(first == 'n' && !names && !zone || comp == 'f' && zeroes(first) == -1) {
      first = '0';
      primary = ONE;
    }

    // count digit signs of the presentation modifier
    int md = 0, od = 0;
    for(final TokenParser tp = new TokenParser(primary); tp.more();) {
      final int cp = tp.next();
      if(cp == '#') od++;
      else if(zeroes(cp) != -1) md++;
    }
    mandatory = md;
    optional = od;

    // year: number of digits (see: Formatting the Year Component)
    if(comp == 'Y') {
      int n = max;
      // decimal-digit-pattern: number of mandatory and optional digit signs, if 2 or more
      if(n == Integer.MAX_VALUE && zeroes(first) != -1 && md + od > 1) n = md + od;
      if(n >= 1 && n <= 18) {
        yearModulus = 1;
        while(n-- > 0) yearModulus *= 10;
      }
    }

    // timezones and names: widths are applied to the output
    if(zone || names) return;
    if(comp == 'f') {
      // fractional seconds: mandatory digits extend the widths
      max = Math.max(max, md);
      min = Math.max(min, md);
      return;
    }
    // integer-valued component: minimum width is enforced, maximum width is ignored
    max = Integer.MAX_VALUE;
  }
}
