package org.basex.query.xquery.item;

import java.math.BigDecimal;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.iter.NodIter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.iter.NodeMore;
import org.basex.util.Token;

/**
 * Node Type.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Node extends Item {
  /** Static node counter. */
  private static int idcounter;
  /** Parent node. */
  protected Node par;
  /** Unique node id. */
  // <CG> XQuery/unique node id: might not be sufficient
  protected int id;
  
  /**
   * Constructor.
   * @param t data type
   */
  protected Node(final Type t) {
    super(t);
    id = idcounter++;
  }

  @Override
  public final boolean bool() {
    return true;
  }

  @Override
  public final long itr() throws XQException {
    return Itr.parse(str());
  }

  @Override
  public final float flt() throws XQException {
    return Flt.parse(str());
  }

  @Override
  public BigDecimal dec() throws XQException {
    return Dec.parse(str());
  }

  @Override
  public final double dbl() throws XQException {
    return Dbl.parse(str());
  }

  @Override
  public final boolean eq(final Item i) throws XQException {
    return i.type == Type.BLN || i.n() ? i.eq(this) : Token.eq(str(), i.str());
  }

  @Override
  public final int diff(final Item i) throws XQException {
    return i.n() ? -i.diff(this) : Token.diff(str(), i.str());
  }
  
  /**
   * Creates a new copy (clone) of the node.
   * @return new copy
   * @throws XQException evaluation exception
   */
  @SuppressWarnings("unused")
  public abstract Node copy() throws XQException;
  
  /**
   * Returns the node name.
   * @return name
   */
  public byte[] nname() {
    return null;
  }
  
  /**
   * Returns the node name.
   * @return name
   */
  public QNm qname() {
    return null;
  }
  
  /**
   * Returns a temporary node name.
   * @param nm temporary qname
   * @return name
   * @throws XQException query exception
   */
  @SuppressWarnings("unused")
  public QNm qname(final QNm nm) throws XQException {
    return qname();
  }

  /**
   * Returns a namespace array.
   * @return namespace array
   */
  public FAttr[] ns() {
    return null;
  }
  
  /**
   * Returns the database name.
   * @return database name
   */
  public byte[] base() {
    return Token.EMPTY;
  }
  
  /**
   * Compares two nodes for equality.
   * @param nod node to be compared
   * @return result of check
   */
  public boolean is(final Node nod) {
    return id == nod.id;
  }
  
  /**
   * Compares two nodes for their unique order.
   * @param nod node to be compared
   * @return result of check
   */
  public int diff(final Node nod) {
    return id - nod.id;
  }
  
  /**
   * Returns a final node representation.
   * @return node
   */
  public Node finish() {
    return this;
  }
  
  /**
   * Returns the parent node.
   * @return parent node
   */
  public Node parent() {
    return par;
  }
  
  /**
   * Sets the parent node.
   * @param p parent node
   */
  public void parent(final Node p) {
    par = p;
  }
  
  /**
   * Returns an ancestor axis iterator.
   * @return iterator
   */
  public abstract NodeIter anc();
  
  /**
   * Returns an ancestor-or-self axis iterator.
   * @return iterator
   */
  public abstract NodeIter ancOrSelf();
  
  /**
   * Returns an attribute axis iterator.
   * @return iterator
   */
  public abstract NodeIter attr();
  
  /**
   * Returns a child axis iterator.
   * @return iterator
   */
  public abstract NodeMore child();
  
  /**
   * Returns a descendant axis iterator.
   * @return iterator
   */
  public abstract NodeIter desc();
  
  /**
   * Returns a descendant-or-self axis iterator.
   * @return iterator
   */
  public abstract NodeIter descOrSelf();
  
  /**
   * Returns a following axis iterator.
   * @return iterator
   */
  public abstract NodeIter foll();
  
  /**
   * Returns a following-sibling axis iterator.
   * @return iterator
   */
  public abstract NodeIter follSibl();
  
  /**
   * Returns a parent axis iterator.
   * @return iterator
   */
  public abstract NodeIter par();
  
  /**
   * Returns a preceding axis iterator.
   * @return iterator
   */
  public abstract NodeIter prec();
  
  /**
   * Returns a preceding-sibling axis iterator.
   * @return iterator
   */
  public abstract NodeIter precSibl();
  
  /**
   * Returns an self axis iterator.
   * @return iterator
   */
  public abstract NodeIter self();

  /**
   * Adds children of a sub node.
   * @param children child nodes
   * @param nodes node builder
   * @throws XQException query exception
   */
  protected final void addDesc(final NodeIter children,
      final NodIter nodes) throws XQException {
    Node ch;
    while((ch = children.next()) != null) {
      nodes.add(ch.finish());
      addDesc(ch.child(), nodes);
    }
  }
}
