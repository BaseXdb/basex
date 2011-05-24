package org.basex.query.up.primitives;

import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.IOFile;
import org.basex.io.PrintOutput;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.item.Uri;
import org.basex.util.InputInfo;

/**
 * Put primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class Put extends Primitive {
  /** Put location. */
  private final Uri uri;
  /** Serializer properties. */
  private final QueryContext ctx;

  /**
   * Constructor.
   * @param ii input info
   * @param n node to put
   * @param u location uri
   * @param c query context
   */
  public Put(final InputInfo ii, final ANode n, final Uri u,
      final QueryContext c) {
    super(ii, n);
    uri = u;
    ctx = c;
  }

  @Override
  public int apply(final int add) throws QueryException {
    PrintOutput out = null;
    try {
      out = new PrintOutput(path());

      final SerializerProp pr = ctx.serProp(false);
      // try to reproduce non-chopped documents correctly
      if(node instanceof DBNode) pr.set(SerializerProp.S_INDENT,
          ((DBNode) node).data.meta.chop ? YES : NO);

      node.serialize(new XMLSerializer(out, pr));
    } catch(final IOException ex) {
      UPPUTERR.thrw(input, path());
    } finally {
      if(out != null) try { out.close(); } catch(final Exception ex) { }
    }
    return 0;
  }

  /**
   * Returns uri as string.
   * @return string uri
   */
  public String path() {
    return new IOFile(uri.toJava()).path();
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