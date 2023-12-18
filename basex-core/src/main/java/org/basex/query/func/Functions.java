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
   * Returns a function call for a function with the specified name and arguments.
   * @param name name of the function
   * @param args positional arguments
   * @param keywords keyword arguments (can be {@code null})
   * @param qc query context
   * @param sc static context
   * @param info input info (can be {@code null})
   * @return function call
   * @throws QueryException query exception
   */
  public static Expr get(final QNm name, final Expr[] args, final QNmMap<Expr> keywords,
      final QueryContext qc, final StaticContext sc, final InputInfo info) throws QueryException {

    // constructor function
    if(eq(name.uri(), XS_URI)) {
      return constructor(name, args, keywords, sc, info);
    }

    // built-in function
    final FuncDefinition fd = builtIn(name);
    if(fd != null) {
      final int min = fd.minMax[0], max = fd.minMax[1];
      final Expr[] prepared = prepareArgs(args, keywords, fd.names, min, max, fd, info);
      final StandardFunc sf = fd.get(sc, info, prepared);
      if(sf.updating()) qc.updating();
      return sf;
    }

    // user-defined function
    return userDefined(name, args, keywords, qc, sc, info);
  }

  /**
   * Returns a function literal for a function with the specified name and arguments.
   * @param name function name
   * @param arity number of arguments
   * @param qc query context
   * @param sc static context
   * @param info input info (can be {@code null})
   * @param runtime {@code true} if this method is called at runtime
   * @return function literal if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public static Expr literal(final QNm name, final int arity, final QueryContext qc,
      final StaticContext sc, final InputInfo info, final boolean runtime) throws QueryException {

    final Literal literal = new Literal(sc, arity);

    // constructor function
    if(eq(name.uri(), XS_URI)) {
      if(arity > 0) literal.add(CAST_PARAM[0], SeqType.ANY_ATOMIC_TYPE_ZO, qc, info);
      final Expr expr = constructor(name, literal.args, null, sc, info);
      final FuncType ft = FuncType.get(literal.anns, null, literal.params);
      return literal(info, expr, ft, name, literal, runtime, false, arity == 0);
    }

    // built-in function
    final FuncDefinition fd = builtIn(name);
    if(fd != null) {
      checkArity(arity, fd.minMax[0], fd.minMax[1], fd, info, true);

      final FuncType ft = fd.type(arity, literal.anns);
      final QNm[] names = fd.paramNames(arity);
      for(int a = 0; a < arity; a++) literal.add(names[a], ft.argTypes[a], qc, info);
      final StandardFunc sf = fd.get(sc, info, literal.args);
      final boolean updating = sf.updating(), context = sf.has(Flag.CTX);
      if(updating) {
        literal.anns = literal.anns.attach(new Ann(info, Annotation.UPDATING, Empty.VALUE));
        qc.updating();
      }
      return literal(info, sf, ft, name, literal, runtime, updating, context);
    }

    // user-defined function
    final StaticFunc sf = qc.functions.get(name, arity);
    if(sf != null) {
      final Expr func = userDefined(sf, qc, sc, info, runtime, literal);
      if(sf.updating) qc.updating();
      return func;
    }

    for(int a = 0; a < arity; a++) literal.add(new QNm(ARG + (a + 1), ""), null, qc, info);

    // Java function
    final JavaCall java = JavaCall.get(name, literal.args, qc, sc, info);
    if(java != null) {
      final SeqType[] sts = new SeqType[arity];
      Arrays.fill(sts, SeqType.ITEM_ZM);
      final SeqType st = FuncType.get(literal.anns, null, sts).seqType();
      return new FuncLit(info, java, literal.params, literal.anns, st, name, literal.vs);
    }
    if(runtime) return null;

    // literal
    final StaticFuncCall call = userDefined(name, literal.args, null, qc, sc, info);
    // safe cast (no context dependency, no runtime evaluation)
    final Closure closure = (Closure) literal(info, call, null, name, literal, false, false, false);
    qc.functions.register(closure);
    return closure;
  }

  /**
   * Creates a function item for a user-defined function.
   * @param sf static function
   * @param qc query context
   * @param sc static context
   * @param info input info (can be {@code null})
   * @return function item
   * @throws QueryException query exception
   */
  public static FuncItem userDefined(final StaticFunc sf, final QueryContext qc,
      final StaticContext sc, final InputInfo info) throws QueryException {
    // safe cast (no context dependency, runtime evaluation)
    return (FuncItem) userDefined(sf, qc, sc, info, true, new Literal(sc, sf.arity()));
  }

  /**
   * Raises an error for the wrong number of function arguments.
   * @param nargs number of supplied arguments
   * @param arities available arities (if first arity is negative, function is variadic)
   * @param function function
   * @param info input info (can be {@code null})
   * @param literal literal
   * @return error
   */
  public static QueryException wrongArity(final int nargs, final IntList arities,
      final Object function, final InputInfo info, final boolean literal) {

    final String supplied = literal ? "Arity " + nargs : arguments(nargs), expected;
    if(!arities.isEmpty() && arities.peek() < 0) {
      expected = "at least " + -arities.peek();
    } else {
      final int as = arities.ddo().size();
      int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
      for(int a = 0; a < as; a++) {
        final int m = arities.get(a);
        if(m < min) min = m;
        if(m > max) max = m;
      }
      final TokenBuilder tb = new TokenBuilder();
      if(as > 2 && max - min + 1 == as) {
        tb.addInt(min).add('-').addInt(max);
      } else {
        for(int a = 0; a < as; a++) {
          if(a != 0) tb.add(a + 1 < as ? ", " : " or ");
          tb.addInt(arities.get(a));
        }
      }
      expected = tb.toString();
    }
    return INVNARGS_X_X_X.get(info, function, supplied, expected);
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
   * Incorporates keywords in the argument list.
   * @param args positional arguments
   * @param keywords keyword arguments
   * @param names parameter names
   * @param function function
   * @param info input info (can be {@code null})
   * @return arguments
   * @throws QueryException query exception
   */
  static Expr[] prepareArgs(final Expr[] args, final QNmMap<Expr> keywords, final QNm[] names,
      final Object function, final InputInfo info) throws QueryException {

    final ExprList list = new ExprList().add(args);
    final int nl = names.length;
    for(final QNm qnm : keywords) {
      int n = nl;
      while(--n >= 0 && !qnm.eq(names[n]));
      if(n == -1) throw KEYWORDUNKNOWN_X_X.get(info, function, qnm);
      if(list.get(n) != null) throw ARGTWICE_X_X.get(info, function, qnm);
      list.set(n, keywords.get(qnm));
    }
    return list.finish();
  }

  /**
   * Tries to resolve the specified function with xs namespace as a cast.
   * @param name function name
   * @param args positional arguments
   * @param keywords keyword arguments (can be {@code null})
   * @param sc static context
   * @param info input info (can be {@code null})
   * @return cast type if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Cast constructor(final QNm name, final Expr[] args, final QNmMap<Expr> keywords,
      final StaticContext sc, final InputInfo info) throws QueryException {

    Type type = ListType.find(name);
    if(type == null) type = AtomType.find(name, false);
    if(type == null) throw WHICHFUNC_X.get(info, AtomType.similar(name));
    if(type.oneOf(AtomType.NOTATION, AtomType.ANY_ATOMIC_TYPE))
      throw ABSTRACTFUNC_X.get(info, name.prefixId());

    final Expr[] prepared = prepareArgs(args, keywords, CAST_PARAM, 0, 1, name.string(), info);
    return new Cast(sc, info, prepared.length != 0 ? prepared[0] :
      new ContextValue(info), SeqType.get(type, Occ.ZERO_OR_ONE));
  }

  /**
   * Returns a cached function call.
   * @param name function name
   * @param args positional arguments
   * @param keywords keyword arguments (can be {@code null})
   * @param qc query context
   * @param sc static context
   * @param info input info (can be {@code null})
   * @return function call
   * @throws QueryException query exception
   */
  private static StaticFuncCall userDefined(final QNm name, final Expr[] args,
      final QNmMap<Expr> keywords, final QueryContext qc, final StaticContext sc,
      final InputInfo info) throws QueryException {

    if(NSGlobal.reserved(name.uri())) throw qc.functions.similarError(name, info);

    final StaticFuncCall call = new StaticFuncCall(name, args, keywords, sc, info);
    qc.functions.register(call);
    return call;
  }

  /**
   * Creates a function literal for a user-defined function.
   * @param sf static function
   * @param qc query context
   * @param sc static context
   * @param info input info (can be {@code null})
   * @param runtime {@code true} if this method is called at runtime
   * @param literal literal data
   * @return function item
   * @throws QueryException query exception
   */
  private static Expr userDefined(final StaticFunc sf, final QueryContext qc,
      final StaticContext sc, final InputInfo info, final boolean runtime, final Literal literal)
          throws QueryException {

    final FuncType sft = sf.funcType();
    final int arity = literal.params.length;
    for(int a = 0; a < arity; a++) literal.add(sf.paramName(a), sft.argTypes[a], qc, info);
    final FuncType ft = FuncType.get(literal.anns, sft.declType,
        Arrays.copyOf(sft.argTypes, arity));

    final StaticFuncCall call = userDefined(sf.name, literal.args, null, qc, sc, info);
    if(call.func != null) literal.anns = call.func.anns;
    return literal(info, call, ft, sf.name, literal, runtime, sf.updating, false);
  }

  /**
   * Raises an error for the wrong number of function arguments.
   * @param nargs number of supplied arguments
   * @param min minimum number of allowed arguments
   * @param max maximum number of allowed arguments
   * @param function function
   * @param info input info (can be {@code null})
   * @param literal literal
   * @throws QueryException query exception
   */
  private static void checkArity(final int nargs, final int min, final int max,
      final Object function, final InputInfo info, final boolean literal) throws QueryException {

    if(nargs < min || nargs > max) {
      final IntList arities = new IntList();
      if(max != Integer.MAX_VALUE) {
        for(int m = min; m <= max; m++) arities.add(m);
      } else {
        arities.add(-min);
      }
      throw wrongArity(nargs, arities, function, info, literal);
    }
  }

  /**
   * Creates a {@link Closure}, a {@link FuncItem} or a {@link FuncLit}.
   * At parse and compile time, a closure is generated to enable inlining and compilation.
   * At runtime, we directly generate a function item.
   * @param info input info (can be {@code null})
   * @param expr function body
   * @param ft function type
   * @param name function name
   * @param literal literal data
   * @param runtime runtime flag
   * @param updating flag for updating functions
   * @param context context-dependent flag
   * @return the function expression
   */
  private static Expr literal(final InputInfo info, final Expr expr, final FuncType ft,
      final QNm name, final Literal literal, final boolean runtime, final boolean updating,
      final boolean context) {

    final VarScope vs = literal.vs;
    final Var[] params = literal.params;
    final AnnList anns = literal.anns;

    // context/positional access must be bound to original focus
    // example for invalid query: let $f := last#0 return (1, 2)[$f()]
    return context ? new FuncLit(info, expr, params, anns, ft.seqType(), name, vs) :
      runtime ? new FuncItem(info, expr, params, anns, ft, vs.sc, vs.stackSize(), name) :
      new Closure(info, expr, params, anns, vs, null, updating ? SeqType.EMPTY_SEQUENCE_Z :
          ft != null ? ft.declType : null, name);
  }

  /**
   * Incorporates keywords in the argument list and checks the arity.
   * @param args positional arguments
   * @param keywords keyword arguments (can be {@code null})
   * @param names parameter names
   * @param min minimum number of allowed arguments
   * @param max maximum number of allowed arguments
   * @param function function
   * @param info input info (can be {@code null})
   * @return arguments
   * @throws QueryException query exception
   */
  private static Expr[] prepareArgs(final Expr[] args, final QNmMap<Expr> keywords,
      final QNm[] names, final int min, final int max, final Object function,
      final InputInfo info) throws QueryException {

    final Expr[] tmp = keywords != null ? prepareArgs(args, keywords, names, function, info) :
      args;
    final int arity = tmp.length;
    for(int a = arity - 1; a >= 0; a--) {
      if(tmp[a] == null) {
        if(a < min) throw ARGMISSING_X_X.get(info, function, names[a].prefixString());
        tmp[a] = Empty.UNDEFINED;
      }
    }
    checkArity(arity, min, max, function, info, false);
    return tmp;
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
    AnnList anns = AnnList.EMPTY;
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
     * @param info input info (can be {@code null})
     */
    void add(final QNm name, final SeqType st, final QueryContext qc, final InputInfo info) {
      final Var var = vs.addNew(name, st, true, qc, info);
      params[a] = var;
      args[a] = new VarRef(info, var);
      a++;
    }
  }
}
