package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Parser for formatting integers in dates and times.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class DateFormat extends FormatParser {
  /** With pattern: ","  min-width ("-" max-width)?. */
  private static final Pattern WIDTH = Pattern.compile("^(\\*|\\d+)(-(\\*|\\d+))?$");

  /**
   * Constructor.
   * @param picture variable marker (info picture)
   * @param def default presentation modifier
   * @param info input info
   * @throws QueryException query exception
   */
  DateFormat(final byte[] picture, final byte[] def, final InputInfo info) throws QueryException {
    super(info);

    // split variable marker
    final int comma = lastIndexOf(picture, ',');
    byte[] pres = comma == -1 ? picture : substring(picture, 0, comma);
    // extract second presentation modifier
    final int pl = pres.length;
    if(pl > 1) {
      final int p = pres[pl - 1];
      if(p == 'a' || p == 'c' || p == 'o' || p == 't') {
        pres = substring(pres, 0, pl - 1);
        if(p == 'o') ordinal = EMPTY;
        if(p == 't') trad = true;
      }
    }

    // choose first character and case
    finish(pres.length == 0 ? def : presentation(pres, def, true));

    // check width modifier
    final byte[] width = comma == -1 ? null : substring(picture, comma + 1);
    if(width != null) {
      final Matcher m = WIDTH.matcher(string(width));
      if(!m.find()) throw PICDATE_X.get(info, width);
      int i = Strings.toInt(m.group(1));
      if(i != Integer.MIN_VALUE) min = i;
      final String mc = m.group(3);
      i = mc != null ? Strings.toInt(mc) : Integer.MIN_VALUE;
      if(i != Integer.MIN_VALUE) max = i;
    }
  }
}
