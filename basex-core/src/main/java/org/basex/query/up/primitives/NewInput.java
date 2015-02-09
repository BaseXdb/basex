package org.basex.query.up.primitives;

import org.basex.io.*;
import org.basex.query.value.node.*;

/**
 * Container for inputs that are to be added to a database.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class NewInput {
  /** Node to be added. */
  public ANode node;
  /** Input reference. */
  public IO io;

  /** Target path. */
  public byte[] path;
  /** Target database. */
  public byte[] dbname;
}
