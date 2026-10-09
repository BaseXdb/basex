package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.constr.*;
import org.basex.query.func.*;
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
    if(exprs[0] instanceof final ButWith bw && disjoint(bw.exprs[1], exprs[1])) {
      final Expr update = new CMap(info, new Expr[] { bw.exprs[1], Empty.UNDEFINED, exprs[1],
        Empty.UNDEFINED }).optimize(cc);
      exprs = new Expr[] { bw.exprs[0], update };
      cc.info(OPTMERGE_X, this);
    }
    // the result carries the record type of the left operand; an inferred shape is no record, but
    // a record may still turn up at runtime, so the check is left to the evaluation step
    final SeqType st = exprs[0].seqType();
    if(st.type instanceof final RecordType rt) {
      // RECORD but with { } → RECORD
      if(st.one() && exprs[1] instanceof final XQMap map && map.structSize() == 0) {
        return cc.replaceWith(this, exprs[0]);
      }
      // RECORD but with { 'a': 1, 'b': 2 } → { 'a': 1, 'b': 2 } coerce to RECORD
      if(covered(exprs[1], rt)) {
        return cc.replaceWith(this, new TypeCheck(info, exprs[1], rt.seqType()).optimize(cc));
      }
      exprType.assign(st.with(Occ.EXACTLY_ONE));
    }
    return values(false, cc) ? cc.preEval(this) : this;
  }

  /**
   * Checks whether the update supplies every non-static field of the record type.
   * @param update update expression (right operand)
   * @param rt record type of the left operand
   * @return result of check
   */
  private static boolean covered(final Expr update, final RecordType rt) {
    if(!(update instanceof ShapeConstructor || update instanceof XQMap) ||
        !(update.seqType().type instanceof final ShapeType ush)) return false;
    final TokenObjectMap<ShapeField> ufields = ush.fields(), fields = rt.fields();
    final int fs = fields.size();
    for(int f = 1; f <= fs; f++) {
      if(!fields.value(f).isStatic() && !ufields.contains(fields.key(f))) return false;
    }
    return true;
  }

  /**
   * Checks if two updates have statically known, disjoint string keys.
   * @param update1 first update
   * @param update2 second update
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean disjoint(final Expr update1, final Expr update2) throws QueryException {
    final TokenSet keys1 = keys(update1), keys2 = keys(update2);
    if(keys1 == null || keys2 == null) return false;
    for(final byte[] key : keys2) {
      if(keys1.contains(key)) return false;
    }
    return true;
  }

  /**
   * Returns the statically known string keys of an update.
   * @param update update expression
   * @return keys, or {@code null} if they are unknown or if a key is no string
   * @throws QueryException query exception
   */
  private TokenSet keys(final Expr update) throws QueryException {
    final TokenSet keys = new TokenSet();
    if(update instanceof final XQMap map) {
      for(final Item key : map.keys()) {
        if(key.type != BasicType.STRING) return null;
        keys.add(key.string(info));
      }
    } else if(Function._MAP_ENTRY.is(update)) {
      if(!(update.arg(0) instanceof final Str key) || key.type != BasicType.STRING) return null;
      keys.add(key.string());
    } else if(update instanceof ShapeConstructor &&
        update.seqType().type instanceof final ShapeType sh && !(sh instanceof RecordType) &&
        update.args().length == sh.fields().size()) {
      // anonymous shapes only: the arguments of a record constructor are coerced to its fields
      for(final byte[] key : sh.fields()) keys.add(key);
    } else {
      return null;
    }
    return keys;
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
      final int i = key.type.isStringOrUntyped() ? fields.index(key.string(null)) : 0;
      // static fields cannot be updated
      if(i == 0 || fields.value(i).isStatic()) throw typeError(update, rt, info);
      values[i - 1] = rt.coerce(i, value, qc, info, null);
    });
    return XQMap.get(rt, values);
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
    qs.tokens(exprs, " but with ", true);
  }
}
