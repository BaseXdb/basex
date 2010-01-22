package org.basex.query.path;

import org.basex.query.item.Nod;
import org.basex.query.iter.NodeIter;

/**
 * XPath axes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public enum Axis {
  // ...order is important here for parsing the Query;
  // axes with longer names are parsed first

  /** Ancestor-or-self axis. */
  ANCORSELF("ancestor-or-self", false) {
    @Override
    NodeIter init(final Nod n) {
      return n.ancOrSelf();
    }
  },

  /** Ancestor axis. */
  ANC("ancestor", false) {
    @Override
    NodeIter init(final Nod n) {
      return n.anc();
    }
  },

  /** Attribute axis. */
  ATTR("attribute", true) {
    @Override
    NodeIter init(final Nod n) {
      return n.attr();
    }
  },

  /** Child Axis. */
  CHILD("child", true) {
    @Override
    NodeIter init(final Nod n) {
      return n.child();
    }
  },

  /** Descendant-or-self axis. */
  DESCORSELF("descendant-or-self", true) {
    @Override
    NodeIter init(final Nod n) {
      return n.descOrSelf();
    }
  },

  /** Descendant axis. */
  DESC("descendant", true) {
    @Override
    NodeIter init(final Nod n) {
      return n.desc();
    }
  },

  /** Following-Sibling axis. */
  FOLLSIBL("following-sibling", false) {
    @Override
    NodeIter init(final Nod n) {
      return n.follSibl();
    }
  },

  /** Following axis. */
  FOLL("following", false) {
    @Override
    NodeIter init(final Nod n) {
      return n.foll();
    }
  },

  /** Parent axis. */
  PARENT("parent", true) {
    @Override
    NodeIter init(final Nod n) {
      return n.par();
    }
  },

  /** Preceding-Sibling axis. */
  PRECSIBL("preceding-sibling", false) {
    @Override
    NodeIter init(final Nod n) {
      return n.precSibl();
    }
  },

  /** Preceding axis. */
  PREC("preceding", false) {
    @Override
    NodeIter init(final Nod n) {
      return n.prec();
    }
  },

  /** Step axis. */
  SELF("self", true) {
    @Override
    NodeIter init(final Nod n) {
      return n.self();
    }
  };

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
  abstract NodeIter init(final Nod n);

  @Override
  public String toString() {
    return name;
  }

  /**
   * Inverts the axis.
   * @return inverted axis
   */
  Axis invert() {
    switch(this) {
      case ANC:        return Axis.DESC;
      case ANCORSELF:  return Axis.DESCORSELF;
      case ATTR:
      case CHILD:      return Axis.PARENT;
      case DESC:       return Axis.ANC;
      case DESCORSELF: return Axis.ANCORSELF;
      case FOLLSIBL:   return Axis.PRECSIBL;
      case FOLL:       return Axis.PREC;
      case PARENT:     return Axis.CHILD;
      case PRECSIBL:   return Axis.FOLLSIBL;
      case PREC:       return Axis.FOLL;
      case SELF:       return Axis.SELF;
      default:         return null;
    }
  }
}
