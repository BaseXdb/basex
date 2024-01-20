package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Date item ({@code xs:date}).
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Dat extends ADate {
  /**
   * Constructor.
   * @param value date
   */
  public Dat(final ADate value) {
    super(AtomType.DATE, value);
    clean();
  }

  /**
   * Constructor.
   * @param value date
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Dat(final byte[] value, final InputInfo info) throws QueryException {
    super(AtomType.DATE);
    date(value, XDATE, info);
  }

  /**
   * Constructor.
   * @param value date
   * @param dur duration
   * @param plus plus/minus flag
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Dat(final Dat value, final Dur dur, final boolean plus, final InputInfo info)
      throws QueryException {

    this(value);
    if(dur instanceof DTDur) {
      calc((DTDur) dur, plus);
      if(year <= MIN_YEAR || year > MAX_YEAR) throw YEARRANGE_X.get(info, year);
    } else {
      calc((YMDur) dur, plus, info);
    }
    clean();
  }

  @Override
  public Dat timeZone(final DTDur dur, final boolean undefined, final InputInfo info)
      throws QueryException {
    final Dat dat = new Dat(this);
    dat.tz(dur, undefined, info);
    dat.clean();
    return dat;
  }

  /**
   * Cleans the item and removes invalid components.
   */
  private void clean() {
    hour = -1;
    minute = -1;
    seconds = null;
  }
}
