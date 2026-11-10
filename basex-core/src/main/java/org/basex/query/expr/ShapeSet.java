package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.value.*;
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
    // the field set is preserved
    exprType.assign(cc.qc.shared.shape(vt.instanceOf(ft) ? type :
      type.put(type.fields().key(index), vt)));
    return values(false, cc) ? cc.preEval(this) : this;
  }

  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final XQMap map = toMap(exprs[0], qc);
    final Value value = exprs[1].value(qc);
    return type.fieldAt(map, index) ? map.putAt(index - 1, value) :
      map.put(type.key(index), value);
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
