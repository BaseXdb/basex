package org.basex.query.parse.syntax;

import java.util.*;

import org.basex.query.parse.gen.*;

/**
 * Accumulates the items of a left-recursive list, which the parser reduces one item at a time.
 * The record of the list is created once, when the list is used by its parent.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
final class ListAcc implements Node {
  /** Start offset. */
  private final int begin;
  /** End offset. */
  private int end;
  /** Items. */
  private Node[] items = new Node[4];
  /** Number of items. */
  private int size;

  /**
   * Constructor.
   * @param begin start offset
   */
  ListAcc(final int begin) {
    this.begin = begin;
  }

  /**
   * Adds an item.
   * @param item item
   * @param offset end offset of the list
   */
  void add(final Node item, final int offset) {
    if(size == items.length) items = Arrays.copyOf(items, size << 1);
    items[size++] = item;
    end = offset;
  }

  /**
   * Sets the end offset.
   * @param offset end offset
   */
  void end(final int offset) {
    end = offset;
  }

  /**
   * Creates the record of the list.
   * @return record
   */
  Node finish() {
    return new Nodes.PositionalArguments(begin, end, Arrays.copyOf(items, size));
  }

  @Override
  public int sym() {
    return Sym.PositionalArguments;
  }

  @Override
  public int begin() {
    return begin;
  }

  @Override
  public int end() {
    return end;
  }
}
