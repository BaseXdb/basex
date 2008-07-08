package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Node;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.query.xquery.util.NodeBuilder;

/**
 * Expression List.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Union extends Arr {
  /**
   * Constructor.
   * @param e expression list
   */
  public Union(final Expr[] e) {
    super(e);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final NodeBuilder nb = new NodeBuilder(false);

    for(final Expr e : expr) {
      final Iter iter = ctx.iter(e);
      Item it;
      while((it = iter.next()) != null) {
        if(!(it.node())) Err.nodes(this);
        final Node node = (Node) it;
        int i = -1;
        while(++i < nb.size) if(CmpN.COMP.EQ.e(nb.list[i], node)) break;
        if(i == nb.size) nb.add(node);
      }
    }
    return nb.iter();
  }
  
  /*
  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {

      final NodeBuilder nb = new NodeBuilder(false);
      Iter iter;
      int exprCount = 0;
      int exprCountTemp = exprCount - 1;
      Item item;
      
      @Override
      public Item next() throws XQException {
        
        while(exprCount < expr.length) {
          
          // new expression ?
          if (exprCount - 1 == exprCountTemp) {
            iter = ctx.iter(expr[exprCount]);
            exprCountTemp++;
          }
          
          while((item = iter.next()) != null) {
            if(!(item.node())) Err.nodes(Union.this);
            final Node node = (Node) item;
            int j = -1;
            while(++j < nb.size) if(CmpN.COMP.EQ.e(nb.list[j], node)) break;
            if(j == nb.size) {
              nb.add(node);
              return node;
            }
          }
          exprCount++;
        }
        return null;
      }
    };
  } */
  
  @Override
  public String toString() {
    return toString(" | ");
  }

  @Override
  public String color() {
    return "FF3300";
  }
}
