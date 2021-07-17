package org.basex.query.func.java;

import static org.basex.query.QueryText.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.core.MainOptions.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
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
  private ArrayList<Method> methods;
  /** Field candidate. */
  private Field field;

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
    // field candidate
    final IntList arities = new IntList();
    final int arity = exprs.length;
    try {
      final Field f = clazz.getField(name);
      final int al = isStatic(f) ? 0 : 1;
      if(arity == al) {
        field = f;
      } else {
        arities.add(al);
      }
    } catch(final NoSuchFieldException ex) {
      Util.debug(ex);
      // field not found
    }

    // method candidates
    final HashMap<String, ArrayList<Method>> allMethods = methods(clazz);
    methods = filter(allMethods, name, types, arity, arities, false);

    if(field != null || !methods.isEmpty()) return true;
    if(!enforce) return false;

    final TokenList names = new TokenList();
    for(final String mthd : allMethods.keySet()) names.add(mthd);
    for(final Field fld : clazz.getFields()) names.add(fld.getName());
    throw noFunction(name, arity, name(), arities, types, info, names.finish());
  }

  @Override
  protected Value eval(final QueryContext qc, final WrapOptions wrap) throws QueryException {
    final Object[] array = field != null ? field(qc) : method(qc);
    if(wrap == WrapOptions.INSTANCE && array[1] != null) return new XQJava(array[1]);
    if(wrap == WrapOptions.VOID) return Empty.VALUE;
    return toValue(array[0], qc, info, wrap);
  }

  /**
   * Tries to return the value of a field.
   * @param qc query context
   * @return result and class instance (instance can be {@code null})
   * @throws QueryException exception
   */
  private Object[] field(final QueryContext qc) throws QueryException {
    final JavaEval je = new JavaEval(this, qc);
    final Object instance = je.instance(isStatic(field));
    try {
      return new Object[] { field.get(instance), instance };
    } catch(final IllegalArgumentException ex) {
      throw je.instanceExpected(ex);
    } catch(final Throwable th) {
      throw je.executionError(th);
    }
  }

  /**
   * Calls a method.
   * @param qc query context
   * @return result and class instance (instance can be {@code null})
   * @throws QueryException exception
   */
  private Object[] method(final QueryContext qc) throws QueryException {
    // find methods with matching parameters
    final ArrayList<Method> candidates = new ArrayList<>(1);
    final JavaEval je = new JavaEval(this, qc);
    for(final Method method : methods) {
      if(je.match(method.getParameterTypes(), isStatic(method), null)) {
        candidates.add(method);
      }
    }
    if(candidates.size() != 1) throw candidates(methods.toArray(new Executable[0]),
        candidates.toArray(new Executable[0]));

    // assign query context if module is inheriting the {@link QueryModule} interface
    final Method method = candidates.get(0);
    final Object instance = je.instance(isStatic(method));
    if(instance instanceof QueryModule) {
      final QueryModule qm = (QueryModule) instance;
      qm.staticContext = sc;
      qm.queryContext = qc;
    }

    // invoke found method
    try {
      return new Object[] { method.invoke(instance, je.args), instance };
    } catch(final IllegalArgumentException ex) {
      throw je.instanceExpected(ex);
    } catch(final Throwable th) {
      throw je.executionError(th);
    }
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
    return QNm.eqName(JAVAPREF + className(clazz), name);
  }

  @Override
  String name() {
    return className(clazz) + COL + name;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DynJavaFunc && name.equals(((DynJavaFunc) obj).name) &&
        super.equals(obj);
  }
}
