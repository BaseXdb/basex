package org.basex.query.up.primitives;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Update primitive for the fn:put() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class Put extends UpdatePrimitive {
  /** Target paths. The same node can be stored in multiple locations. */
  private final StringList paths = new StringList(1);

  /**
   * Constructor.
   * @param i input info
   * @param p pre
   * @param d data
   * @param u uri
   */
  public Put(final InputInfo i, final int p, final Data d, final String u) {
    super(PrimitiveType.PUT, p, d, i);
    paths.add(u);
  }

  @Override
  public void apply() throws QueryException {
    for(final String u : paths) {
      final DBNode node = new DBNode(data, pre);
      try {
        final PrintOutput po = new PrintOutput(u);
        try {
          final SerializerProp pr = new SerializerProp();
          // try to reproduce non-chopped documents correctly
          pr.set(SerializerProp.S_INDENT, node.data.meta.chop ? YES : NO);
          final Serializer ser = Serializer.get(po, pr);
          ser.serialize(node);
          ser.close();
        } finally {
          po.close();
        }
      } catch(final IOException ex) {
        UPPUTERR.thrw(info, u);
      }
    }
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    for(final String u : ((Put) p).paths) paths.add(u);
  }

  @Override
  public int size() {
    return paths.size();
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + targetNode() + ", " + paths.get(0) + ']';
  }
}
