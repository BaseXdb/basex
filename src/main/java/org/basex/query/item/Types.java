package org.basex.query.item;

import static org.basex.query.QueryTokens.*;
import static org.basex.util.Token.*;

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
    final AtomType st = AtomType.find(type, atom);
    if(st != null) return st;

    // node types
    if(!atom) {
      final NodeType nt = NodeType.find(type);
      if(nt != null) return nt;
    }

    if(!atom && type.uri() == Uri.EMPTY) {
      final byte[] ln = type.ln();
      if(eq(ln, token(FUNCTION))) return FunType.ANY_FUN;
      if(eq(ln, MAP)) return MapType.ANY_MAP;
    }

    return null;
  }
}
