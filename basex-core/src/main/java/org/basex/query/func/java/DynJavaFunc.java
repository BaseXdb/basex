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
 * @author BaseX Team 2005-24, BSD License
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
   * @param info input info (can be {@code null})
   */
  DynJavaFunc(final Class<?> clazz, final String name, final String[] types, final Expr[] args,
      final InputInfo info) {
    super(clazz, types, args, info);
    this.name = name;
  }

  /**
   * Initializes the dynamic call (happens at parse time).
   * @param enforce enforce checks
   * @return success flag
   * @throws QueryException query exception
   */
  public boolean init(final boolean enforce) throws QueryException {
    final IntList arities = new IntList();
    final int arity = exprs.length;

    // field candidate
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
    methods = candidates(allMethods, name, types, arity, arities, false);

    if(field != null || !methods.isEmpty()) return true;
    if(!enforce) return false;

    final TokenList names = new TokenList();
    for(final String mthd : allMethods.keySet()) names.add(mthd);
    for(final Field fld : clazz.getFields()) names.add(fld.getName());
    throw noMember(name, types, arity, name(), arities, names.finish(), info);
  }

  @Override
  protected Value eval(final QueryContext qc, final WrapOptions wrap) throws QueryException {
    final Object[] array = field != null ? field(qc) : method(qc);
    if(wrap == WrapOptions.INSTANCE && array[1] != null) return new XQJava(array[1]);
    if(wrap == WrapOptions.VOID) return Empty.VALUE;
    return toValue(array[0], qc, info, wrap);
  }

  /**
   * Returns the value of a field.
   * @param qc query context
   * @return result and class instance (instance can be {@code null})
   * @throws QueryException exception
   */
  private Object[] field(final QueryContext qc) throws QueryException {
    final Object instance = instance(values(qc), isStatic(field));
    try {
      return new Object[] { field.get(instance), instance };
    } catch(final IllegalArgumentException ex) {
      throw instanceExpected(ex);
    } catch(final Throwable th) {
      throw executionError(th, new Object[0]);
    }
  }

  /**
   * Returns the result of a method invocation.
   * @param qc query context
   * @return result and class instance (instance can be {@code null})
   * @throws QueryException exception
   */
  private Object[] method(final QueryContext qc) throws QueryException {
    final Value[] values = values(qc);

    // find the best candidate with matching parameters
    final ArrayList<JavaCandidate> candidates = new ArrayList<>(1);
    for(final Method method : methods) {
      final JavaCandidate jc = candidate(values, method.getParameterTypes(), isStatic(method));
      if(jc != null) {
        jc.executable = method;
        candidates.add(jc);
      }
    }
    final JavaCandidate jc = bestCandidate(candidates);
    if(jc == null) throw noCandidate(candidates, methods.toArray(Executable[]::new));

    // assign query context if module is inheriting the {@link QueryModule} interface
    final Method method = (Method) jc.executable;
    final Object instance = instance(values, isStatic(method));
    if(instance instanceof QueryModule) {
      final QueryModule qm = (QueryModule) instance;
      qm.staticContext = sc();
      qm.queryContext = qc;
    }

    // invoke found method
    try {
      return new Object[] { method.invoke(instance, jc.arguments), instance };
    } catch(final IllegalArgumentException ex) {
      throw instanceExpected(ex);
    } catch(final Throwable th) {
      throw executionError(th, jc.arguments);
    }
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final DynJavaFunc f = new DynJavaFunc(clazz, name, types, copyAll(cc, vm, exprs), info);
    f.field = field;
    f.methods = methods;
    return copyType(f);
  }

  @Override
  String desc() {
    return QNm.eqName(JAVA_PREFIX_COLON + className(clazz), name);
  }

  @Override
  String name() {
    return className(clazz) + ':' + name;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof DynJavaFunc && name.equals(((DynJavaFunc) obj).name) &&
        super.equals(obj);
  }
}
