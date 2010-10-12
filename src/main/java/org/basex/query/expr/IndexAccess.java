package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Serializer;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken.IndexType;
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
  /** Index type. */
  final IndexType ind;

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
    ind = t;
    ictx = ic;
    type = SeqType.NOD_ZM;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    Iter[] iter = {};
    final Iter ir = ctx.iter(expr);
    Item it;
    while((it = ir.next()) != null) {
      final int s = iter.length;
      final Iter[] tmp = new Iter[s + 1];
      System.arraycopy(iter, 0, tmp, 0, s);
      iter = tmp;
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
    final boolean mem = data instanceof MemData;
    final boolean text = ind == IndexType.TEXT;
    final byte kind = text ? Data.TEXT : Data.ATTR;

    return term.length <= MAXLEN ? new Iter() {
      final IndexIterator ii = data.ids(new ValuesToken(ind, term));

      @Override
      public Item next() {
        while(ii.more()) {
          final int p = ii.next();
          // main memory instance: check if text is no comment, etc.
          if(!mem || data.kind(p) == kind) return new DBNode(data, p, kind);
        }
        return null;
      }
    } : new Iter() {
      // fallback solution: parse complete data if string is too long
      int pre = -1;

      @Override
      public Item next() {
        while(++pre != data.meta.size) {
          if(data.kind(pre) == kind && eq(data.text(pre, text), term))
            return new DBNode(data, pre, kind);
        }
        return null;
      }
    };
  }

  @Override
  public boolean duplicates() {
    return ictx.dupl;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, DATA, token(ictx.data.meta.name),
        TYPE, token(ind.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return name() + PAR1 + "\"" + ictx.data.meta.name + "\"" + SEP +
      expr + SEP + ind + PAR2;
  }
}
