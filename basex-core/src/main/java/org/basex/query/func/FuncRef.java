package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
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
public final class FuncRef extends Single {
  /** Function to resolve this reference. */
  private final QuerySupplier<Expr> resolve;

  /**
   * Constructor.
   * @param resolve function to resolve the reference
   */
  public FuncRef(final QuerySupplier<Expr> resolve) {
    super(null, null, Types.ITEM_ZM);
    this.resolve = resolve;
  }

  /**
   * Resolves the function reference.
   * @throws QueryException query exception
   */
  public void resolve() throws QueryException {
    expr = resolve.get();
  }

  @Override
  public void checkUp() throws QueryException {
    expr.checkUp();
  }

  @Override
  public boolean vacuous() {
    return expr.vacuous();
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return expr.compile(cc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    throw Util.notExpected();
  }

  @Override
  public void toString(final QueryString qs) {
    expr.toString(qs);
  }
}
