package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import org.basex.io.serial.Serializer;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Namespace node.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FNames extends FNode {
  /** Namespace name. */
  private final byte[] name;

  /**
   * Default constructor.
   * @param n name
   * @param v value
   */
  public FNames(final byte[] n, final byte[] v) {
    super(NodeType.NSP);
    name = n;
    val = v;
  }

  @Override
  public QNm qname() {
    return new QNm(name);
  }

  @Override
  public byte[] name() {
    return name;
  }

  @Override
  public FNode copy() {
    return new FNames(name, val);
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.namespace(name, val);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, name, VAL, val);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add(' ').add(XMLNS);
    if(name.length != 0) tb.add(':').add(name);
    return tb.add("=\"").add(Token.string(val).replaceAll("\"", "\"\"")).
        add('"').toString();
  }
}
