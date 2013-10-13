package org.basex.query.value.item;

import static org.basex.data.DataText.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Untyped atomic item ({@code xs:untypedAtomic}).
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Atm extends Item {
  /** String data. */
  final byte[] val;

  /**
   * Constructor.
   * @param v value
   */
  public Atm(final byte[] v) {
    super(AtomType.ATM);
    val = v;
  }

  /**
   * Constructor.
   * @param v value
   */
  public Atm(final String v) {
    this(Token.token(v));
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return val;
  }

  @Override
  public boolean bool(final InputInfo ii) throws QueryException {
    return val.length != 0;
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return it.type.isUntyped() ? coll == null ? Token.eq(val, it.string(ii)) :
      coll.compare(val, it.string(ii)) == 0 : it.eq(this, coll, ii);
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return it.type.isUntyped() ? coll == null ? Token.diff(val, it.string(ii)) :
      coll.compare(val, it.string(ii)) : -it.diff(this, coll, ii);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Atm && Token.eq(val, ((Atm) cmp).val);
  }

  @Override
  public String toJava() {
    return Token.string(val);
  }

  @Override
  public String toString() {
    final ByteList tb = new ByteList();
    tb.add('"');
    for(final byte v : val) {
      if(v == '&') tb.add(E_AMP);
      else tb.add(v);
      if(v == '"') tb.add(v);
    }
    return tb.add('"').toString();
  }
}
