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
      return node.ancestorIter(true);
    }
  },

  /** Ancestor axis. */
  ANCESTOR("ancestor", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.ancestorIter(false);
    }
  },

  /** Attribute axis. */
  ATTRIBUTE("attribute", true) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info)
      throws QueryException {
      if(node instanceof JNode) throw ATTRAXIS_X.get(info, node.seqType());
      return node.attributeIter();
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
      return node.followingSiblingIter(true);
    }
  },

  /** Following-sibling axis. */
  FOLLOWING_SIBLING("following-sibling", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.followingSiblingIter(false);
    }
  },

  /** Following-or-self axis. */
  FOLLOWING_OR_SELF("following-or-self", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.followingIter(true);
    }
  },

  /** Following axis. */
  FOLLOWING("following", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.followingIter(false);
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
      return node.parentIter();
    }
  },

  /** Preceding-sibling-or-self axis. */
  PRECEDING_SIBLING_OR_SELF("preceding-sibling-or-self", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.precedingSiblingIter(true);
    }
  },

  /** Preceding-sibling axis. */
  PRECEDING_SIBLING("preceding-sibling", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.precedingSiblingIter(false);
    }
  },

  /** Preceding-or-self axis. */
  PRECEDING_OR_SELF("preceding-or-self", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.precedingIter(true);
    }
  },

  /** Preceding axis. */
  PRECEDING("preceding", false) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.precedingIter(false);
    }
  },

  /** Self axis. */
  SELF("self", true) {
    @Override
    BasicNodeIter iter(final GNode node, final Test test, final InputInfo info) {
      return node.selfIter();
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
