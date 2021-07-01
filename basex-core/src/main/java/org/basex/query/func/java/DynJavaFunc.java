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
import org.basex.util.list.*;

/**
 * Dynamic invocation of a Java field or method.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class DynJavaFunc extends DynJavaCall {
  /** Name of Java field/method. */
  private final String name;

  /** Method candidates. */
  public ArrayList<Method> methods;
  /** Field candidate. */
  public Field field;

  /**
   * Constructor.
   * @param clazz Java class
   * @param name name of Java field/method
   * @param types types provided in the parsed expression string (can be {@code null})
   * @param args arguments
   * @param sc static context
   * @param info input info
   */
  DynJavaFunc(final Class<?> clazz, final String name, final String[] types,
      final Expr[] args, final StaticContext sc, final InputInfo info) {

    super(clazz, types, args, sc, info);
    this.name = name;
  }

  /**
   * Initializes the dynamic call.
   * @param enforce enforce checks
   * @return success flag
   * @throws QueryException query exception
   */
  public boolean init(final boolean enforce) throws QueryException {
    final int arity = exprs.length;

    // field candidate: supplied expression is class instance
    final IntList arities = new IntList();
    try {
      final Field f = clazz.getField(name);
      final int al = isStatic(f) ? 0 : 1;
      if(arity == al) {
        field = f;
      } else {
        arities.add(al);
      }
    } catch(final NoSuchFieldException ex) {
      // field not found
    }

    // method candidates: no check supplied expression: class instance or does not
    methods = new ArrayList<>();
    for(final Method m : clazz.getMethods()) {
      // check name, types and parameter count
      if(!m.getName().equals(name)) continue;
      final Class<?>[] params = m.getParameterTypes();
      final int al = params.length + (isStatic(m) ? 0 : 1);
      if(al == arity) {
        if(typesMatch(params, types)) methods.add(m);
      } else if(types == null) {
        arities.add(al);
      }
    }
    if(field != null || !methods.isEmpty()) return true;
    if(!enforce) return false;

    final TokenList names = new TokenList();
    for(final Method m : clazz.getMethods()) names.add(m.getName());
    for(final Field f : clazz.getFields()) names.add(f.getName());
    throw noFunction(name, arity, name(), arities, types, info, names.finish());
  }

  @Override
  protected Object eval(final QueryContext qc) throws QueryException {
    return field != null ? field(qc) : method(qc);
  }

  /**
   * Tries to return the value of a field.
   * @param qc query context
   * @return resulting object
   * @throws QueryException exception
   */
  private Object field(final QueryContext qc) throws QueryException {
    final JavaEval je = new JavaEval(this, qc);
    final Object instance = je.classInstance(isStatic(field));
    try {
      return field.get(instance);
    } catch(final IllegalArgumentException ex) {
      throw je.instanceError(ex);
    } catch(final Throwable th) {
      throw je.execError(th);
    }
  }

  /**
   * Calls a method.
   * @param qc query context
   * @return resulting object
   * @throws QueryException exception
   */
  private Object method(final QueryContext qc) throws QueryException {
    // find methods with matching parameters
    final ArrayList<Method> candidates = new ArrayList<>(1);
    final JavaEval je = new JavaEval(this, qc);
    for(final Method m : methods) {
      if(je.match(m.getParameterTypes(), isStatic(m), null)) candidates.add(m);
    }

    // single method found: instantiate class
    final int cs = candidates.size();
    if(cs == 1) {
      // assign query context if module is inheriting the {@link QueryModule} interface
      final Method method = candidates.get(0);
      final Object instance = je.classInstance(isStatic(method));
      if(instance instanceof QueryModule) {
        final QueryModule qm = (QueryModule) instance;
        qm.staticContext = sc;
        qm.queryContext = qc;
      }

      // invoke found method
      try {
        return method.invoke(instance, je.args);
      } catch(final IllegalArgumentException ex) {
        throw je.instanceError(ex);
      } catch(final Throwable th) {
        throw je.execError(th);
      }
    }

    // otherwise, raise error
    if(cs > 1) throw je.multipleError(JAVAMULTIMETH_X_X_X, cs);

    final int size = methods.size();
    final Method method = methods.get(0);
    // remove static argument
    final Expr[] args = je.exprs;
    if(size == 1 && !isStatic(method) || args.length > 0 &&
        ((args[0] instanceof Jav || args[0] instanceof JavaCall))) {
      je.exprs = Arrays.copyOfRange(args, 1, args.length);
    }

    if(size > 1) throw JAVAMETH_X_X_X.get(info, size, name(), JavaEval.types(exprs));
    throw je.argsError(method);
  }

  /**
   * Checks if the specified method is static.
   * @param method method
   * @return result of check
   */
  private static boolean isStatic(final Method method) {
    return Modifier.isStatic(method.getModifiers());
  }

  /**
   * Checks if the specified field is static.
   * @param field field
   * @return result of check
   */
  private static boolean isStatic(final Field field) {
    return Modifier.isStatic(field.getModifiers());
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final DynJavaFunc f = new DynJavaFunc(clazz, name, types, copyAll(cc, vm, exprs), sc, info);
    f.field = field;
    f.methods = methods;
    return copyType(f);
  }

  @Override
  String desc() {
    return QNm.eqName(JAVAPREF + clazz.getName(), name);
  }

  @Override
  String name() {
    return clazz.getName() + COL + name;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DynJavaFunc && name.equals(((DynJavaFunc) obj).name) &&
        super.equals(obj);
  }
}
