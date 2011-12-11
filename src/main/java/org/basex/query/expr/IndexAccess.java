package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.ValuesToken;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.func.Function;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.ANode;
import org.basex.query.item.SeqType;
import org.basex.query.item.Str;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.IndexContext;
import org.basex.util.InputInfo;

/**
 * This index class retrieves texts and attribute values from the index.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class IndexAccess extends Single {
  /** Index context. */
  final IndexContext ictx;
  /** Index type. */
  final IndexType itype;

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
    itype = t;
    ictx = ic;
    type = SeqType.NOD_ZM;
  }

  @Override
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    NodeIter[] iter = {};
    final Iter ir = ctx.iter(expr);
    for(Item it; (it = ir.next()) != null;) {
      final int s = iter.length;
      final NodeIter[] tmp = new NodeIter[s + 1];
      System.arraycopy(iter, 0, tmp, 0, s);
      iter = tmp;
      iter[s] = index(it.string(input));
    }
    return iter.length == 0 ? new NodeCache() : iter.length == 1 ? iter[0] :
      new Union(input, expr).eval(iter);
  }

  /**
   * Returns an index iterator.
   * @param term term to be found
   * @return iterator
   */
  private AxisIter index(final byte[] term) {
    final Data data = ictx.data;

    // access index if term is not too long, and if index exists.
    // otherwise, scan data sequentially
    final IndexIterator ii = term.length <= data.meta.maxlen &&
      (itype == IndexType.TEXT ? data.meta.textindex : data.meta.attrindex) ?
      data.iter(new ValuesToken(itype, term)) : scan(term);

    return new AxisIter() {
      final byte kind = itype == IndexType.TEXT ? Data.TEXT : Data.ATTR;
      final boolean mem = data instanceof MemData;

      @Override
      public ANode next() {
        while(ii.more()) {
          final int p = ii.next();
          // main memory instance: check if text is no comment, etc.
          if(!mem || data.kind(p) == kind) return new DBNode(data, p, kind);
        }
        return null;
      }
    };
  }

  /**
   * Returns scan-based iterator.
   * @param val value to be found
   * @return node iterator
   */
  private IndexIterator scan(final byte[] val) {
    return new IndexIterator() {
      final byte kind = itype == IndexType.TEXT ? Data.TEXT : Data.ATTR;
      final boolean text = itype == IndexType.TEXT;
      final Data data = ictx.data;
      int pre = -1;

      @Override
      public double score() {
        return -1;
      }
      @Override
      public int next() {
        return pre;
      }
      @Override
      public boolean more() {
        while(++pre < data.meta.size) {
          if(data.kind(pre) == kind && eq(data.text(pre, text), val))
            return true;
        }
        return false;
      }
    };
  }

  @Override
  public boolean iterable() {
    return ictx.iterable;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, DATA, token(ictx.data.meta.name),
        TYP, token(itype.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return (itype == IndexType.TEXT ?
        Function._DB_TEXT : Function._DB_ATTRIBUTE).get(input,
            Str.get(ictx.data.meta.name), expr).toString();
  }
}
