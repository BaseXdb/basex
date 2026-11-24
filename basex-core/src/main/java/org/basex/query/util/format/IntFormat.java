package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Parser for formatting integers.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IntFormat extends FormatParser {
  /** Whether the radix was specified explicitly. */
  private final boolean hasExplicitRadix;

  /**
   * Constructor.
   * @param picture picture
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public IntFormat(final byte[] picture, final InputInfo info) throws QueryException {
    super(info);

    final int sc = lastIndexOf(picture, ';');
    int rc = indexOf(picture, '^');
    if(rc != -1) {
      int xuc = indexOf(picture, 'X', rc + 1);
      int xlc = indexOf(picture, 'x', rc + 1);
      if(sc != -1 && xuc > sc) xuc = -1;
      if(sc != -1 && xlc > sc) xlc = -1;
      if(xuc == -1 && xlc == -1) {
        rc = -1;
      } else {
        final byte[] prefix = substring(picture, 0, rc);
        radix = toInt(prefix);
        // reject leading zeros, signs and whitespace
        if(radix < 2 || radix > 36 || !eq(token(radix), prefix)) {
          rc = -1;
          radix = 10;
        } else if(xuc != -1 && xlc != -1) {
          throw DIFFMAND_X.get(info, picture);
        }
      }
    }
    hasExplicitRadix = rc != -1;

    final byte[] pres = substring(picture, rc + 1, sc == -1 ? picture.length : sc);
    if(pres.length == 0) throw PICEMPTY_X.get(info, picture);
    if(hasExplicitRadix) {
      for(final int cp : cps(pres)) {
        if(cp != 'x' && cp != 'X' && alphanumeric(cp)) {
          throw INVDDPATTERN_X.get(info, picture);
        }
      }
    }
    finish(presentation(pres, cpToken('1'), false, false));
    if(sc == -1) return;

    // parses the format modifier
    final byte[] mod = substring(picture, sc + 1);

    final int ml = mod.length;
    int m = 0;
    // parse cardinal/ordinal flag
    if(m < ml && (mod[m] == 'o' || mod[m] == 'c')) {
      numType = mod[m++] == 'o' ? NumeralType.ORDINAL : NumeralType.CARDINAL;
      if(m < ml && mod[m] == '(') {
        // the variation string may contain parentheses: it ends at the last closing one
        final int close = lastIndexOf(mod, ')');
        if(close <= m + 1) throw INVMODIFIER_X.get(info, mod);
        modifier = substring(mod, m + 1, close);
        m = close + 1;
      }
    }
    // parse alphabetical/traditional flag
    if(m < ml && (mod[m] == 'a' || mod[m] == 't')) m++;
    if(m < ml) throw INVMODIFIER_X.get(info, mod);
  }

  /**
   * Returns the zero base for the specified code point, or {@code -1}.
   * @param ch character
   * @return zero base
   */
  @Override
  public int zeroes(final int ch) {
    // '0': normalized zero digit, passed on by callers (rejected in the picture by the constructor)
    if(hasExplicitRadix) return ch == 'x' || ch == 'X' || ch == '0' ? '0' : -1;
    return super.zeroes(ch);
  }

  /**
   * Checks if a character is a valid digit.
   * @param ch character
   * @param zero zero character
   * @return result of check
   */
  @Override
  public boolean digit(final int ch, final int zero) {
    if(hasExplicitRadix) return ch == 'x' || ch == 'X';
    return super.digit(ch, zero);
  }
}
