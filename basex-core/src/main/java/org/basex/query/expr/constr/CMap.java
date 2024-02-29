package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
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
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class CMap extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr key and value expression, interleaved
   */
  public CMap(final InputInfo info, final Expr[] expr) {
    super(info, SeqType.MAP_O, expr);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // map { <_>A</_>: 1 }  ->  map { 'A': 1 }
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) exprs[e] = exprs[e].simplifyFor(Simplify.DATA, cc);

    // map { $a: $b }  ->  map:entry($a, $b)
    if(el == 2) return cc.function(_MAP_ENTRY, info, exprs);

    // determine static key type (all keys must be single items)
    AtomType kt = null;
    for(int e = 0; e < el; e += 2) {
      final SeqType st = exprs[e].seqType();
      final AtomType type = st.type.atomic();
      if(type == null || !st.one() || st.mayBeArray()) {
        kt = null;
        break;
      }
      kt = kt == null ? type : kt.union(type);
    }
    if(kt == null) kt = AtomType.ANY_ATOMIC_TYPE;

    // determine static value type
    SeqType dt = null;
    for(int e = 1; e < el; e += 2) {
      final SeqType dst = exprs[e].seqType();
      dt = dt == null ? dst : dt.union(dst);
    }
    exprType.assign(MapType.get(kt, dt != null ? dt : SeqType.ITEM_ZM));

    return allAreValues(true) ? cc.preEval(this) : this;
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final MapBuilder mb = new MapBuilder();
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      final Item key = toAtomItem(exprs[e], qc);
      final Value value = exprs[e + 1].value(qc);
      if(mb.contains(key)) throw MAPDUPLKEY_X_X_X.get(info, key, mb.get(key), value);
      mb.put(key, value);
    }
    return mb.map();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CMap(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CMap && super.equals(obj);
  }

  @Override
  public String description() {
    return MAP;
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(MAP).token(" { ");
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      if(e != 0) qs.token(',');
      qs.token(exprs[e]).token(':').token(exprs[e + 1]);
    }
    qs.token(" }");
  }
}
