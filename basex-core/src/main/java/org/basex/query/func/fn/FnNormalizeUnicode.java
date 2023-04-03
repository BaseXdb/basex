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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnNormalizeUnicode extends StandardFunc {
  @Override
  public AStr item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final AStr value = toZeroStr(arg(0), qc);

    Form frm = Form.NFC;
    if(defined(1)) {
      final byte[] form = uc(trim(toToken(arg(1), qc)));
      if(form.length == 0) return value;
      try {
        frm = Form.valueOf(string(form));
      } catch(final IllegalArgumentException ex) {
        Util.debug(ex);
        throw NORMUNI_X.get(info, form);
      }
    }
    return value.ascii(info) ? value :
      Str.get(Normalizer.normalize(string(value.string(info)), frm));
  }
}
