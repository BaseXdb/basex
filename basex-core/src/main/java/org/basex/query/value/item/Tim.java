package org.basex.query.value.item;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Time item ({@code xs:time}).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Tim extends ADate {
  /**
   * Constructor.
   * @param value time
   */
  public Tim(final ADate value) {
    super(AtomType.TIM, value);
    clean();
  }

  /**
   * Constructor.
   * @param value time
   * @param ii input info
   * @throws QueryException query exception
   */
  public Tim(final byte[] value, final InputInfo ii) throws QueryException {
    super(AtomType.TIM);
    time(value, XTIME, ii);
    clean();
  }

  /**
   * Constructor.
   * @param value time
   * @param dur duration to be added/subtracted
   * @param plus plus/minus flag
   */
  public Tim(final Tim value, final DTDur dur, final boolean plus) {
    super(AtomType.TIM, value);
    calc(dur, plus);
    clean();
  }

  @Override
  public void timeZone(final DTDur zone, final boolean spec, final InputInfo ii)
      throws QueryException {
    tz(zone, spec, ii);
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
