package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class JavaModuleFunc extends JavaMapping {
  /** Java module. */
  private final Object module;
  /** Method to be called. */
  private final Method method;
  /** Method parameters. */
  private final Class<?>[] params;
  /** Indicates if function parameters are of (sub)class {@link Value}. */
  private final boolean[] vTypes;

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param module Java module
   * @param method Java method/field
   * @param args arguments
   */
  JavaModuleFunc(final StaticContext sc, final InputInfo info, final Object module,
      final Method method, final Expr[] args) {

    super(sc, info, args);
    this.module = module;
    this.method = method;
    params = method.getParameterTypes();
    vTypes = JavaFunc.values(params);
  }

  @Override
  public boolean isVacuous() {
    return method.getReturnType() == Void.TYPE;
  }

  @Override
  protected Object eval(final Value[] vals, final QueryContext qc) throws QueryException {
    // assign context if module is inheriting {@link QueryModule}
    if(module instanceof QueryModule) {
      final QueryModule mod = (QueryModule) module;
      mod.staticContext = sc;
      mod.queryContext = qc;
    }

    final Object[] args = JavaFunc.args(params, vTypes, vals, true);
    if(args != null) {
      try {
        return method.invoke(module, args);
      } catch(final Exception ex) {
        Throwable e = ex;
        if(e.getCause() != null) {
          Util.debug(e);
          e = e.getCause();
        }
        throw e instanceof QueryException ? ((QueryException) e).info(info) :
          JAVAERROR_X.get(info, e);
      }
    }

    // compose error message: expected arguments
    final TokenBuilder expect = new TokenBuilder();
    for(final Class<?> c : method.getParameterTypes()) {
      if(!expect.isEmpty()) expect.add(", ");
      expect.add(Util.className(c));
    }
    throw JAVAARGS_X_X.get(info, method.getName() + '(' + expect + ')',
        method.getName() + '(' + foundArgs(vals) + ')');
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new JavaModuleFunc(sc, info, module, method, copyAll(qc, scp, vs, exprs));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, name()), exprs);
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
    return Util.className(module) + ':' + method.getName();
  }

  @Override
  public String toString() {
    return name() + PAREN1 + toString(SEP) + PAREN2;
  }

  @Override
  public boolean has(final Flag f) {
    return f == Flag.NDT && method.getAnnotation(Deterministic.class) == null ||
      (f == Flag.CTX || f == Flag.FCS) &&
      method.getAnnotation(FocusDependent.class) == null ||
      super.has(f);
  }
}
