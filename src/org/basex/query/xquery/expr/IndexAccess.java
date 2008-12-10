package org.basex.query.xquery.expr;

import static org.basex.query.xpath.XPText.*;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;

/**
 * This index class retrieves attribute values from the index.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IndexAccess extends Expr {
  /** Index type. */
  public final IndexToken ind;

  /**
   * Constructor.
   * @param i index reference
   */
  public IndexAccess(final IndexToken i) {
    ind = i;
  }

  @Override
  public Expr comp(final XQContext ctx) {
    return this;
  }
  
  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {
      final Data data = ((DNode) ctx.item).data;
      final IndexIterator it = data.ids(ind);
      @Override
      public Item next() {
        if (it.more()) {
          final int id = it.next();
          ctx.item = new DNode(data, id);
        } else {
          ctx.item = null;
        }
        return ctx.item;
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, Token.token(TYPE), Token.token(ind.type.toString()));
    ser.item(ind.get());
    ser.closeElement();
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public String toString() {
    return BaseX.info("%(%, \"%\")", name(), ind.type, ind.get());
  }
}
