package org.basex.query.up.primitives;

import org.basex.io.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Container for inputs that are to be appended to a database.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class NewInput {
  /** Database path. */
  public String path;
  /** Node to be added ({@code null} if input reference exists). */
  public ANode node;
  /** Input to be added ({@code null} if node exists). */
  public IO io;

  @Override
  public String toString() {
    return Util.className(this) + '[' + "path: \"" + path + "\", " +
        (node != null ? "node" : "io: " + io) + ']';
  }
}
