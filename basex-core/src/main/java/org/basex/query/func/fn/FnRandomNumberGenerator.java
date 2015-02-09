package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnRandomNumberGenerator extends StandardFunc {
  /** Number key. */
  private static final Str NUMBER = Str.get("number");
  /** Next key. */
  private static final Str NEXT = Str.get("next");
  /** Permute key. */
  private static final Str PERMUTE = Str.get("permute");

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long seed;
    if(exprs.length > 0) {
      seed = exprs[0].atomItem(qc, info).hash(ii);
    } else {
      seed = qc.initDateTime().nano;
    }
    return result(new Random(seed), qc);
  }

  /**
   * Returns a new result.
   * @param random random number generator
   * @param qc query context
   * @return map
   * @throws QueryException query exception
   */
  private Map result(final Random random, final QueryContext qc) throws QueryException {
    final Dbl number = Dbl.get(random.nextDouble());
    final FuncItem next = RuntimeExpr.funcItem(new Next(random, info), 0, sc, qc);
    final FuncItem permute = RuntimeExpr.funcItem(new Permute(random, info), 1, sc, qc);
    return Map.EMPTY.put(NUMBER, number, info).put(NEXT, next, info).put(PERMUTE, permute, info);
  }


  /** Returns a new number. */
  private final class Next extends RuntimeExpr {
    /** Random number generator. */
    private final Random random;

    /**
     * Constructor.
     * @param random random number generator
     * @param info input info
     */
    private Next(final Random random, final InputInfo info) {
      super(info);
      this.random = random;
    }

    @Override
    public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
      return result(random, qc);
    }
  }

  /** Returns a random permutation of supplied values. */
  private static final class Permute extends RuntimeExpr {
    /** Random number generator. */
    private final Random random;

    /**
     * Constructor.
     * @param random random number generator
     * @param info input info
     */
    private Permute(final Random random, final InputInfo info) {
      super(info);
      this.random = random;
    }

    @Override
    public Value value(final QueryContext qc) throws QueryException {
      final Value value = qc.get(params[0]).value(qc);
      final int sz = (int) value.size();
      final Item[] items = new Item[sz];
      value.writeTo(items, 0);
      for(int i = 0; i < sz; i++) {
        final int r = i + random.nextInt(sz - i);
        final Item item = items[i];
        items[i] = items[r];
        items[r] = item;
      }
      return Seq.get(items);
    }
  }
}
