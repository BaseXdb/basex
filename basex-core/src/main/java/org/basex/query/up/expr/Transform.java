package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Transform expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class Transform extends Arr {
  /** Variable bindings created by copy clause. */
  private final Let[] copies;

  /**
   * Constructor.
   * @param info input info
   * @param copies copy expressions
   * @param mod modify expression
   * @param rtrn return expression
   */
  public Transform(final InputInfo info, final Let[] copies, final Expr mod, final Expr rtrn) {
    super(info, SeqType.ITEM_ZM, mod, rtrn);
    this.copies = copies;
  }

  @Override
  public void checkUp() throws QueryException {
    for(final Let copy : copies) copy.checkUp();
    final Expr modify = exprs[0];
    modify.checkUp();
    if(!modify.vacuous() && !modify.has(Flag.UPD)) throw UPMODIFY.get(info);
    exprs[1].checkUp();
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    for(final Let copy : copies) copy.expr = copy.expr.compile(cc);
    return super.compile(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    for(final Let copy : copies) {
      copy.exprType.assign(copy.expr);
    }
    // name of node may change
    final SeqType st = exprs[1].seqType();
    exprType.assign(st.type, st.occ);
    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Updates tmp = qc.updates(), updates = new Updates(true);
    qc.updates = updates;

    try {
      for(final Let copy : copies) {
        final Iter iter = copy.expr.iter(qc);
        Item item = iter.next();
        if(!(item instanceof ANode)) throw UPSINGLE_X_X.get(
            copy.info, copy.var.name, item == null ? Empty.VALUE : item);
        final Item i2 = iter.next();
        if(i2 != null)
          throw UPSINGLE_X_X.get(copy.info, copy.var.name, ValueBuilder.concat(item, i2, qc));

        // create main memory copy of node
        item = ((ANode) item).copy(qc);
        // add resulting node to variable
        qc.set(copy.var, item);
        updates.addData(item.data());
      }
      if(!exprs[0].value(qc).isEmpty()) throw UPMODIFY.get(info);

      updates.prepare(qc);
      updates.apply(qc);
    } finally {
      qc.updates = tmp;
    }
    return exprs[1].value(qc);
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final Let copy : copies) {
      if(copy.has(flags)) return true;
    }
    if(Flag.CNS.in(flags) || Flag.UPD.in(flags) && exprs[1].has(Flag.UPD)) return true;
    final Flag[] flgs = Flag.UPD.remove(flags);
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    for(final Let copy : copies) {
      if(!copy.inlineable(ic)) return false;
    }
    return super.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, copies).plus(super.count(var));
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    final boolean a = ic.inline(copies), b = ic.inline(exprs);
    return a || b ? optimize(ic.cc) : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Transform(info, copyAll(cc, vm, copies), exprs[0].copy(cc, vm),
        exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, copies) && super.accept(visitor);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Let copy : copies) size += copy.exprSize();
    for(final Expr expr : exprs) size += expr.exprSize();
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Transform && Array.equals(copies, ((Transform) obj).copies)
        && super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), copies, exprs);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(COPY);
    boolean more = false;
    for(final Let copy : copies) {
      if(more) qs.token(SEP);
      else more = true;
      qs.token(copy.var.id()).token(ASSIGN).token(copy.expr);
    }
    qs.token(MODIFY).token(exprs[0]).token(RETURN).token(exprs[1]);
  }
}
