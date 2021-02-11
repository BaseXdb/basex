package org.basex.query.func.java;

import static org.basex.query.QueryText.*;

import java.lang.reflect.*;

import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.QueryModule.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.Array;
import org.basex.util.hash.*;

/**
 * Static invocation of a function in an imported Java class instance.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StaticJavaCall extends JavaCall {
  /** Java module. */
  private final Object module;
  /** Method to be called. */
  private final Method method;
  /** Method parameter types. */
  private final Class<?>[] params;
  /** Indicates if function parameters are of (sub)class {@link Value}. */
  private final boolean[] values;

  /**
   * Constructor.
   * @param module Java module
   * @param method Java method/field
   * @param args arguments
   * @param perm required permission
   * @param updating updating flag
   * @param sc static context
   * @param info input info
   */
  StaticJavaCall(final Object module, final Method method, final Expr[] args, final Perm perm,
      final boolean updating, final StaticContext sc, final InputInfo info) {

    super(args, perm, updating, sc, info);
    this.module = module;
    this.method = method;
    params = method.getParameterTypes();

    final int pl = params.length;
    values = new boolean[pl];
    for(int p = 0; p < pl; p++) values[p] = Value.class.isAssignableFrom(params[p]);
  }

  @Override
  public boolean vacuous() {
    return method.getReturnType() == Void.TYPE;
  }

  @Override
  protected Object eval(final QueryContext qc) throws QueryException {
    final JavaEval je = new JavaEval(this, qc);
    if(je.match(params, true, values)) {
      // assign query context if module is inheriting the {@link QueryModule} interface
      if(module instanceof QueryModule) {
        final QueryModule qm = (QueryModule) module;
        qm.staticContext = sc;
        qm.queryContext = qc;
      }

      // invoke found method
      try {
        return method.invoke(module, je.args);
      } catch(final Throwable th) {
        throw je.execError(th);
      }
    }
    // arguments could not be matched: raise error
    throw je.argsError(method, false);
  }

  @Override
  public StaticJavaCall copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new StaticJavaCall(module, method, copyAll(cc, vm, exprs), perm, updating, sc,
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
    return Util.className(module) + COL + method.getName();
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof StaticJavaCall)) return false;
    final StaticJavaCall j = (StaticJavaCall) obj;
    return module.equals(j.module) && method.equals(j.method) && Array.equals(params, j.params) &&
        super.equals(obj);
  }
}
