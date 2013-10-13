package org.basex.query.value.item;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Time item ({@code xs:time}).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Tim extends ADate {
  /**
   * Constructor.
   * @param time time
   */
  public Tim(final ADate time) {
    super(AtomType.TIM, time);
    clean();
  }

  /**
   * Constructor.
   * @param time time
   * @param ii input info
   * @throws QueryException query exception
   */
  public Tim(final byte[] time, final InputInfo ii) throws QueryException {
    super(AtomType.TIM);
    time(time, XTIME, ii);
    clean();
  }

  /**
   * Constructor.
   * @param time time
   * @param dur duration to be added/subtracted
   * @param plus plus/minus flag
   */
  public Tim(final Tim time, final DTDur dur, final boolean plus) {
    super(AtomType.TIM, time);
    calc(dur, plus);
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
    yea = Long.MAX_VALUE;
    mon = -1;
    day = -1;
  }
}
