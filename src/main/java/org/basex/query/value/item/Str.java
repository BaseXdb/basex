package org.basex.query.value.item;

import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * String item ({@code xs:string}, {@code xs:normalizedString}, {@code xs:language},
 * etc.).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class Str extends AStr {
  /** Zero-length string. */
  public static final Str ZERO = new Str(Token.EMPTY);
  /** String data. */
  final byte[] val;

  /**
   * Constructor.
   * @param v value
   */
  private Str(final byte[] v) {
    this(v, AtomType.STR);
  }

  /**
   * Constructor.
   * @param v value
   * @param t data type
   */
  public Str(final byte[] v, final AtomType t) {
    super(t);
    val = v;
  }

  /**
   * Returns an instance of this class.
   * @param v value
   * @return instance
   */
  public static Str get(final byte[] v) {
    return v.length == 0 ? ZERO : new Str(v);
  }

  /**
   * Returns an instance of this class.
   * @param s string
   * @return instance
   */
  public static Str get(final String s) {
    return get(Token.token(s.toString()));
  }

  /**
   * Returns an instance of this class.
   * @param v object (will be converted to token)
   * @param ctx query context
   * @param ii input info
   * @return instance
   * @throws QueryException query exception
   */
  public static Str get(final Object v, final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final byte[] bytes = Token.token(v.toString());
    if(ctx.context.prop.is(Prop.CHECKSTRINGS)) {
      final int bl = bytes.length;
      for(int b = 0; b < bl; b += Token.cl(bytes, b)) {
        final int cp = Token.cp(bytes, b);
        if(!XMLToken.valid(cp)) INVCODE.thrw(ii, Integer.toHexString(cp));
      }
    }
    return get(bytes);
  }

  @Override
  public final byte[] string(final InputInfo ii) {
    return val;
  }

  /**
   * Returns the string value.
   * @return string value
   */
  public final byte[] string() {
    return val;
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Str)) return false;
    final Str i = (Str) cmp;
    return type == i.type && Token.eq(val, i.val);
  }

  @Override
  public final String toJava() {
    return Token.string(val);
  }

  @Override
  public final String toString() {
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
