package org.basex.query.xpath.item;

import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.query.xpath.XPContext;
import org.basex.util.Array;
import org.basex.util.Levenshtein;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * XPath Value representing a NodeSet.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Nod extends Item {
  /** Precedence. */
  static final int PREC = Integer.MAX_VALUE;
  /** Node array. */
  public int[] nodes;
  /** Data reference.. */
  public Data data;
  /** Number of stored values. */
  public int size;

  /** Current node set position. */
  public int currPos;
  /** Current node set size. */
  public int currSize;
  
  /**
   * Constructor, creating a new node set from the specified node ids.
   * @param ids node ids
   * @param ctx query context
   */
  public Nod(final int[] ids, final XPContext ctx) {
    data = ctx.item.data;
    nodes = ids;
    size = ids.length;
  }
  
  /**
   * Constructor, creating an empty node set.
   * @param ctx query context
   */
  public Nod(final XPContext ctx) {
    this(ctx.item.data);
  }

  /**
   * Constructor, creating a new node set from the specified node ids.
   * @param d data reference
   */
  public Nod(final Data d) {
    this(Array.NOINTS, d);
  }

  /**
   * Constructor, creating a new node set from the specified node ids.
   * @param ids node ids
   * @param d data reference
   */
  public Nod(final int[] ids, final Data d) {
    nodes = ids;
    size = ids.length;
    data = d;
  }

  /**
   * Sets a single node.
   * @param pre node to be set.
   */
  public void set(final int pre) {
    if(nodes.length == 0) nodes = new int[] { pre };
    else nodes[0] = pre;
    size = 1;
  }

  @Override
  public long size() {
    return size;
  }
  
  @Override
  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < size && !ser.finished(); c++) serialize(ser, c);
  }

  @Override
  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.openResult();
    ser.node(data, nodes[n]);
    ser.closeResult();
  }

  @Override
  public Nod eval(final XPContext ctx) {
    return this;
  }

  @Override
  public boolean bool() {
    return size > 0;
  }

  @Override
  public byte[] str() {
    if(nodes.length == 1) return data.atom(nodes[0]);
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < size; i++) tb.add(data.atom(nodes[i]));
    return tb.finish();
  }

  @Override
  public double num() {
    if(nodes.length == 1) return data.atomNum(nodes[0]);
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < size; i++) tb.add(data.atom(nodes[i]));
    return Token.toDouble(tb.finish());
  }

  @Override
  public int prec() {
    return PREC;
  }

  @Override
  public boolean lt(final Item v) {
    for(int i = 0; i < size; i++) {
      if(data.atomNum(nodes[i]) < v.num()) return true;
    }
    return false;
  }

  @Override
  public boolean le(final Item v) {
    for(int i = 0; i < size; i++) {
      if(data.atomNum(nodes[i]) <= v.num()) return true;
    }
    return false;
  }

  @Override
  public boolean gt(final Item v) {
    for(int i = 0; i < size; i++) {
      if(data.atomNum(nodes[i]) > v.num()) return true;
    }
    return false;
  }

  @Override
  public boolean ge(final Item v) {
    for(int i = 0; i < size; i++) {
      if(data.atomNum(nodes[i]) >= v.num()) return true;
    }
    return false;
  }

  @Override
  public boolean eq(final Item v) {
    for(int i = 0; i < size; i++) {
      if(new Str(data.atom(nodes[i])).eq(v)) return true;
    }
    return false;
  }

  @Override
  public boolean appr(final Item v) {
    for(int i = 0; i < size; i++) {
      if(new Str(data.atom(nodes[i])).appr(v)) return true;
    }
    return false;
  }

  @Override
  public boolean apprContains(final Item v) {
    if(ls == null) ls = new Levenshtein();
    final byte[] qu = v.str();
    for(int i = 0; i < size; i++) {
      if(ls.contains(data.atom(nodes[i]), qu)) return true;
    }
    return false;
  }

  /**
   * inverse contains (this NodeSet is the argument to val.apprContains).
   * @param val value to contain one of these nodes
   * @return whether val contains one of the node-values in this set
   */
  public boolean apprContainedIn(final Item val) {
    for(int i = 0; i < size; i++) {
      if(val.apprContains(new Str(data.atom(nodes[i])))) return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return "NodeSet[" + size + " Nodes]";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, Token.token("size"), Token.token(size));
  }
}
