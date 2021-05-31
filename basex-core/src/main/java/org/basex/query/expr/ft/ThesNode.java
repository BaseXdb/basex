package org.basex.query.expr.ft;

import org.basex.util.*;

/**
 * Single thesaurus node.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class ThesNode {
  /** Term. */
  final byte[] term;

  /** Related nodes. */
  ThesNode[] nodes = new ThesNode[1];
  /** Type of relationship. */
  byte[][] relation = new byte[1][];
  /** Entries. */
  int size;

  /**
   * Constructor.
   * @param term term
   */
  ThesNode(final byte[] term) {
    this.term = term;
  }

  /**
   * Adds a relationship to the node.
   * @param node related node
   * @param rel type of relationship
   */
  void add(final ThesNode node, final byte[] rel) {
    if(size == nodes.length) {
      final int s = Array.newCapacity(size);
      nodes = Array.copy(nodes, new ThesNode[s]);
      relation = Array.copyOf(relation, s);
    }
    nodes[size] = node;
    relation[size++] = rel;
  }
}
