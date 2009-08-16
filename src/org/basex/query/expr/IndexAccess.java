package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.data.Data.Type;
import org.basex.index.IndexIterator;
import org.basex.index.ValuesToken;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This index class retrieves texts and attribute values from the index.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class IndexAccess extends Single {
  /** Index context. */
  final IndexContext ictx;
  /** Access type. */
  final Type type;

  /**
   * Constructor.
   * @param e index expression
   * @param t access type
   * @param ic index context
   */
  public IndexAccess(final Expr e, final Type t, final IndexContext ic) {
    super(e);
    type = t;
    ictx = ic;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return new Iter() {
      final ValuesToken ind = new ValuesToken(type, expr.atomic(ctx).str());
      final IndexIterator it = ictx.data.ids(ind);

      @Override
      public Item next() {
        return it.more() ? new DBNode(ictx.data, it.next()) : null;
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
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof IndexAccess;
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(type.toString()));
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(name());
    tb.add("(" + type + ", ");
    tb.add(expr);
    return tb.add(")").toString();
  }
}
