package org.basex.query.func;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryModule;
import static org.basex.query.QueryText.*;
import org.basex.query.expr.Expr;
import org.basex.query.item.Value;
import static org.basex.query.util.Err.JAVAERR;
import static org.basex.query.util.Err.JAVAMOD;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Java function binding.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class JavaModuleFunc extends JavaMapping {
  /** Java module. */
  private final QueryModule module;
  /** Java method. */
  private final Method mth;

  /**
   * Constructor.
   * @param ii input info
   * @param jm Java module
   * @param m Java method/field
   * @param a arguments
   */
  JavaModuleFunc(final InputInfo ii, final QueryModule jm, final Method m,
      final Expr[] a) {
    super(ii, a);
    module = jm;
    mth = m;
  }

  @Override
  protected Object eval(final Value[] args) throws QueryException {
    try {
      try {
        return mth.invoke(module, (Object[]) args);
      } catch(final IllegalArgumentException iae) {
        final Object[] ar = new Object[args.length];
        for(int a = 0; a < args.length; a++) ar[a] = args[a].toJava();
        return mth.invoke(module, ar);
      }
    } catch(final InvocationTargetException ex) {
      throw JAVAERR.thrw(input, ex.getCause());
    } catch(final Throwable ex) {
      final TokenBuilder found = new TokenBuilder();
      for(final Value a : args) {
        if(found.size() != 0) found.add(", ");
        found.addExt(a.type);
      }
      throw JAVAMOD.thrw(input, signature(), name() + '(' + found + ')');
    }
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, NAM, Token.token(name()));
    for(final Expr arg : expr) arg.plan(ser);
    ser.closeElement();
  }

  @Override
  public String description() {
    return name() + " method";
  }

  /**
   * Returns the function descriptor.
   * @return string
   */
  private String name() {
    return module.getClass().getSimpleName() + "." + mth.getName();
  }

  /**
   * Returns the function signature.
   * @return string
   */
  private String signature() {
    final TokenBuilder exp = new TokenBuilder();
    for(final Class<?> c : mth.getParameterTypes()) {
      if(exp.size() != 0) exp.add(", ");
      exp.add(c.getSimpleName());
    }
    return name() + '(' + exp + ')';
  }

  @Override
  public String toString() {
    return name() + PAR1 + toString(SEP) + PAR2;
  }
}
