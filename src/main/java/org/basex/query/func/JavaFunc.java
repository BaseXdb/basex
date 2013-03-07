package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.lang.reflect.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.Type;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Java function binding.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class JavaFunc extends JavaMapping {
  /** Java class. */
  private final Class<?> cls;
  /** Java method. */
  private final String mth;

  /**
   * Constructor.
   * @param ii input info
   * @param c Java class
   * @param m Java method/field
   * @param a arguments
   */
  JavaFunc(final InputInfo ii, final Class<?> c, final String m, final Expr[] a) {
    super(ii, a);
    cls = c;
    mth = m;
  }

  @Override
  protected Object eval(final Value[] args, final QueryContext ctx)
      throws QueryException {

    try {
      return mth.equals(NEW) ? constructor(args) : method(args, ctx);
    } catch(final InvocationTargetException ex) {
      final Throwable cause = ex.getCause();
      throw cause instanceof QueryException ? ((QueryException) cause).info(info) :
        JAVAERR.thrw(info, cause);
    } catch(final QueryException ex) {
      throw ex;
    } catch(final Throwable ex) {
      Util.debug(ex);
      throw JAVAFUN.thrw(info, name(), foundArgs(args));
    }
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return new JavaFunc(info, cls, mth, copyAll(ctx, scp, vs, expr));
  }

  /**
   * Calls a constructor.
   * @param ar arguments
   * @return resulting object
   * @throws Exception exception
   */
  private Object constructor(final Value[] ar) throws Exception {
    for(final Constructor<?> con : cls.getConstructors()) {
      final Object[] arg = args(con.getParameterTypes(), ar, true);
      if(arg != null) return con.newInstance(arg);
    }
    throw JAVACON.thrw(info, name(), foundArgs(ar));
  }

  /**
   * Calls a method.
   * @param ar arguments
   * @param ctx query context
   * @return resulting object
   * @throws Exception exception
   */
  private Object method(final Value[] ar, final QueryContext ctx) throws Exception {
    // check if a field with the specified name exists
    try {
      final Field f = cls.getField(mth);
      final boolean st = Modifier.isStatic(f.getModifiers());
      if(ar.length == (st ? 0 : 1)) {
        return f.get(st ? null : instObj(ar[0]));
      }
    } catch(final NoSuchFieldException ex) { /* ignored */ }

    for(final Method meth : cls.getMethods()) {
      if(!meth.getName().equals(mth)) continue;
      final boolean st = Modifier.isStatic(meth.getModifiers());
      final Object[] arg = args(meth.getParameterTypes(), ar, st);
      if(arg != null) {
        Object inst = null;
        if(!st) {
          inst = instObj(ar[0]);
          if(inst instanceof QueryModule) ((QueryModule) inst).context = ctx;
        }
        return meth.invoke(inst, arg);
      }
    }
    throw JAVAMTH.thrw(info, name(), foundArgs(ar));
  }

  /**
   * Creates the instance on which a non-static field getter or method is
   * invoked.
   * @param v XQuery value
   * @return Java object
   * @throws QueryException query exception
   */
  private Object instObj(final Value v) throws QueryException {
    return cls.isInstance(v) ? v : v.toJava();
  }

  /**
   * Checks if the arguments conform with the specified parameters.
   * @param params parameters
   * @param args arguments
   * @param stat static flag
   * @return argument array or {@code null}
   * @throws QueryException query exception
   */
  static Object[] args(final Class<?>[] params, final Value[] args,
      final boolean stat) throws QueryException {

    final int s = stat ? 0 : 1;
    final int l = args.length - s;
    if(l != params.length) return null;

    // function arguments
    final Object[] val = new Object[l];
    int a = 0;

    for(final Class<?> par : params) {
      // check original type
      final Value arg = args[s + a];
      if(par.isInstance(arg)) {
        val[a++] = arg;
        continue;
      }
      // check Java type
      if(arg instanceof Jav) {
        final Object jav = ((Jav) arg).toJava();
        if(par.isInstance(jav)) {
          val[a++] = jav;
          continue;
        }
      }
      // check XQuery type
      final Type jtype = type(par);
      if(jtype == null || !arg.type.instanceOf(jtype) && !jtype.instanceOf(arg.type))
        return null;
      val[a++] = arg.toJava();
    }
    return val;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, cls + "." + mth), expr);
  }

  @Override
  public String description() {
    final StringBuilder sb = new StringBuilder();
    if(mth.equals(NEW)) sb.append(NEW).append(' ').append(cls.getSimpleName());
    else sb.append(name());
    return sb.append("(...)").toString();
  }

  /**
   * Returns the function descriptor.
   * @return string
   */
  private String name() {
    return cls.getSimpleName() + '.' + mth;
  }

  @Override
  public String toString() {
    return cls + "." + mth + PAR1 + toString(SEP) + PAR2;
  }
}
