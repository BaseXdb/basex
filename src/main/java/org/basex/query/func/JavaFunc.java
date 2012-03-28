package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.io.*;
import java.lang.reflect.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.query.item.Type;
import org.basex.util.*;

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
      throw cause instanceof QueryException ? ((QueryException) cause).info(input) :
        JAVAERR.thrw(input, cause);
    } catch(final Throwable ex) {
      // compose found arguments
      final TokenBuilder found = new TokenBuilder();
      for(final Value a : args) {
        if(!found.isEmpty()) found.add(", ");
        found.addExt(a.type);
      }
      throw JAVAFUN.thrw(input, name(), found);
    }
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
    throw new Exception();
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
          if(inst instanceof QueryModule) ((QueryModule) inst).init(ctx);
        }
        return meth.invoke(inst, arg);
      }
    }
    throw new Exception();
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
  private static Object[] args(final Class<?>[] params, final Value[] args,
      final boolean stat) throws QueryException {

    final int s = stat ? 0 : 1;
    final int l = args.length - s;
    if(l != params.length) return null;

    // function arguments
    final Object[] val = new Object[l];
    int a = 0;

    for(final Class<?> par : params) {
      final Value arg = args[s + a];

      final Object next;
      if(par.isInstance(arg)) {
        next = arg;
      } else {
        final Type jtype = type(par);
        if(jtype == null || !arg.type.instanceOf(jtype)
            && !jtype.instanceOf(arg.type)) return null;
        next = arg.toJava();
      }
      val[a++] = next;
    }
    return val;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, Token.token(cls + "." + mth));
    for(final Expr arg : expr) arg.plan(ser);
    ser.closeElement();
  }

  @Override
  public String description() {
    return name() + (mth.equals(NEW) ? " constructor" : " method");
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
