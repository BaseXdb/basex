package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Node;
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
public final class Root extends Expr {
  @Override
  public Expr comp(final XQContext ctx) {
    return this;
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    if(ctx.item == null) Err.or(XPNOCTX, this);
    
    final SeqIter ir = new SeqIter();
    final Iter iter = ctx.item.iter();
    Item i;
    while((i = iter.next()) != null) {
      if(!i.node()) Err.or(XPNOCTX, this);
      Node n = (Node) i;
      while(n.parent() != null) n = n.parent();
      ir.add(n);
    }
    return ir;
  }

  @Override
  public boolean uses(final Using use) {
    return false;
  }

  @Override
  public Type returned() {
    return Type.DOC;
  }
  
  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this);
  }

  @Override
  public String color() {
    return "FFCC66";
  }

  @Override
  public String toString() {
    return "root()";
  }
}
