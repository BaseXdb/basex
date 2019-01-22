package org.basex.query.value.item;

import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Unsigned long ({@code xs:unsignedLong}).
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class Uln extends ANum {
  /** Maximum unsigned long values. */
  public static final BigDecimal MAXULN = BigDecimal.valueOf(Long.MAX_VALUE).multiply(
      BigDecimal.valueOf(2)).add(BigDecimal.ONE);
  /** Decimal value. */
  private final BigInteger value;

  /**
   * Constructor.
   * @param value decimal value
   */
  private Uln(final BigInteger value) {
    super(AtomType.ULN);
    this.value = value;
  }

  /**
   * Constructor.
   * @param value big decimal value
   * @return value
   */
  public static Uln get(final BigInteger value) {
    return new Uln(value);
  }

  @Override
  public byte[] string() {
    return token(value.toString());
  }

  @Override
  public boolean bool(final InputInfo info) {
    return value.signum() != 0;
  }

  @Override
  public long itr() {
    return value.longValue();
  }

  @Override
  public float flt() {
    return value.floatValue();
  }

  @Override
  public double dbl() {
    return value.doubleValue();
  }

  @Override
  public BigDecimal dec(final InputInfo info) {
    return new BigDecimal(value);
  }

  @Override
  public ANum abs() {
    final long l = itr();
    return l >= 0 ? this : Int.get(-l);
  }

  @Override
  public Uln ceiling() {
    return this;
  }

  @Override
  public Uln floor() {
    return this;
  }

  @Override
  public ANum round(final int scale, final boolean even) {
    return scale >= 0 ? this :
      Int.get(Dec.get(new BigDecimal(value)).round(scale, even).dec(null).longValue());
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo info) throws QueryException {
    return item.type == AtomType.ULN ? value.equals(((Uln) item).value) :
           item.type == AtomType.DBL || item.type == AtomType.FLT ? item.eq(this, coll, sc, info) :
             value.compareTo(BigInteger.valueOf(item.itr(info))) == 0;
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo info)
      throws QueryException {
    if(item.type == AtomType.ULN) return value.compareTo(((Uln) item).value);
    if(item.type == AtomType.DBL || item.type == AtomType.FLT) return -item.diff(this, coll, info);
    return value.compareTo(BigInteger.valueOf(item.itr(info)));
  }

  @Override
  public Object toJava() {
    return new BigInteger(value.toString());
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Uln && value.compareTo(((Uln) obj).value) == 0;
  }
}
