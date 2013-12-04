package org.basex.query.var;

import static org.basex.query.util.Err.*;

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
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class Variables extends ExprInfo implements Iterable<StaticVar> {
  /** The variables. */
  private final HashMap<QNm, VarEntry> vars = new HashMap<QNm, VarEntry>();

  /**
   * Declares a new static variable.
   * @param nm variable name
   * @param t type
   * @param a annotations
   * @param e bound expression, possibly {@code null}
   * @param ext {@code external} flag
   * @param sctx static context
   * @param scp variable scope
   * @param xqdoc current xqdoc cache
   * @param ii input info
   * @return static variable reference
   * @throws QueryException query exception
   */
  public StaticVar declare(final QNm nm, final SeqType t, final Ann a, final Expr e,
      final boolean ext, final StaticContext sctx, final VarScope scp, final String xqdoc,
      final InputInfo ii) throws QueryException {

    final StaticVar var = new StaticVar(sctx, scp, a, nm, t, e, ext, xqdoc, ii);
    final VarEntry ve = vars.get(nm);
    if(ve != null) ve.setVar(var);
    else vars.put(nm, new VarEntry(var));
    return var;
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
   * @param sc static context
   * @throws QueryException query exception
   */
  public void check(final StaticContext sc) throws QueryException {
    for(final Entry<QNm, VarEntry> e : vars.entrySet()) {
      final QNm name = e.getKey();
      final VarEntry ve = e.getValue();
      if(ve.var == null) {
        if(name.uri().length != 0) throw VARUNDEF.get(ve.refs[0].info, ve.refs[0]);
        ve.setVar(new StaticVar(sc, name, ve.refs[0].info));
      }
    }
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
    for(final VarEntry v : vars.values()) sb.append(v.var.fullDesc(sb));
    return sb.toString();
  }

  /**
   * Checks if a variable with the given name exists.
   * @param nm variable name
   * @return result of check
   */
  public boolean exists(final QNm nm) {
    return vars.get(nm) != null;
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
      public boolean hasNext() {
        return iter.hasNext();
      }

      @Override
      public StaticVar next() {
        return iter.next().getValue().var;
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }

  /** Entry for static variables and their references. */
  private static class VarEntry {
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
      if(var != null) throw VARDUPL.get(vr.info, var);
      var = vr;
      for(final StaticVarRef ref : refs) ref.init(var);
    }

    /**
     * Adds a reference to this variable.
     * @param ref reference to add
     * @throws QueryException query exception
     */
    void addRef(final StaticVarRef ref) throws QueryException {
      refs = Array.add(refs, new StaticVarRef[refs.length + 1], ref);
      if(var != null) ref.init(var);
    }
  }
}
