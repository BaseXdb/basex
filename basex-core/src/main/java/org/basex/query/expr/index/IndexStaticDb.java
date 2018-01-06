package org.basex.query.expr.index;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class defines a static database source for index operations.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class IndexStaticDb extends IndexDb {
  /** Data reference. */
  private final Data data;

  /**
   * Constructor.
   * @param info input info
   * @param data data reference
   */
  public IndexStaticDb(final InputInfo info, final Data data) {
    this(data, false, info);
  }

  /**
   * Constructor.
   * @param data data reference
   * @param iterable iterable flag
   * @param info input info
   */
  public IndexStaticDb(final Data data, final boolean iterable, final InputInfo info) {
    super(info, iterable);
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
  public boolean removable(final Var var) {
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.NEVER;
  }

  @Override
  public IndexDb inline(final Var var, final Expr ex, final CompileContext cc)
      throws QueryException {
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
    return new IndexStaticDb(data, iterable, info);
  }

  @Override
  public Data data() {
    return data;
  }

  @Override
  public Data data(final QueryContext qc, final IndexType type) throws QueryException {
    type.check(data, info);
    return data;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof IndexStaticDb && data == ((IndexStaticDb) obj).data && super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(QueryText.NAME, data.meta.name));
  }

  @Override
  public Expr source() {
    return Str.get(data.meta.name);
  }
}
