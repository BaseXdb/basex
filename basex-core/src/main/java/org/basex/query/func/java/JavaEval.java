package org.basex.query.func.java;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.Type;
import org.basex.util.*;

/**
 * Evaluator for Java arguments.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class JavaEval {
  /** Java call. */
  private final JavaCall call;
  /** Query context. */
  private final QueryContext qc;
  /** Class to be called. */
  private final Class<?> clazz;
  /** Expressions (will be evaluated to values). */
  private final Expr[] exprs;

  /** Java arguments resulting from the parameter matching. */
  Object[] args = {};

  /**
   * Constructor.
   * @param call Java call
   * @param qc query context
   */
  JavaEval(final JavaCall call, final QueryContext qc) {
    this(call, qc, null);
  }

  /**
   * Constructor.
   * @param call Java call
   * @param qc query context
   */
  JavaEval(final DynJavaCall call, final QueryContext qc) {
    this(call, qc, call.clazz);
  }

  /**
   * Constructor.
   * @param call Java call
   * @param qc query context
   * @param clazz class (can be {@code null})
   */
  private JavaEval(final JavaCall call, final QueryContext qc, final Class<?> clazz) {
    this.call = call;
    this.qc = qc;
    this.clazz = clazz;
    exprs = call.exprs.clone();
  }

  /**
   * Evaluates the first argument.
   * @param stat static flag
   * @return Java object (can be {@code null})
   * @throws QueryException query exception
   */
  Object instance(final boolean stat) throws QueryException {
    if(stat) return null;
    final Value value = exprs[0].value(qc);
    exprs[0] = value;
    return clazz.isInstance(value) ? value : value.toJava();
  }

  /**
   * Checks if the XQuery arguments match the function parameters.
   * The converted arguments are stored in {@link #args}.
   * @param params parameter types
   * @param stat static flag
   * @param xquery indicates which parameter types are XQuery values (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  boolean match(final Class<?>[] params, final boolean stat, final boolean[] xquery)
      throws QueryException {

    // start with second argument if function is not static
    final int s = stat ? 0 : 1, pl = params.length;
    if(pl != exprs.length - s) return false;

    // function arguments
    final Object[] values = new Object[pl];
    for(int p = 0; p < pl; p++) {
      final Class<?> param = params[p];
      final Expr expr = exprs[s + p];

      if(param == Expr.class) {
        values[p] = expr;
      } else {
        final Value arg = expr.value(qc);
        exprs[s + p] = arg;
        final Type type = JavaMapping.type(param, true);
        if(type != null && arg.type.instanceOf(type)) {
          // convert to Java object if an XQuery type exists for the function parameter
          values[p] = arg.toJava();
        } else {
          // convert to Java object
          // - if argument is a Java object wrapper, or
          // - if function parameter is not a {@link Value} instance
          final boolean convert = arg instanceof XQJava ||
              !(xquery != null ? xquery[p] : Value.class.isAssignableFrom(params[p]));
          values[p] = convert ? arg.toJava() : arg;

          // if argument is no instance of the function parameter, check for null value
          if(!param.isInstance(values[p]) && (values[p] != null || param.isPrimitive()))
            return false;
        }
      }
    }
    args = values;
    return true;
  }

  /**
   * Returns an error for field/method invocations in which first argument is no class instance.
   * @param ex exception
   * @return exception
   */
  QueryException instanceExpected(final IllegalArgumentException ex) {
    Util.debug(ex);
    return JAVANOINSTANCE_X_X.get(call.info, JavaCall.className(clazz),
        JavaCall.argType(exprs[0]));
  }

  /**
   * Returns a Java execution error.
   * @param th throwable
   * @return exception
   */
  QueryException executionError(final Throwable th) {
    Util.debug(th);
    final Throwable root = Util.rootException(th);
    return root instanceof QueryException ? ((QueryException) root).info(call.info) :
      JAVAEXEC_X_X_X.get(call.info, root, call.name(), JavaCall.argTypes(args));
  }
}
