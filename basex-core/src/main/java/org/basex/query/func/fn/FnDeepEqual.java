package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnDeepEqual extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, info, 0));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final Iter input1 = arg(0).iter(qc), input2 = arg(1).iter(qc);
    final Item options = arg(2).item(qc, info);

    final DeepEqualOptions deo = new DeepEqualOptions();
    if(options instanceof XQMap) {
      toOptions(options, deo, qc);
    } else {
      deo.set(DeepEqualOptions.COLLATION, toStringOrNull(options, qc));
    }

    final String collation = deo.get(DeepEqualOptions.COLLATION);
    final Collation coll = collation != null ? toCollation(Token.token(collation), qc) : null;

    final DeepEqual de = new DeepEqual(info, coll, qc, deo);
    final Value ie = deo.get(DeepEqualOptions.ITEMS_EQUAL);
    if(!ie.isEmpty()) de.itemsEqual = toFunction(ie, 2, qc);

    final boolean eq = de.equal(input1, input2);
    if(!eq) de.debug();
    return eq;
  }

  @Override
  public boolean hasNDT() {
    // diagnostics are a side effect: the call must not be pre-evaluated
    return debug() || super.hasNDT();
  }

  /**
   * Indicates if diagnostics may be requested via the 'debug' option.
   * @return result of check
   */
  private boolean debug() {
    if(!defined(2)) return false;
    final Expr options = arg(2);
    // dynamic options: the option may be enabled
    if(!(options instanceof final Item item)) return true;
    // string (collation) or empty sequence: no diagnostics
    if(!(item instanceof final XQMap map)) return false;
    return map.value(Str.get(DeepEqualOptions.DEBUG.name())) == Bln.TRUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input1 = arg(0), input2 = arg(1);
    if(!defined(2)) {
      // deep-equal($x, $x) → true()
      if(!input1.seqType().mayBeWrapped() && !input2.seqType().mayBeWrapped() &&
          input1.equals(input2) && !input1.has(Flag.NDT)) return Bln.TRUE;
      // reject arguments of different size
      final long size1 = input1.size(), size2 = input2.size();
      if(size1 != -1 && size2 != -1 && size1 != size2)
        return cc.voidAndReturn(input1, cc.voidAndReturn(input2, Bln.FALSE, info), info);
    }
    return this;
  }

  @Override
  public int hofOffsets() {
    return functionOption(2) ? Integer.MAX_VALUE : 0;
  }
}
