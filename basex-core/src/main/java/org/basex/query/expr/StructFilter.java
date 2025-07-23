package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import java.util.concurrent.atomic.*;

import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Filter expression for structured data.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StructFilter extends AFilter {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param preds predicate expressions
   */
  public StructFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, SeqType.FUNCTION_O, root, preds);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // flatten nested filters
    if(root instanceof StructFilter) {
      final StructFilter filter = (StructFilter) root;
      root = filter.root;
      exprs = ExprList.concat(filter.exprs, exprs);
    }
    // return empty root
    if(root == XQArray.empty() || root == XQMap.empty()) return cc.replaceWith(this, root);

    return this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Item item = root.item(qc, info);
    if(item.isEmpty()) return item;
    if(!(item instanceof XQStruct)) throw STRUCT_FILTER_X.get(info, item);

    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qc.focus = qf;
    try {
      for(final Expr expr : exprs) {
        qf.size = item.structSize();
        final AtomicInteger a = new AtomicInteger();

        // arrays
        if(item instanceof XQArray) {
          final ArrayBuilder ab = new ArrayBuilder(qc);
          for(final Value value : ((XQArray) item).iterable()) {
            qc.checkStop();
            qf.value = value;
            qf.pos = a.incrementAndGet();
            if(expr.test(qc, info, a.get())) ab.add(value);
          }
          item = ab.array();
        } else {
          // maps
          final MapBuilder mb = new MapBuilder();
          final XQMap map = (XQMap) item;

          map.forEach((key, value) -> {
            qc.checkStop();
            qf.value = XQMap.pair(key, value);
            qf.pos = a.incrementAndGet();
            if(expr.test(qc, info, a.get())) mb.put(key, value);
          });
          item = mb.map();
        }
      }
      return item;
    } finally {
      qc.focus = focus;
    }
  }

  @Override
  protected Expr type(final Expr expr, final boolean optimize) {
    exprType.assign(root.seqType());
    return null;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof StructFilter && root.equals(((StructFilter) obj).root) &&
        super.equals(obj);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new StructFilter(info, root, exprs));
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(root).token('?');
    super.toString(qs);
  }
}
