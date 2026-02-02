package org.basex.query.expr.constr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
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
    super(info, Types.MAP_O, expr);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // flatten nested maps: { 1: 2, { 3: 4, 5: 6 }, () } → { 1: 2, 3: 4, 5: 6 }
    int el = exprs.length;
    if(((Checks<Expr>) expr -> expr == Empty.UNDEFINED).any(exprs)) {
      final ExprList list = new ExprList();
      for(int e = 0; e < el; e += 2) {
        final Expr expr = exprs[e];
        if(!(nested(e) && flatten(expr, list))) list.add(expr).add(exprs[e + 1]);
      }
      exprs = list.finish();
      el = exprs.length;
    }

    // atomize keys: { <_>A</_>: 1 } → { 'A': 1 }
    for(int e = 0; e < el; e += 2) {
      if(!nested(e)) {
        exprs[e] = exprs[e].simplifyFor(Simplify.DATA, cc);
      }
    }

    // empty map?
    if(el == 0) return XQMap.empty();
    if(el == 2) {
      // { $a: $b } → map:entry($a, $b)
      if(!nested(0)) return cc.function(_MAP_ENTRY, info, exprs);
      // { { 'a': <a/> } } → { 'a': <a/> }
      if(exprs[0].seqType().instanceOf(Types.MAP_O)) return exprs[0];
    }

    // not too large, only strings as keys? replace with record constructor
    boolean record = el <= RecordType.MAX_GENERATED_SIZE;
    for(int e = 0; e < el && record; e += 2) {
      if(nested(e) || !(exprs[e] instanceof AStr && exprs[e].seqType().eq(Types.STRING_O))) {
        record = false;
      }
    }
    if(record) {
      final TokenObjectMap<RecordField> fields = new TokenObjectMap<>(el / 2);
      final ExprList args = new ExprList(el / 2);
      for(int e = 0; e < el; e += 2) {
        final Expr key = exprs[e], value = exprs[e + 1];
        if(fields.put(((AStr) key).string(info), new RecordField(value.seqType())) != null) {
          throw MAPDUPLKEY_X.get(info, key);
        }
        args.add(value);
      }
      final RecordType rt = cc.qc.shared.record(new RecordType(fields));
      return new CRecord(info, rt, args.finish()).optimize(cc);
    }

    // determine static types
    Type kt = null;
    SeqType vt = null;
    for(int e = 0; e < el; e += 2) {
      Type ekt = BasicType.ANY_ATOMIC_TYPE;
      SeqType evt = Types.ITEM_ZM;
      if(nested(e)) {
        if(exprs[e].seqType().type instanceof final MapType mt) {
          ekt = mt.keyType();
          evt = mt.valueType();
        }
      } else {
        final SeqType kst = exprs[e].seqType();
        final Type akt = kst.type.atomic();
        if(akt != null) ekt = akt;
        evt = exprs[e + 1].seqType();
      }
      kt = e == 0 ? ekt : kt.union(ekt);
      vt = e == 0 ? evt : vt.union(evt);
    }
    exprType.assign(MapType.get(kt, vt));

    return values(true, cc) ? cc.preEval(this) : this;
  }

  /**
   * Flattens nested map constructors.
   * @param expr expression to flatten
   * @param list list of expressions
   * @return {@code true} if the expression was flattened
   * @throws QueryException query exception
   */
  private boolean flatten(final Expr expr, final ExprList list) throws QueryException {
    if(expr == Empty.VALUE) {
      // ignore empty values
    } else if(expr instanceof CMap) {
      // { 1: <a/> }
      list.add(expr.args());
    } else if(expr instanceof CRecord) {
      // { 'a': <a/> }
      final RecordType rt = (RecordType) expr.seqType().type;
      final boolean extensible = rt.isExtensible();
      if(extensible || rt.hasOptional()) return false;
      final TokenObjectMap<RecordField> fields = rt.fields();
      final int fs = fields.size();
      for(int f = 1; f <= fs; f++) list.add(Str.get(fields.key(f))).add(expr.arg(f - 1));
    } else if(Function._MAP_ENTRY.is(expr)) {
      // map:entry('a', 'b')
      list.add(expr.arg(0)).add(expr.arg(1));
    } else if(expr instanceof final Value value && value.type instanceof MapType) {
      // { 'a': 'b' }
      for(final Item item : value) {
        ((XQMap) item).forEach((k, v) -> list.add(k).add(v));
      }
    } else {
      return false;
    }
    return true;
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final int el = exprs.length;
    final MapBuilder mb = new MapBuilder(el >>> 1);
    final QueryBiConsumer<Item, Value> add = (key, value) -> {
      if(mb.contains(key)) throw MAPDUPLKEY_X.get(info, key);
      mb.put(key, value);
    };
    for(int e = 0; e < el; e += 2) {
      if(exprs[e + 1] != Empty.UNDEFINED) {
        add.accept(toAtomItem(exprs[e], qc), exprs[e + 1].value(qc));
      } else {
        final Iter iter = exprs[e].iter(qc);
        for(Item item; (item = iter.next()) != null;) {
          toMap(item).forEach(add);
        }
      }
    }
    return mb.map(this);
  }

  @Override
  public long structSize() {
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      if(nested(e)) return -1;
    }
    return el >> 1;
  }

  /**
   * Returns if the specified expression refers to a nested map.
   * @param e offset to key/value pair
   * @return result of check
   */
  private boolean nested(final int e) {
    return exprs[e + 1] == Empty.UNDEFINED;
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
    return MAP + " constructor";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token("{ ");
    final int el = exprs.length;
    for(int e = 0; e < el; e += 2) {
      if(e != 0) qs.token(',');
      qs.token(exprs[e]);
      if(exprs[e + 1] != Empty.UNDEFINED) qs.token(':').token(exprs[e + 1]);
    }
    qs.token(" }");
  }
}
