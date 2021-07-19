package org.basex.query.func.java;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.core.MainOptions.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
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
    for(final Constructor<?> cnstr : clazz.getConstructors()) {
      final Class<?>[] params = cnstr.getParameterTypes();
      if(params.length == el && typesMatch(params, types)) constrs.add(cnstr);
    }
    if(!constrs.isEmpty()) return true;
    if(!enforce) return false;

    throw JAVACONSTR_X_X.get(info, name(), el);
  }

  @Override
  protected Value eval(final QueryContext qc, final WrapOptions wrap) throws QueryException {
    // find constructors with matching parameters
    final ArrayList<Constructor<?>> candidates = new ArrayList<>(1);
    final JavaEval je = new JavaEval(this, qc);
    for(final Constructor<?> cnstr : constrs) {
      if(je.match(cnstr.getParameterTypes(), true, null)) candidates.add(cnstr);
    }
    if(candidates.size() != 1) throw noCandidate(constrs.toArray(new Executable[0]),
        candidates.toArray(new Executable[0]));

    // single constructor found: instantiate class
    try {
      return new XQJava(candidates.get(0).newInstance(je.args));
    } catch(final Throwable th) {
      throw je.executionError(th);
    }
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final DynJavaConstr c = new DynJavaConstr(clazz, types, copyAll(cc, vm, exprs), sc, info);
    c.constrs = constrs;
    return copyType(c);
  }

  @Override
  String desc() {
    return QNm.eqName(JAVAPREF + className(clazz), NEW);
  }

  @Override
  String name() {
    return className(clazz);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DynJavaConstr && super.equals(obj);
  }
}
