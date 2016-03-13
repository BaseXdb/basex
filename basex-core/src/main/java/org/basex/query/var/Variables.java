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
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Container of global variables of a module.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
public final class Variables extends ExprInfo implements Iterable<StaticVar> {
  /** The variables. */
  private final HashMap<QNm, VarEntry> vars = new HashMap<>();

  /**
   * Declares a new static variable.
   * @param nm variable name
   * @param type declared type
   * @param anns annotations
   * @param expr bound expression, possibly {@code null}
   * @param ext {@code external} flag
   * @param sc static context
   * @param scope variable scope
   * @param doc current xqdoc cache
   * @param ii input info
   * @return static variable reference
   * @throws QueryException query exception
   */
  public StaticVar declare(final QNm nm, final SeqType type, final AnnList anns, final Expr expr,
      final boolean ext, final StaticContext sc, final VarScope scope, final String doc,
      final InputInfo ii) throws QueryException {

    final StaticVar var = new StaticVar(sc, scope, anns, nm, type, expr, ext, doc, ii);
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

  /**
   * Returns a new reference to the (possibly not yet declared) variable with the given name.
   * @param ii input info
   * @param nm variable name
   * @param sc static context
   * @return reference
   * @throws QueryException if the variable is not visible
   */
  public StaticVarRef newRef(final QNm nm, final StaticContext sc, final InputInfo ii)
      throws QueryException {

    final StaticVarRef ref = new StaticVarRef(ii, nm, sc);
    final VarEntry e = vars.get(nm), entry = e != null ? e : new VarEntry(null);
    if(e == null) vars.put(nm, entry);
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

  /** Entry for static variables and their references. */
  private static class VarEntry {
    /** The static variable. */
    StaticVar var;
    /** Variable references. */
    ArrayList<StaticVarRef> refs = new ArrayList<>(1);

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
