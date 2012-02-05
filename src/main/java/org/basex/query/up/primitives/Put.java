package org.basex.query.up.primitives;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;
import java.io.IOException;

import org.basex.data.Data;
import org.basex.io.IOFile;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerProp;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Uri;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Update primitive for the fn:put() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class Put extends UpdatePrimitive {
  /** Put location. The same node can be stored in multiple locations. */
  private Uri[] uri = new Uri[1];
  /** Serializer properties. */
  private final QueryContext ctx;

  /**
   * Constructor.
   * @param i input info
   * @param p pre
   * @param d data
   * @param u uri
   * @param context query context
   */
  public Put(final InputInfo i, final int p, final Data d, final Uri u,
      final QueryContext context) {
    super(PrimitiveType.PUT, p, d, i);
    uri[0] = u;
    ctx = context;
  }

  @Override
  public void apply() throws QueryException {
    for(int i = 0; i < uri.length; i++) {
      PrintOutput po = null;
      final DBNode node = new DBNode(data, pre);
      try {
        po = new PrintOutput(path(i));
        final SerializerProp pr = ctx.serProp(false);
        // try to reproduce non-chopped documents correctly
        pr.set(SerializerProp.S_INDENT, node.data.meta.chop ? YES : NO);
        node.serialize(Serializer.get(po, pr));
      } catch(final IOException ex) {
        UPPUTERR.thrw(input, path(i));
      } finally {
        if(po != null) try { po.close(); } catch(final IOException ex) { }
      }
    }
  }

  /**
   * Returns uri as string.
   * @param p uri position
   * @return string uri
   */
  public String path(final int p) {
    return new IOFile(uri[p].toJava()).path();
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    final int l = uri.length;
    final Uri[] t = new Uri[l + 1];
    System.arraycopy(uri, 0, t, 0, l);
    t[l] = ((Put) p).uri[0];
    uri = t;
  }

  @Override
  public int size() {
    return uri.length;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + targetNode() + ", " + uri[0] + ']';
  }
}
