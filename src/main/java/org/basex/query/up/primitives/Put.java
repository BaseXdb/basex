package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.data.DataText;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.item.Uri;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Put primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public final class Put extends UpdatePrimitive {
  /** Put location. */
  private final Uri uri;

  /**
   * Constructor.
   * @param ii input info
   * @param n node to put
   * @param u location uri
   */
  public Put(final InputInfo ii, final Nod n, final Uri u) {
    super(ii, n);
    uri = u;
  }

  @Override
  public void apply(final int add) throws QueryException {
    PrintOutput out = null;
    try {
      out = new PrintOutput(Token.string(path()));
      final SerializerProp sp = new SerializerProp();
      sp.set(SerializerProp.S_INDENT, DataText.NO);
      XMLSerializer xml = new XMLSerializer(out, sp);
      node.serialize(xml);
    } catch(final IOException ex) {
      UPPUTERR.thrw(input, path());
    } finally {
      try { out.close(); } catch(final Exception ex) { }
    }
  }

  /**
   * Returns uri as string.
   * @return string uri
   */
  public byte[] path() {
    return uri.atom();
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.PUT;
  }

  @Override
  public String toString() {
    return "fn:put";
  }
}