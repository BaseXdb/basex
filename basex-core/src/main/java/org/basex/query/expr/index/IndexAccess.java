package org.basex.query.expr.index;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * This abstract class retrieves values from an index.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class IndexAccess extends Simple {
  /** Index database. */
  IndexDb db;

  /**
   * Constructor.
   * @param db index database
   * @param info input info
   */
  IndexAccess(final IndexDb db, final InputInfo info) {
    super(info);
    this.db = db;
    seqType = SeqType.NOD_ZM;
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
    return db.has(flag);
  }

  @Override
  public boolean removable(final Var var) {
    return db.removable(var);
  }

  @Override
  public VarUsage count(final Var var) {
    return db.count(var);
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    final IndexDb sub = db.inline(var, ex, cc);
    if(sub != null) {
      db = sub;
      return this;
    }
    return null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return db.accept(visitor);
  }

  @Override
  public int exprSize() {
    return db.exprSize() + 1;
  }

  @Override
  public final boolean iterable() {
    return seqType().zeroOrOne() || db.iterable();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof IndexAccess && db.equals(((IndexAccess) obj).db);
  }
}
