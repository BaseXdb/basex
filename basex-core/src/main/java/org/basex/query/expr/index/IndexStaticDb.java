package org.basex.query.expr.index;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class defines a static database source for index operations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class IndexStaticDb extends IndexDb {
  /** Data reference. */
  private final Data data;

  /**
   * Constructor.
   * @param data data reference
   * @param info input info
   */
  public IndexStaticDb(final Data data, final InputInfo info) {
    super(info);
    this.data = data;
  }

  @Override
  public void checkUp() {
  }

  @Override
  public Expr compile(final CompileContext cc) {
    return this;
  }

  @Override
  public boolean has(final Flag... flags) {
    return false;
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.NEVER;
  }

  @Override
  public IndexDb inline(final InlineContext ic) {
    return null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return true;
  }

  @Override
  public int exprSize() {
    return 1;
  }

  @Override
  public IndexDb copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new IndexStaticDb(data, info));
  }

  @Override
  public Data data() {
    return data;
  }

  @Override
  Data data(final QueryContext qc) {
    return data;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof IndexStaticDb && data == ((IndexStaticDb) obj).data && super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this));
  }

  @Override
  public void plan(final QueryString qs) {
    qs.quoted(Token.token(data.meta.name));
  }
}
