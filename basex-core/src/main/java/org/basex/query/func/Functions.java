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
import org.basex.query.value.item.*;
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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class Functions {
  /** Signatures of built-in functions. */
  public static final ArrayList<FuncDefinition> DEFINITIONS = new ArrayList<>();

  /** Cached functions. */
  private static final TokenSet CACHE = new TokenSet();
  /** URIs of built-in functions. */
  private static final TokenSet URIS = new TokenSet();

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
      if(!CACHE.add(new QNm(fd.local(), fd.uri()).id())) {
        throw Util.notExpected("Duplicate function definition: " + fd);      }
    }
  }

  /**
   * Tries to resolve the specified function with xs namespace as a cast.
   * @param arity number of arguments
   * @param name function name
   * @param ii input info
   * @return cast type if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Type getCast(final QNm name, final long arity, final InputInfo ii)
      throws QueryException {

    final byte[] ln = name.local();
    Type type = ListType.find(name);
    if(type == null) type = AtomType.find(name, false);

    // no constructor function found, or abstract type specified
    if(type != null && type != AtomType.NOT && type != AtomType.AAT) {
      if(arity == 1) return type;
      throw FUNCARITY_X_X_X.get(ii, name.string(), arguments(arity), 1);
    }

    // include similar function name in error message
    final Levenshtein ls = new Levenshtein();
    for(final AtomType tp : AtomType.VALUES) {
      if(tp.parent == null) continue;
      final byte[] u = tp.name.uri();
      if(eq(u, XS_URI) && tp != AtomType.NOT && tp != AtomType.AAT &&
          ls.similar(lc(ln), lc(tp.string()))) {
        throw FUNCSIMILAR_X_X.get(ii, name.prefixId(), tp.toString());
      }
    }

    // no similar name: constructor function found, or abstract type specified
    throw WHICHFUNC_X.get(ii, name.prefixId());
  }

  /**
   * Returns a built-in function with the specified name.
   * @param name function name
   * @return function if found, {@code null} otherwise
   */
  static FuncDefinition getBuiltIn(final QNm name) {
    final int i = CACHE.id(name.id());
    if(i == 0) return null;
    final FuncDefinition fd = DEFINITIONS.get(i - 1);
    if(!eq(fd.uri(), name.uri())) {
      throw Util.notExpected(name);
    }
    return eq(fd.uri(), name.uri()) ? fd : null;
  }

  /**
   * Checks if the specific URI is statically available.
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
   * Returns a built-in function with the specified name and arity.
   * Raises an error if the function is found, but has a different arity.
   * @param name function name
   * @param arity number of arguments
   * @param ii input info
   * @return function if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static FuncDefinition getBuiltIn(final QNm name, final long arity, final InputInfo ii)
      throws QueryException {

    final FuncDefinition fd = getBuiltIn(name);
    if(fd == null) return null;

    final int min = fd.minMax[0], max = fd.minMax[1];
    if(arity >= min && arity <= max) return fd;

    final IntList arities = new IntList();
    if(max != Integer.MAX_VALUE) {
      for(int m = min; m <= max; m++) arities.add(m);
    }
    throw wrongArity(fd, arity, arities, ii);
  }

  /**
   * Raises an error for the wrong number of function arguments.
   * @param function function
   * @param arity number of supplied arguments
   * @param arities expected arities
   * @param ii input info
   * @return error
   */
  public static QueryException wrongArity(final Object function, final long arity,
      final IntList arities, final InputInfo ii) {

    final int as = arities.ddo().size();
    if(as == 0) return FUNCARITY_X_X.get(ii, function, arguments(arity));

    int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    for(int a = 0; a < as; a++) {
      final int m = arities.get(a);
      if(m < min) min = m;
      if(m > max) max = m;
    }

    final StringBuilder ext = new StringBuilder();
    if(as > 2 && max - min + 1 == as) {
      ext.append(min).append('-').append(max);
    } else {
      for(int a = 0; a < as; a++) {
        if(a != 0) ext.append(a + 1 < as ? ", " : " or ");
        ext.append(arities.get(a));
      }
    }
    return FUNCARITY_X_X_X.get(ii, function, arguments(arity), ext);
  }

  /**
   * Returns the specified function.
   * @param name function qname
   * @param args optional arguments
   * @param sc static context
   * @param ii input info
   * @return function instance
   * @throws QueryException query exception
   */
  private static StandardFunc get(final QNm name, final Expr[] args, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    final FuncDefinition fd = getBuiltIn(name, args.length, ii);
    return fd == null ? null : fd.function.get(sc, ii, args);
  }

  /**
   * Creates either a {@link FuncItem} or a {@link Closure} depending on when the method is called.
   * At parse and compile time a closure is generated to enable inlining and compilation, at
   * runtime we directly generate a function item.
   * @param anns function annotations
   * @param name function name, may be {@code null}
   * @param params formal parameters
   * @param ft function type
   * @param expr function body
   * @param vs variable scope
   * @param ii input info
   * @param runtime run-time flag
   * @param updating flag for updating functions
   * @return the function expression
   */
  private static Expr closureOrFItem(final AnnList anns, final QNm name, final Var[] params,
      final FuncType ft, final Expr expr, final VarScope vs, final InputInfo ii,
      final boolean runtime, final boolean updating) {
    return runtime ? new FuncItem(vs.sc, anns, name, params, ft, expr, vs.stackSize(), ii) :
      new Closure(ii, name, updating ? SeqType.EMP : ft.declType, params, expr, anns, null, vs);
  }

  /**
   * Gets a function literal for a known function.
   * @param name function name
   * @param arity number of arguments
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @param runtime {@code true} if this method is called at run-time, {@code false} otherwise
   * @return function literal if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public static Expr getLiteral(final QNm name, final int arity, final QueryContext qc,
      final StaticContext sc, final InputInfo ii, final boolean runtime) throws QueryException {

    // parse type constructors
    if(eq(name.uri(), XS_URI)) {
      final Type type = getCast(name, arity, ii);
      final VarScope vs = new VarScope(sc);
      final Var[] params = { vs.addNew(new QNm(ITEM, ""), SeqType.AAT_ZO, true, qc, ii) };
      final SeqType st = SeqType.get(type, Occ.ZERO_ONE);
      final Expr expr = new Cast(sc, ii, new VarRef(ii, params[0]), st);
      final AnnList anns = new AnnList();
      final FuncType ft = FuncType.get(anns, expr.seqType(), params);
      return closureOrFItem(anns, name, params, ft, expr, vs, ii, runtime, false);
    }

    // built-in functions
    final FuncDefinition fd = getBuiltIn(name, arity, ii);
    if(fd != null) {
      final AnnList anns = new AnnList();
      final VarScope vs = new VarScope(sc);
      final FuncType ft = fd.type(arity, anns);
      final QNm[] names = fd.paramNames(arity);
      final Var[] params = new Var[arity];
      final Expr[] args = new Expr[arity];
      for(int i = 0; i < arity; i++) {
        params[i] = vs.addNew(names[i], ft.argTypes[i], true, qc, ii);
        args[i] = new VarRef(ii, params[i]);
      }

      final StandardFunc sf = fd.function.get(sc, ii, args);
      final boolean upd = sf.has(Flag.UPD);
      if(upd) {
        anns.add(new Ann(ii, Annotation.UPDATING));
        qc.updating();
      }
      // context/positional access must be bound to original focus
      // example for invalid query: let $f := last#0 return (1,2)[$f()]
      return sf.has(Flag.CTX)
          ? new FuncLit(anns, name, params, sf, ft.seqType(), vs, ii)
          : closureOrFItem(anns, name, params, fd.type(arity, anns), sf, vs, ii, runtime, upd);
    }

    // user-defined function
    final StaticFunc sf = qc.funcs.get(name, arity);
    if(sf != null) {
      final FuncType ft = sf.funcType();
      final VarScope vs = new VarScope(sc);
      final Var[] params = new Var[arity];
      final Expr[] args = new Expr[arity];
      for(int a = 0; a < arity; a++) {
        params[a] = vs.addNew(sf.paramName(a), ft.argTypes[a], true, qc, ii);
        args[a] = new VarRef(ii, params[a]);
      }
      final boolean upd = sf.updating;
      final TypedFunc tf = qc.funcs.undeclaredFuncCall(sf.name, args, sc, ii);
      final Expr func = closureOrFItem(tf.anns, sf.name, params, ft, tf.func, vs, ii,
          runtime, upd);
      if(upd) qc.updating();
      return func;
    }

    // Java function
    final SeqType[] sts = new SeqType[arity];
    Arrays.fill(sts, SeqType.ITEM_ZM);
    final AnnList anns = new AnnList();
    final SeqType st = FuncType.get(anns, SeqType.ITEM_ZM, sts).seqType();
    final VarScope vs = new VarScope(sc);
    final Var[] params = new Var[arity];
    final Expr[] args = new Expr[arity];
    final int vl = params.length;
    for(int v = 0; v < vl; v++) {
      params[v] = vs.addNew(new QNm(ARG + (v + 1), ""), null, true, qc, ii);
      args[v] = new VarRef(ii, params[v]);
    }
    final JavaCall jf = JavaCall.get(name, args, qc, sc, ii);
    return jf == null ? null : new FuncLit(anns, name, params, jf, st, vs, ii);
  }

  /**
   * Returns a function item for a user-defined function.
   * @param sf static function
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return resulting value
   * @throws QueryException query exception
   */
  public static FuncItem getUser(final StaticFunc sf, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    final FuncType ft = sf.funcType();
    final VarScope vs = new VarScope(sc);
    final int arity = sf.params.length;
    final Var[] args = new Var[arity];
    final int al = args.length;
    final Expr[] calls = new Expr[al];
    for(int a = 0; a < al; a++) {
      args[a] = vs.addNew(sf.paramName(a), ft.argTypes[a], true, qc, ii);
      calls[a] = new VarRef(ii, args[a]);
    }
    final TypedFunc tf = qc.funcs.undeclaredFuncCall(sf.name, calls, sc, ii);
    return new FuncItem(sc, tf.anns, sf.name, args, ft, tf.func, vs.stackSize(), ii);
  }

  /**
   * Returns a function call with the specified name and number of arguments.
   * @param name name of the function
   * @param args optional arguments
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return function call
   * @throws QueryException query exception
   */
  public static Expr get(final QNm name, final Expr[] args, final QueryContext qc,
      final StaticContext sc, final InputInfo ii) throws QueryException {

    // type constructors
    if(eq(name.uri(), XS_URI)) {
      final Type type = getCast(name, args.length, ii);
      final SeqType st = SeqType.get(type, Occ.ZERO_ONE);
      return new Cast(sc, ii, args[0], st);
    }

    // built-in functions
    final StandardFunc sf = get(name, args, sc, ii);
    if(sf != null) {
      if(sf.definition.has(Flag.UPD)) qc.updating();
      return sf;
    }

    // user-defined function
    final TypedFunc tf = qc.funcs.funcCall(name, args, sc, ii);
    if(tf != null) {
      if(tf.anns.contains(Annotation.UPDATING)) qc.updating();
      return tf.func;
    }

    // Java function
    final JavaCall jf = JavaCall.get(name, args, qc, sc, ii);
    if(jf != null) return jf;

    // user-defined function that has not been declared yet
    return qc.funcs.undeclaredFuncCall(name, args, sc, ii).func;
  }

  /**
   * Returns an exception if the name of a built-in function is similar to the specified name.
   * @param name name of input function
   * @param ii input info
   * @return query exception or {@code null}
   */
  static QueryException similarError(final QNm name, final InputInfo ii) {
    // find similar function in three runs
    final byte[] local = name.local(), uri = name.uri();
    final Levenshtein ls = new Levenshtein();
    for(int mode = 0; mode < 3; mode++) {
      for(final byte[] key : CACHE) {
        final int i = indexOf(key, '}');
        final byte[] slocal = substring(key, i + 1), suri = substring(key, 2, i);
        if(mode == 0 ?
          // find functions with identical URIs and similar local names
          eq(uri, suri) && ls.similar(local, slocal) : mode == 1 ?
          // find functions with identical local names
          eq(local, substring(key, i + 1)) :
          // find functions with identical URIs and local names that start with the specified name
          eq(uri, substring(key, 2, i)) && startsWith(substring(key, i + 1), local)) {
          final QNm sim = new QNm(slocal, suri);
          return FUNCSIMILAR_X_X.get(ii, name.prefixId(), sim.prefixId());
        }
      }
    }
    return null;
  }

  /*
   * Returns the names of all functions. Used to update MediaWiki syntax highlighter.
   * All function names are listed in reverse order to give precedence to longer names.
   * @param args ignored
  public static void main(final String... args) {
    final org.basex.util.list.StringList sl = new org.basex.util.list.StringList();
    for(FuncSignature sig : SIGNATURES) {
      sl.add(sig.toString().replaceAll("^fn:|\\(.*", ""));
    }
    for(final String s : sl.sort(false, false)) {
      Util.out(s + ' ');
    }
    Util.outln("fn:");
  }
   */
}
