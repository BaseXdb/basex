package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;
import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Map constructor.
 *
 * @author BaseX Team 2005-20, BSD License
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

    Type key = null;
    for(int e = 0; e < el; e += 2) {
      final SeqType st = exprs[e].seqType();
      final Type type = st.type.atomic();
      if(type == null || !st.one()) {
        key = null;
        break;
      }
      key = key == null ? type : key.union(type);
    }
    if(key == null) key = AtomType.AAT;

    // determine static value type
    SeqType vt = null;
    for(int e = 1; e < el; e += 2) {
      final SeqType dst = exprs[e].seqType();
      vt = vt == null ? dst : vt.union(dst);
    }
    vt = vt != null ? vt.union(SeqType.EMP) : SeqType.ITEM_ZM;

    exprType.assign(MapType.get((AtomType) key, vt));

    return allAreValues(true) ? cc.preEval(this) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    XQMap map = XQMap.EMPTY;
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      final Item key = exprs[e].atomItem(qc, info);
      if(key == Empty.VALUE) throw EMPTYFOUND.get(info);
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
    qs.token(MAP).token(" {");
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      if(e != 0) qs.token(SEP);
      qs.token(exprs[e]).token(": ").token(exprs[e + 1]);
    }
    qs.token(" }");
  }
}
