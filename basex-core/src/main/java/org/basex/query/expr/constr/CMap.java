package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

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
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class CMap extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param expr key and value expression, interleaved
   */
  public CMap(final InputInfo info, final Expr[] expr) {
    super(info, SeqType.MAP_O, expr);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // determine static key type (all keys must be single items)
    final int el = exprs.length;
    if(el == 2) return cc.function(_MAP_ENTRY, info, exprs);

    Type kt = null;
    for(int e = 0; e < el; e += 2) {
      final SeqType st = exprs[e].seqType();
      final Type type = st.type.atomic();
      if(type == null || !st.one()) {
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
    dt = dt != null ? dt.union(SeqType.EMPTY_SEQUENCE_Z) : SeqType.ITEM_ZM;

    exprType.assign(MapType.get((AtomType) kt, dt));

    return allAreValues(true) ? cc.preEval(this) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQMap map = XQMap.EMPTY;
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      final Item key = toAtomItem(exprs[e], qc);
      final Value value = exprs[e + 1].value(qc);
      if(map.contains(key, info)) throw MAPDUPLKEY_X_X_X.get(info, key, map.get(key, info), value);
      map = map.put(key, value, info);
    }
    return map;
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
  public void plan(final QueryString qs) {
    qs.token(MAP).token(" { ");
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      if(e != 0) qs.token(',');
      qs.token(exprs[e]).token(':').token(exprs[e + 1]);
    }
    qs.token(" }");
  }
}
