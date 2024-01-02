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
 * @author BaseX Team 2005-24, BSD License
 * @author Lukas Kircher
 */
public final class Transform extends Copy {
  /** Variable bindings created by copy clause. */
  private final Let[] copies;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param copies copy expressions
   * @param modify modify expression
   * @param rtrn return expression
   */
  public Transform(final InputInfo info, final Let[] copies, final Expr modify, final Expr rtrn) {
    super(info, SeqType.ITEM_ZM, modify, rtrn);
    this.copies = copies;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    // type of let variable must not match expression type (name of node may change)
    for(final Let copy : copies) copy.exprType.assign(copy.expr);
    return super.compile(cc);
  }

  @Override
  public void checkUp() throws QueryException {
    for(final Let copy : copies) copy.checkUp();
    super.checkUp();
    result().checkUp();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Updates tmp = qc.updates(), updates = new Updates(true);
    qc.updates = updates;

    try {
      for(final Let copy : copies) {
        final Iter iter = copy.expr.iter(qc);
        Item item = iter.next();
        Value error = null;
        if(item == null) {
          error = Empty.VALUE;
        } else if(!(item instanceof ANode)) {
          error = item;
        } else {
          final Item item2 = iter.next();
          if(item2 != null) error = ValueBuilder.concat(item, item2, qc);
        }
        if(error != null) throw UPSINGLE_X_X.get(copy.info(), copy.var.name, error);

        // create main memory copy of node
        item = ((ANode) item).copy(qc);
        // add resulting node to variable
        qc.set(copy.var, item);
        updates.addData(item.data());
      }

      if(!modify().value(qc).isEmpty()) throw UPMODIFY.get(info);
      updates.prepare(qc);
      updates.apply(qc);
    } finally {
      qc.updates = tmp;
    }
    return result().value(qc);
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final Let copy : copies) {
      if(copy.has(flags)) return true;
    }
    return Flag.CNS.in(flags) ||
        Flag.UPD.in(flags) && result().has(Flag.UPD) ||
        super.has(Flag.UPD.remove(flags));
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
    return copyType(new Transform(info, copyAll(cc, vm, copies), modify().copy(cc, vm),
        result().copy(cc, vm)));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, copies) && super.accept(visitor);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Let copy : copies) size += copy.exprSize();
    return size + super.exprSize();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Transform && Array.equals(copies, ((Transform) obj).copies)
        && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), copies, exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(COPY);
    boolean more = false;
    for(final Let copy : copies) {
      if(more) qs.token(SEP);
      else more = true;
      qs.token(copy.var.id()).token(":=").token(copy.expr);
    }
    qs.token(MODIFY).token(modify()).token(RETURN).token(result());
  }
}
