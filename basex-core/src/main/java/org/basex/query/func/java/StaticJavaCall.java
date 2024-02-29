package org.basex.query.func.java;

import static org.basex.query.QueryError.*;

import java.lang.reflect.*;

import org.basex.core.MainOptions.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.QueryModule.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.Array;
import org.basex.util.hash.*;

/**
 * Static invocation of a function in an imported Java class instance.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class StaticJavaCall extends JavaCall {
  /** Java module. */
  private final Object module;
  /** Method to be called. */
  private final Method method;
  /** Method parameter types. */
  private final Class<?>[] params;

  /**
   * Constructor.
   * @param module Java module
   * @param method Java method/field
   * @param args arguments
   * @param perm required permission
   * @param updating updating flag
   * @param info input info (can be {@code null})
   */
  StaticJavaCall(final Object module, final Method method, final Expr[] args, final Perm perm,
      final boolean updating, final InputInfo info) {

    super(args, perm, updating, info);
    this.module = module;
    this.method = method;
    params = method.getParameterTypes();

    final int pl = params.length;
    xquery = new boolean[pl];
    for(int p = 0; p < pl; p++) xquery[p] = Value.class.isAssignableFrom(params[p]);
  }

  @Override
  public boolean vacuous() {
    return method.getReturnType() == Void.TYPE;
  }

  @Override
  protected Value eval(final QueryContext qc, final WrapOptions wrap) throws QueryException {
    // arguments could not be matched: raise error
    final JavaCandidate jc = candidate(values(qc), params, true);
    if(jc == null) throw JAVAARGS_X_X_X.get(info, name(),
        JavaCall.paramTypes(method, true), argTypes(exprs));

    // assign query context if module is inheriting the {@link QueryModule} interface
    if(module instanceof QueryModule) {
      final QueryModule qm = (QueryModule) module;
      qm.staticContext = sc();
      qm.queryContext = qc;
    }

    // invoke found method
    try {
      final Object result = method.invoke(module, jc.arguments);
      if(wrap == WrapOptions.INSTANCE) return new XQJava(module);
      if(wrap == WrapOptions.VOID) return Empty.VALUE;
      return toValue(result, qc, info, wrap);
    } catch(final Throwable th) {
      throw executionError(th, jc.arguments);
    }
  }

  @Override
  public StaticJavaCall copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new StaticJavaCall(module, method, copyAll(cc, vm, exprs), perm, updating,
        info));
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.UPD.in(flags) && method.getAnnotation(Updating.class) != null ||
      Flag.NDT.in(flags) && method.getAnnotation(Deterministic.class) == null ||
      (Flag.CTX.in(flags) || Flag.POS.in(flags)) &&
      method.getAnnotation(FocusDependent.class) != null ||
      super.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return (ic.var != null || method.getAnnotation(FocusDependent.class) == null) &&
        super.inlineable(ic);
  }

  @Override
  String desc() {
    return name();
  }

  @Override
  String name() {
    return className(module.getClass()) + ':' + method.getName();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof StaticJavaCall)) return false;
    final StaticJavaCall java = (StaticJavaCall) obj;
    return module.equals(java.module) && method.equals(java.method) &&
        Array.equals(params, java.params) && super.equals(obj);
  }
}
