package org.basex.query.up.primitives;

import org.basex.io.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Container for inputs that are to be added to a database.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class NewInput {
  /** Target path. */
  public String path;
  /** Node to be added (can be {@code null}). */
  public ANode node;
  /** Input reference (can be {@code null}). */
  public IO io;

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(Util.className(getClass())).add('[');
    tb.add("path: \"").add(path).add("\", ");
    if(node != null) tb.add("node");
    else tb.add("io: ").addExt(io);
    return tb.add(']').toString();
  }
}
