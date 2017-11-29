package org.basex.query.expr.index;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class defines the database source for index operations.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class IndexDb extends ParseExpr {
  /** Flag for iterative evaluation. */
  final boolean iterable;

  /**
   * Constructor.
   * @param info input info
   * @param iterable iterable flag
   */
  IndexDb(final InputInfo info, final boolean iterable) {
    super(info, SeqType.EMP);
    this.iterable = iterable;
  }

  /**
   * Returns a data reference.
   * @param qc query context
   * @param type index type
   * @return data reference
   * @throws QueryException query exception
   */
  public abstract Data data(QueryContext qc, IndexType type) throws QueryException;

  /**
   * Source expression.
   * @return source expression
   */
  public abstract Expr source();

  @Override
  public abstract IndexDb inline(Var var, Expr ex, CompileContext cc) throws QueryException;

  @Override
  public abstract IndexDb copy(CompileContext cc, IntObjMap<Var> vm);

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof IndexDb && iterable == ((IndexDb) obj).iterable;
  }

  @Override
  public final boolean iterable() {
    return iterable;
  }

  @Override
  public final String toString() {
    return source().toString();
  }
}
