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
import org.basex.util.similarity.*;

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
   * @param localNames local variables in scope at reference time (can be {@code null})
   */
  private record UnresolvedRef(StaticVarRef ref, boolean hasImport, QNm[] localNames) { }
  /** All unresolved variable references. */
  private final ArrayList<UnresolvedRef> unresolvedRefs = new ArrayList<>();
  /** Variables grouped by module URI, then by QName (private variables may share names). */
  private final TokenObjectMap<QNmMap<ArrayList<StaticVar>>> varsByModule =
      new TokenObjectMap<>();
  /** Variables with the initializing expressions of record fields. */
  private final ArrayList<StaticVar> fields = new ArrayList<>();

  /**
   * Declares a new static variable in a given module.
   * @param var variable
   * @param imports imported module URIs
   * @param expr bound expression (can be {@code null})
   * @param anns annotations
   * @param external {@code external} flag
   * @param vs variable scope
   * @param doc xqdoc string
   * @return static variable
   * @throws QueryException query exception
   */
  public StaticVar declare(final Var var, final TokenSet imports, final Expr expr,
      final AnnList anns, final boolean external, final VarScope vs, final String doc)
      throws QueryException {

    final StaticContext sc = var.info.sc();
    final byte[] modUri = QNm.uri(sc.module);
    final byte[] varUri = var.name.uri();
    final boolean priv = anns.contains(Annotation.PRIVATE);
    if(!Token.eq(modUri, varUri)) {
      if(modUri != Token.EMPTY && !priv) throw MODULENS_X.get(var.info, var);
      if(imports.contains(varUri)) {
        // variable already declared in imported module
        if(get(var.name, varUri, sc) != null) throw VARDUPL_X.get(var.info, var.name.string());
      }
    }
    final ArrayList<StaticVar> vars = varsByModule.computeIfAbsent(modUri, QNmMap::new).
        computeIfAbsent(var.name, ArrayList::new);
    for(final StaticVar sv : vars) {
      // private variables of other modules with the same URI are never in scope
      if(sv.sc == sc || !priv && sv.visible(sc)) {
        throw VARDUPL_X.get(var.info, var.name.string());
      }
    }

    final StaticVar sv = new StaticVar(var, expr, anns, external, vs, doc, false);
    vars.add(sv);
    return sv;
  }

  /**
   * Declares a variable for the initializing expression of a record field.
   * @param var variable
   * @param expr initializing expression
   * @param vs variable scope
   * @return reference to the variable
   */
  public Expr declareField(final Var var, final Expr expr, final VarScope vs) {
    final StaticVar sv = new StaticVar(var, expr, AnnList.EMPTY, false, vs, "", true);
    fields.add(sv);
    final StaticVarRef ref = new StaticVarRef(var.info, var.name);
    ref.init(sv);
    return ref;
  }

  /**
   * Ensures that none of the variable expressions is updating.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final StaticVar var : this) var.checkUp();
    for(final StaticVar var : fields) var.checkUp();
  }

  /**
   * Resolves all references and checks for existing and visible declarations.
   * @throws QueryException query exception
   */
  public void resolve() throws QueryException {
    for(final UnresolvedRef ur : unresolvedRefs) {
      final StaticVarRef ref = ur.ref;

      // try to resolve the reference within the local module
      final StaticContext sc = ref.sc();
      final byte[] modUri = QNm.uri(sc.module);
      StaticVar var = get(ref.name, modUri, sc);

      if(var == null) {
        // try to resolve the reference from a module
        final byte[] refUri = ref.name.uri();
        var = get(ref.name, refUri, sc);
        if(var == null) {
          if(get(ref.name, modUri, null) != null || get(ref.name, refUri, null) != null) {
            throw VARPRIVATE_X.get(ref.info(), ref);
          }
          throw VARUNDEF_X.get(ref.info(),
              QueryError.similar(ref, similarName(ref.name, ur.localNames, sc)));
        }
        if(!Token.eq(modUri, refUri) && !ur.hasImport) {
          throw INVISIBLEVAR_X.get(ref.info(), ref.name.varString());
        }
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
   * Compiles the variables of record fields.
   * @param cc compilation context
   * @throws QueryException query exception
   */
  public void compileFields(final CompileContext cc) throws QueryException {
    for(final StaticVar var : fields) var.compile(cc);
  }

  /**
   * Returns a new reference to the (possibly not yet declared) variable with the given name.
   * @param name variable name
   * @param info input info
   * @param imports URIs of imported modules
   * @param localNames local variables in scope at reference time ({@code null} if not required)
   * @return reference
   */
  public StaticVarRef newRef(final QNm name, final InputInfo info, final TokenSet imports,
      final QNm[] localNames) {
    if(info == null) throw Util.notExpected();
    final StaticVarRef ref = new StaticVarRef(info, name);
    unresolvedRefs.add(new UnresolvedRef(ref, imports.contains(name.uri()), localNames));
    return ref;
  }

  /**
   * Indicates if a static variable with the given name has already been declared.
   * @param name variable name
   * @return result of check
   */
  public boolean declared(final QNm name) {
    for(final QNmMap<ArrayList<StaticVar>> vars : varsByModule.values()) {
      if(vars.contains(name)) return true;
    }
    return false;
  }

  /**
   * Returns a formatted "$name" hint for the variable name most similar to the supplied reference,
   * or {@code null} if no similar name is found among the in-scope and declared variables.
   * @param name unresolved variable name
   * @param localNames local variables in scope at reference time ({@code null} if not required)
   * @param sc static context of the reference
   * @return formatted hint or {@code null}
   */
  private String similarName(final QNm name, final QNm[] localNames, final StaticContext sc) {
    final ArrayList<QNm> candidates = new ArrayList<>();
    if(localNames != null) Collections.addAll(candidates, localNames);
    for(final StaticVar sv : this) {
      if(sv.visible(sc)) candidates.add(sv.name);
    }
    final QNm similar = Levenshtein.similarOrPrefix(name.local(),
        candidates.toArray(QNm[]::new), QNm::local);
    return similar != null ? Util.info("$%", similar.prefixString()) : null;
  }

  /**
   * Returns a visible variable for the specified QName and module, preferring the variable
   * declared in the given static context.
   * @param name QName
   * @param module module URI
   * @param sc static context of the reference ({@code null} to also return invisible variables)
   * @return variable entry, or {@code null}
   */
  private StaticVar get(final QNm name, final byte[] module, final StaticContext sc) {
    final QNmMap<ArrayList<StaticVar>> map = varsByModule.get(module);
    final ArrayList<StaticVar> vars = map != null ? map.get(name) : null;
    if(vars == null) return null;

    StaticVar var = null;
    for(final StaticVar sv : vars) {
      if(sc == null || sv.visible(sc)) {
        if(sv.sc == sc) return sv;
        if(var == null) var = sv;
      }
    }
    return var;
  }

  /**
   * Binds all external variables.
   * @param qc query context
   * @param bindings variable bindings
   * @param cast cast flag, values must be instances of the declared types if false
   * @throws QueryException query exception
   */
  public void bindExternal(final QueryContext qc, final QNmMap<Value> bindings, final boolean cast)
      throws QueryException {

    for(final QNm qnm : bindings) {
      if(qnm != QNm.EMPTY) {
        final Value val = bindings.get(qnm);
        for(final QNmMap<ArrayList<StaticVar>> vars : varsByModule.values()) {
          final ArrayList<StaticVar> list = vars.get(qnm);
          if(list != null) {
            for(final StaticVar var : list) var.bind(val, qc, cast);
          }
        }
      }
    }
  }

  @Override
  public Iterator<StaticVar> iterator() {
    return list().iterator();
  }

  /**
   * Returns all variables.
   * @return variables
   */
  private ArrayList<StaticVar> list() {
    final ArrayList<StaticVar> list = new ArrayList<>();
    for(final QNmMap<ArrayList<StaticVar>> vars : varsByModule.values()) {
      for(final ArrayList<StaticVar> svs : vars.values()) list.addAll(svs);
    }
    return list;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    if(!varsByModule.isEmpty()) plan.add(plan.create(this), list().toArray(StaticVar[]::new));
  }

  @Override
  public void toString(final QueryString qs) {
    for(final StaticVar var : this) qs.token(var);
  }
}
