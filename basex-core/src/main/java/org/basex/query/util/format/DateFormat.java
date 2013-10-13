package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Parser for formatting integers in dates and times.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class DateFormat extends FormatParser {
  /** With pattern: ","  min-width ("-" max-width)?. */
  private static final Pattern WIDTH = Pattern.compile("^(\\*|\\d+)(-(\\*|\\d+))?$");

  /**
   * Constructor.
   * @param pic variable marker (info picture)
   * @param df default presentation modifier
   * @param ii input info
   * @throws QueryException query exception
   */
  public DateFormat(final byte[] pic, final byte[] df, final InputInfo ii)
      throws QueryException {

    super(ii);

    // split variable marker
    final int comma = lastIndexOf(pic, ',');
    byte[] pres = comma == -1 ? pic : substring(pic, 0, comma);
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
    finish(pres.length == 0 ? df : presentation(pres, df, true));

    // check width modifier
    final byte[] width = comma == -1 ? null : substring(pic, comma + 1);
    if(width != null) {
      final Matcher m = WIDTH.matcher(string(width));
      if(!m.find()) PICDATE.thrw(ii, width);
      int i = toInt(m.group(1));
      if(i != Integer.MIN_VALUE) min = i;
      final String mc = m.group(3);
      i = mc != null ? toInt(mc) : Integer.MIN_VALUE;
      if(i != Integer.MIN_VALUE) max = i;
    }
  }
}
