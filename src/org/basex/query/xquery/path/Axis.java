package org.basex.query.xquery.path;

import org.basex.query.xquery.item.Nod;
import org.basex.query.xquery.iter.NodeIter;

/**
 * XPath Axes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum Axis {
  // ...order is important here for parsing the Query;
  // axes with longer names are parsed first
  
  /** Ancestor-or-self axis. */
  ANCORSELF("ancestor-or-self", false, true) {
    @Override
    public NodeIter init(final Nod n) {
      return n.ancOrSelf();
    }
  },
  
  /** Ancestor axis. */
  ANC("ancestor", false, true) {
    @Override
    public NodeIter init(final Nod n) {
      return n.anc();
    }
  },
  
  /** Attribute axis. */
  ATTR("attribute", true, false) {
    @Override
    public NodeIter init(final Nod n) {
      return n.attr();
    }
  },
  
  /** Child Axis. */
  CHILD("child", true, true) {
    @Override
    public NodeIter init(final Nod n) {
      return n.child();
    }
  },
  
  /** Descendant-or-self axis. */
  DESCORSELF("descendant-or-self", true, true) {
    @Override
    public NodeIter init(final Nod n) {
      return n.descOrSelf();
    }
  },
  
  /** Descendant axis. */
  DESC("descendant", true, true) {
    @Override
    public NodeIter init(final Nod n) {
      return n.desc();
    }
  },
  
  /** Following-Sibling axis. */
  FOLLSIBL("following-sibling", false, false) {
    @Override
    public NodeIter init(final Nod n) {
      return n.follSibl();
    }
  },
  
  /** Following axis. */
  FOLL("following", false, false) {
    @Override
    public NodeIter init(final Nod n) {
      return n.foll();
    }
  },
  
  /** Parent axis. */
  PARENT("parent", false, true) {
    @Override
    public NodeIter init(final Nod n) {
      return n.par();
    }
  },
  
  /** Preceding-Sibling axis. */
  PRECSIBL("preceding-sibling", false, false) {
    @Override
    public NodeIter init(final Nod n) {
      return n.precSibl();
    }
  },
  
  /** Preceding axis. */
  PREC("preceding", false, false) {
    @Override
    public NodeIter init(final Nod n) {
      return n.prec();
    }
  },

  /** Step axis. */
  SELF("self", true, true) {
    @Override
    public NodeIter init(final Nod n) {
      return n.self();
    }
  };

  /** Axis string. */
  public final String name;
  /** Descendant axis flag. */
  public final boolean down;
  /** Vertical axis flag. */
  public final boolean vert;

  /**
   * Constructor.
   * @param n axis string
   * @param d descendant flag
   * @param v vertical flag
   */
  Axis(final String n, final boolean d, final boolean v) {
    name = n;
    down = d;
    vert = v;
  }

  /**
   * Returns a node iterator.
   * @param n input node
   * @return node iterator
   */
  public abstract NodeIter init(final Nod n);
  
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
