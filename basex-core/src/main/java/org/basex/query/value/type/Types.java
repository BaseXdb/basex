package org.basex.query.value.type;

import org.basex.query.value.type.Type.*;

/**
 * Numeric access to types.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Types {
  /** Private constructor. */
  private Types() { }

  /** Types. */
  private static final Type[] TYPES = new Type[ID.LAST.asByte()];

  static {
    for(final AtomType type : AtomType.values()) TYPES[type.index()] = type;
    for(final NodeType type : NodeType.values()) TYPES[type.index()] = type;
    TYPES[ID.FUN.asByte()] = SeqType.FUNCTION;
    TYPES[ID.MAP.asByte()] = SeqType.MAP;
    TYPES[ID.ARRAY.asByte()] = SeqType.ARRAY;
  }

  /**
   * Returns the type at the specified index.
   * @param index index
   * @return corresponding type if found, {@code null} otherwise
   */
  public static Type type(final int index) {
    return TYPES[index];
  }
}
