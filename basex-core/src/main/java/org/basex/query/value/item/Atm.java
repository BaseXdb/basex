package org.basex.query.value.item;

import static org.basex.data.DataText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Untyped atomic item ({@code xs:untypedAtomic}).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Atm extends Item {
  /** String data. */
  private final byte[] value;

  /**
   * Constructor.
   * @param value value
   */
  public Atm(final byte[] value) {
    super(AtomType.ATM);
    this.value = value;
  }

  /**
   * Constructor.
   * @param value value
   */
  public Atm(final String value) {
    this(Token.token(value));
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return value;
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return value.length != 0;
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return it.type.isUntyped() ? coll == null ? Token.eq(value, it.string(ii)) :
      coll.compare(value, it.string(ii)) == 0 : it.eq(this, coll, sc, ii);
  }

  @Override
  public boolean sameKey(final Item it, final InputInfo ii) throws QueryException {
    return it.type.isStringOrUntyped() && eq(it, null, null, ii);
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    return it.type.isUntyped() ? coll == null ? Token.diff(value, it.string(ii)) :
      coll.compare(value, it.string(ii)) : -it.diff(this, coll, ii);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Atm && Token.eq(value, ((Atm) cmp).value);
  }

  @Override
  public String toJava() {
    return Token.string(value);
  }

  @Override
  public String toString() {
    return toString(value);
  }

  /**
   * Returns a string representation of the specified value.
   * @param value value
   * @return string
   */
  public static String toString(final byte[] value) {
    final ByteList tb = new ByteList().add('"');
    for(final byte v : value) {
      if(v == '&') tb.add(E_AMP);
      else if(v == '\r') tb.add(E_CR);
      else if(v == '\n') tb.add(E_NL);
      else tb.add(v);
      if(v == '"') tb.add('"');
    }
    return tb.add('"').toString();
  }
}
