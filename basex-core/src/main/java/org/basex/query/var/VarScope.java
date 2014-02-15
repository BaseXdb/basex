package org.basex.query.var;

import java.util.*;
import java.util.Map.Entry;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * The scope of variables, either the query, a use-defined or an inline function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class VarScope {
  /** Static context. */
  private final StaticContext sc;

  /** Local variables in this scope. */
  private final ArrayList<Var> vars = new ArrayList<Var>();
  /** This scope's closure. */
  private final Map<Var, Expr> closure = new HashMap<Var, Expr>();

  /**
   * Constructor for a top-level module.
   * @param sctx static context
   */
  public VarScope(final StaticContext sctx) {
    sc = sctx;
  }

  /**
   * Adds a variable to this scope.
   * @param var variable to be added
   * @return the variable (for convenience)
   */
  private Var add(final Var var) {
    var.slot = vars.size();
    vars.add(var);
    return var;
  }

  /**
   * Creates a new local variable in this scope.
   * @param ctx query context
   * @param name variable name
   * @param typ type of the variable
   * @param param function parameter flag
   * @return the variable
   */
  public Var newLocal(final QueryContext ctx, final QNm name, final SeqType typ,
      final boolean param) {
    return add(new Var(ctx, sc, name, typ, param));
  }

  /**
   * Creates a new copy of the given variable in this scope.
   * @param ctx query context
   * @param var variable to copy
   * @return the variable
   */
  public Var newCopyOf(final QueryContext ctx, final Var var) {
    return add(new Var(ctx, sc, var));
  }

  /**
   * Get the closure of this scope.
   * @return mapping from non-local to local variables
   */
  public Map<Var, Expr> closure() {
    return closure;
  }

  /**
   * Enters this scope.
   * @param ctx query context
   * @return old frame pointer
   */
  public int enter(final QueryContext ctx) {
    return ctx.stack.enterFrame(vars.size());
  }

  /**
   * Exits this scope.
   * @param ctx query context
   * @param fp frame pointer
   */
  public void exit(final QueryContext ctx, final int fp) {
    ctx.stack.exitFrame(fp);
  }

  /**
   * Deletes all unused variables from this scope and assigns stack slots.
   * This method should be run after compiling the scope.
   * @param expr the scope
   */
  public void cleanUp(final Scope expr) {
    final BitSet declared = new BitSet();
    final BitSet used = new BitSet();
    expr.visit(new ASTVisitor() {
      @Override
      public boolean declared(final Var var) {
        declared.set(var.id);
        return true;
      }

      @Override
      public boolean used(final VarRef ref) {
        used.set(ref.var.id);
        return true;
      }
    });

    // purge all unused variables
    final Iterator<Var> iter = vars.iterator();
    while(iter.hasNext()) {
      final Var v = iter.next();
      if(!declared.get(v.id)) {
        v.slot = -1;
        iter.remove();
      }
    }

    // remove unused entries from the closure
    final Iterator<Entry<Var, Expr>> cls = closure.entrySet().iterator();
    while(cls.hasNext()) {
      final Entry<Var, Expr> e = cls.next();
      final Var v = e.getKey();
      if(!used.get(v.id)) {
        cls.remove();
        v.slot = -1;
        vars.remove(v);
      }
    }

    // assign new stack slots
    for(int i = vars.size(); --i >= 0;) vars.get(i).slot = i;
  }

  @Override
  public String toString() {
    return Util.className(this) + vars.toString();
  }

  /**
   * Stack-frame size needed for this scope.
   * @return stack-frame size
   */
  public int stackSize() {
    return vars.size();
  }

  /**
   * Copies this VarScope.
   * @param ctx query context
   * @param scp new parent scope
   * @param vs variable mapping
   * @return copied scope
   */
  public VarScope copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    final VarScope cscp = new VarScope(sc);
    for(final Var v : vars) vs.put(v.id, cscp.newCopyOf(ctx, v));
    for(final Entry<Var, Expr> e : closure.entrySet()) {
      final Var v = vs.get(e.getKey().id);
      final Expr ex = e.getValue().copy(ctx, scp, vs);
      cscp.closure.put(v, ex);
    }
    return cscp;
  }
}
