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
  public Bln value(final QueryContext qc) throws QueryException {
    return Bln.get(ebv(qc));
  }

  @Override
  protected boolean ebv(final QueryContext qc) throws QueryException {
    final Iter input1 = arg(0).iter(qc), input2 = arg(1).iter(qc);
    final DeepEqualOptions opts = toOptions(qc);

    final String collation = opts.get(DeepEqualOptions.COLLATION);
    final Collation coll = collation != null ? toCollation(Token.token(collation), qc) : null;

    final DeepEqual deep = new DeepEqual(info, coll, qc, opts);
    final Value ie = opts.get(DeepEqualOptions.ITEMS_EQUAL);
    if(!ie.isEmpty()) deep.itemsEqual = toFunction(ie, 2, qc);

    final boolean eq = deep.equal(input1, input2);
    if(!eq) deep.debug();
    return eq;
  }

  @Override
  public boolean hasNDT() {
    // diagnostics are a side effect: the call must not be pre-evaluated;
    // dynamic options may enable them, a string (collation) cannot
    return !(arg(2) instanceof final Item item) || item instanceof final XQMap map &&
        map.value(Str.get(DeepEqualOptions.DEBUG.name())) == Bln.TRUE || super.hasNDT();
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
      // deep-equal($x, ()) → empty($x)
      if(size2 == 0) return cc.function(Function.EMPTY, info, input1);
      if(size1 == 0) return cc.function(Function.EMPTY, info, input2);
      // deep-equal((1, 2), (1, 2, 3)) → false()
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
