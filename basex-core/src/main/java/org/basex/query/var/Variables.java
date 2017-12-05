package org.basex.query.var;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Container of global variables of a module.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class Variables extends ExprInfo implements Iterable<StaticVar> {
  /** The variables. */
  private final HashMap<QNm, VarEntry> vars = new HashMap<>();

  /**
   * Declares a new static variable.
   * @param var variable
   * @param anns annotations
   * @param expr bound expression, possibly {@code null}
   * @param ext {@code external} flag
   * @param doc xqdoc string
   * @param vs variable scope
   * @return static variable reference
   * @throws QueryException query exception
   */
  public StaticVar declare(final Var var, final AnnList anns, final Expr expr, final boolean ext,
      final String doc, final VarScope vs) throws QueryException {
    final StaticVar sv = new StaticVar(vs, anns, var, expr, ext, doc);
    final VarEntry ve = vars.get(var.name);
    if(ve != null) ve.setVar(sv);
    else vars.put(var.name, new VarEntry(sv));
    return sv;
  }

  /**
   * Ensures that none of the variable expressions is updating.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final VarEntry e : vars.values()) e.var.checkUp();
  }

  /**
   * Checks if all variables were declared and are visible to all their references.
   * @throws QueryException query exception
   */
  public void check() throws QueryException {
    for(final VarEntry ve : vars.values()) {
      if(ve.var == null) {
        final StaticVarRef vr = ve.refs.get(0);
        throw VARUNDEF_X.get(vr.info, vr);
      }
    }
  }

  /**
   * Returns a new reference to the (possibly not yet declared) variable with the given name.
   * @param info input info
   * @param name variable name
   * @param sc static context
   * @return reference
   * @throws QueryException if the variable is not visible
   */
  public StaticVarRef newRef(final QNm name, final StaticContext sc, final InputInfo info)
      throws QueryException {

    final StaticVarRef ref = new StaticVarRef(info, name, sc);
    final VarEntry e = vars.get(name), entry = e != null ? e : new VarEntry(null);
    if(e == null) vars.put(name, entry);
    entry.addRef(ref);
    return ref;
  }

  /**
   * Binds all external variables.
   * @param qc query context
   * @param bindings variable bindings
   * @throws QueryException query exception
   */
  public void bindExternal(final QueryContext qc, final HashMap<QNm, Value> bindings)
      throws QueryException {

    for(final Entry<QNm, Value> entry : bindings.entrySet()) {
      final VarEntry ve = vars.get(entry.getKey());
      if(ve != null) ve.var.bind(entry.getValue(), qc);
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
    for(final VarEntry v : vars.values()) sb.append(v.var);
    return sb.toString();
  }

  /** Entry for static variables and their references. */
  private static class VarEntry {
    /** The static variable. */
    StaticVar var;
    /** Variable references. */
    final ArrayList<StaticVarRef> refs = new ArrayList<>(1);

    /**
     * Constructor.
     * @param var variable, possibly {@code null}
     */
    VarEntry(final StaticVar var) {
      this.var = var;
    }

    /**
     * Sets the variable for existing references.
     * @param vr variable to set
     * @throws QueryException if the variable was already declared
     */
    void setVar(final StaticVar vr) throws QueryException {
      if(var != null) throw VARDUPL_X.get(vr.info, var.name.string());
      var = vr;
      for(final StaticVarRef ref : refs) ref.init(var);
    }

    /**
     * Adds a reference to this variable.
     * @param ref reference to add
     * @throws QueryException query exception
     */
    void addRef(final StaticVarRef ref) throws QueryException {
      refs.add(ref);
      if(var != null) ref.init(var);
    }
  }
}
