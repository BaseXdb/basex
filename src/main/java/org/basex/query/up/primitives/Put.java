package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;
import java.io.IOException;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.Nod;
import org.basex.query.item.Uri;
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
   * @param up updating expression
   * @param n node to put
   * @param u location uri
   */
  public Put(final ParseExpr up, final Nod n, final Uri u) {
    super(up, n);
    uri = u;
  }

  @Override
  public void apply(final int add) throws QueryException {
    PrintOutput out = null;
    try {
      out = new PrintOutput(Token.string(path()));
      node.serialize(new XMLSerializer(out));
    } catch(final IOException ex) {
      parent.error(UPPUTERR, path());
    } finally {
      try { if(out != null) out.close(); } catch(final IOException ex) { }
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