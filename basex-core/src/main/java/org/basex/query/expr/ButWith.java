package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.constr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * {@code but with} expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ButWith extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param record record expression (left operand)
   * @param update update expression (right operand)
   */
  public ButWith(final InputInfo info, final Expr record, final Expr update) {
    super(info, Types.RECORD_O, record, update);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // RECORD but with { 'a': A } but with { 'b': B } → RECORD but with { 'a': A, 'b': B }
    // (the intermediate record must not be subject to an integrity constraint)
    if(exprs[0] instanceof final ButWith bw && bw.seqType().type instanceof final RecordType rt &&
        !rt.any() && !constrained(rt, cc.qc) && disjoint(bw.exprs[1], exprs[1])) {
      final Expr update = new CMap(info, new Expr[] { bw.exprs[1], Empty.UNDEFINED, exprs[1],
        Empty.UNDEFINED }).optimize(cc);
      exprs = new Expr[] { bw.exprs[0], update };
      cc.info(OPTMERGE_X, this);
    }
    // local:p(A, B) but with { 'b': C } → local:p(A, C)
    final Expr constr = constructor(cc);
    if(constr != null) return cc.replaceWith(this, constr);

    // the result carries the label of the left operand; if no label is known statically, a record
    // may still turn up at runtime, so the check is left to the evaluation step
    final SeqType st = exprs[0].seqType();
    if(st.type instanceof RecordType) {
      // RECORD but with { } → RECORD
      if(st.one() && exprs[1] instanceof final XQMap map && map.structSize() == 0) {
        return cc.replaceWith(this, exprs[0]);
      }
      exprType.assign(st.with(Occ.EXACTLY_ONE));
    }
    return values(false, cc) ? cc.preEval(this) : this;
  }

  /**
   * Checks if records of the specified or a derived type may have integrity constraints.
   * @param rt record type
   * @param qc query context
   * @return result of check
   */
  private static boolean constrained(final RecordType rt, final QueryContext qc) {
    if(rt.constrained()) return true;
    for(final TypeDecl td : qc.typeDecls) {
      if(td.seqType.type instanceof final RecordType dt && dt.constrained() &&
          RecordType.derives(dt, rt)) return true;
    }
    return false;
  }

  /**
   * Returns a record constructor call with the arguments replaced by the update values.
   * @param cc compilation context
   * @return constructor call, or {@code null} if the expression cannot be rewritten
   * @throws QueryException query exception
   */
  private Expr constructor(final CompileContext cc) throws QueryException {
    // the replaced arguments are dropped, and the intermediate record is not checked
    if(!(exprs[0] instanceof final ShapeConstructor sc) ||
        !(sc.seqType().type instanceof final RecordType rt) || rt.any() || rt.constrained()) {
      return null;
    }
    final TokenObjectMap<Expr> entries = entries(exprs[1]);
    if(entries == null || sc.has(Flag.NDT)) return null;

    // supplied arguments, overwritten by the update values
    final TokenObjectMap<Expr> supplied = sc.entries();
    final int es = entries.size();
    for(int e = 1; e <= es; e++) supplied.put(entries.key(e), entries.value(e));
    return ShapeConstructor.record(info, rt, supplied, cc);
  }

  /**
   * Checks if two updates have statically known, disjoint string keys.
   * @param update1 first update
   * @param update2 second update
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean disjoint(final Expr update1, final Expr update2) throws QueryException {
    final TokenObjectMap<Expr> entries1 = entries(update1), entries2 = entries(update2);
    if(entries1 == null || entries2 == null) return false;
    for(final byte[] key : entries2) {
      if(entries1.contains(key)) return false;
    }
    return true;
  }

  /**
   * Returns the statically known string keys and value expressions of an update.
   * @param update update expression
   * @return entries, or {@code null} if the keys are unknown or if a key is no string
   * @throws QueryException query exception
   */
  private TokenObjectMap<Expr> entries(final Expr update) throws QueryException {
    // anonymous shapes only: the arguments of a record constructor are coerced to its fields
    if(update instanceof final ShapeConstructor sc) {
      return sc.seqType().type instanceof RecordType ? null : sc.entries();
    }
    final TokenObjectMap<Expr> entries = new TokenObjectMap<>();
    if(update instanceof final XQMap map) {
      for(final Item key : map.keys()) {
        if(key.type != BasicType.STRING) return null;
        entries.put(key.string(info), map.get(key));
      }
    } else if(Function._MAP_ENTRY.is(update)) {
      if(!(update.arg(0) instanceof final Str key) || key.type != BasicType.STRING) return null;
      entries.put(key.string(), update.arg(1));
    } else {
      return null;
    }
    return entries;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQMap record = toMap(exprs[0], qc);
    if(!(record.type instanceof final RecordType rt)) {
      throw typeError(record, Types.RECORD, info);
    }
    final XQMap update = toMap(exprs[1], qc);
    if(update.structSize() == 0) return record;

    // compact record layout: access values by position, otherwise by key
    final TokenObjectMap<ShapeField> fields = rt.fields();
    final int fs = fields.size();
    final Value[] values = new Value[fs];
    final boolean sameOrder = record instanceof XQShapeMap;
    for(int f = 0; f < fs; f++) {
      values[f] = sameOrder ? record.valueAt(f) : record.getOrNull(rt.key(f + 1));
    }
    update.forEach((key, value) -> {
      final int i = rt.index(key);
      // fixed fields cannot be updated
      if(i == 0 || fields.value(i).isFixed()) throw typeError(update, rt, info);
      values[i - 1] = rt.coerce(i, value, qc, info, null);
    });
    return rt.build(values, true, qc, info);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new ButWith(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof ButWith && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.operands(exprs, " but with ");
  }
}
