package org.basex.query.xpath.internal;

import static org.basex.query.xpath.XPText.*;

import org.basex.BaseX;
import org.basex.data.Serializer;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
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
  private final IndexToken ind;

  /**
   * Constructor.
   * @param i index reference
   */
  public IndexAccess(final IndexToken i) {
    ind = i;
  }

  @Override
  public NodeSet eval(final XPContext ctx) {
    final IndexIterator it = ctx.item.data.ids(ind);
    final int[] ids = new int[it.size()];
    int i = 0;
    while(it.more()) ids[i++] = it.next();
    ctx.item = new NodeSet(ids, ctx);
    return ctx.item;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, Token.token(TYPE), Token.token(ind.type.toString()));
    ser.item(ind.get());
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "CC99FF";
  }

  @Override
  public String toString() {
    return BaseX.info("%(%, \"%\")", name(), ind.type, ind.get());
  }
}
