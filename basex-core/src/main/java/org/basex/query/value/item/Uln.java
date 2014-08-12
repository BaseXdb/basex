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
  public static final BigDecimal MAX = new BigDecimal(Long.MAX_VALUE).multiply(
      BigDecimal.valueOf(2)).add(BigDecimal.ONE);
  /** Decimal value. */
  private final BigDecimal value;

  /**
   * Constructor.
   * @param value decimal value
   */
  private Uln(final BigDecimal value) {
    super(AtomType.ULN);
    this.value = value;
  }

  /**
   * Constructor.
   * @param value big decimal value
   * @return value
   */
  public static Uln get(final BigDecimal value) {
    return new Uln(value);
  }

  @Override
  public byte[] string() {
    return token(value.toPlainString());
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
    return value;
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
  public boolean eq(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    return it.type == AtomType.DBL || it.type == AtomType.FLT ?
        it.eq(this, coll, ii) : value.compareTo(it.dec(ii)) == 0;
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    final double d = it.dbl(ii);
    return d == Double.NEGATIVE_INFINITY ? -1 : d == Double.POSITIVE_INFINITY ? 1 :
      Double.isNaN(d) ? UNDEF : value.compareTo(it.dec(ii));
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
