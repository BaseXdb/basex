package org.basex.query.xpath.internal;

import static org.basex.query.xpath.XPText.*;
import org.basex.data.Serializer;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.IntList;
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
  private final IndexToken index;

  /**
   * Constructor.
   * @param ind index reference
   */
  public IndexAccess(final IndexToken ind) {
    index = ind;
  }

  @Override
  public NodeSet eval(final XPContext ctx) {
    final IndexIterator it = ctx.local.data.ids(index);
    final IntList il = new IntList();
    while(it.more()) {
      ctx.checkStop();
      il.add(it.next());
    }
    ctx.local = new NodeSet(il.finish(), ctx);
    return ctx.local;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, Token.token(TYPE),
        Token.token(index.type.toString()));
    ser.item(index.get());
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public String toString() {
    return Token.string(name()) + "(" + index.type + ", \"" +
      Token.string(index.get()) + "\")";
  }
}
