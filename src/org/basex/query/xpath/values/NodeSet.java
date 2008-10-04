package org.basex.query.xpath.values;

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
public final class NodeSet extends Item {
  /** Precedence. */
  static final int PREC = Integer.MAX_VALUE;
  /** Current node set position. */
  public int currPos;
  /** Current node set size. */
  public int currSize;
  /** Node array. */
  public int[] nodes;
  /** Data reference.. */
  public Data data;
  /** Number of stored values.. */
  public int size;

  /* FTIdPos array.
  public int[][] ftidpos;
  /* Pointer for FTQueries - each idpos has a pointer at 
   * its search string position in the xpath query.
  public int[] ftpointer;*/
  
  /**
   * Constructor, creating a new node set from the specified node ids.
   * @param ids node ids
   * @param ctx query context
   */
  public NodeSet(final int[] ids, final XPContext ctx) {
    data = ctx.local.data;
    nodes = ids;
    size = ids.length;
  }

  /*
   * Constructor, creating a new node set from the specified node ids.
   * @param ids node ids
   * @param ctx query context
   * @param ftIds ids and pos values for fulltext queries
   * @param ftPointer Pointer for ftIds
  public NodeSet(final int[] ids, final XPContext ctx, 
      final int[][] ftIds, final int[] ftPointer) {
    data = ctx.local.data;
    nodes = ids;
    size = ids.length;
    ftidpos = ftIds;
    ftpointer = ftPointer;
  }
   */
  
  /**
   * Constructor, creating an empty node set.
   * @param ctx query context
   */
  public NodeSet(final XPContext ctx) {
    this(ctx.local.data);
  }

  /**
   * Constructor, creating a new node set from the specified node ids.
   * @param d data reference
   */
  public NodeSet(final Data d) {
    this(Array.NOINTS, d);
  }

  /**
   * Constructor, creating a new node set from the specified node ids.
   * @param ids node ids
   * @param d data reference
   */
  public NodeSet(final int[] ids, final Data d) {
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
  public int size() {
    return size;
  }
  
  @Override
  public void serialize(final Serializer ser) throws IOException {
    ser.open(size);
    for(int c = 0; c < size; c++) {
      if(ser.finished()) break;
      ser.openResult();
      ser.xml(data, nodes[c]);
      ser.closeResult();
    }
    ser.close(size);
  }

  @Override
  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.xml(data, nodes[n]);
  }

  @Override
  public NodeSet eval(final XPContext ctx) {
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
      if(new Literal(data.atom(nodes[i])).eq(v)) return true;
    }
    return false;
  }

  @Override
  public boolean appr(final Item v) {
    for(int i = 0; i < size; i++) {
      if(new Literal(data.atom(nodes[i])).appr(v)) return true;
    }
    return false;
  }

  @Override
  public boolean apprContains(final Item v) {
    final byte[] qu = v.str();
    
    for(int i = 0; i < size; i++) {
      if(Levenshtein.contains(data.atom(nodes[i]), qu)) return true;
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
      if(val.apprContains(new Literal(data.atom(nodes[i])))) return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return name() + "[" + size + " Nodes]";
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, Token.token("size"), Token.token(size));
  }
}
