package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.java.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.Type;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * This class provides access to built-in and user-defined functions.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Functions {
  /** Signatures of built-in functions. */
  public static final ArrayList<FuncDefinition> DEFINITIONS = new ArrayList<>();

  /** Cached functions. */
  private static final TokenObjMap<QNm> CACHE = new TokenObjMap<>();
  /** URIs of built-in functions. */
  private static final TokenSet URIS = new TokenSet();
  /** Cast parameter. */
  private static final QNm[] CAST_PARAM = { new QNm("value") };

  /** Private constructor. */
  private Functions() { }

  // initializes built-in XQuery functions
  static {
    // add built-in core functions
    Function.init(DEFINITIONS);
    // add built-in API functions if available
    final Class<?> clz = Reflect.find("org.basex.query.func.ApiFunction");
    final Method mth = Reflect.method(clz, "init", ArrayList.class);
    if(mth != null) Reflect.invoke(mth, null, DEFINITIONS);

    for(final FuncDefinition fd : DEFINITIONS) {
      URIS.add(fd.uri);
      final QNm qnm = new QNm(fd.local(), fd.uri());
      CACHE.put(qnm.internal(), qnm);
    }
  }

  /**
   * Returns the definition of a built-in function with the specified name.
   * @param name function name
   * @return function definition if found, {@code null} otherwise
   */
  static FuncDefinition builtIn(final QNm name) {
    final int id = CACHE.id(name.internal());
    return id != 0 ? DEFINITIONS.get(id - 1) : null;
  }

  /**
   * Checks if the specified URI is statically available.
   * @param uri URI to check
   * @return result of check
   */
  public static boolean staticURI(final byte[] uri) {
    for(final byte[] u : URIS) {
      if(eq(uri, u)) return true;
    }
    return false;
  }

  /**
   * Raises an error for the wrong number of function arguments.
   * @param arity number of supplied arguments
   * @param min minimum number of allowed arguments
   * @param max maximum number of allowed arguments
   * @param function function
   * @param ii input info
   * @throws QueryException query exception
   */
  private static void checkArity(final int arity, final int min, final int max,
      final Object function, final InputInfo ii) throws QueryException {

    if(arity < min || arity > max) {
      final IntList arities = new IntList();
      if(max != Integer.MAX_VALUE) {
        for(int m = min; m <= max; m++) arities.add(m);
      }
      throw wrongArity(arity, arities, function, ii);
    }
  }

  /**
   * Raises an error for the wrong number of function arguments.
   * @param arity number of supplied arguments
   * @param arities expected arities
   * @param function function
   * @param ii input info
   * @return error
   */
  public static QueryException wrongArity(final int arity, final IntList arities,
      final Object function, final InputInfo ii) {

    final int as = arities.ddo().size();
    if(as == 0) return FUNCARITY_X_X.get(ii, function, arguments(arity));

    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    for(int a = 0; a < as; a++) {
      final int m = arities.get(a);
      if(m < min) min = m;
      if(m > max) max = m;
    }

    final TokenBuilder ext = new TokenBuilder();
    if(as > 2 && max - min + 1 == as) {
      ext.addInt(min).add('-').addInt(max);
    } else {
      for(int a = 0; a < as; a++) {
        if(a != 0) ext.add(a + 1 < as ? ", " : " or ");
        ext.addInt(arities.get(a));
      }
    }
    return FUNCARITY_X_X_X.get(ii, function, arguments(arity), ext);
  }

  /**
   * Tries to resolve the specified function with xs namespace as a cast.
   * @param name function name
   * @param args positional arguments
   * @param keywords keyword arguments (can be {@code null})
   * @param sc static context
   * @param ii input info
   * @return cast type if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Cast cast(final QNm name, final Expr[] args, final QNmMap<Expr> keywords,
      final StaticContext sc, final InputInfo ii) throws QueryException {

    Type type = ListType.find(name);
    if(type == null) type = AtomType.find(name, false);
    if(type == null) throw WHICHFUNC_X.get(ii, AtomType.similar(name));
    if(type.oneOf(AtomType.NOTATION, AtomType.ANY_ATOMIC_TYPE))
      throw ABSTRACTFUNC_X.get(ii, name.prefixId());

    final Expr[] prepared = prepareArgs(args, keywords, CAST_PARAM, 0, 1, name.string(), ii);
    return new Cast(sc, ii, prepared.length != 0 ? prepared[0] :
      new ContextValue(ii), SeqType.get(type, Occ.ZERO_OR_ONE));
  }

  /**
   * Returns an instance of a built-in function.
   * @param name function qname
   * @param args positional arguments
   * @param keywords keyword arguments (can be {@code null})
   * @param sc static context
   * @param ii input info
   * @return function instance if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static StandardFunc builtIn(final QNm name, final Expr[] args,
      final QNmMap<Expr> keywords, final StaticContext sc, final InputInfo ii)
          throws QueryException {

    final FuncDefinition fd = builtIn(name);
    if(fd == null) return null;

    final int min = fd.minMax[0], max = fd.minMax[1];
    return fd.get(sc, ii, prepareArgs(args, keywords, fd.names, min, max, fd, ii));
  }

  /**
   * Creates either a {@link Closure} or a {@link FuncItem} depending on when the method is called.
   * At parse and compile time, a closure is generated to enable inlining and compilation.
   * At runtime, we directly generate a function item.
   * @param ii input info
   * @param expr function body
   * @param ft function type
   * @param name function name, may be {@code null}
   * @param lit literal data
   * @param runtime runtime flag
   * @param updating flag for updating functions
   * @param ctx context-dependent flag
   * @return the function expression
   */
  private static Expr closureOrFuncItem(final InputInfo ii, final Expr expr, final FuncType ft,
      final QNm name, final Literal lit, final boolean runtime, final boolean updating,
      final boolean ctx) {

    final VarScope vs = lit.vs;
    final Var[] params = lit.params;
    final AnnList anns = lit.anns();

    // context/positional access must be bound to original focus
    // example for invalid query: let $f := last#0 return (1, 2)[$f()]
    return ctx ? new FuncLit(ii, expr, ft.seqType(), name, params, anns, vs) :
      runtime ? new FuncItem(lit.vs.sc, anns, name, params, ft, expr, vs.stackSize(), ii) :
      new Closure(ii, expr, updating ? SeqType.EMPTY_SEQUENCE_Z : ft.declType, name, params,
        anns, null, vs);
  }

  /**
   * Creates a function literal for a known function.
   * @param name function name
   * @param arity number of arguments
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @param runtime {@code true} if this method is called at runtime
   * @return function literal if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public static Expr literal(final QNm name, final int arity, final QueryContext qc,
      final StaticContext sc, final InputInfo ii, final boolean runtime) throws QueryException {

    final Literal lit = new Literal(sc, arity);

    // constructor function
    if(eq(name.uri(), XS_URI)) {
      if(arity > 0) lit.add(CAST_PARAM[0], SeqType.ANY_ATOMIC_TYPE_ZO, qc, ii);
      final Expr expr = cast(name, lit.args, null, sc, ii);
      final FuncType ft = FuncType.get(lit.anns(), null, lit.params);
      return closureOrFuncItem(ii, expr, ft, name, lit, runtime, false, arity == 0);
    }

    // built-in function
    final FuncDefinition fd = builtIn(name);
    if(fd != null) {
      checkArity(arity, fd.minMax[0], fd.minMax[1], fd, ii);

      final FuncType ft = fd.type(arity, lit.anns());
      final QNm[] names = fd.paramNames(arity);
      for(int a = 0; a < arity; a++) lit.add(names[a], ft.argTypes[a], qc, ii);
      final StandardFunc sf = fd.get(sc, ii, lit.args);
      final boolean updating = sf.has(Flag.UPD), ctx = sf.has(Flag.CTX);
      if(updating) {
        lit.anns().add(new Ann(ii, Annotation.UPDATING, Empty.VALUE));
        qc.updating();
      }
      return closureOrFuncItem(ii, sf, ft, name, lit, runtime, updating, ctx);
    }

    // user-defined function
    final StaticFunc sf = qc.functions.get(name, arity);
    if(sf != null) {
      final Expr func = userDefined(sf, qc, sc, ii, runtime, lit);
      if(sf.updating) qc.updating();
      return func;
    }

    for(int a = 0; a < arity; a++) lit.add(new QNm(ARG + (a + 1), ""), null, qc, ii);

    // Java function
    final JavaCall java = JavaCall.get(name, lit.args, qc, sc, ii);
    if(java != null) {
      final SeqType[] sts = new SeqType[arity];
      Arrays.fill(sts, SeqType.ITEM_ZM);
      final SeqType st = FuncType.get(lit.anns(), null, sts).seqType();
      return new FuncLit(ii, java, st, name, lit.params, lit.anns(), lit.vs);
    }
    if(runtime) return null;

    // literal
    final StaticFuncCall call = funcCall(name, lit.args, qc, sc, ii);
    final Closure closure = new Closure(ii, call, null, name, lit.params, lit.anns(), null, lit.vs);
    return qc.functions.register(closure);
  }

  /**
   * Creates a function item for a user-defined function.
   * @param sf static function
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return function item
   * @throws QueryException query exception
   */
  public static FuncItem userDefined(final StaticFunc sf, final QueryContext qc,
      final StaticContext sc, final InputInfo ii) throws QueryException {
    return (FuncItem) userDefined(sf, qc, sc, ii, true, new Literal(sc, sf.params.length));
  }

  /**
   * Creates a function literal for a user-defined function.
   * @param sf static function
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @param runtime {@code true} if this method is called at runtime
   * @param lit literal data
   * @return function item
   * @throws QueryException query exception
   */
  public static Expr userDefined(final StaticFunc sf, final QueryContext qc, final StaticContext sc,
      final InputInfo ii, final boolean runtime, final Literal lit) throws QueryException {

    final FuncType ft = sf.funcType();
    final int arity = sf.params.length;
    for(int a = 0; a < arity; a++) lit.add(sf.paramName(a), ft.argTypes[a], qc, ii);
    final StaticFuncCall call = funcCall(sf.name, lit.args, qc, sc, ii);
    if(call.func != null) lit.anns = call.func.anns;
    return closureOrFuncItem(ii, call, ft, sf.name, lit, runtime, sf.updating, false);
  }


  /**
   * Returns a function call with the specified name and number of arguments.
   * @param name name of the function
   * @param args positional arguments
   * @param keywords keyword arguments (can be {@code null})
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return function call
   * @throws QueryException query exception
   */
  public static Expr get(final QNm name, final Expr[] args, final QNmMap<Expr> keywords,
      final QueryContext qc, final StaticContext sc, final InputInfo ii) throws QueryException {

    // constructor function
    if(eq(name.uri(), XS_URI)) return cast(name, args, keywords, sc, ii);

    // built-in function
    final StandardFunc sf = builtIn(name, args, keywords, sc, ii);
    if(sf != null) {
      if(sf.updating()) qc.updating();
      return sf;
    }

    // temporary: reject keyword parameters for other function types
    if(keywords != null && !NSGlobal.reserved(name.uri()))
      throw KEYWORDSUPPORT_X.get(ii, name.prefixString());

    // user-defined function
    return funcCall(name, args, qc, sc, ii);
  }

  /**
   * Returns an info message for a similar function.
   * @param qname name of type
   * @return info string
   */
  static byte[] similar(final QNm qname) {
    // find similar function in several attempts
    final ArrayList<QNm> qnames = new ArrayList<>(CACHE.size());
    for(final QNm qnm : CACHE.values()) qnames.add(qnm);
    final byte[] local = lc(qname.local()), uri = qname.uri();

    // find functions with identical URIs and similar local names
    Object similar = Levenshtein.similar(qname.local(), qnames.toArray(),
        o -> eq(uri, ((QNm) o).uri()) ? ((QNm) o).local() : null);
    // find functions with identical local names
    for(final QNm qnm : qnames) {
      if(similar == null && eq(lc(qnm.local()), local)) similar = qnm;
    }
    // find functions with identical URIs and local names that start with the specified name
    for(final QNm qnm : qnames) {
      if(similar == null && eq(uri, qnm.uri()) && startsWith(lc(qnm.local()), local)) similar = qnm;
    }
    return QueryError.similar(qname.prefixString(),
        similar != null ? ((QNm) similar).prefixString() : null);
  }

  /**
   * Incorporates keywords in the argument list and checks the arity.
   * @param args positional arguments
   * @param keywords keyword arguments (can be {@code null})
   * @param names parameter names
   * @param min minimum number of allowed arguments
   * @param max maximum number of allowed arguments
   * @param function function
   * @param ii input info
   * @return arguments
   * @throws QueryException query exception
   */
  private static Expr[] prepareArgs(final Expr[] args, final QNmMap<Expr> keywords,
      final QNm[] names, final int min, final int max, final Object function,
      final InputInfo ii) throws QueryException {

    Expr[] tmp = args;
    if(keywords != null) {
      final ExprList list = new ExprList().add(args);
      for(final QNm qnm : keywords) {
        int i = names.length;
        while(--i >= 0 && !qnm.eq(names[i]));
        if(i == -1) throw KEYWORDUNKNOWN_X_X.get(ii, function, qnm);
        if(list.get(i) != null) throw ARGTWICE_X_X.get(ii, function, qnm);
        list.set(i, keywords.get(qnm));
      }
      // assign dummy arguments
      for(int l = list.size() - 1; l >= 0; l--) {
        if(list.get(l) == null) {
          if(l < min) throw ARGMISSING_X_X.get(ii, function, names[l].prefixString());
          list.set(l, Empty.UNDEFINED);
        }
      }
      tmp = list.finish();
    }
    checkArity(tmp.length, min, max, function, ii);

    return tmp;
  }

  /**
   * Returns a cached function call.
   * @param name function name
   * @param args arguments
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return function call
   * @throws QueryException query exception
   */
  private static StaticFuncCall funcCall(final QNm name, final Expr[] args, final QueryContext qc,
      final StaticContext sc, final InputInfo ii) throws QueryException {

    if(NSGlobal.reserved(name.uri())) throw qc.functions.similarError(name, ii);
    final StaticFuncCall call = new StaticFuncCall(name, args, sc, ii);
    qc.functions.register(call);
    return call;
  }

  /**
   * Container for function literals.
   *
   * @author BaseX Team 2005-23, BSD License
   * @author Christian Gruen
   */
  private static class Literal {
    /** Variable scope. */
    final VarScope vs;
    /** Parameters. */
    final Var[] params;
    /** Arguments. */
    final Expr[] args;
    /** Annotations. */
    AnnList anns;
    /** Parameter counter. */
    int a;

    /**
     * Constructor.
     * @param sc static context
     * @param arity arity
     */
    Literal(final StaticContext sc, final int arity) {
      vs = new VarScope(sc);
      params = new Var[arity];
      args = new Expr[arity];
    }

    /**
     * Adds a parameter and argument.
     * @param name parameter name
     * @param st parameter type
     * @param qc query context
     * @param ii input info
     */
    void add(final QNm name, final SeqType st, final QueryContext qc, final InputInfo ii) {
      final Var var = vs.addNew(name, st, true, qc, ii);
      params[a] = var;
      args[a] = new VarRef(ii, var);
      a++;
    }

    /**
     * Returns the annotations.
     * @return annotations
     */
    AnnList anns() {
      if(anns == null) anns = new AnnList();
      return anns;
    }
  }
}
