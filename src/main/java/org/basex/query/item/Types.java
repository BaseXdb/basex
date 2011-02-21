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

    return !atom && type.uri() == Uri.EMPTY && eq(type.ln(), token(FUNCTION)) ?
        FunType.ANY : null;
  }
}
