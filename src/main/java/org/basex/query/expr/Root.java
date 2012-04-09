package org.basex.query.expr;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.util.*;

/**
 * Root node.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Root extends Simple {
  /**
   * Constructor.
   * @param ii input info
   */
  public Root(final InputInfo ii) {
    super(ii);
    type = SeqType.DOC_ZM;
  }

  @Override
  public Expr comp(final QueryContext ctx) {
    return ctx.value != null && ctx.value.type == NodeType.DOC ? ctx.value : this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter iter = checkCtx(ctx).iter();
    final NodeCache nc = new NodeCache().random();
    for(Item i; (i = iter.next()) != null;) {
      final ANode n = root(i);
      if(n == null || n.type != NodeType.DOC) CTXNODE.thrw(info);
      nc.add(n);
    }
    return nc;
  }

  /**
   * Returns the root node of the specified item.
   * @param v input node
   * @return root node
   */
  public static ANode root(final Value v) {
    if(!v.type.isNode()) return null;
    ANode n = (ANode) v;
    while(true) {
      final ANode p = n.parent();
      if(p == null) return n;
      n = p;
    }
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX;
  }

  @Override
  public boolean iterable() {
    return true;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Root;
  }

  @Override
  public String toString() {
    return "root()";
  }
}
