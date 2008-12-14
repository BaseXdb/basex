package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xquery.iter.NodIter;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Document Node Fragment.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
    for(int c = 0; c < children.size; c++) tb.add(children.list[c].str());
    return tb.finish();
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < children.size; c++) children.list[c].serialize(ser);
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
    return Token.string(name()) + "(" + Token.string(base) + ")";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, BASE, base);
  }
}
