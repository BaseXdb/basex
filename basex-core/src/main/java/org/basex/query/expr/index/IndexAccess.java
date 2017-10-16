package org.basex.query.expr.index;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * This abstract class retrieves values from an index.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class IndexAccess extends Simple {
  /** Index context. */
  final IndexContext ictx;

  /**
   * Constructor.
   * @param ictx index context
   * @param info input info
   */
  IndexAccess(final IndexContext ictx, final InputInfo info) {
    super(info);
    this.ictx = ictx;
  }

  /**
   * Sets the number of results.
   * @param s number of results
   */
  public void size(final long s) {
    size = s;
    seqType = seqType().withSize(s);
  }

  @Override
  public abstract NodeIter iter(QueryContext qc) throws QueryException;

  @Override
  public boolean has(final Flag flag) {
    return input().has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    return input().removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return input().count(var);
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    if(ictx.input != null) {
      final Expr sub = ictx.input.inline(var, ex, cc);
      if(sub == null) return null;
      ictx.input = sub;
    }
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return input().accept(visitor);
  }

  @Override
  public int exprSize() {
    return input().exprSize();
  }

  @Override
  public final boolean iterable() {
    return ictx.iterable || seqType().zeroOrOne();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof IndexAccess && ictx.equals(((IndexAccess) obj).ictx);
  }

  /**
   * Returns the index input expression.
   * @return input
   */
  protected Expr input() {
    return ictx.input();
  }
}
