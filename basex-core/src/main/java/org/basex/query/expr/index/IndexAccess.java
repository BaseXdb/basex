package org.basex.query.expr.index;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * This abstract class retrieves values from an index.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class IndexAccess extends Simple {
  /** Index database. */
  IndexDb db;

  /**
   * Constructor.
   * @param db index database
   * @param info input info
   * @param type type
   */
  IndexAccess(final IndexDb db, final InputInfo info, final Type type) {
    super(info, SeqType.get(type, Occ.ZERO_OR_MORE));
    this.db = db;
  }

  @Override
  public boolean has(final Flag... flags) {
    return db.has(flags);
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return db.inlineable(ic);
  }

  @Override
  public VarUsage count(final Var var) {
    return db.count(var);
  }

  /**
   * Inlines an expression (see {@link Expr#inline(InlineContext)}).
   * @param ic inlining context
   * @return result of inlining
   * @throws QueryException query exception
   */
  final boolean inlineDb(final InlineContext ic) throws QueryException {
    final IndexDb inlined = db.inline(ic);
    if(inlined == null) return false;
    db = inlined;
    return true;
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
  public final boolean ddo() {
    return true;
  }

  @Override
  public final Data data() {
    return db.data();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof IndexAccess && db.equals(((IndexAccess) obj).db);
  }
}
