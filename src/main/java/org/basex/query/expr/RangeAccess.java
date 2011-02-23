package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.index.RangeToken;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * This index class retrieves range values from the index.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class RangeAccess extends Simple {
  /** Index type. */
  final IndexToken ind;
  /** Index context. */
  final IndexContext ictx;

  /**
   * Constructor.
   * @param ii input info
   * @param t index reference
   * @param ic index context
   */
  RangeAccess(final InputInfo ii, final IndexToken t, final IndexContext ic) {
    super(ii);
    ind = t;
    ictx = ic;
    type = SeqType.NOD_ZM;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    final Data data = ictx.data;
    final byte kind = ind.type() == IndexType.TEXT ? Data.TEXT : Data.ATTR;

    return new Iter() {
      final IndexIterator it = data.ids(ind);
      @Override
      public Item next() {
        return it.more() ? new DBNode(data, it.next(), kind) : null;
      }
    };
  }

  @Override
  public boolean duplicates() {
    return ictx.dupl;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    final RangeToken rt = (RangeToken) ind;
    ser.emptyElement(this, DATA, token(ictx.data.meta.name),
        MIN, Token.token(rt.min), MAX, Token.token(rt.max),
        TYP, Token.token(rt.ind.toString()));
  }

  @Override
  public String toString() {
    final RangeToken rt = (RangeToken) ind;
    return name() + PAR1 + "\"" + ictx.data.meta.name + "\"" + SEP +
      rt.min + "-" + rt.max + SEP + rt.ind + PAR2;
  }
}
