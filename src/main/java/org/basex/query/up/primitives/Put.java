package org.basex.query.up.primitives;

import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.item.Uri;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Put primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class Put extends UpdatePrimitive {
  /** Put location. */
  private final Uri uri;

  /** Serializer properties. */
  private final SerializerProp props;

  /**
   * Constructor.
   * @param ii input info
   * @param n node to put
   * @param u location uri
   * @param sp serializer properties
   */
  public Put(final InputInfo ii, final ANode n, final Uri u,
      final SerializerProp sp) {
    super(ii, n);
    uri = u;
    props = sp;
  }

  @Override
  public void apply(final int add) throws QueryException {
    PrintOutput out = null;
    try {
      out = new PrintOutput(Token.string(path()));

      SerializerProp pr = props;
      // try to reproduce non-chopped documents correctly
      if(props == null && node instanceof DBNode) {
        pr = new SerializerProp();
        pr.set(SerializerProp.S_INDENT,
            ((DBNode) node).data.meta.chop ? YES : NO);
      }
      node.serialize(new XMLSerializer(out, pr));
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