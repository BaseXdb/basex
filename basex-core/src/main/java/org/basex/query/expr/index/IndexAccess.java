package org.basex.query.expr.index;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * This abstract class retrieves values from an index.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class IndexAccess extends Simple {
  /** Index database. */
  IndexDb db;

  /**
   * Constructor.
   * @param db index database
   * @param info input info
   * @param type type index type
   */
  IndexAccess(final IndexDb db, final InputInfo info, final IndexType type) {
    super(info, type == IndexType.TEXT || type == IndexType.FULLTEXT ? SeqType.TXT_ZM :
      SeqType.ATT_ZM);
    this.db = db;
  }

  @Override
  public boolean has(final Flag... flags) {
    return db.has(flags);
  }

  @Override
  public boolean inlineable(final Var var) {
    return db.inlineable(var);
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
