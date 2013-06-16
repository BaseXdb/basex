package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.lang.reflect.*;

import org.basex.query.*;
import org.basex.query.QueryModule.Deterministic;
import org.basex.query.QueryModule.FocusDependent;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Java function binding.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class JavaModuleFunc extends JavaMapping {
  /** Java module. */
  private final Object module;
  /** Method to be called. */
  private final Method mth;

  /**
   * Constructor.
   * @param ii input info
   * @param jm Java module
   * @param m Java method/field
   * @param a arguments
   */
  JavaModuleFunc(final InputInfo ii, final Object jm, final Method m, final Expr[] a) {
    super(ii, a);
    module = jm;
    mth = m;
  }

  @Override
  protected Object eval(final Value[] vals, final QueryContext ctx)
      throws QueryException {

    // assign context if module is inheriting {@link QueryModule}
    if(module instanceof QueryModule) ((QueryModule) module).context = ctx;

    final Object[] args = JavaFunc.args(mth.getParameterTypes(), vals, true);
    if(args != null) {
      try {
        return mth.invoke(module, args);
      } catch(final Exception ex) {
        Throwable e = ex;
        if(e.getCause() != null) {
          Util.debug(e);
          e = e.getCause();
        }
        throw e instanceof QueryException ? ((QueryException) e).info(info) :
          JAVAERR.thrw(info, e);
      }
    }

    // compose error message: expected arguments
    final TokenBuilder expect = new TokenBuilder();
    for(final Class<?> c : mth.getParameterTypes()) {
      if(!expect.isEmpty()) expect.add(", ");
      expect.add(c.getSimpleName());
    }
    throw JAVAMOD.thrw(info, mth.getName() + '(' + expect + ')',
        mth.getName() + '(' + foundArgs(vals) + ')');
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new JavaModuleFunc(info, module, mth, copyAll(ctx, scp, vs, expr));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, name()), expr);
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
    return module.getClass().getSimpleName() + ':' + mth.getName();
  }

  @Override
  public String toString() {
    return name() + PAR1 + toString(SEP) + PAR2;
  }

  @Override
  public boolean has(final Flag f) {
    return f == Flag.NDT && mth.getAnnotation(Deterministic.class) == null ||
      (f == Flag.CTX || f == Flag.FCS) &&
      mth.getAnnotation(FocusDependent.class) == null ||
      super.has(f);
  }
}
