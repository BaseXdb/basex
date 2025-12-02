package org.basex.query.var;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Container of global variables of a query.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class Variables extends ExprInfo implements Iterable<StaticVar> {
  /**
   * An unresolved variable reference.
   * @param ref reference
   * @param hasImport whether there was an import statement for the ref's URI
   */
  private record UnresolvedRef(StaticVarRef ref, boolean hasImport) { }
  /** All unresolved variable references. */
  private final ArrayList<UnresolvedRef> unresolvedRefs = new ArrayList<>();
  /** The variables by declaring module. */
  private final TokenObjectMap<QNmMap<StaticVar>> varsByModule = new TokenObjectMap<>();

  /**
   * Declares a new static variable in a given module.
   * @param var variable
   * @param imports imported module URIs
   * @param expr bound expression, possibly {@code null}
   * @param anns annotations
   * @param external {@code external} flag
   * @param vs variable scope
   * @param doc xqdoc string
   * @return static variable reference
   * @throws QueryException query exception
   */
  public StaticVar declare(final Var var, final TokenSet imports, final Expr expr,
      final AnnList anns, final boolean external, final VarScope vs, final String doc)
      throws QueryException {

    final byte[] modUri = QNm.uri(var.info.sc().module);
    final byte[] varUri = var.name.uri();
    if(!Token.eq(modUri, varUri)) {
      if(modUri != Token.EMPTY && !anns.contains(Annotation.PRIVATE)) {
        throw MODULENS_X.get(var.info, var);
      }
      if(imports.contains(varUri)) {
        final StaticVar sv = get(var.name, varUri);
        if(sv != null && !sv.anns.contains(Annotation.PRIVATE)) {
          // variable already declared in imported module
          throw VARDUPL_X.get(var.info, var.name.string());
        }
      }
    }
    final QNmMap<StaticVar> vars = varsByModule.computeIfAbsent(modUri, QNmMap::new);
    if(vars.contains(var.name)) throw VARDUPL_X.get(var.info, var.name.string());

    final StaticVar sv = new StaticVar(var, expr, anns, external, vs, doc);
    vars.put(var.name, sv);
    return sv;
  }

  /**
   * Ensures that none of the variable expressions is updating.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final StaticVar var : this) var.checkUp();
  }

  /**
   * Resolves all references and checks for existing and visible declarations.
   * @throws QueryException query exception
   */
  public void resolve() throws QueryException {
    for(final UnresolvedRef ur : unresolvedRefs) {
      final StaticVarRef ref = ur.ref;

      // try to resolve the reference within the local module
      final byte[] modUri = QNm.uri(ref.sc().module);
      StaticVar var = get(ref.name, modUri);

      if(var == null) {
        // try to resolve the reference from a module
        final byte[] refUri = ref.name.uri();
        var = get(ref.name, refUri);
        if(var == null) throw VARUNDEF_X.get(ref.info(), ref);
        if(!Token.eq(modUri, refUri) && !ur.hasImport) {
          throw INVISIBLEVAR_X.get(ref.info(), ref.name);
        }
      }

      if(var.anns.contains(Annotation.PRIVATE) && !Token.eq(modUri, QNm.uri(var.sc.module))) {
        throw VARPRIVATE_X.get(ref.info(), ref);
      }
      ref.init(var);
    }
    unresolvedRefs.clear();
  }

  /**
   * Compiles all static variables.
   * @param cc compilation context
   * @throws QueryException query exception
   */
  public void compileAll(final CompileContext cc) throws QueryException {
    for(final StaticVar var : this) var.compile(cc);
  }

  /**
   * Returns a new reference to the (possibly not yet declared) variable with the given name.
   * @param name variable name
   * @param info input info
   * @param imports URIs of imported modules
   * @return reference
   */
  public StaticVarRef newRef(final QNm name, final InputInfo info, final TokenSet imports) {
    if(info == null) throw Util.notExpected();
    final StaticVarRef ref = new StaticVarRef(info, name);
    unresolvedRefs.add(new UnresolvedRef(ref, imports.contains(name.uri())));
    return ref;
  }

  /**
   * Returns the variable for the specified QName and module, or {@code null} if it does not exist.
   * @param name QName
   * @param module module URI
   * @return variable entry, or {@code null}
   */
  private StaticVar get(final QNm name, final byte[] module) {
    final QNmMap<StaticVar> vars = varsByModule.get(module);
    return vars == null ? null : vars.get(name);
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
        final Value val = bindings.get(qnm);
        for(final QNmMap<StaticVar> vars : varsByModule.values()) {
          final StaticVar var = vars.get(qnm);
          if(var != null) var.bind(val, qc, cast);
        }
      }
    }
  }

  @Override
  public Iterator<StaticVar> iterator() {
    return new Iterator<>() {
      /** Iterator over modules. */
      private final Iterator<QNmMap<StaticVar>> modules = varsByModule.values().iterator();
      /** Iterator over StaticVar objects of the current module. */
      private Iterator<StaticVar> vars = Collections.emptyIterator();

      @Override
      public boolean hasNext() {
        while(!vars.hasNext()) {
          if(!modules.hasNext()) return false;
          vars = modules.next().values().iterator();
        }
        return true;
      }

      @Override
      public StaticVar next() {
        if(!vars.hasNext()) throw new NoSuchElementException();
        return vars.next();
      }
    };
  }

  @Override
  public void toXml(final QueryPlan plan) {
    if(varsByModule.isEmpty()) return;

    final ArrayList<ExprInfo> list = new ArrayList<>();
    for(final StaticVar var : this) list.add(var);
    plan.add(plan.create(this), list.toArray());
  }

  @Override
  public void toString(final QueryString qs) {
    for(final StaticVar var : this) qs.token(var);
  }
}
