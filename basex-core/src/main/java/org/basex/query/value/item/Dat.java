package org.basex.query.value.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Date item ({@code xs:date}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Dat extends ADate {
  /**
   * Constructor.
   * @param value date
   */
  public Dat(final ADate value) {
    super(AtomType.DAT, value);
    clean();
  }

  /**
   * Constructor.
   * @param value date
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dat(final byte[] value, final InputInfo ii) throws QueryException {
    super(AtomType.DAT);
    date(value, XDATE, ii);
  }

  /**
   * Constructor.
   * @param value date
   * @param dur duration
   * @param plus plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dat(final Dat value, final Dur dur, final boolean plus, final InputInfo ii)
      throws QueryException {

    this(value);
    if(dur instanceof DTDur) {
      calc((DTDur) dur, plus);
      if(yea <= MIN_YEAR || yea > MAX_YEAR) throw YEARRANGE_X.get(ii, yea);
    } else {
      calc((YMDur) dur, plus, ii);
    }
    clean();
  }

  @Override
  public void timeZone(final DTDur tz, final boolean spec, final InputInfo ii)
      throws QueryException {
    tz(tz, spec, ii);
    clean();
  }

  /**
   * Cleans the item and removes invalid components.
   */
  private void clean() {
    hou = -1;
    min = -1;
    sec = null;
  }
}
