package org.basex.query.up.primitives;

import org.basex.query.item.Nod;

/**
 * Abstract udpate primitive which holds an aditional 'name' attribute to for
 * updating values, names, etc.. 
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class NewValue extends UpdatePrimitive {
  /** New name. */
  final byte[] name;

  /**
   * Constructor.
   * @param n target node
   * @param newName new name
   */
  public NewValue(final Nod n, final byte[] newName) {
    super(n);
    name = newName;
  }

  @Override
  public void check() {
  }
}