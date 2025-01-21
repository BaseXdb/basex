package org.basex.query.var;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Container of global variables of a module.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class Variables extends ExprInfo implements Iterable<StaticVar> {
  /** The variables. */
  private final QNmMap<VarEntry> vars = new QNmMap<>();

  /**
   * Declares a new static variable.
   * @param var variable
   * @param expr bound expression, possibly {@code null}
   * @param anns annotations
   * @param external {@code external} flag
   * @param vs variable scope
   * @param doc xqdoc string
   * @return static variable reference
   * @throws QueryException query exception
   */
  public StaticVar declare(final Var var, final Expr expr, final AnnList anns,
      final boolean external, final VarScope vs, final String doc) throws QueryException {
    final StaticVar sv = new StaticVar(var, expr, anns, external, vs, doc);
    varEntry(var.name).setVar(sv);
    return sv;
  }

  /**
   * Ensures that none of the variable expressions is updating.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final VarEntry ve : vars.values()) ve.var.checkUp();
  }

  /**
   * Checks if all variables were declared and are visible to all their references.
   * @throws QueryException query exception
   */
  public void check() throws QueryException {
    for(final VarEntry ve : vars.values()) {
      final StaticVar var = ve.var;
      if(var == null) {
        final StaticVarRef ref = ve.refs.get(0);
        throw VARUNDEF_X.get(ref.info(), ref);
      }
      final QNm varMod = var.info.sc().module;
      final byte[] varModUri = varMod == null ? Token.EMPTY : varMod.uri();
      for(final StaticVarRef ref : ve.refs) {
        if(!ref.hasImport) {
          final QNm refMod = ref.info().sc().module;
          final byte[] refModUri = refMod == null ? Token.EMPTY : refMod.uri();
          if(!Token.eq(varModUri, refModUri))  throw INVISIBLEVAR_X.get(ref.info(), var.name);
        }
      }
    }
  }

  /**
   * Returns a new reference to the (possibly not yet declared) variable with the given name.
   * @param name variable name
   * @param info input info (can be {@code null})
   * @param hasImport indicates whether a module import for the variable name's URI was present
   * @return reference
   * @throws QueryException if the variable is not visible
   */
  public StaticVarRef newRef(final QNm name, final InputInfo info, final boolean hasImport)
      throws QueryException {
    final StaticVarRef ref = new StaticVarRef(info, name, hasImport);
    varEntry(name).addRef(ref);
    return ref;
  }

  /**
   * Returns a variable entry for the specified QName.
   * @param name QName
   * @return variable entry
   */
  private VarEntry varEntry(final QNm name) {
    VarEntry entry = vars.get(name);
    if(entry == null) {
      entry = new VarEntry();
      vars.put(name, entry);
    }
    return entry;
  }

  /**
   * Binds all external variables.
   * @param qc query context
   * @param bindings variable bindings
   * @param cast cast flag, value will be coerced if false
   * @throws QueryException query exception
   */
  public void bindExternal(final QueryContext qc, final QNmMap<Value> bindings, final boolean cast)
      throws QueryException {

    for(final QNm qnm : bindings) {
      if(qnm != QNm.EMPTY) {
        final VarEntry ve = vars.get(qnm);
        if(ve != null) ve.var.bind(bindings.get(qnm), qc, cast);
      }
    }
  }

  @Override
  public Iterator<StaticVar> iterator() {
    final Iterator<QNm> qnames = vars.iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return qnames.hasNext();
      }

      @Override
      public StaticVar next() {
        return vars.get(qnames.next()).var;
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }

  @Override
  public void toXml(final QueryPlan plan) {
    if(vars.isEmpty()) return;

    final ArrayList<ExprInfo> list = new ArrayList<>(vars.size());
    for(final VarEntry ve : vars.values()) list.add(ve.var);
    plan.add(plan.create(this), list.toArray());
  }

  @Override
  public void toString(final QueryString qs) {
    for(final VarEntry ve : vars.values()) qs.token(ve.var);
  }

  /** Entry for static variables and their references. */
  private static final class VarEntry {
    /** The static variable. */
    StaticVar var;
    /** Variable references. */
    final ArrayList<StaticVarRef> refs = new ArrayList<>(1);

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
