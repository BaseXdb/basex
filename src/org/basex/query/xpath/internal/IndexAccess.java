package org.basex.query.xpath.internal;

import static org.basex.query.xpath.XPText.*;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.Index;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Token;

/**
 * This index class retrieves attribute values from the index.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 * @author Christian Gruen
 */
public final class IndexAccess extends InternalExpr {
  /** Index type. */
  private final Index.TYPE type;
  /** Token to be found in the index. */
  private final byte[] token;

  /**
   * Constructor.
   * @param typ index type
   * @param tok index token
   */
  public IndexAccess(final Index.TYPE typ, final byte[] tok) {
    token = tok;
    type = typ;
  }

  @Override
  public NodeSet eval(final XPContext ctx) {
    final Data data = ctx.local.data;
    ctx.local = new NodeSet(data.ids(type, token), ctx);
    return ctx.local;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, TYPE, Token.token(type.toString()));
    ser.item(token);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public String toString() {
    return Token.string(name()) + "(" + type + ", \"" +
      Token.string(token) + "\")";
  }
}
