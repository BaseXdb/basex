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
 * Changes the value of a record.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RecordSet extends Arr {
  /** Type of processed record. */
  private final RecordType type;
  /** Index of record entry (starting with 1). */
  private final int index;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param record record expression
   * @param index index of record entry (starting with 1)
   * @param value value to assign
   */
  public RecordSet(final InputInfo info, final Expr record, final int index, final Expr value) {
    super(info, Types.MAP_O, record, value);
    this.type = (RecordType) record.seqType().type;
    this.index = index;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr value = exprs[1];
    Type tp = null;
    final SeqType vt = value.seqType(), ft = type.fields().value(index).seqType();
    if(vt.instanceOf(ft)) {
      // structure does not change (new value has same type): propagate the de-sealed record type
      tp = type.open();
    } else {
      // otherwise, derive new record type
      tp = type.copy(null, type.fields().key(index), vt.union(ft), cc);
    }
    if(tp != null) exprType.assign(tp);
    return values(false, cc) ? cc.preEval(this) : this;
  }

  @Override
  public Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    // map:put(RECORD, FIELD, VALUE) coerce to T → (RECORD coerce to T) +:= map:entry(FIELD, VALUE)
    if(tc.seqType().type instanceof final RecordType rt && rt.strict() &&
        rt.fields().contains(type.fields().key(index)) &&
        (exprs[0] instanceof RecordSet || type.instanceOf(rt))) {
      final Expr rec = tc.check(exprs[0], cc);
      final Expr entry = cc.function(_MAP_ENTRY, info, Str.get(type.fields().key(index)), exprs[1]);
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
    return copyType(new RecordSet(info, exprs[0].copy(cc, vm), index, exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final RecordSet rs && index == rs.index &&
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
