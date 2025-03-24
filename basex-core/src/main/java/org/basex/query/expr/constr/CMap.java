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
 * @author BaseX Team, BSD License
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
    // { <_>A</_>: 1 }  ->  { 'A': 1 }
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) exprs[e] = exprs[e].simplifyFor(Simplify.DATA, cc);

    // { $a: $b }  ->  map:entry($a, $b)
    if(el == 2) return cc.function(_MAP_ENTRY, info, exprs);

    // check if record type can be defined for constructed map
    final MapType mt;
    boolean record = true;
    for(int e = 0; e < el && record; e += 2) {
      if(!(exprs[e] instanceof AStr)) record = false;
    }
    if(record) {
      final TokenObjectMap<RecordField> fields = new TokenObjectMap<>();
      for(int e = 0; e < el; e += 2) {
        fields.put(((Item) exprs[e]).string(info), new RecordField(false, exprs[e + 1].seqType()));
      }
      mt = cc.qc.newRecord(false, fields);
    } else {
      // determine static types
      AtomType kt = AtomType.ANY_ATOMIC_TYPE;
      SeqType vt = SeqType.ITEM_ZM;
      for(int e = 0; e < el; e += 2) {
        final SeqType kst = exprs[e].seqType(), dst = exprs[e + 1].seqType();
        final AtomType akt = kst.type.atomic();
        kt = akt == null || !kst.one() || kst.mayBeArray() ? AtomType.ANY_ATOMIC_TYPE :
          e == 0 ? akt : kt.union(akt);
        vt = e == 0 ? dst : vt.union(dst);
      }
      mt = MapType.get(kt, vt);
    }
    exprType.assign(mt);

    return values(true, cc) ? cc.preEval(this) : this;
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int el = exprs.length;
    final MapBuilder mb = new MapBuilder(el >>> 1);
    for(int e = 0; e < el; e += 2) {
      final Item key = toAtomItem(exprs[e], qc);
      final Value value = exprs[e + 1].value(qc);
      if(mb.contains(key)) throw MAPDUPLKEY_X.get(info);
      mb.put(key, value);
    }
    return mb.map(this);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
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
    qs.token("{ ");
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      if(e != 0) qs.token(',');
      qs.token(exprs[e]).token(':').token(exprs[e + 1]);
    }
    qs.token(" }");
  }
}
