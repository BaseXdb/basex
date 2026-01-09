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
  /** Record type. */
  private final RecordType type;
  /** Index of record entry (starting with 1). */
  private final int index;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param record record
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
    final Expr map = exprs[0], value = exprs[1];
    Type tp = null;
    final RecordField field = type.fields().value(index);
    if(value.seqType().instanceOf(field.seqType())) {
      tp = type;
    } else if(map.seqType().type instanceof final MapType mt) {
      tp = type.union(mt.keyType(), value.seqType());
    }
    if(tp != null) exprType.assign(tp);
    return values(false, cc) ? cc.preEval(this) : this;
  }

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return toMap(exprs[0], qc).putAt(index - 1, exprs[1].value(qc));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return new RecordSet(info, exprs[0].copy(cc, vm), index, exprs[1].copy(cc, vm));
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
