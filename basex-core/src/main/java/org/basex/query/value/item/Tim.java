package org.basex.query.value.item;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Time item ({@code xs:time}).
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Tim extends ADate {
  /**
   * Constructor.
   * @param value time
   */
  public Tim(final ADate value) {
    super(AtomType.TIME, value);
    clean();
  }

  /**
   * Constructor.
   * @param value time
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public Tim(final byte[] value, final InputInfo info) throws QueryException {
    super(AtomType.TIME);
    time(value, XTIME, info);
    clean();
  }

  /**
   * Constructor.
   * @param value time
   * @param dur duration to be added/subtracted
   * @param plus plus/minus flag
   */
  public Tim(final Tim value, final DTDur dur, final boolean plus) {
    super(AtomType.TIME, value);
    calc(dur, plus);
    clean();
  }

  @Override
  public void timeZone(final DTDur dur, final boolean undefined, final InputInfo info)
      throws QueryException {
    super.timeZone(dur, undefined, info);
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
