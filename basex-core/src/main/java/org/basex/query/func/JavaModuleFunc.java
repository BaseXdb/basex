package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.lang.reflect.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.QueryModule.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.Type;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.Array;
import org.basex.util.hash.*;

/**
 * Java function binding.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class JavaModuleFunc extends JavaFunction {
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
   * @param perm required permission
   */
  JavaModuleFunc(final StaticContext sc, final InputInfo info, final Object module,
      final Method method, final Expr[] args, final Perm perm) {

    super(sc, info, args, perm);
    this.module = module;
    this.method = method;
    params = method.getParameterTypes();
    vTypes = values(params);
  }

  @Override
  public boolean isVacuous() {
    return method.getReturnType() == Void.TYPE;
  }

  @Override
  protected Object eval(final Value[] args, final QueryContext qc) throws QueryException {
    // assign context if module is inheriting {@link QueryModule}
    if(module instanceof QueryModule) {
      final QueryModule mod = (QueryModule) module;
      mod.staticContext = sc;
      mod.queryContext = qc;
    }

    final Object[] jargs = javaArgs(params, vTypes, args, true);
    if(jargs != null) {
      try {
        return method.invoke(module, jargs);
      } catch(final Exception ex) {
        final Throwable th = Util.rootException(ex);
        if(th instanceof QueryException) throw ((QueryException) th).info(info);
        throw JAVAERROR_X_X_X.get(info, name(), foundArgs(args), th);
      }
    }

    // compose error message: expected arguments
    final TokenBuilder expect = new TokenBuilder();
    for(final Class<?> param : method.getParameterTypes()) {
      if(!expect.isEmpty()) expect.add(", ");
      final Type t = JavaMapping.type(param, false);
      expect.add(t != null ? t.toString() : Util.className(param));
    }
    final String name = method.getName();
    throw JAVAARGS_X_X_X.get(info, name, expect, foundArgs(args));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new JavaModuleFunc(sc, info, module, method, copyAll(cc, vm, exprs), perm);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAME, name()), exprs);
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
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof JavaModuleFunc)) return false;
    final JavaModuleFunc j = (JavaModuleFunc) obj;
    return module.equals(j.module) && method.equals(j.method) && Array.equals(params, j.params) &&
        super.equals(obj);
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.NDT && method.getAnnotation(Deterministic.class) == null ||
      (flag == Flag.CTX || flag == Flag.POS) &&
      method.getAnnotation(FocusDependent.class) == null ||
      super.has(flag);
  }

  @Override
  public String toString() {
    return name() + toString(SEP);
  }
}
