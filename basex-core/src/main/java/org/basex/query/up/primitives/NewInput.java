package org.basex.query.up.primitives;

import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Container for inputs that are to be appended to a database.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NewInput {
  /** Database path. */
  public String path;
  /** Resource type (defaults to XML). */
  public ResourceType type = ResourceType.XML;
  /** Node to be added (only for XML resources). */
  public XNode node;
  /** IO reference to be added ({@code null} for in-memory inputs). */
  public IO io;
  /** XDM value to be stored (for binary or value resources). */
  public Value value;

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this));
    sb.append("[path: \"").append(path).append("\", type: ").append(type).append(", ");
    if(node != null) sb.append("node");
    else if(io != null) sb.append("io: ").append(io);
    else sb.append("value");
    return sb.append(']').toString();
  }
}
