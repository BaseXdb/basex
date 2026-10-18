package org.basex.query.expr.path;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XPath axes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum Axis {
  // ...order is important here for parsing the Query;
  // axes with longer names are parsed first

  /** Ancestor-or-self axis. */
  ANCESTOR_OR_SELF("ancestor-or-self", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.ancestorIter(true, test);
    }
  },

  /** Ancestor axis. */
  ANCESTOR("ancestor", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.ancestorIter(false, test);
    }
  },

  /** Attribute axis. */
  ATTRIBUTE("attribute", true) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info)
      throws QueryException {
      if(node instanceof JNode) throw ATTRAXIS_X.get(info, node.seqType());
      return node.attributeIter(test);
    }
  },

  /** Child axis. */
  CHILD("child", true) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.childIter(test, false);
    }
  },

  /** Descendant-or-self axis. */
  DESCENDANT_OR_SELF("descendant-or-self", true) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.descendantIter(true, test);
    }
  },

  /** Descendant axis. */
  DESCENDANT("descendant", true) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.descendantIter(false, test);
    }
  },

  /** Following-sibling-or-self axis. */
  FOLLOWING_SIBLING_OR_SELF("following-sibling-or-self", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.followingSiblingIter(true, test);
    }
  },

  /** Following-sibling axis. */
  FOLLOWING_SIBLING("following-sibling", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.followingSiblingIter(false, test);
    }
  },

  /** Following-or-self axis. */
  FOLLOWING_OR_SELF("following-or-self", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.followingIter(true, test);
    }
  },

  /** Following axis. */
  FOLLOWING("following", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.followingIter(false, test);
    }
  },

  /** Item axis (JNodes only). */
  ITEM("item", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info)
      throws QueryException {
      if(node instanceof final JNode jnode) return jnode.itemIter(test);
      throw ITEMAXIS_X.get(info, node.seqType());
    }
  },

  /** Parent axis. */
  PARENT("parent", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.parentIter(test);
    }
  },

  /** Preceding-sibling-or-self axis. */
  PRECEDING_SIBLING_OR_SELF("preceding-sibling-or-self", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.precedingSiblingIter(true, test);
    }
  },

  /** Preceding-sibling axis. */
  PRECEDING_SIBLING("preceding-sibling", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.precedingSiblingIter(false, test);
    }
  },

  /** Preceding-or-self axis. */
  PRECEDING_OR_SELF("preceding-or-self", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.precedingIter(true, test);
    }
  },

  /** Preceding axis. */
  PRECEDING("preceding", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.precedingIter(false, test);
    }
  },

  /** Self axis. */
  SELF("self", true) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.selfIter(test);
    }
  };

  /** Name of axis. */
  public final String name;
  /** Downward axis. */
  public final boolean down;

  /**
   * Constructor.
   * @param name axis string
   * @param down downward axis
   */
  Axis(final String name, final boolean down) {
    this.name = name;
    this.down = down;
  }

  /**
   * Returns a node iterator.
   * @param node input node
   * @param test node test
   * @param info input info (can be {@code null})
   * @return node iterator
   * @throws QueryException query exception
   */
  abstract BasicNodeIter iter(GNode node, Test test, InputInfo info) throws QueryException;

  /**
   * Checks if this is one of the specified axes.
   * @param axis1 first axis
   * @param axis2 second axis
   * @return result of check
   */
  public boolean oneOf(final Axis axis1, final Axis axis2) {
    return this == axis1 || this == axis2;
  }

  /**
   * Checks if this is one of the specified axes.
   * @param axis1 first axis
   * @param axis2 second axis
   * @param axis3 third axis
   * @return result of check
   */
  public boolean oneOf(final Axis axis1, final Axis axis2, final Axis axis3) {
    return this == axis1 || this == axis2 || this == axis3;
  }

  /**
   * Checks if this is one of the specified candidates.
   * @param candidates candidates
   * @return result of check
   */
  public boolean oneOf(final Axis... candidates) {
    return Enums.oneOf(this, candidates);
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * Returns the axis that is equivalent to this axis if it is applied to root nodes.
   * @return equivalent axis, or {@code null} if no nodes will be returned
   */
  public final Axis onRoot() {
    return switch(this) {
      case ANCESTOR, FOLLOWING, FOLLOWING_SIBLING, PARENT, PRECEDING, PRECEDING_SIBLING -> null;
      case ANCESTOR_OR_SELF, FOLLOWING_OR_SELF, FOLLOWING_SIBLING_OR_SELF, PRECEDING_OR_SELF,
           PRECEDING_SIBLING_OR_SELF -> SELF;
      default -> this;
    };
  }

  /**
   * Returns the axis that is equivalent to this axis if it is applied to leaf nodes.
   * @return equivalent axis, or {@code null} if no nodes will be returned
   */
  public final Axis onLeaf() {
    return switch(this) {
      case CHILD, DESCENDANT -> null;
      case DESCENDANT_OR_SELF -> SELF;
      default -> this;
    };
  }

  /**
   * Inverts the axis.
   * @return inverted axis
   */
  public final Axis invert() {
    return switch(this) {
      case ANCESTOR                  -> DESCENDANT;
      case ANCESTOR_OR_SELF          -> DESCENDANT_OR_SELF;
      case ATTRIBUTE, CHILD          -> PARENT;
      case DESCENDANT                -> ANCESTOR;
      case DESCENDANT_OR_SELF        -> ANCESTOR_OR_SELF;
      case FOLLOWING_SIBLING         -> PRECEDING_SIBLING;
      case FOLLOWING_SIBLING_OR_SELF -> PRECEDING_SIBLING_OR_SELF;
      case FOLLOWING                 -> PRECEDING;
      case FOLLOWING_OR_SELF         -> PRECEDING_OR_SELF;
      case ITEM                      -> PARENT;
      case PARENT                    -> CHILD;
      case PRECEDING_SIBLING         -> FOLLOWING_SIBLING;
      case PRECEDING_SIBLING_OR_SELF -> FOLLOWING_SIBLING_OR_SELF;
      case PRECEDING                 -> FOLLOWING;
      case PRECEDING_OR_SELF         -> FOLLOWING_OR_SELF;
      case SELF                      -> SELF;
    };
  }
}
