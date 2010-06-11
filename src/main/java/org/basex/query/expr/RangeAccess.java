package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.data.Data.IndexType;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.index.RangeToken;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.util.Token;

/**
 * This index class retrieves range values from the index.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class RangeAccess extends Simple {
  /** Index type. */
  final IndexToken ind;
  /** Index context. */
  final IndexContext ictx;

  /**
   * Constructor.
   * @param i index reference
   * @param ic index context
   */
  RangeAccess(final IndexToken i, final IndexContext ic) {
    ind = i;
    ictx = ic;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    final Data data = ictx.data;
    final byte kind = ind.type() == IndexType.TXT ? Data.TEXT : Data.ATTR;

    return new Iter() {
      final IndexIterator it = data.ids(ind);
      @Override
      public Item next() {
        return it.more() ? new DBNode(data, it.next(), kind) : null;
      }
    };
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.NOD_ZM;
  }

  @Override
  public boolean duplicates(final QueryContext ctx) {
    return ictx.dupl;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof RangeAccess;
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(ind.type().toString()));
    final RangeToken rt = (RangeToken) ind;
    ser.attribute(MIN, Token.token(rt.min));
    ser.attribute(MAX, Token.token(rt.max));
    ser.closeElement();
  }

  @Override
  public String toString() {
    final RangeToken rt = (RangeToken) ind;
    return name() + "(" + rt.min + "-" + rt.max + ", " + ind.type() + ")";
  }
}
