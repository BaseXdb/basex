package org.basex.query.up;

import org.basex.query.item.Nod;


/**
 * Represents a rename primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class RenamePrimitive extends UpdatePrimitive {
  /** New name. */
  final byte[] newName;

  /**
   * Constructor.
   * @param n target node
   * @param name new name
   */
  public RenamePrimitive(final Nod n, final byte[] name) {
    super(n);
    newName = name;
  }
}
