package org.basex.query.expr.constr;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Map constructor.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class CMap extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param expr key and value expression, interleaved
   */
  public CMap(final InputInfo info, final Expr[] expr) {
    super(info, expr);
    seqType = SeqType.MAP_O;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return allAreValues() ? preEval(qc) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Map map = Map.EMPTY;
    final int es = exprs.length;
    for(int e = 0; e < es; e += 2) {
      final Value key = exprs[e].atomValue(qc, ii);
      if(!(key instanceof Item)) throw SEQFOUND_X.get(ii, key);
      final Item k = (Item) key;
      final Value v = qc.value(exprs[e + 1]);
      if(map.contains(k, ii)) throw MAPDUPLKEY_X_X_X.get(ii, k, map.get(k, ii), v);
      map = map.put(k, v, ii);
    }
    return map;
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CMap(info, copyAll(qc, scp, vs, exprs));
  }

  @Override
  public String description() {
    return QueryText.MAPSTR;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder("{ ");
    boolean key = true;
    for(final Expr e : exprs) {
      tb.add(key ? tb.size() > 2 ? ", " : "" : ":").add(e.toString());
      key ^= true;
    }
    return tb.add(" }").toString();
  }
}
