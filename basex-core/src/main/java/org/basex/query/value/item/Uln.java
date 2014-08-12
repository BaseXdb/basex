package org.basex.query.value.item;

import static org.basex.util.Token.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Unsigned long ({@code xs:unsignedLong}).
 *
 * @author BaseX Team 2005-14, BSD License
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
  public boolean bool(final InputInfo ii) {
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
  public BigDecimal dec(final InputInfo ii) {
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
  public boolean eq(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    return it.type == AtomType.ULN ? value.equals(((Uln) it).value) :
           it.type == AtomType.DBL || it.type == AtomType.FLT ? it.eq(this, coll, ii) :
             value.compareTo(BigInteger.valueOf(it.itr(ii))) == 0;
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    if(it.type == AtomType.ULN) return value.compareTo(((Uln) it).value);
    if(it.type == AtomType.DBL || it.type == AtomType.FLT) return -it.diff(this, coll, ii);
    return value.compareTo(BigInteger.valueOf(it.itr(ii)));
  }

  @Override
  public Object toJava() {
    return new BigInteger(value.toString());
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Uln && value.compareTo(((Uln) cmp).value) == 0;
  }
}
