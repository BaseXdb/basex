package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.IndexIterator;
import org.basex.index.RangeToken;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;

/**
 * This index class retrieves range values from the index.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IndexRange extends Simple {
  /** Index type. */
  final RangeToken ind;
  /** Data reference. */
  final Data data;

  /**
   * Constructor.
   * @param d data reference
   * @param i index terms
   */
  public IndexRange(final Data d, final RangeToken i) {
    data = d;
    ind = i;
  }

  @Override
  public Iter iter(final XQContext ctx) {
    return new Iter() {
      IndexIterator it;
      
      @Override
      public Item next() {
        if(it == null) it = data.ids(ind);
        
        if(it.more()) {
          ctx.item = new DBNode(data, it.next());
        } else {
          ctx.item = null;
        }
        return ctx.item;
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, TYPE, Token.token(ind.type.toString()),
        MIN, Token.token(ind.min), MAX, Token.token(ind.max));
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public String toString() {
    return BaseX.info("%(%, %-%)", name(), ind.type, ind.min, ind.max);
  }
}
