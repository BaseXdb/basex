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
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.query.item.SeqType;
import org.basex.query.iter.AxisIter;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.iter.NodeIter;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * This index class retrieves texts and attribute values from the index.
 *
 * @author BaseX Team 2005-11, BSD License
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
  public NodeIter iter(final QueryContext ctx) throws QueryException {
    NodeIter[] iter = {};
    final Iter ir = ctx.iter(expr);
    for(Item it; (it = ir.next()) != null;) {
      final int s = iter.length;
      final NodeIter[] tmp = new NodeIter[s + 1];
      System.arraycopy(iter, 0, tmp, 0, s);
      iter = tmp;
      iter[s] = index(it.atom(input));
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
    return term.length <= MAXLEN &&
      (ind == IndexType.TEXT ? data.meta.textindex : data.meta.attrindex) ?
      index(data, term) : scan(data, term);
  }

  /**
   * Returns index-based results.
   * @param data data reference
   * @param val value to be found
   * @return node iterator
   */
  private AxisIter index(final Data data, final byte[] val) {
    final byte kind = ind == IndexType.TEXT ? Data.TEXT : Data.ATTR;
    final boolean mem = data instanceof MemData;
    return new AxisIter() {
      final IndexIterator ii = data.ids(new ValuesToken(ind, val));

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
   * Returns scan-based results.
   * @param data data reference
   * @param val value to be found
   * @return node iterator
   */
  private AxisIter scan(final Data data, final byte[] val) {
    final boolean text = ind == IndexType.TEXT;
    final byte kind = text ? Data.TEXT : Data.ATTR;
    return new AxisIter() {
      // fallback solution: parse complete data if string is too long
      int pre = -1;

      @Override
      public ANode next() {
        while(++pre != data.meta.size) {
          if(data.kind(pre) == kind && eq(data.text(pre, text), val))
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
        TYP, token(ind.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return new TokenBuilder(NodeType.DOC.nam).add(" { \"").
      add(ictx.data.meta.name).add("\" }/").add(DB).add(':').
      add(ind.toString().toLowerCase()).
      add(PAR1).add(expr.toString()).add(PAR2).toString();
  }
}
