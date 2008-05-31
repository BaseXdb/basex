package org.basex.query.xquery.path;

import org.basex.query.xquery.item.Node;
import org.basex.query.xquery.iter.NodeIter;

/**
 * XPath Axes Enumeration.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum Axis {
  // ...order is important for parsing the Query;
  // axes with longer names are parsed first
  
  /** Ancestor-or-self axis. */
  ANCORSELF("ancestor-or-self") {
    @Override
    public NodeIter init(final Node n) {
      return n.ancOrSelf();
    }
  },
  
  /** Ancestor axis. */
  ANC("ancestor") {
    @Override
    public NodeIter init(final Node n) {
      return n.anc();
    }
  },
  
  /** Attribute axis. */
  ATTR("attribute") {
    @Override
    public NodeIter init(final Node n) {
      return n.attr();
    }
  },
  
  /** Child Axis. */
  CHILD("child") {
    @Override
    public NodeIter init(final Node n) {
      return n.child();
    }
  },
  
  /** Descendant-or-self axis. */
  DESCORSELF("descendant-or-self") {
    @Override
    public NodeIter init(final Node n) {
      return n.descOrSelf();
    }
  },
  
  /** Descendant axis. */
  DESC("descendant") {
    @Override
    public NodeIter init(final Node n) {
      return n.desc();
    }
  },
  
  /** Following-Sibling axis. */
  FOLLSIBL("following-sibling") {
    @Override
    public NodeIter init(final Node n) {
      return n.follSibl();
    }
  },
  
  /** Following axis. */
  FOLL("following") {
    @Override
    public NodeIter init(final Node n) {
      return n.foll();
    }
  },
  
  /** Parent axis. */
  PARENT("parent") {
    @Override
    public NodeIter init(final Node n) {
      return n.par();
    }
  },
  
  /** Preceding-Sibling axis. */
  PRECSIBL("preceding-sibling") {
    @Override
    public NodeIter init(final Node n) {
      return n.precSibl();
    }
  },
  
  /** Preceding axis. */
  PREC("preceding") {
    @Override
    public NodeIter init(final Node n) {
      return n.prec();
    }
  },

  /** Step axis. */
  SELF("self") {
    @Override
    public NodeIter init(final Node n) {
      return n.self();
    }
  };

  /** Axis string. */
  public final String name;

  /**
   * Constructor, initializing the enum constants.
   * @param n axis string
   */
  Axis(final String n) {
    name = n;
  }

  /**
   * Returns a node iterator.
   * @param n input node
   * @return node iterator
   */
  public abstract NodeIter init(final Node n);
  
  @Override
  public String toString() {
    return name;
  }
}
