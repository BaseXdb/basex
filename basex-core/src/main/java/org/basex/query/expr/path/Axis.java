package org.basex.query.expr.path;

import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XPath axes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public enum Axis {
  // ...order is important here for parsing the Query;
  // axes with longer names are parsed first

  /** Ancestor-or-self axis. */
  ANCORSELF("ancestor-or-self", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.ancestorOrSelf();
    }
  },

  /** Ancestor axis. */
  ANC("ancestor", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.ancestor();
    }
  },

  /** Attribute axis. */
  ATTR("attribute", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.attributes();
    }
  },

  /** Child Axis. */
  CHILD("child", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.children();
    }
  },

  /** Descendant-or-self axis. */
  DESCORSELF("descendant-or-self", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.descendantOrSelf();
    }
  },

  /** Descendant axis. */
  DESC("descendant", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.descendant();
    }
  },

  /** Following-Sibling axis. */
  FOLLSIBL("following-sibling", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.followingSibling();
    }
  },

  /** Following axis. */
  FOLL("following", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.following();
    }
  },

  /** Parent axis. */
  PARENT("parent", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.parentIter();
    }
  },

  /** Preceding-Sibling axis. */
  PRECSIBL("preceding-sibling", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.precedingSibling();
    }
  },

  /** Preceding axis. */
  PREC("preceding", false) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.preceding();
    }
  },

  /** Step axis. */
  SELF("self", true) {
    @Override
    BasicNodeIter iter(final ANode n) {
      return n.self();
    }
  };

  /** Cached enums (faster). */
  public static final Axis[] VALUES = values();
  /** Axis string. */
  public final String name;
  /** Descendant axis flag. */
  public final boolean down;

  /**
   * Constructor.
   * @param name axis string
   * @param down descendant flag
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
  abstract BasicNodeIter iter(final ANode n);

  @Override
  public String toString() {
    return name;
  }

  /**
   * Inverts the axis.
   * @return inverted axis
   */
  final Axis invert() {
    switch(this) {
      case ANC:        return DESC;
      case ANCORSELF:  return DESCORSELF;
      case ATTR:
      case CHILD:      return PARENT;
      case DESC:       return ANC;
      case DESCORSELF: return ANCORSELF;
      case FOLLSIBL:   return PRECSIBL;
      case FOLL:       return PREC;
      case PARENT:     return CHILD;
      case PRECSIBL:   return FOLLSIBL;
      case PREC:       return FOLL;
      case SELF:       return SELF;
      default:         throw Util.notExpected();
    }
  }
}
