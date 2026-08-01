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
 * Returns a field value of a map with a known shape.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ShapeGet extends Single {
  /** Type of the processed map. */
  private final ShapeType type;
  /** Index of the field (starting with 1). */
  private final int index;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param map map expression
   * @param index index of the field (starting with 1)
   */
  public ShapeGet(final InputInfo info, final Expr map, final int index) {
    this(info, map, index, (ShapeType) map.seqType().type);
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param map map expression
   * @param index index of the field (starting with 1)
   * @param type type of the processed map
   */
  private ShapeGet(final InputInfo info, final Expr map, final int index,
      final ShapeType type) {
    super(info, map, Types.ITEM_ZM);
    this.type = type;
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
    return copyType(new ShapeGet(info, expr.copy(cc, vm), index, type));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final ShapeGet rg && index == rg.index &&
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
