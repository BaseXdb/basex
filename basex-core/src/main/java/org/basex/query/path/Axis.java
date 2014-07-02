package org.basex.query.path;

import org.basex.query.iter.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * XPath axes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public enum Axis {
  // ...order is important here for parsing the Query;
  // axes with longer names are parsed first

  /** Ancestor-or-self axis. */
  ANCORSELF("ancestor-or-self", false) {
    @Override
    AxisIter iter(final ANode n) {
      return n.ancestorOrSelf();
    }
  },

  /** Ancestor axis. */
  ANC("ancestor", false) {
    @Override
    AxisIter iter(final ANode n) {
      return n.ancestor();
    }
  },

  /** Attribute axis. */
  ATTR("attribute", true) {
    @Override
    AxisIter iter(final ANode n) {
      return n.attributes();
    }
  },

  /** Child Axis. */
  CHILD("child", true) {
    @Override
    AxisIter iter(final ANode n) {
      return n.children();
    }
  },

  /** Descendant-or-self axis. */
  DESCORSELF("descendant-or-self", true) {
    @Override
    AxisIter iter(final ANode n) {
      return n.descendantOrSelf();
    }
  },

  /** Descendant axis. */
  DESC("descendant", true) {
    @Override
    AxisIter iter(final ANode n) {
      return n.descendant();
    }
  },

  /** Following-Sibling axis. */
  FOLLSIBL("following-sibling", false) {
    @Override
    AxisIter iter(final ANode n) {
      return n.followingSibling();
    }
  },

  /** Following axis. */
  FOLL("following", false) {
    @Override
    AxisIter iter(final ANode n) {
      return n.following();
    }
  },

  /** Parent axis. */
  PARENT("parent", true) {
    @Override
    AxisIter iter(final ANode n) {
      return n.parentIter();
    }
  },

  /** Preceding-Sibling axis. */
  PRECSIBL("preceding-sibling", false) {
    @Override
    AxisIter iter(final ANode n) {
      return n.precedingSibling();
    }
  },

  /** Preceding axis. */
  PREC("preceding", false) {
    @Override
    AxisIter iter(final ANode n) {
      return n.preceding();
    }
  },

  /** Step axis. */
  SELF("self", true) {
    @Override
    AxisIter iter(final ANode n) {
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
   * @param n axis string
   * @param d descendant flag
   */
  Axis(final String n, final boolean d) {
    name = n;
    down = d;
  }

  /**
   * Returns a node iterator.
   * @param n input node
   * @return node iterator
   */
  abstract AxisIter iter(final ANode n);

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
