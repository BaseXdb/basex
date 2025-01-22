package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnRound extends NumericFn {
  /** Rounding mode. */
  public enum RoundMode {
    /** Mode. */ FLOOR,
    /** Mode. */ CEILING,
    /** Mode. */ TOWARD_ZERO,
    /** Mode. */ AWAY_FROM_ZERO,
    /** Mode. */ HALF_TO_FLOOR,
    /** Mode. */ HALF_TO_CEILING,
    /** Mode. */ HALF_TOWARD_ZERO,
    /** Mode. */ HALF_AWAY_FROM_ZERO,
    /** Mode. */ HALF_TO_EVEN;

    @Override
    public String toString() {
      return EnumOption.string(this);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item mode = arg(2).atomItem(qc, info);
    return round(qc, mode.isEmpty() ? RoundMode.HALF_TO_CEILING :
      toEnum(mode, RoundMode.class));
  }

  /**
   * Rounds values.
   * @param qc query context
   * @param mode rounding mode
   * @return number or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item round(final QueryContext qc, final RoundMode mode) throws QueryException {
    final ANum value = toNumberOrNull(arg(0), qc);
    final Item precision = arg(1).atomItem(qc, info);

    final int scale = precision.isEmpty() ? 0 : (int) Math.max(-1 << 20,
        Math.min(1 << 20, toLong(precision)));
    return value == null ? Empty.VALUE : value.round(scale, mode);
  }
}
