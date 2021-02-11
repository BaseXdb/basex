package org.basex.query.expr.index;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This index class retrieves string ranges from a value index.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StringRangeAccess extends IndexAccess {
  /** Index token. */
  private final StringRange index;

  /**
   * Constructor.
   * @param info input info
   * @param index index token
   * @param db index database
   */
  public StringRangeAccess(final InputInfo info, final StringRange index, final IndexDb db) {
    super(db, info, index.type() == IndexType.TEXT ? NodeType.TEXT : NodeType.ATTRIBUTE);
    this.index = index;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final IndexType type = index.type();
    final Data data = db.data(qc, type);

    return new DBNodeIter(data) {
      final byte kind = type == IndexType.TEXT ? Data.TEXT : Data.ATTR;
      final IndexIterator ii = index.min.length <= data.meta.maxlen &&
          index.max.length <= data.meta.maxlen ? data.iter(index) : scan(data);

      @Override
      public DBNode next() {
        return ii.more() ? new DBNode(data, ii.pre(), kind) : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final IndexType it = index.type();
    final Data data = db.data(qc, it);

    final IndexIterator ii = index.min.length <= data.meta.maxlen &&
        index.max.length <= data.meta.maxlen ? data.iter(index) : scan(data);
    final IntList list = new IntList();
    while(ii.more()) list.add(ii.pre());
    return DBNodeSeq.get(list.finish(), data, this);
  }

  /**
   * Returns scan-based iterator.
   * @param data data reference
   * @return node iterator
   */
  private IndexIterator scan(final Data data) {
    return new IndexIterator() {
      final boolean text = index.type() == IndexType.TEXT;
      final byte kind = text ? Data.TEXT : Data.ATTR;
      final int sz = data.meta.size;
      int pre = -1;

      @Override
      public int pre() {
        return pre;
      }
      @Override
      public boolean more() {
        while(++pre < sz) {
          if(data.kind(pre) != kind) continue;
          final byte[] txt = data.text(pre, text);
          final int min = Token.diff(txt, index.min), max = Token.diff(txt, index.max);
          if(min >= (index.mni ? 0 : 1) && max <= (index.mxi ? 0 : 1)) return true;
        }
        return false;
      }
      @Override
      public int size() {
        return Math.max(1, sz >>> 2);
      }
    };
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    return inlineDb(ic) ? optimize(ic.cc) : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new StringRangeAccess(info, index, db.copy(cc, vm)));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof StringRangeAccess && index.equals(((StringRangeAccess) obj).index) &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, INDEX, index.type(), MIN, index.min, MAX, index.max), db);
  }

  @Override
  public void plan(final QueryString qs) {
    final Function function = index.type() == IndexType.TEXT ? Function._DB_TEXT_RANGE :
      Function._DB_ATTRIBUTE_RANGE;
    qs.function(function, db, Str.get(index.min), Str.get(index.max));
  }
}
