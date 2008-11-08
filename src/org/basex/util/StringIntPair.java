package org.basex.util;

/**
 * Encapsulates a String and an int value.
 * 
 * @author joggele
 */

public class StringIntPair {
  /** String. */
  private String str;
  /** Int value. */
  private int val;
  
  /**
   * Constructor.
   * 
   * @param s String for this Element
   * @param v Value assigned to this String
   */
  public StringIntPair(final String s, final int v) {
    super();
    str = s;
    val = v;
  }
  
  /**
   * Getting the String.
   * 
   * @return str
   */
  public String getStr() {
    return str;
  }
  
  /**
   * Sets the String.
   * @param s String to assign
   */
  public void setStr(final String s) {
    str = s;
  }
  
  /**
   * Returns the Value.
   * 
   * @return val
   */
  public int getVal() {
    return val;
  }
  
  /**
   * Returns the Value.
   * 
   * @param v Value to assign
   */
  public void setVal(final int v) {
    val = v;
  }

  @Override
  public String toString() {
    return "[" + str + "," + val + "]";  
  }
}