package org.basex.query.xpath.item;

/**
 * This enumeration assembles different value comparisons.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum Calc {
  /** Expression Type: addition. */
  PLUS("+") {
    @Override
    public double eval(final double d1, final double d2) {
      return d1 + d2;
    }
  },

  /** Expression Type: subtraction. */
  MINUS("-") {
    @Override
    public double eval(final double d1, final double d2) {
      return d1 - d2;
    }
  },

  /** Expression Type: multiplication. */
  MULT("*") {
    @Override
    public double eval(final double d1, final double d2) {
      return d1 * d2;
    }
  },

  /** Expression Type: division. */
  DIV("div") {
    @Override
    public double eval(final double d1, final double d2) {
      return d1 / d2;
    }
  },

  /** Expression Type: modulo. */
  MOD("mod") {
    @Override
    public double eval(final double d1, final double d2) {
      return d1 % d2;
    }
  };
  
  /** String representation. */
  public final String name;
  
  /**
   * Constructor.
   * @param n string representation
   */
  Calc(final String n) { name = n; }

  /**
   * Evaluates the expression.
   * @param d1 first value
   * @param d2 second value
   * @return result
   */
  public abstract double eval(double d1, double d2);
  
  @Override
  public String toString() { return name; }
}
