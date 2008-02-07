package org.basex.query.xpath.values;

import org.basex.util.Token;

/**
 * This enumeration assembles different value comparisons.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum Comp {
  /** Expression Type: less or equal. */
  LE("<=") {
    @Override
    public boolean eval(final Item v1, final Item v2) {
      return v1.le(v2);
    }
  },

  /** Expression Type: less. */
  LT("<") {
    @Override
    public boolean eval(final Item v1, final Item v2) {
      return v1.lt(v2);
    }
  },
  
  /** Expression Type: greater of equal. */
  GE(">=") {
    @Override
    public boolean eval(final Item v1, final Item v2) {
      return v1.ge(v2);
    }
  },
  
  /** Expression Type: greater. */
  GT(">") {
    @Override
    public boolean eval(final Item v1, final Item v2) {
      return v1.gt(v2);
    }
  },

  /** Expression Type: equal. */
  EQ("=") {
    @Override
    public boolean eval(final Item v1, final Item v2) {
      return v1.eq(v2);
    }
  },
  
  /** Expression Type: not equal. */
  NE("!=") {
    @Override
    public boolean eval(final Item v1, final Item v2) {
      return !v1.eq(v2);
    }
  },
  
  /** Expression Type: approximate. */
  APPR("~") {
    @Override
    public boolean eval(final Item v1, final Item v2) {
      return v1.appr(v2);
    }
  },
  
  /** Expression Type: approximate. */
  APPRWORD("~>") {
    @Override
    public boolean eval(final Item v1, final Item v2) {
      return v1.apprContains(v2);
    }
  },
  
  /** Expression Type: contains. */
  WORD("contains") {
    @Override
    public boolean eval(final Item v1, final Item v2) {
      return v1.contains(v2);
    }
  },

  /** Expression Type: ftcontains. */
  FTCONTAINS("ftcontains") {
    @Override
    public boolean eval(final Item v1, final Item v2) {
      return v1.contains(v2);
    }
  };
  
  /** String representation. */
  public final byte[] name;
  
  /**
   * Constructor.
   * @param n string representation
   */
  Comp(final String n) { name = Token.token(n); }

  /**
   * Evaluates the expression.
   * @param v1 first value
   * @param v2 second value
   * @return result
   */
  public abstract boolean eval(Item v1, Item v2);
  
  @Override
  public String toString() { return Token.string(name); }
}
