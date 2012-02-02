package org.basex.query.util.json;

import org.basex.util.list.ObjList;

/**
 * JSON structure.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class JStruct extends JValue {
  /** Values. */
  private final ObjList<JValue> nodes = new ObjList<JValue>();

  /**
   * Adds a child.
   * @param n child to be added
   */
  void add(final JValue n) {
    nodes.add(n);
  }

  /**
   * Returns the specified value.
   * @param p node position
   * @return value
   */
  JValue value(final int p) {
    return nodes.get(p);
  }

  /**
   * Returns the number of children.
   * @return number of children
   */
  int size() {
    return nodes.size();
  }
}
