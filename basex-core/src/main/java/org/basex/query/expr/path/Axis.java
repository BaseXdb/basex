package org.basex.query.expr.path;

import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XPath axes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum Axis {
  // ...order is important here for parsing the Query;
  // axes with longer names are parsed first

  /** Ancestor-or-self axis. */
  ANCESTOR_OR_SELF("ancestor-or-self", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.ancestorOrSelfIter();
    }
  },

  /** Ancestor axis. */
  ANCESTOR("ancestor", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.ancestorIter();
    }
  },

  /** Attribute axis. */
  ATTRIBUTE("attribute", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.attributeIter();
    }
  },

  /** Child axis. */
  CHILD("child", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.childIter();
    }
  },

  /** Descendant-or-self axis. */
  DESCENDANT_OR_SELF("descendant-or-self", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.descendantOrSelfIter();
    }
  },

  /** Descendant axis. */
  DESCENDANT("descendant", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.descendantIter();
    }
  },

  /** Following-sibling axis. */
  FOLLOWING_SIBLING("following-sibling", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.followingSiblingIter();
    }
  },

  /** Following axis. */
  FOLLOWING("following", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.followingIter();
    }
  },

  /** Parent axis. */
  PARENT("parent", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.parentIter();
    }
  },

  /** Preceding-sibling axis. */
  PRECEDING_SIBLING("preceding-sibling", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.precedingSiblingIter();
    }
  },

  /** Preceding axis. */
  PRECEDING("preceding", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.precedingIter();
    }
  },

  /** Step axis. */
  SELF("self", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.selfIter();
    }
  };

  /** Cached enums (faster). */
  public static final Axis[] VALUES = values();
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
   * @param n input node
   * @return node iterator
   */
  abstract BasicNodeIter iter(ANode n);

  @Override
  public String toString() {
    return name;
  }

  /**
   * Inverts the axis.
   * @return inverted axis
   */
  public final Axis invert() {
    switch(this) {
      case ANCESTOR:           return DESCENDANT;
      case ANCESTOR_OR_SELF:   return DESCENDANT_OR_SELF;
      case ATTRIBUTE:
      case CHILD:              return PARENT;
      case DESCENDANT:         return ANCESTOR;
      case DESCENDANT_OR_SELF: return ANCESTOR_OR_SELF;
      case FOLLOWING_SIBLING:  return PRECEDING_SIBLING;
      case FOLLOWING:          return PRECEDING;
      case PARENT:             return CHILD;
      case PRECEDING_SIBLING:  return FOLLOWING_SIBLING;
      case PRECEDING:          return FOLLOWING;
      case SELF:               return SELF;
      default:                 throw Util.notExpected();
    }
  }
}
