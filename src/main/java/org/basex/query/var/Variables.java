package org.basex.query.var;

import java.util.*;
import java.util.Map.Entry;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Container of global variables of a module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Variables extends ExprInfo implements Iterable<StaticVar> {
  /** The variables. */
  private final HashMap<QNm, VarEntry> vars = new HashMap<QNm, VarEntry>();

  /**
   * Looks for a variable with the given name in the defined variables.
   * @param name variable name
   * @return declaration if found, {@null} otherwise
   */
  public StaticVar get(final QNm name) {
    final VarEntry e = vars.get(name);
    return e == null ? null : e.var;
  }

  /**
   * Declares a new static variable.
   * @param nm variable name
   * @param t type
   * @param a annotations
   * @param e bound expression, possibly {@code null}
   * @param ext {@code external} flag
   * @param sctx static context
   * @param scp variable scope
   * @param ii input info
   * @throws QueryException query exception
   */
  public void declare(final QNm nm, final SeqType t, final Ann a, final Expr e,
      final boolean ext, final StaticContext sctx, final VarScope scp, final InputInfo ii)
          throws QueryException {
    final StaticVar var = new StaticVar(sctx, scp, ii, a, nm, t, e, ext);
    final VarEntry ve = vars.get(nm);
    if(ve != null) ve.setVar(var);
    else vars.put(nm, new VarEntry(var));
  }

  /**
   * Checks if none of the variables contains an updating expression.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final VarEntry e : vars.values()) e.var.checkUp();
  }

  /**
   * Checks if all variables were declared and are visible to all their references.
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void check(final QueryContext ctx) throws QueryException {
    for(final Entry<QNm, VarEntry> e : vars.entrySet()) {
      final QNm name = e.getKey();
      final VarEntry ve = e.getValue();
      if(ve.var == null) {
        if(name.uri().length != 0) throw Err.VARUNDEF.thrw(ve.refs[0].info, ve.refs[0]);
        ve.setVar(new StaticVar(ctx, name, ve.refs[0].info));
      }
    }
  }

  /**
   * Checks if no static variables are declared.
   * @return {@code true} if no static variables are used, {@code false} otherwise
   */
  public boolean isEmpty() {
    return vars.isEmpty();
  }

  @Override
  public void plan(final FElem plan) {
    if(vars.isEmpty()) return;
    final FElem e = planElem();
    for(final VarEntry v : vars.values()) v.var.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final VarEntry v : vars.values()) v.var.fullDesc(sb);
    return sb.toString();
  }

  /**
   * Checks if a variable with the given name was already declared.
   * @param nm variable name
   * @return result of check
   */
  public boolean declared(final QNm nm) {
    final VarEntry entry = vars.get(nm);
    return entry != null && entry.var != null;
  }

  /**
   * returns a new reference to the (possibly not yet declared) variable
   * with the given name.
   * @param ii input info
   * @param nm variable name
   * @param sctx static context
   * @return reference
   * @throws QueryException if the variable is not visible
   */
  public StaticVarRef newRef(final QNm nm, final StaticContext sctx, final InputInfo ii)
      throws QueryException {
    final StaticVarRef ref = new StaticVarRef(ii, nm, sctx);
    final VarEntry e = vars.get(nm), entry = e != null ? e : new VarEntry(null);
    if(e == null) vars.put(nm, entry);
    entry.addRef(ref);
    return ref;
  }

  /**
   * Binds all external variables.
   * @param ctx query context
   * @param bindings variable bindings
   * @throws QueryException query exception
   */
  public void bindExternal(final QueryContext ctx, final HashMap<QNm, Expr> bindings)
      throws QueryException {
    for(final Entry<QNm, Expr> e : bindings.entrySet()) {
      final VarEntry ve = vars.get(e.getKey());
      if(ve != null) ve.var.bind(e.getValue(), ctx);
    }
  }

  @Override
  public Iterator<StaticVar> iterator() {
    final Iterator<Entry<QNm, VarEntry>> iter = vars.entrySet().iterator();
    return new Iterator<StaticVar>() {
      @Override
      public void remove() { }

      @Override
      public StaticVar next() {
        return iter.next().getValue().var;
      }

      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }
    };
  }

  /** Entry for static variables and their references. */
  private class VarEntry {
    /** The static variable. */
    StaticVar var;
    /** Variable references. */
    StaticVarRef[] refs = { };

    /**
     * Constructor.
     * @param vr variable, possibly {@code null}
     */
    VarEntry(final StaticVar vr) {
      var = vr;
    }

    /**
     * Sets the variable for existing references.
     * @param vr variable to set
     * @throws QueryException if the variable was already declared
     */
    void setVar(final StaticVar vr) throws QueryException {
      if(var != null) throw Err.VARDUPL.thrw(vr.info, var);
      var = vr;
      for(final StaticVarRef ref : refs) ref.init(var);
    }

    /**
     * Adds a reference to this variable.
     * @param ref reference to add
     * @throws QueryException query exception
     */
    void addRef(final StaticVarRef ref) throws QueryException {
      refs = Array.add(refs, ref);
      if(var != null) ref.init(var);
    }
  }
}
