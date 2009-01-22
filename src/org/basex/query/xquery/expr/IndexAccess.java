package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.index.RangeToken;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.DBNode;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This index class retrieves attribute values from the index.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IndexAccess extends Simple {
  /** Index type. */
  final IndexToken ind;
  /** Data reference. */
  final Data data;

  /**
   * Constructor.
   * @param d data reference
   * @param i index reference
   */
  public IndexAccess(final Data d, final IndexToken i) {
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
  public String color() {
    return "CC99FF";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(ind.type.toString()));
    if(ind instanceof RangeToken) {
      final RangeToken rt = (RangeToken) ind;
      ser.attribute(MIN, Token.token(rt.min));
      ser.attribute(MAX, Token.token(rt.max));
    } else {
      ser.text(ind.get());
    }
    ser.closeElement();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(name());
    tb.add("(" + ind.type + ", ");
    if(ind instanceof RangeToken) {
      final RangeToken rt = (RangeToken) ind;
      tb.add(rt.min + "-" + rt.max);
    } else {
      tb.add('"');
      tb.add(ind.get());
      tb.add('"');
    }
    return tb.add(")").toString();
  }
}
