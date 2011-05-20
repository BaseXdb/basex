package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.ANode;
import org.basex.query.item.NodeType;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Replace value primitive.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceValue extends Primitive {
  /** New value. */
  private final byte[] value;

  /**
   * Constructor.
   * @param ii input info
   * @param n target node
   * @param val new value
   */
  public ReplaceValue(final InputInfo ii, final ANode n, final byte[] val) {
    super(PrimitiveType.REPLACEVALUE, ii, n);
    value = val;
  }

  @Override
  public int apply(final int add) {
    final DBNode n = (DBNode) node;
    final Data d = n.data;

    if(n.type == NodeType.TXT && value.length == 0) {
      // empty text nodes must be removed
      d.delete(n.pre);
    } else {
      d.replace(n.pre, d.kind(n.pre), value);
    }
    return 0;
  }

  @Override
  public void merge(final Primitive p) throws QueryException {
    UPMULTREPV.thrw(input, node);
  }

  @Override
  public String toString() {
    return Util.info("%[%, %]", Util.name(this), node, value);
  }
}
