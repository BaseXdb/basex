package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.SeqIter;
import org.basex.query.xquery.util.Err;

/**
 * Root node.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Root extends Simple {
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Iter iter = checkCtx(ctx);;
    final SeqIter ir = new SeqIter();
    Item i;
    while((i = iter.next()) != null) {
      if(!i.node()) Err.or(CTXNODE, this);
      Nod n = (Nod) i;
      while(n.parent() != null) n = n.parent();
      ir.add(n);
    }
    return ir;
  }

  @Override
  public Type returned() {
    return Type.DOC;
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
