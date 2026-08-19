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
  /** Options, parsed at compile time (can be {@code null}). */
  private DeepEqualOptions options;

  @Override
  protected Bln item(final QueryContext qc) throws QueryException {
    return Bln.get(ebv(qc));
  }

  @Override
  protected boolean test(final QueryContext qc, final long pos) throws QueryException {
    final Iter input1 = arg(0).iter(qc), input2 = arg(1).iter(qc);
    final DeepEqualOptions opts = options != null ? options : options(qc);

    final String collation = opts.get(DeepEqualOptions.COLLATION);
    final Collation coll = collation != null ? toCollation(Token.token(collation), qc) : null;

    final DeepEqual de = new DeepEqual(info, coll, qc, opts);
    final Value ie = opts.get(DeepEqualOptions.ITEMS_EQUAL);
    if(!ie.isEmpty()) de.itemsEqual = toFunction(ie, 2, qc);

    final boolean eq = de.equal(input1, input2);
    if(!eq) de.debug();
    return eq;
  }

  /**
   * Returns the comparison options.
   * @param qc query context
   * @return options
   * @throws QueryException query exception
   */
  private DeepEqualOptions options(final QueryContext qc) throws QueryException {
    final Item item = arg(2).item(qc, info);
    final DeepEqualOptions opts = new DeepEqualOptions();
    if(item instanceof XQMap) {
      toOptions(item, opts, qc);
    } else {
      opts.set(DeepEqualOptions.COLLATION, toStringOrNull(item, qc));
    }
    return opts;
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
    // dynamic options: the option may be enabled
    if(!(arg(2) instanceof final Item item)) return true;
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
    // parse constant options
    if(arg(2) instanceof Value) options = options(cc.qc).seal();
    return this;
  }

  @Override
  public int hofOffsets() {
    return functionOption(2) ? Integer.MAX_VALUE : 0;
  }
}
