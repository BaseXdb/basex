package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQTokens.*;
import static org.basex.query.xquery.XQText.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.SeqType;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;

/**
 * Treat as expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Treat extends Single {
  /** Instance. */
  final SeqType seq;

  /**
   * Constructor.
   * @param e expression
   * @param s sequence type
   */
  public Treat(final Expr e, final SeqType s) {
    super(e);
    seq = s;
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final Iter iter = ctx.iter(expr);
    final Item it = iter.next();
    if(it == null) {
      if(seq.type == Type.EMP || seq.occ % 2 != 0) return Iter.EMPTY;
      Err.empty(this);
    }
    if(seq.occ < 2) {
      if(iter.next() != null) Err.or(NOTREATS, info(), seq);
      if(!it.type.instance(seq.type)) Err.or(NOTREAT, info(), seq, it.type);
      return it.iter();
    }

    return new Iter() {
      Item i = it;
      
      @Override
      public Item next() throws XQException {
        if(i == null) return null;
        if(!i.type.instance(seq.type)) Err.or(NOTREAT, info(), seq, i.type);
        final Item ii = i;
        i = iter.next();
        return ii;
      }
      @Override
      public String toString() {
        return Treat.this.toString();
      }
    };
  }

  @Override
  public String toString() {
    return expr + " castable?";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, TYPE, Token.token(seq.toString()));
    expr.plan(ser);
    ser.closeElement();
  }
}
