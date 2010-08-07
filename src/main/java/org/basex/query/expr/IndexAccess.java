package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.data.Data.IndexType;
import org.basex.index.IndexIterator;
import org.basex.index.ValuesToken;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * This index class retrieves texts and attribute values from the index.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class IndexAccess extends Single {
  /** Index context. */
  private final IndexContext ictx;
  /** Access type. */
  final IndexType type;

  /**
   * Constructor.
   * @param ii input info
   * @param e index expression
   * @param t access type
   * @param ic index context
   */
  public IndexAccess(final InputInfo ii, final Expr e, final IndexType t,
      final IndexContext ic) {
    super(ii, e);
    type = t;
    ictx = ic;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    Iter[] iter = {};
    final Iter ir = ctx.iter(expr);
    Item it;
    while((it = ir.next()) != null) {
      final int s = iter.length;
      final Iter[] t = new Iter[s + 1];
      System.arraycopy(iter, 0, t, 0, s);
      iter = t;
      iter[s] = index(it.atom());
    }
    return iter.length == 0 ? Iter.EMPTY : iter.length == 1 ? iter[0] :
      new Union(input, expr).eval(iter);
  }

  /**
   * Returns an index iterator.
   * @param term term to be found
   * @return iterator
   */
  private Iter index(final byte[] term) {
    final Data data = ictx.data;
    final boolean text = type == IndexType.TXT;
    final byte kind = text ? Data.TEXT : Data.ATTR;

    return term.length <= MAXLEN ? new Iter() {
      final IndexIterator ii = data.ids(new ValuesToken(type, term));

      @Override
      public Item next() {
        return ii.more() ? new DBNode(data, ii.next(), kind) : null;
      }
    } : new Iter() {
      // just in case: parse complete data if string is too long
      int pre = -1;

      @Override
      public Item next() {
        while(++pre != data.meta.size) {
          if(data.kind(pre) == kind && eq(data.text(pre, text), term))
            return new DBNode(data, pre);
        }
        return null;
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
    return cmp instanceof IndexAccess;
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, token(type.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return name() + "(" + expr + ", " + type + ")";
  }
}
