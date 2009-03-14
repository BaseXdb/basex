package org.basex.index;

/**
 * This class defines access to index text tokens.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class RangeToken extends IndexToken {
  /** Minimum value. */
  public double min;
  /** Maximum value. */
  public double max;

  /**
   * Constructor.
   * @param t index type
   * @param mn minimum value
   * @param mx maximum value
   */
  public RangeToken(final boolean t, final double mn, final double mx) {
    super(t ? Type.TXT : Type.ATV);
    min = mn;
    max = mx;
  }
  
  @Override
  public boolean range() {
    return true;
  }
}
