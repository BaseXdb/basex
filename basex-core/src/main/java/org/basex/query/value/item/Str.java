package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * String item ({@code xs:string}, {@code xs:normalizedString}, {@code xs:language}, etc.).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class Str extends AStr {
  /** Wildcard string. */
  public static final Str WC = new Str(new byte[] { '*' });
  /** Zero-length string. */
  public static final Str ZERO = new Str(Token.EMPTY);
  /** String data. */
  final byte[] value;

  /**
   * Constructor.
   * @param value value
   */
  private Str(final byte[] value) {
    this(value, AtomType.STR);
  }

  /**
   * Constructor.
   * @param value value
   * @param type item type
   */
  public Str(final byte[] value, final AtomType type) {
    super(type);
    this.value = value;
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Str get(final byte[] value) {
    return value.length == 0 ? ZERO : new Str(value);
  }

  /**
   * Returns an instance of this class.
   * @param value string
   * @return instance
   */
  public static Str get(final String value) {
    return get(Token.token(value));
  }

  /**
   * Returns an instance of this class.
   * @param value object (will be converted to token)
   * @param qc query context
   * @param ii input info
   * @return instance
   * @throws QueryException query exception
   */
  public static Str get(final Object value, final QueryContext qc, final InputInfo ii)
      throws QueryException {

    final byte[] bytes = Token.token(value.toString());
    if(qc.context.options.get(MainOptions.CHECKSTRINGS)) {
      final int bl = bytes.length;
      for(int b = 0; b < bl; b += Token.cl(bytes, b)) {
        final int cp = Token.cp(bytes, b);
        if(!XMLToken.valid(cp)) throw INVCODE_X.get(ii, Integer.toHexString(cp));
      }
    }
    return get(bytes);
  }

  @Override
  public final byte[] string(final InputInfo ii) {
    return value;
  }

  /**
   * Returns the string value.
   * @return string value
   */
  public final byte[] string() {
    return value;
  }

  @Override
  public final boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Str)) return false;
    final Str i = (Str) cmp;
    return type == i.type && Token.eq(value, i.value);
  }

  @Override
  public final String toJava() {
    return Token.string(value);
  }
}
