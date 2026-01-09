package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Returns the value of a record.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RecordGet extends Single {
  /** Record type. */
  private final RecordType type;
  /** Index of record entry (starting with 1). */
  private final int index;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param record record
   * @param index index of record entry (starting with 1)
   */
  public RecordGet(final InputInfo info, final Expr record, final int index) {
    super(info, record, Types.ITEM_ZM);
    this.type = (RecordType) record.seqType().type;
    this.index = index;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprType.assign(type.fields().value(index).seqType());
    return expr instanceof Value ? cc.preEval(this) : this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return toMap(expr, qc).valueAt(index - 1);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return new RecordGet(info, expr.copy(cc, vm), index);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final RecordGet rg && index == rg.index &&
        super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, AT, index), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(_MAP_GET.definition().name.prefixId());
    qs.params(new Expr[] { expr, Str.get(type.fields().key(index)) });
  }
}
