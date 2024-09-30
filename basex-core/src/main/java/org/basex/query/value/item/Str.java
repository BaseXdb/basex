package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.util.function.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * String item ({@code xs:string}, {@code xs:normalizedString}, {@code xs:language}, etc.).
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Str extends AStr {
  /** Zero-length string. */
  public static final Str EMPTY = new Str(Token.EMPTY);
  /** Key string. */
  public static final Str KEY = Str.get("key");
  /** Value string. */
  public static final Str VALUE = Str.get("value");

  /** Unicode character cache. */
  private static final IntObjMap<Str> CACHE = new IntObjMap<>();
  /** Single ASCII characters. */
  private static final Str[] CHAR;

  // caches single ASCII characters
  static {
    final int nl = 128;
    CHAR = new Str[nl];
    for(int n = 0; n < nl; n++) CHAR[n] = new Str(Token.cpToken(n));
  }

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
  private Str(final byte[] value, final Type type) {
    super(value, type);
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @return instance
   */
  public static Str get(final byte[] value) {
    final int vl = value.length;
    if(vl == 0) return EMPTY;
    if(vl == 1) return CHAR[value[0]];
    if(vl > 4 || vl > Token.cl(value, 0)) return new Str(value);
    // cache single characters
    synchronized(CACHE) {
      return CACHE.computeIfAbsent(Token.cp(value, 0), () -> new Str(value));
    }
  }

  /**
   * Returns an instance of this class.
   * @param cp codepoint
   * @return instance
   */
  public static Str get(final int cp) {
    if(cp < 128) return CHAR[cp];
    synchronized(CACHE) {
      return CACHE.computeIfAbsent(cp, () -> new Str(Token.cpToken(cp)));
    }
  }

  /**
   * Returns an instance of this class.
   * @param value value
   * @param type type
   * @return instance
   */
  public static Str get(final byte[] value, final Type type) {
    return type == AtomType.STRING ? get(value) : new Str(value, type);
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
   * @param info input info
   * @return instance
   * @throws QueryException query exception
   */
  public static Str get(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {

    if(value == null) return Str.EMPTY;

    // invalid Unicode characters: raise error or add replacement character
    final boolean validate = qc.context.options.get(MainOptions.CHECKSTRINGS);
    final TokenBuilder tb = new TokenBuilder();
    final TokenParser tp = new TokenParser(Token.token(value));
    while(tp.more()) {
      final int cp = tp.next();
      if(XMLToken.valid(cp)) {
        tb.add(cp);
      } else {
        if(validate) throw INVCODE_X.get(info, Integer.toHexString(cp));
        tb.add(Token.REPLACEMENT);
      }
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
  public int hash() {
    return Token.hash(value);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // E['x']  ->  E[true()]
      expr = Bln.get(this != EMPTY);
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public Item materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {
    return type instanceof EnumType ? get(string()) : this;
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    return !(type instanceof EnumType);
  }

  @Override
  public String toJava() {
    return Token.string(value);
  }

}
