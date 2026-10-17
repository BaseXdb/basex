package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnNormalizeUnicode extends StandardFunc {
  @Override
  public AStr value(final QueryContext qc) throws QueryException {
    final AStr value = toZeroStr(arg(0), qc);
    final byte[] form = toTokenOrNull(arg(1), qc);

    NormalizationForm nf = NormalizationForm.NFC;
    if(form != null) {
      final byte[] norm = uc(trim(form));
      if(norm.length == 0) return value;
      nf = Enums.get(NormalizationForm.class, string(norm));
      if(nf == null) throw NORMUNI_X.get(info, form);
    }
    final byte[] string = value.string(info), normalized = nf.normalize(string);
    return normalized == string ? value : Str.get(normalized);
  }
}
