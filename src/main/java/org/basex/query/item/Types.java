package org.basex.query.item;

/**
 * XQuery data type register.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Types {

  /** Hidden default constructor. */
  private Types() { }

  /**
   * Finds and returns the specified data type.
   * @param type type as string
   * @param atom atomic type
   * @return type or {@code null}
   */
  public static Type find(final QNm type, final boolean atom) {
    // atomic types
    final SimpleType st = SimpleType.find(type, atom);
    if(st != null) return st;

    // node types
    final NodeType nt = NodeType.find(type, atom);
    if(nt != null) return nt;

    return null;
  }
}
