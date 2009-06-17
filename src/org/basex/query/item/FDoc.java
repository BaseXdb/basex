package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.iter.NodIter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Document Node Fragment.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FDoc extends FNode {
  /** Base URI. */
  private byte[] base;

  /**
   * Constructor.
   * @param ch children
   * @param b base uri
   */
  public FDoc(final NodIter ch, final byte[] b) {
    super(Type.DOC);
    children = ch;
    base = b;
  }
  
  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    for(int c = 0; c < children.size; c++) {
      final Nod n = children.item[c];
      if(n.type != Type.COM && n.type != Type.PI) tb.add(n.str());
    }
    return tb.finish();
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < children.size; c++) children.item[c].serialize(ser);
  }

  @Override
  public byte[] base() {
    return base;
  }

  @Override
  public FDoc copy() {
    return new FDoc(children, base);
  }

  @Override
  public String toString() {
    return name() + "(" + Token.string(base) + ")";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, BASE, base);
  }
}
