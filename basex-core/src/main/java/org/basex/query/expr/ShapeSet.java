package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Changes a field value of a map with a known shape.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ShapeSet extends Arr {
  /** Type of the processed map. */
  private final ShapeType type;
  /** Index of the field (starting with 1). */
  private final int index;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param map map expression
   * @param index index of the field (starting with 1)
   * @param value value to assign
   */
  public ShapeSet(final InputInfo info, final Expr map, final int index, final Expr value) {
    super(info, Types.MAP_O, map, value);
    this.type = (ShapeType) map.seqType().type;
    this.index = index;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final SeqType vt = exprs[1].seqType(), ft = type.fields().value(index).seqType();
    // the field set is preserved (without the record annotation) if the value matches the field
    exprType.assign(vt.instanceOf(ft) ? type.shape() : type.union(type.keyType(), vt));
    return values(false, cc) ? cc.preEval(this) : this;
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    // map:put(RECORD, FIELD, VALUE) coerce to T → (RECORD coerce to T) +:= map:entry(FIELD, VALUE)
    final byte[] key = type.fields().key(index);
    if(tc.seqType().type instanceof final ShapeType sh && sh.strict() &&
        sh.fields().contains(key) &&
        // the operand keeps its own type, which must not constrain the field any further
        type.fields().value(index).seqType().eq(sh.fields().get(key).seqType()) &&
        (exprs[0] instanceof ShapeSet || type.instanceOf(sh))) {
      final Expr rec = tc.check(exprs[0], cc);
      final Expr entry = cc.function(_MAP_ENTRY, info, Str.get(key), exprs[1]);
      return new RecordPut(info, rec != null ? rec : exprs[0], entry).optimize(cc);
    }
    return null;
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return toMap(exprs[0], qc).putAt(index - 1, exprs[1].value(qc));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new ShapeSet(info, exprs[0].copy(cc, vm), index, exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final ShapeSet rs && index == rs.index &&
        super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, AT, index), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(_MAP_PUT.definition().name.prefixId());
    qs.params(new Expr[] { exprs[0], Str.get(type.fields().key(index)), exprs[1] });
  }
}
