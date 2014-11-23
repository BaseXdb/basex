package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.lang.reflect.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Java function binding.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class JavaFunc extends JavaMapping {
  /** Java class. */
  private final Class<?> clazz;
  /** Java method. */
  private final String method;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param clazz Java class
   * @param method Java method/field
   * @param args arguments
   */
  JavaFunc(final StaticContext sc, final InputInfo info, final Class<?> clazz, final String method,
      final Expr[] args) {
    super(sc, info, args);
    this.clazz = clazz;
    this.method = method;
  }

  @Override
  protected Object eval(final Value[] args, final QueryContext qc) throws QueryException {
    try {
      return method.equals(NEW) ? constructor(args) : method(args, qc);
    } catch(final InvocationTargetException ex) {
      final Throwable cause = ex.getCause();
      throw cause instanceof QueryException ? ((QueryException) cause).info(info) :
        JAVAERROR_X.get(info, cause);
    } catch(final QueryException ex) {
      throw ex;
    } catch(final Throwable ex) {
      Util.debug(ex);
      throw JAVACALL_X_X.get(info, name(), foundArgs(args));
    }
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new JavaFunc(sc, info, clazz, method, copyAll(qc, scp, vs, exprs));
  }

  /**
   * Calls a constructor.
   * @param ar arguments
   * @return resulting object
   * @throws Exception exception
   */
  private Object constructor(final Value[] ar) throws Exception {
    for(final Constructor<?> con : clazz.getConstructors()) {
      final Object[] arg = args(con.getParameterTypes(), null, ar, true);
      if(arg != null) return con.newInstance(arg);
    }
    throw JAVACONSTR_X_X.get(info, name(), foundArgs(ar));
  }

  /**
   * Calls a method.
   * @param ar arguments
   * @param qc query context
   * @return resulting object
   * @throws Exception exception
   */
  private Object method(final Value[] ar, final QueryContext qc) throws Exception {
    // check if a field with the specified name exists
    try {
      final Field f = clazz.getField(method);
      final boolean st = Modifier.isStatic(f.getModifiers());
      if(ar.length == (st ? 0 : 1)) {
        return f.get(st ? null : instObj(ar[0]));
      }
    } catch(final NoSuchFieldException ex) { /* ignored */ }

    for(final Method meth : clazz.getMethods()) {
      if(!meth.getName().equals(method)) continue;
      final boolean st = Modifier.isStatic(meth.getModifiers());
      final Object[] arg = args(meth.getParameterTypes(), null, ar, st);
      if(arg != null) {
        Object inst = null;
        if(!st) {
          inst = instObj(ar[0]);
          if(inst instanceof QueryModule) {
            final QueryModule mod = (QueryModule) inst;
            mod.staticContext = sc;
            mod.queryContext = qc;
          }
        }
        return meth.invoke(inst, arg);
      }
    }
    throw JAVAMETHOD_X_X.get(info, name(), foundArgs(ar));
  }

  /**
   * Creates the instance on which a non-static field getter or method is
   * invoked.
   * @param v XQuery value
   * @return Java object
   * @throws QueryException query exception
   */
  private Object instObj(final Value v) throws QueryException {
    return clazz.isInstance(v) ? v : v.toJava();
  }

  /**
   * Converts the arguments to objects that match the specified function parameters.
   * {@code null} is returned if conversion is not possible.
   * @param params parameters
   * @param vTypes value types
   * @param args arguments
   * @param stat static flag
   * @return argument array or {@code null}
   * @throws QueryException query exception
   */
  static Object[] args(final Class<?>[] params, final boolean[] vTypes, final Value[] args,
      final boolean stat) throws QueryException {

    final int s = stat ? 0 : 1;
    final int l = args.length - s;
    if(l != params.length) return null;

    // function arguments
    final boolean[] vType = vTypes == null ? values(params) : vTypes;
    final Object[] vals = new Object[l];
    for(int a = 0; a < l; a++) {
      final Class<?> param = params[a];
      final Value arg = args[s + a];

      if(arg.type.instanceOf(type(param))) {
        // convert to Java object if an XQuery type exists for the function parameter
        vals[a] = arg.toJava();
      } else {
        // convert to Java object if
        // - argument is of type {@link Jav}, wrapping a Java object, or
        // - function parameter is not of type {@link Value}, or a sub-class of it
        vals[a] = arg instanceof Jav || !vType[a] ? arg.toJava() : arg;
        // abort conversion if argument is not an instance of function parameter
        if(!param.isInstance(vals[a])) return null;
      }
    }
    return vals;
  }

  /**
   * Returns a boolean array that indicated which of the specified function parameters are of
   * (sub)class {@link Value}.
   * @param params parameters
   * @return array
   */
  static boolean[] values(final Class<?>[] params) {
    final int l = params.length;
    final boolean[] vals = new boolean[l];
    for(int a = 0; a < l; a++) vals[a] = Value.class.isAssignableFrom(params[a]);
    return vals;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, clazz.getName() + '.' + method), exprs);
  }

  @Override
  public String description() {
    final StringBuilder sb = new StringBuilder();
    if(method.equals(NEW)) sb.append(NEW).append(' ').append(Util.className(clazz));
    else sb.append(name());
    return sb.append("(...)").toString();
  }

  /**
   * Returns the function descriptor.
   * @return string
   */
  private String name() {
    return Util.className(clazz) + '.' + method;
  }

  @Override
  public String toString() {
    return clazz + "." + method + PAREN1 + toString(SEP) + PAREN2;
  }
}
