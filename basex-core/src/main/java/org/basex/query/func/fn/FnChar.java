package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnChar extends StandardFunc {
  @Override
  public final Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toToken(arg(0), qc);

    final int nl = value.length;
    if(nl == 2 && value[0] == '\\') {
      // check for backslash-escape sequence
      final byte cp = value[1];
      if(cp == 't') return Str.TAB;
      if(cp == 'n') return Str.NL;
      if(cp == 'r') return Str.CR;
    } else {
      final byte[] token = convert(value, nl);
      if(token != null) return Str.get(token);
      throw CHARINV_X.get(info, QueryError.similar(value, XMLToken.similarEntity(value)));
    }
    throw CHARINV_X.get(info, value);
  }

  /**
   * Converts a character name or glyph to a string.
   * @param name token
   * @param nl length of name
   * @return token or {@code null}
   */
  private static byte[] convert(final byte[] name, final int nl) {
    if(nl == 0) return null;
    // check for HTML character reference names
    if(name[0] != '#') return XMLToken.getEntity(name);
    // parse decimal or hexadecimal codepoint value
    int cp = 0;
    final boolean dec = nl < 2 || name[1] != 'x';
    for(int i = dec ? 1 : 2; i < nl; i++) {
      final int d = Token.uc(name[i]);
      if(d < '0' || d > '9' && (dec || d < 'A' || d > 'F')) return null;
      cp = cp * (dec ? 10 : 16) + d - (d <= '9' ? 0x30 : 0x37);
    }
    return XMLToken.valid(cp) ? new TokenBuilder().add(cp).finish() : null;
  }
}
