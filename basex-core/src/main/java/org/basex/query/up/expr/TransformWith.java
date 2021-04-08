package org.basex.query.up.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Transform expression.
 *
 * @author BaseX Team 2005-21, BSD License
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
    super(info, SeqType.NODE_ZM, source, modify);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return cc.get(new Dummy(exprs[0].seqType().with(Occ.EXACTLY_ONE), null),
        () -> super.compile(cc));
  }

  @Override
  public Expr optimize(final CompileContext cc) {
    // name of node may change
    final SeqType st = exprs[0].seqType();
    exprType.assign(st.type, st.occ);
    return this;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(exprs[0]);
    final Expr modify = exprs[1];
    modify.checkUp();
    if(!modify.vacuous() && !modify.has(Flag.UPD)) throw UPMODIFY.get(info);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);
    final Updates tmp = qc.updates();
    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qc.focus = qf;

    final ValueBuilder vb = new ValueBuilder(qc);
    try {
      for(final Item item : value) {
        if(!(item instanceof ANode)) throw UPSOURCE_X.get(info, item);

        // create main memory copy of node
        final ANode node = ((ANode) item).copy(qc);
        // set resulting node as context
        qf.value = node;

        final Updates updates = new Updates(true);
        qc.updates = updates;
        updates.addData(node.data());

        if(!exprs[1].value(qc).isEmpty()) throw UPMODIFY.get(info);

        updates.prepare(qc);
        updates.apply(qc);
        vb.add(node);
        qf.pos++;
      }
    } finally {
      qc.updates = tmp;
      qc.focus = focus;
    }
    return vb.value(this);
  }

  @Override
  public boolean has(final Flag... flags) {
    if(Flag.CNS.in(flags)) return true;

    // Context dependency, positional access: only check first expression.
    // Example: . update { delete node a }
    if(Flag.CTX.in(flags) && exprs[0].has(Flag.CTX)) return true;
    if(Flag.POS.in(flags) && exprs[0].has(Flag.POS)) return true;

    final Flag[] flgs = Flag.UPD.remove(Flag.POS.remove(Flag.CTX.remove(flags)));
    return flgs.length != 0 && super.has(flgs);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return exprs[0].inlineable(ic) && !(ic.expr instanceof ContextValue && exprs[1].uses(ic.var));
  }

  @Override
  public VarUsage count(final Var var) {
    // context reference check: only consider source expression
    return var == null ? exprs[0].count(var) : super.count(var);
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    final Expr inlined = exprs[0].inline(ic);
    boolean changed = inlined != null;
    if(changed) exprs[0] = inlined;

    // do not inline context reference in updating expressions
    changed |= ic.var != null && ic.cc.ok(exprs[0], () -> {
      final Expr expr = exprs[1].inline(ic);
      if(expr == null) return false;
      exprs[1] = expr;
      return true;
    });
    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new TransformWith(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof TransformWith && super.equals(obj);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(exprs[0]).token(UPDATE).brace(exprs[1]);
  }
}
