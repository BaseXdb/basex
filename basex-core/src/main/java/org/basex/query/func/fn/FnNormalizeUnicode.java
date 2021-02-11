package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.text.*;
import java.text.Normalizer.Form;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnNormalizeUnicode extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] str = toZeroToken(exprs[0], qc);

    Form form = Form.NFC;
    if(exprs.length == 2) {
      final byte[] n = uc(trim(toToken(exprs[1], qc)));
      if(n.length == 0) return Str.get(str);
      try {
        form = Form.valueOf(string(n));
      } catch(final IllegalArgumentException ex) {
        throw NORMUNI_X.get(info, n);
      }
    }
    return ascii(str) ? Str.get(str) : Str.get(Normalizer.normalize(string(str), form));
  }
}
