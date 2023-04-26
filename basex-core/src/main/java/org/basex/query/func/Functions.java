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
   * Tries to resolve the specified function with xs namespace as a cast.
   * @param arity number of arguments
   * @param name function name
   * @param ii input info
   * @return cast type if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Type getCast(final QNm name, final long arity, final InputInfo ii)
      throws QueryException {

    Type type = ListType.find(name);
    if(type == null) type = AtomType.find(name, false);
    if(type == null) throw WHICHFUNC_X.get(ii, AtomType.similar(name));
    if(type == AtomType.NOTATION || type == AtomType.ANY_ATOMIC_TYPE)
      throw ABSTRACTFUNC_X.get(ii, name.prefixId());
    if(arity != 1) throw FUNCARITY_X_X_X.get(ii, name.string(), arguments(arity), 1);
    return type;
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
   * @param fd function definition
   * @param arity number of supplied arguments
   * @param ii input info
   * @return query exception
   */
  public static QueryException wrongArity(final FuncDefinition fd, final int arity,
      final InputInfo ii) {
    final IntList arities = new IntList();
    if(!fd.variadic()) {
      final int min = fd.minMax[0], max = fd.minMax[1];
      for(int m = min; m <= max; m++) arities.add(m);
    }
    return wrongArity(fd, arity, arities, ii);
  }

  /**
   * Raises an error for the wrong number of function arguments.
   * @param function function
   * @param arity number of supplied arguments
   * @param arities expected arities
   * @param ii input info
   * @return error
   */
  public static QueryException wrongArity(final Object function, final int arity,
      final IntList arities, final InputInfo ii) {

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

    final int arity = args.length, min = fd.minMax[0], max = fd.minMax[1];
    if(arity <= max) {
      if(keywords != null) {
        final ExprList list = new ExprList().add(args);
        for(final QNm qnm : keywords) {
          final int i = fd.indexOf(qnm);
          if(i == -1) throw KEYWORDUNKNOWN_X_X.get(ii, fd, qnm);
          if(list.get(i) != null) throw ARGTWICE_X_X.get(ii, fd, qnm);
          list.set(i, keywords.get(qnm));
        }
        // assign dummy arguments
        for(int l = list.size() - 1; l >= 0; l--) {
          if(list.get(l) == null) {
            if(l < min) throw ARGMISSING_X_X.get(ii, fd, fd.names[l].prefixString());
            list.set(l, Empty.UNDEFINED);
          }
        }
        return fd.get(sc, ii, list.finish());
      } else if(arity >= min) {
        return fd.get(sc, ii, args);
      }
    }
    throw wrongArity(fd, arity, ii);
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
   * @param runtime runtime flag
   * @param updating flag for updating functions
   * @return the function expression
   */
  private static Expr closureOrFItem(final AnnList anns, final QNm name, final Var[] params,
      final FuncType ft, final Expr expr, final VarScope vs, final InputInfo ii,
      final boolean runtime, final boolean updating) {
    return runtime ? new FuncItem(vs.sc, anns, name, params, ft, expr, vs.stackSize(), ii) :
      new Closure(ii, name, updating ? SeqType.EMPTY_SEQUENCE_Z : ft.declType,
        params, expr, anns, null, vs);
  }

  /**
   * Gets a function literal for a known function.
   * @param name function name
   * @param arity number of arguments
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @param runtime {@code true} if this method is called at runtime, {@code false} otherwise
   * @return function literal if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public static Expr getLiteral(final QNm name, final int arity, final QueryContext qc,
      final StaticContext sc, final InputInfo ii, final boolean runtime) throws QueryException {

    // parse type constructor
    if(eq(name.uri(), XS_URI)) {
      final Type type = getCast(name, arity, ii);
      final VarScope vs = new VarScope(sc);
      final Var[] params = {
        vs.addNew(new QNm(ITEM, ""), SeqType.ANY_ATOMIC_TYPE_ZO, true, qc, ii)
      };
      final SeqType st = SeqType.get(type, Occ.ZERO_OR_ONE);
      final Expr expr = new Cast(sc, ii, new VarRef(ii, params[0]), st);
      final AnnList anns = new AnnList();
      final FuncType ft = FuncType.get(anns, expr.seqType(), params);
      return closureOrFItem(anns, name, params, ft, expr, vs, ii, runtime, false);
    }

    // built-in function
    final FuncDefinition fd = builtIn(name);
    if(fd != null) {
      if(arity < fd.minMax[0] || arity > fd.minMax[1]) throw wrongArity(fd, arity, ii);

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

      final StandardFunc sf = fd.get(sc, ii, args);
      final boolean upd = sf.has(Flag.UPD);
      if(upd) {
        anns.add(new Ann(ii, Annotation.UPDATING, Empty.VALUE));
        qc.updating();
      }
      // context/positional access must be bound to original focus
      // example for invalid query: let $f := last#0 return (1,2)[$f()]
      return sf.has(Flag.CTX)
          ? new FuncLit(anns, name, params, sf, ft.seqType(), vs, ii)
          : closureOrFItem(anns, name, params, fd.type(arity, anns), sf, vs, ii, runtime, upd);
    }

    // user-defined function
    final StaticFunc sf = qc.functions.get(name, arity);
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
      final TypedFunc tf = qc.functions.undeclaredFuncCall(sf.name, args, sc, ii);
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
    final TypedFunc tf = qc.functions.undeclaredFuncCall(sf.name, calls, sc, ii);
    return new FuncItem(sc, tf.anns, sf.name, args, ft, tf.func, vs.stackSize(), ii);
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

    // type constructor
    if(keywords == null && eq(name.uri(), XS_URI)) {
      final Type type = getCast(name, args.length, ii);
      final SeqType st = SeqType.get(type, Occ.ZERO_OR_ONE);
      return new Cast(sc, ii, args[0], st);
    }

    // built-in function
    final StandardFunc sf = builtIn(name, args, keywords, sc, ii);
    if(sf != null) {
      if(sf.updating()) qc.updating();
      return sf;
    }

    // reject keyword parameters for other function types
    if(keywords == null) {
      // user-defined function
      final TypedFunc tf = qc.functions.funcCall(name, args, sc, ii);
      if(tf != null) {
        if(tf.anns.contains(Annotation.UPDATING)) qc.updating();
        return tf.func;
      }

      // Java function
      final JavaCall jf = JavaCall.get(name, args, qc, sc, ii);
      if(jf != null) return jf;
    } else if(!NSGlobal.reserved(name.uri())) {
      throw KEYWORDSUPPORT_X.get(ii, name.prefixString());
    }

    // user-defined function that has not been declared yet
    return qc.functions.undeclaredFuncCall(name, args, sc, ii).func;
  }

  /**
   * Returns an info message for a similar function.
   * @param qname name of type
   * @return info string
   */
  static byte[] similar(final QNm qname) {
    // find similar function in three runs
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
}
