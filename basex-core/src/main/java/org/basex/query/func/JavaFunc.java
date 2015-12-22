package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.lang.reflect.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Java function binding.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class JavaFunc extends JavaFunction {
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
    super(sc, info, args, Perm.ADMIN);
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
    for(final Constructor<?> cons : clazz.getConstructors()) {
      final Object[] jargs = javaArgs(cons.getParameterTypes(), null, ar, true);
      if(jargs != null) return cons.newInstance(jargs);
    }
    throw JAVACONSTR_X_X.get(info, name(), foundArgs(ar));
  }

  /**
   * Calls a method.
   * @param args arguments
   * @param qc query context
   * @return resulting object
   * @throws Exception exception
   */
  private Object method(final Value[] args, final QueryContext qc) throws Exception {
    // check if a field with the specified name exists
    try {
      final Field f = clazz.getField(method);
      final boolean st = Modifier.isStatic(f.getModifiers());
      if(args.length == (st ? 0 : 1)) return f.get(st ? null : instObj(args[0]));
    } catch(final NoSuchFieldException ex) { /* ignored */ }

    Method meth = null;
    Object inst = null;
    Object[] margs = null;
    for(final Method m : clazz.getMethods()) {
      if(!m.getName().equals(method)) continue;
      final boolean st = Modifier.isStatic(m.getModifiers());
      final Class<?>[] pTypes = m.getParameterTypes();
      final Object[] jArgs = javaArgs(pTypes, null, args, st);
      if(jArgs != null) {
        if(meth != null) throw JAVAAMBIG_X.get(info, Util.className(clazz) + '.' +
            method + '#' + pTypes.length);
        meth = m;
        margs = jArgs;

        if(!st) {
          inst = instObj(args[0]);
          if(inst instanceof QueryModule) {
            final QueryModule mod = (QueryModule) inst;
            mod.staticContext = sc;
            mod.queryContext = qc;
          }
        }
      }
    }
    if(meth != null) return meth.invoke(inst, margs);

    throw JAVAMETHOD_X_X.get(info, name(), foundArgs(args));
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
   * Returns the function descriptor.
   * @return string
   */
  private String name() {
    return Util.className(clazz) + '.' + method;
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

  @Override
  public String toString() {
    return clazz + "." + method + PAREN1 + toString(SEP) + PAREN2;
  }
}
