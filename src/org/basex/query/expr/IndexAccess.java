package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.index.RangeToken;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
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
  /** Index context. */
  final IndexContext ictx;

  /**
   * Constructor.
   * @param i index reference
   * @param ic index context
   */
  public IndexAccess(final IndexToken i, final IndexContext ic) {
    ind = i;
    ictx = ic;
  }
  
  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      final IndexIterator it = ictx.data.ids(ind);
      final int s = it.size();
      int p = -1;
      
      @Override
      public Item next() {
        return ++p < s ? new DBNode(ictx.data, it.next()) : null;
      }
    };
  }

  @Override
  public Return returned(final QueryContext ctx) {
    return Return.NODSEQ;
  }

  @Override
  public boolean duplicates(final QueryContext ctx) {
    return ictx.dupl;
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
