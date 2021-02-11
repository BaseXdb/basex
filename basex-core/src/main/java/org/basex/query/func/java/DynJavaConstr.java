package org.basex.query.func.java;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Dynamic invocation of a Java constructor.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class DynJavaConstr extends DynJavaCall {
  /** Constructor candidates. */
  private ArrayList<Constructor<?>> constrs;

  /**
   * Constructor.
   * @param clazz Java class
   * @param types types provided in the parsed expression string (can be {@code null})
   * @param args arguments
   * @param sc static context
   * @param info input info
   */
  DynJavaConstr(final Class<?> clazz, final String[] types, final Expr[] args,
      final StaticContext sc, final InputInfo info) {
    super(clazz, types, args, sc, info);
  }

  /**
   * Initializes the dynamic call.
   * @param enforce enforce checks
   * @return {@code true} if at least one candidate for invocation was found
   * @throws QueryException query exception
   */
  public boolean init(final boolean enforce) throws QueryException {
    final int el = exprs.length;

    // method candidates: no check supplied expression: class instance or does not
    constrs = new ArrayList<>();
    for(final Constructor<?> c : clazz.getConstructors()) {
      final Class<?>[] params = c.getParameterTypes();
      if(params.length == el && typesMatch(params, types)) constrs.add(c);
    }
    if(!constrs.isEmpty()) return true;
    if(!enforce) return false;

    throw JAVACONSTR_X_X.get(info, name(), el);
  }

  @Override
  protected Object eval(final QueryContext qc) throws QueryException {
    // loop through all constructors, compare types
    final JavaEval je = new JavaEval(this, qc);
    Constructor<?> constr = null;
    for(final Constructor<?> c : constrs) {
      // do not invoke function if arguments do not match
      if(!je.match(c.getParameterTypes(), true, null)) continue;

      if(constr != null) throw je.multipleError(DYNMULTICONS_X_X);
      constr = c;
    }

    // constructor found: instantiate class
    if(constr != null) {
      try {
        return constr.newInstance(je.args);
      } catch(final Exception ex) {
        throw je.execError(ex);
      }
    }

    // no constructor found: raise error
    throw je.argsError(constrs.get(0), constrs.size() > 1);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final DynJavaConstr c = new DynJavaConstr(clazz, types, copyAll(cc, vm, exprs), sc, info);
    c.constrs = constrs;
    return copyType(c);
  }

  @Override
  String desc() {
    return QNm.eqName(JAVAPREF + clazz.getName(), NEW);
  }

  @Override
  String name() {
    return clazz.getName();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DynJavaConstr && super.equals(obj);
  }
}
