package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDivideDecimals extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final BigDecimal value = checkType(arg(0), AtomType.DECIMAL, qc).dec(info);
    final BigDecimal divisor = checkType(arg(1), AtomType.DECIMAL, qc).dec(info);
    final Item precision = arg(2).atomItem(qc, info);
    if(divisor.signum() == 0) throw DIVZERO_X.get(info, value);

    final int scale = precision.isEmpty() ? 0 : (int) Math.max(Integer.MIN_VALUE,
        Math.min(Integer.MAX_VALUE, toLong(precision)));
    final BigDecimal quotient = value.divide(divisor, scale, RoundingMode.DOWN);
    final BigDecimal remainder = value.subtract(quotient.multiply(divisor));

    final MapBuilder map = new MapBuilder();
    map.put("quotient", Dec.get(quotient));
    map.put("remainder", Dec.get(remainder));
    return map.map();
  }
}
