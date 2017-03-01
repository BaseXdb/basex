package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Transform expression.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class TransformWith extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param source source expression
   * @param modify modify expression
   */
  public TransformWith(final InputInfo info, final Expr source, final Expr modify) {
    super(info, source, modify);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final QueryFocus focus = cc.qc.focus;
    final Value v = focus.value;
    try {
      focus.value = null;
      return super.compile(cc);
    } finally {
      focus.value = v;
    }
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(exprs[0]);
    final Expr modify = exprs[1];
    modify.checkUp();
    if(!modify.isVacuous() && !modify.has(Flag.UPD)) throw UPMODIFY.get(info);
  }

  @Override
  public BasicIter<?> iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Updates upd = qc.updates();
    final QueryFocus qf = qc.focus;
    final Value cv = qf.value;

    final ValueBuilder vb = new ValueBuilder();
    try {
      final Iter ir = qc.iter(exprs[0]);
      for(Item it; (it = ir.next()) != null;) {
        if(!(it instanceof ANode)) throw UPSOURCE_X.get(info, it);

        // copy node to main memory data instance
        it = ((ANode) it).dbNodeCopy(qc.context.options);
        // set resulting node as context
        qf.value = it;

        final Updates updates = new Updates(true);
        qc.updates = updates;
        updates.addData(it.data());

        final Value v = qc.value(exprs[1]);
        if(!v.isEmpty()) throw BASX_UPMODIFY.get(info);

        updates.prepare(qc);
        updates.apply(qc);
        vb.add(it);
      }
    } finally {
      qc.updates = upd;
      qf.value = cv;
    }
    return vb.value();
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.UPD ? exprs[0].has(flag) : super.has(flag);
    //return flag != Flag.UPD && super.has(flag);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new TransformWith(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm));
  }

  @Override
  public String toString() {
    return toString(' ' + QueryText.UPDATE + ' ');
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr expr : exprs) sz += expr.exprSize();
    return sz;
  }
}
