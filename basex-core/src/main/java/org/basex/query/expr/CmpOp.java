package org.basex.query.expr;

import java.util.*;

import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Operators for general, value and node comparisons.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum CmpOp {
  /** General comparison: less or equal. */
  LE("<=", "precedes-or-is") {
    @Override
    public boolean eval(final int v) {
      return v != Item.NAN_DUMMY && v <= 0;
    }

    @Override
    public boolean eval(final ANode node1, final ANode node2) {
      return node1.compare(node2) <= 0;
    }

    @Override
    public CmpOp swap() {
      return GE;
    }

    @Override
    public CmpOp invert() {
      return GT;
    }
  },

  /** General comparison: less. */
  LT("<", "<<", "precedes") {
    @Override
    public boolean eval(final int v) {
      return v != Item.NAN_DUMMY && v < 0;
    }

    @Override
    public boolean eval(final ANode node1, final ANode node2) {
      return node1.compare(node2) < 0;
    }

    @Override
    public CmpOp swap() {
      return GT;
    }

    @Override
    public CmpOp invert() {
      return GE;
    }
  },

  /** General comparison: greater of equal. */
  GE(">=", "follows-or-is") {
    @Override
    public boolean eval(final int v) {
      return v >= 0;
    }

    @Override
    public boolean eval(final ANode node1, final ANode node2) {
      return node1.compare(node2) >= 0;
    }

    @Override
    public CmpOp swap() {
      return LE;
    }

    @Override
    public CmpOp invert() {
      return LT;
    }
  },

  /** General comparison: greater. */
  GT(">", ">>", "follows") {
    @Override
    public boolean eval(final int v) {
      return v > 0;
    }

    @Override
    public boolean eval(final ANode node1, final ANode node2) {
      return node1.compare(node2) > 0;
    }

    @Override
    public CmpOp swap() {
      return LT;
    }

    @Override
    public CmpOp invert() {
      return LE;
    }
  },

  /** General comparison: equal. */
  EQ("=", "is") {
    @Override
    public boolean eval(final int v) {
      return v == 0;
    }

    @Override
    public boolean eval(final ANode node1, final ANode node2) {
      return node1.is(node2);
    }

    @Override
    public CmpOp swap() {
      return EQ;
    }

    @Override
    public CmpOp invert() {
      return NE;
    }
  },

  /** General comparison: not equal. */
  NE("!=", "is-not") {
    @Override
    public boolean eval(final int v) {
      return v != 0;
    }

    @Override
    public boolean eval(final ANode node1, final ANode node2) {
      return !node1.is(node2);
    }

    @Override
    public CmpOp swap() {
      return NE;
    }

    @Override
    public CmpOp invert() {
      return EQ;
    }
  };

  /** General comparator. */
  public final String general;
  /** Value comparator. */
  public final String value;
  /** Node comparators. */
  public final String[] nodes;

  /**
   * Constructor.
   * @param general string representation or general comparator
   * @param nodes string representation or node comparators
   */
  CmpOp(final String general, final String... nodes) {
    this.general = general;
    this.nodes = nodes;
    value = name().toLowerCase(Locale.ENGLISH);
  }

  /**
   * Evaluates the expression.
   * @param diff difference between the compared values
   * @return result
   */
  public abstract boolean eval(int diff);

  /**
   * Compares two nodes.
   * @param node1 first node
   * @param node2 second node
   * @return result of comparison
   */
  public abstract boolean eval(ANode node1, ANode node2);

  /**
   * Swaps the comparator.
   * @return swapped comparator
   */
  public abstract CmpOp swap();

  /**
   * Inverts the comparator.
   * @return inverted comparator
   */
  public abstract CmpOp invert();

  /**
   * Returns the string representation of the value comparator.
   * @return value comparator
   */
  public String toValueString() {
    return value;
  }

  /**
   * Returns the first string representation of the node comparator.
   * @return node comparator
   */
  public String toNodeString() {
    return nodes[0];
  }

  /**
   * Returns the string representation of the general comparator.
   * @return value comparator
   */
  @Override
  public String toString() {
    return general;
  }

  /**
   * Checks if this is one of the specified candidates.
   * @param candidates candidates
   * @return result of check
   */
  public boolean oneOf(final CmpOp... candidates) {
    return Enums.oneOf(this, candidates);
  }
}
