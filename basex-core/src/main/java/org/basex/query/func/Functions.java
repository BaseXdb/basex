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
import org.basex.query.util.list.*;
import org.basex.query.util.parse.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Functions {
  /** Signatures of built-in functions. */
  public static final ArrayList<FuncDefinition> DEFINITIONS = new ArrayList<>();

  /** Cached functions. */
  private static final TokenObjectMap<QNm> CACHE = new TokenObjectMap<>();
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
      final byte[] id = qnm.unique();
      if(CACHE.contains(id)) throw Util.notExpected("Function % is defined twice.", id);
      CACHE.put(id, qnm);
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
   * Creates a function call or a function item expression.
   * @param name function name
   * @param fb function arguments
   * @param qc query context
   * @param hasImport indicates whether a module import for the function name's URI was present
   * @return function call
   * @throws QueryException query exception
   */
  public static Expr get(final QNm name, final FuncBuilder fb, final QueryContext qc,
      final boolean hasImport) throws QueryException {

    // partial function call?
    if(fb.placeholders > 0) return dynamic(item(name, fb.arity, false, fb.info, qc, hasImport), fb);

    // constructor function
    if(eq(name.uri(), XS_URI)) return constructorCall(name, fb);

    // built-in function
    final FuncDefinition fd = builtIn(name);
    if(fd != null) {
      final int min = fd.minMax[0], max = fd.minMax[1];
      final Expr[] prepared = prepareArgs(fb, fd.names, min, max, fd);
      final StandardFunc sf = fd.get(fb.info, prepared);
      if(sf.hasUPD()) qc.updating();
      return sf;
    }

    // user-defined function
    return staticCall(name, fb, qc, hasImport);
  }

  /**
   * Creates a dynamic function call or a partial function expression.
   * @param expr function expression
   * @param fb function arguments
   * @return function call
   * @throws QueryException query exception
   */
  public static Expr dynamic(final Expr expr, final FuncBuilder fb) throws QueryException {
    final int ph = fb.placeholders;
    final Expr[] args;
    int[] phPerm = null;

    if(fb.keywords == null) {
      args = fb.args();
    } else {
      final XQFunctionExpr fe = (XQFunctionExpr) expr;
      final int arity = fe.arity();
      final QNm[] names = new QNm[arity];
      for(int a = 0; a < arity; ++a) names[a] = fe.paramName(a);
      args = prepareArgs(fb, names, expr);
      if(ph > 0) phPerm = preparePlaceholders(fb, names, args);
    }

    // no placeholders: create dynamic function call
    // all arguments are placeholders in the original order: return original function expression
    // otherwise, create partially applied function with optional placeholder permutation
    return ph == 0 ? new DynFuncCall(fb.info, expr, args) :
           ph == args.length && phPerm == null ? expr :
           new PartFunc(fb.info, ExprList.concat(args, expr), ph, phPerm);
  }

  /**
   * Creates a function item expression.
   * @param name function name
   * @param arity number of arguments
   * @param runtime {@code true} if this method is called at runtime
   * @param info input info (can be {@code null})
   * @param qc query context
   * @param hasImport indicates whether a module import for the function name's URI was present
   * @return literal if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public static Expr item(final QNm name, final int arity, final boolean runtime,
      final InputInfo info, final QueryContext qc, final boolean hasImport) throws QueryException {

    final FuncBuilder fb = new FuncBuilder(info, arity, runtime);

    // constructor function
    if(eq(name.uri(), XS_URI)) {
      if(arity > 0) fb.add(CAST_PARAM[0], SeqType.ANY_ATOMIC_TYPE_ZO, qc);
      final Expr expr = constructorCall(name, fb);
      final FuncType ft = FuncType.get(fb.anns, null, fb.params);
      return item(expr, fb, ft, name, false, arity == 0);
    }

    // built-in function
    final FuncDefinition fd = builtIn(name);
    if(fd != null) {
      final IntList arts = checkArity(arity, fd.minMax[0], fd.minMax[1]);
      if(arts != null) throw wrongArity(arity, arts, true, info, fd);

      final FuncType ft = fd.type(arity, fb.anns);
      final QNm[] names = fd.paramNames(arity);
      for(int a = 0; a < arity; a++) fb.add(names[a], ft.argTypes[a], qc);
      final StandardFunc sf = fd.get(info, fb.args());
      final boolean updating = sf.hasUPD();
      if(updating) {
        fb.anns = fb.anns.attach(new Ann(info, Annotation.UPDATING, Empty.VALUE));
        qc.updating();
      }
      return item(sf, fb, ft, name, updating, sf.has(Flag.CTX));
    }

    // user-defined function
    final StaticFunc sf = qc.functions.get(name, arity);
    if(sf != null) {
      final Expr func = item(sf, fb, qc, hasImport);
      if(sf.updating) qc.updating();
      return func;
    }

    for(int a = 0; a < arity; a++) fb.add(new QNm(ARG + (a + 1), ""), null, qc);

    // Java function
    final JavaCall java = JavaCall.get(name, fb.args(), qc, info);
    if(java != null) {
      final SeqType[] sts = new SeqType[arity];
      Arrays.fill(sts, SeqType.ITEM_ZM);
      final SeqType st = FuncType.get(fb.anns, null, sts).seqType();
      return new FuncLit(info, java, fb.params, fb.anns, st, name, fb.vs);
    }
    if(runtime) return null;

    // closure
    final StaticFuncCall call = staticCall(name, fb, qc, hasImport);
    // safe cast (no context dependency, no runtime evaluation)
    final Closure closure = (Closure) item(call, fb, null, name, false, false);
    qc.functions.register(closure);
    return closure;
  }

  /**
   * Raises an error for the wrong number of function arguments.
   * @param nargs number of supplied arguments
   * @param arities available arities (if single arity is negative, function is variadic)
   * @param literal literal flag
   * @param info input info (can be {@code null})
   * @param function function (for error messages)
   * @return error
   */
  public static QueryException wrongArity(final int nargs, final IntList arities,
      final boolean literal, final InputInfo info, final Object function) {

    final String arity = arity(literal ? "Arity " + nargs : arguments(nargs), arities);
    return INVNARGS_X_X.get(info, function, arity);
  }

  /**
   * Returns the definition of a built-in function with the specified name.
   * @param name function name
   * @return function definition if found, {@code null} otherwise
   */
  static FuncDefinition builtIn(final QNm name) {
    final int id = CACHE.id(name.unique());
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
   * @param fb function arguments
   * @param names parameter names
   * @param function function (for error messages)
   * @return arguments
   * @throws QueryException query exception
   */
  static Expr[] prepareArgs(final FuncBuilder fb, final QNm[] names, final Object function)
      throws QueryException {

    final ExprList list = new ExprList(fb.args());
    final int nl = names.length;
    for(final QNm qnm : fb.keywords) {
      int n = nl;
      while(--n >= 0 && !qnm.eq(names[n]));
      if(n == -1) throw KEYWORDUNKNOWN_X_X.get(fb.info, function, qnm);
      if(list.get(n) != null) throw ARGTWICE_X_X.get(fb.info, function, qnm);
      list.set(n, fb.keywords.get(qnm));
    }
    return list.finish();
  }

  /**
   * Returns a constructor call.
   * @param name function name
   * @param fb function arguments
   * @return cast type if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Cast constructorCall(final QNm name, final FuncBuilder fb) throws QueryException {
    Type type = ListType.find(name);
    if(type == null) type = AtomType.find(name, false);
    if(type == null) throw WHICHFUNC_X.get(fb.info, AtomType.similar(name));
    if(type.oneOf(AtomType.NOTATION, AtomType.ANY_ATOMIC_TYPE))
      throw ABSTRACTFUNC_X.get(fb.info, name.prefixId());

    final Expr[] prepared = prepareArgs(fb, CAST_PARAM, 0, 1, name.string());
    return new Cast(fb.info, prepared.length != 0 ? prepared[0] :
      new ContextValue(fb.info), SeqType.get(type, Occ.ZERO_OR_ONE));
  }

  /**
   * Creates a cached function call.
   * @param name function name
   * @param fb function arguments
   * @param qc query context
   * @param hasImport indicates whether a module import for the function name's URI was present
   * @return function call
   * @throws QueryException query exception
   */
  private static StaticFuncCall staticCall(final QNm name, final FuncBuilder fb,
      final QueryContext qc, final boolean hasImport) throws QueryException {

    if(NSGlobal.reserved(name.uri())) throw qc.functions.similarError(name, fb.info);

    final StaticFuncCall call = new StaticFuncCall(name, fb.args(), fb.keywords, fb.info,
        hasImport);
    qc.functions.register(call);
    return call;
  }

  /**
   * Creates a function item expression for a user-defined function.
   * @param sf static function
   * @param fb function arguments
   * @param qc query context
   * @param hasImport indicates whether a module import for the function name's URI was present
   * @return function item
   * @throws QueryException query exception
   */
  public static Expr item(final StaticFunc sf, final FuncBuilder fb, final QueryContext qc,
      final boolean hasImport) throws QueryException {

    final FuncType sft = sf.funcType();
    final int arity = fb.params.length;
    for(int a = 0; a < arity; a++) fb.add(sf.paramName(a), sft.argTypes[a], qc);
    final FuncType ft = FuncType.get(fb.anns, sft.declType, Arrays.copyOf(sft.argTypes, arity));

    final StaticFuncCall call = staticCall(sf.name, fb, qc, hasImport);
    if(call.func != null) fb.anns = call.func.anns;
    return item(call, fb, ft, sf.name, sf.updating, false);
  }

  /**
   * Checks if the specified number of arguments matches the allowed min/max range.
   * If not, returns an array with the allowed numbers of parameters.
   * @param nargs number of supplied arguments
   * @param min minimum number of allowed arguments
   * @param max maximum number of allowed arguments
   * @return arities or {@code null}
   */
  public static IntList checkArity(final long nargs, final int min, final int max) {
    if(nargs >= min && nargs <= max) return null;

    final IntList arities = new IntList();
    if(max != Integer.MAX_VALUE) {
      for(int m = min; m <= max; m++) arities.add(m);
    } else {
      arities.add(-min);
    }
    return arities;
  }

  /**
   * Creates a function item expression ({@link Closure}, {@link FuncItem}, or {@link FuncLit}).
   * At parse and compile time, a closure is generated to enable inlining and compilation.
   * At runtime, we directly generate a function item.
   * @param expr function body
   * @param fb function arguments
   * @param ft function type
   * @param name function name
   * @param updating flag for updating functions
   * @param context context-dependent flag
   * @return the function expression
   */
  private static Expr item(final Expr expr, final FuncBuilder fb, final FuncType ft,
      final QNm name, final boolean updating, final boolean context) {

    final Var[] params = fb.params;
    final AnnList anns = fb.anns;
    final VarScope vs = fb.vs;

    // context access must be bound to original focus
    // example: let $f := last#0 return (1, 2)[$f()]
    if(context) {
      return new FuncLit(fb.info, expr, params, anns, ft.seqType(), name, vs);
    }
    // runtime: create function item
    if(fb.runtime) {
      return new FuncItem(fb.info, expr, params, anns, ft, vs.stackSize(), name);
    }
    // otherwise: create closure
    final SeqType declType = updating ? SeqType.EMPTY_SEQUENCE_Z : ft != null ? ft.declType : null;
    return new Closure(fb.info, expr, params, anns, vs, null, declType, name);
  }

  /**
   * Incorporates keywords in the argument list and checks the arity.
   * @param fb function arguments
   * @param names parameter names
   * @param min minimum number of allowed arguments
   * @param max maximum number of allowed arguments
   * @param function function (for error messages)
   * @return arguments
   * @throws QueryException query exception
   */
  private static Expr[] prepareArgs(final FuncBuilder fb, final QNm[] names, final int min,
      final int max, final Object function) throws QueryException {

    final Expr[] args = fb.keywords != null ? prepareArgs(fb, names, function) : fb.args();
    final int arity = args.length;
    for(int a = arity - 1; a >= 0; a--) {
      if(args[a] == null) {
        if(a < min) throw ARGMISSING_X_X.get(fb.info, function, names[a].prefixString());
        args[a] = Empty.UNDEFINED;
      }
    }
    final IntList arities = checkArity(arity, min, max);
    if(arities != null) throw wrongArity(arity, arities, false, fb.info, function);
    return args;
  }

  /**
   * Calculates the permutation of argument placeholder ordinals, that will map from the order of
   * placeholders in the target function's argument list to the parameter positions in the parameter
   * list of a partially evaluated function.
   * @param fb function arguments
   * @param names parameter names
   * @param args arguments with optional placeholders
   * @return an integer array, where the value at index i indicates the index in the parameter list
   *         of the partially evaluated function of the i-th placeholder in the (positional) target
   *         function argument list.
   */
  private static int[] preparePlaceholders(final FuncBuilder fb, final QNm[] names,
      final Expr[] args) {
    final int[] phPerm = new int[fb.placeholders];
    final int posArgs = fb.arity - fb.keywords.size();
    int p = 0;
    for(int a = 0; a < posArgs; ++a) {
      if(PartFunc.placeholder(args[a])) {
        phPerm[p] = p;
        ++p;
      }
    }
    final int nonKwPh = p;
    for(int a = posArgs; a < fb.arity; ++a) {
      if(PartFunc.placeholder(args[a])) {
        final QNm name = names[a];
        int i = nonKwPh;
        for(final QNm qnm : fb.keywords) {
          if(qnm.eq(name)) break;
          if(PartFunc.placeholder(fb.keywords.get(qnm))) ++i;
        }
        phPerm[p++] = i;
      }
    }
    return phPerm;
  }
}
