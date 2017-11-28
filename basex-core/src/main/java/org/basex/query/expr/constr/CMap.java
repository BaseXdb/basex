package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;

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
 * @author BaseX Team 2005-17, BSD License
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
    // key type
    final int el = exprs.length;
    Type key = null;
    for(int e = 0; e < el; e += 2) {
      final SeqType kst = exprs[e].seqType();
      final Type kt = kst.atomicType();
      if(kt == null || !kst.one()) {
        key = null;
        break;
      }
      key = key == null ? kt : key.union(kt);
    }

    // value type
    SeqType vt = null;
    for(int e = 1; e < el; e += 2) {
      final SeqType dst = exprs[e].seqType();
      vt = vt == null ? dst : vt.union(dst);
    }

    // assign type if at least one key/value pair exists, and if all keys are single items
    if(key != null) exprType.assign(MapType.get((AtomType) key, vt));

    return allAreValues(true) ? cc.preEval(this) : this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Map map = Map.EMPTY;
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      final Value key = exprs[e].atomValue(qc, info);
      if(!(key instanceof Item)) throw SEQFOUND_X.get(info, key);
      final Item k = (Item) key;
      final Value v = qc.value(exprs[e + 1]);
      if(map.contains(k, info)) throw MAPDUPLKEY_X_X_X.get(info, k, map.get(k, info), v);
      map = map.put(k, v, info);
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
    return QueryText.MAP;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder("map { ");
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      if(e != 0) tb.add(", ");
      tb.addExt(exprs[e]).add(':').addExt(exprs[e + 1]);
    }
    return tb.add(" }").toString();
  }
}
