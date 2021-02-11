package org.basex.query.up.primitives;

import org.basex.io.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Container for inputs that are to be appended to a database.
 *
 * @author BaseX Team 2005-21, BSD License
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
    return Util.className(this) + '[' + "path: \"" + path + "\", " +
        (node != null ? "node" : "io: " + io) + ']';
  }
}
