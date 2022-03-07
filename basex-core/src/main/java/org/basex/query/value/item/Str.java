package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * String item ({@code xs:string}, {@code xs:normalizedString}, {@code xs:language}, etc.).
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Str extends AStr {
  /** Zero-length string. */
  public static final Str EMPTY = new Str(Token.EMPTY);
  /** Single-spaced string. */
  public static final Str SPACE = new Str(Token.SPACE);

  /**
   * Constructor.
   * @param value value
   */
  private Str(final byte[] value) {
    this(value, AtomType.STRING);
  }

  /**
   * Constructor.
   * @param value value
   * @param type item type
   */
  public Str(final byte[] value, final AtomType type) {
    super(type, value);
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Str get(final byte[] value) {
    return value.length == 0 ? EMPTY : new Str(value);
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
   * Returns a valid string representation of the specified value.
   * @param value object (can be {@code null}, will be converted to token otherwise)
   * @param qc query context
   * @param inf input info
   * @return instance
   * @throws QueryException query exception
   */
  public static Str get(final Object value, final QueryContext qc, final InputInfo inf)
      throws QueryException {

    if(value == null) return Str.EMPTY;

    final boolean validate = qc.context.options.get(MainOptions.CHECKSTRINGS);
    final byte[] bytes = Token.token(value.toString());

    // check if string is valid
    boolean valid = true;
    final TokenParser pt = new TokenParser(bytes);
    while(valid && pt.more()) {
      final int cp = pt.next();
      valid = XMLToken.valid(cp);
      if(!valid && validate) throw INVCODE_X.get(inf, Integer.toHexString(cp));
    }
    if(valid) return get(bytes);

    // if not, replace invalid characters with replacement character
    final TokenBuilder tb = new TokenBuilder(bytes.length);
    pt.reset();
    while(pt.more()) {
      final int cp = pt.next();
      tb.add(XMLToken.valid(cp) ? cp : Token.REPLACEMENT);
    }
    return get(tb.finish());
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return value;
  }

  /**
   * Returns the string value.
   * @return string value
   */
  public byte[] string() {
    return value;
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) {
    return mode.oneOf(Simplify.EBV, Simplify.PREDICATE) ?
      cc.simplify(this, Bln.get(this != EMPTY)) : this;
  }

  @Override
  public String toJava() {
    return Token.string(value);
  }
}
