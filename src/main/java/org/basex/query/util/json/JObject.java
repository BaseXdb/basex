package org.basex.query.util.json;

import static org.basex.data.DataText.*;

import org.basex.util.list.TokenList;

/**
 * JSON object.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class JObject extends JStruct {
  /** Names. */
  final TokenList names = new TokenList();

  /**
   * Adds a child.
   * @param n name to be added
   * @param v value to be added
   */
  void add(final byte[] n, final JValue v) {
    names.add(n);
    add(v);
  }

  /**
   * Returns the specified name.
   * @param p node position
   * @return name
   */
  byte[] name(final int p) {
    return names.get(p);
  }

  @Override
  byte[] type() {
    return OBJ;
  }
}
