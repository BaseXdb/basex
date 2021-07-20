package org.basex.query.func.java;

import static org.basex.query.QueryText.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.core.MainOptions.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

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
   * Initializes the dynamic call (happens at parse time).
   * @param enforce enforce checks
   * @return {@code true} if at least one candidate for invocation was found
   * @throws QueryException query exception
   */
  public boolean init(final boolean enforce) throws QueryException {
    final IntList arities = new IntList();
    final int arity = exprs.length;

    constrs = new ArrayList<>();
    for(final Constructor<?> cnstr : clazz.getConstructors()) {
      final Class<?>[] params = cnstr.getParameterTypes();
      final int pl = params.length;
      if(pl == arity) {
        if(typesMatch(params, types)) constrs.add(cnstr);
      } else {
        arities.add(pl);
      }
    }
    if(!constrs.isEmpty()) return true;
    if(!enforce) return false;

    throw Functions.wrongArity(name(), arity, arities, info);
  }

  @Override
  protected Value eval(final QueryContext qc, final WrapOptions wrap) throws QueryException {
    // find constructors with matching parameters
    final ArrayList<Constructor<?>> candidates = new ArrayList<>(1);
    Object[] args = null;
    for(final Constructor<?> cnstr : constrs) {
      final Object[] tmp = args(cnstr.getParameterTypes(), true, null, qc);
      if(tmp != null) {
        candidates.add(cnstr);
        args = tmp;
      }
    }
    if(candidates.size() != 1) throw noCandidate(constrs.toArray(new Executable[0]),
        candidates.toArray(new Executable[0]));

    // single constructor found: instantiate class
    try {
      return new XQJava(candidates.get(0).newInstance(args));
    } catch(final Throwable th) {
      throw executionError(th, args);
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
    return className(clazz) + COL + NEW;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DynJavaConstr && super.equals(obj);
  }
}
