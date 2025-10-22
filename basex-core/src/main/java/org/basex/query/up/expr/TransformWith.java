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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TransformWith extends Copy {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param target target expression
   * @param modify modify expression
   */
  public TransformWith(final InputInfo info, final Expr target, final Expr modify) {
    super(info, Types.NODE_ZM, modify, target);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    arg(target(), arg -> arg.compile(cc));
    cc.pushFocus(new Dummy(arg(target()), true), true);
    try {
      arg(update(), arg -> arg.compile(cc));
    } finally {
      cc.removeFocus();
    }
    return optimize(cc);
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(arg(target()));
    super.checkUp();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = arg(target()).value(qc);
    final Updates tmp = qc.updates();
    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qc.focus = qf;

    final ValueBuilder vb = new ValueBuilder(qc);
    try {
      for(final Item item : value) {
        if(!(item instanceof final ANode node)) throw UPSOURCE_X.get(info, item);

        // create main memory copy of node
        final Item copy = node.copy(qc);
        // set resulting node as context
        qf.value = copy;

        final Updates updates = new Updates(true);
        qc.updates = updates;
        updates.addData(copy.data());

        if(!arg(update()).value(qc).isEmpty()) throw UPMODIFY.get(info);
        updates.prepare(qc);
        updates.apply(qc);
        vb.add(copy);
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
    // Context dependency, positional access: only check first expression.
    // Example: . update { delete node a }
    return Flag.CNS.oneOf(flags) ||
           Flag.CTX.oneOf(flags) && arg(target()).has(Flag.CTX) ||
           Flag.POS.oneOf(flags) && arg(target()).has(Flag.POS) ||
           super.has(Flag.remove(flags, Flag.UPD, Flag.POS, Flag.CTX));
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    // do not replace $v with .:  EXPR update { rename node . as $v }
    return arg(target()).inlineable(ic) && !(ic.expr.has(Flag.CTX) && arg(update()).uses(ic.var));
  }

  @Override
  public VarUsage count(final Var var) {
    // context reference check: only consider source expression
    return var == null ? arg(target()).count(null) : super.count(var);
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    final Expr inlined = arg(target()).inline(ic);
    boolean changed = inlined != null;
    if(changed) arg(1, arg -> inlined);

    // do not inline context reference in updating expressions
    changed |= ic.var != null && ic.cc.ok(arg(target()), true, () -> {
      final Expr expr = arg(update()).inline(ic);
      if(expr == null) return false;
      arg(update(), arg -> expr);
      return true;
    });
    return changed ? optimize(ic.cc) : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new TransformWith(info,
        arg(target()).copy(cc, vm), arg(update()).copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof TransformWith && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(arg(target())).token(UPDATE).brace(arg(update()));
  }
}
