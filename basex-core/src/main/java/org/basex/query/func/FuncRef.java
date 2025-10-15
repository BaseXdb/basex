package org.basex.query.func;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A reference to an unresolved function call or named function item, to be resolved after parsing,
 * when all user-defined function declarations have been processed.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class FuncRef extends Arr {
  /** Function to resolve this reference. */
  private final ThrowingFunction<QueryContext, Expr, QueryException> resolve;
  /** Function to convert to string. */
  private final Consumer<QueryString> toString;
  /** Function call or function item after resolution. */
  private Expr expr;

  /**
   * Constructor for static function calls.
   * @param name function name
   * @param fb function builder
   * @param hasImport indicates whether a module import for the function name's URI was present
   */
  public FuncRef(final QNm name, final FuncBuilder fb, final boolean hasImport) {
    this(fb.info,
        qc -> Functions.get(name, fb, qc, hasImport),
        qs -> qs.token(name.prefixId()).params(fb.args()));
  }

  /**
   * Constructor for named function references.
   * @param name function name
   * @param arity function arity
   * @param info input info (can be {@code null})
   * @param hasImport indicates whether a module import for the function name's URI was present
   */
  public FuncRef(final QNm name, final int arity, final InputInfo info, final boolean hasImport) {
    this(info,
        qc -> Functions.item(name, arity, false, info, qc, hasImport),
        qs -> qs.token(name.prefixId()).token('#').token(arity));
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param resolve function to resolve the reference
   * @param toString function to convert to string
   */
  private FuncRef(final InputInfo info,
      final ThrowingFunction<QueryContext, Expr, QueryException> resolve,
      final Consumer<QueryString> toString) {
    super(info, SeqType.ITEM_ZM);
    this.resolve = resolve;
    this.toString = toString;
  }

  /**
   * Functional interface for functions that may throw an exception.
   * @param <T> parameter type
   * @param <R> result type
   * @param <E> exception type
   */
  @FunctionalInterface
  public interface ThrowingFunction<T, R, E extends Exception> {
    /**
     * Applies this function to the given argument.
     * @param t argument
     * @return result
     * @throws E exception
     */
    R apply(T t) throws E;
  }

  /**
   * Resolves the function reference.
   * @param qc query context
   * @throws QueryException query exception
   */
  public void resolve(final QueryContext qc) throws QueryException {
    expr = resolve.apply(qc);
  }

  /**
   * Returns the resolved expression.
   * @return expression
   */
  private Expr expr() {
    if(expr == null) throw Util.notExpected();
    return expr;
  }

  @Override
  public boolean has(final Flag... flags) {
    return expr().has(flags);
  }

  @Override
  public boolean vacuous() {
    return expr().vacuous();
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return expr().iter(qc);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return expr().compile(cc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    throw Util.notExpected();
  }

  @Override
  public void toString(final QueryString qs) {
    toString.accept(qs);
  }
}
