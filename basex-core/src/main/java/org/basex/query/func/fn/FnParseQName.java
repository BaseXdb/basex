package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnParseQName extends StandardFunc {
  /** EQName syntax. */
  private static final Pattern EQNAME = Pattern.compile("Q\\{([^{}]*)\\}(.*)$");

  @Override
  public QNm item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] eqname = trim(toToken(exprs[0], qc));
    if(XMLToken.isNCName(eqname)) return new QNm(eqname, EMPTY);
    if(XMLToken.isQName(eqname)) {
      final QNm qnm = new QNm(eqname, sc);
      if(!qnm.hasURI() && qnm.hasPrefix()) throw NSDECL_X.get(ii, qnm.prefix());
      return qnm;
    }
    final Matcher m = EQNAME.matcher(string(eqname));
    if(m.matches()) {
      final byte[] uri = token(m.group(1)), ncname = token(m.group(2));
      if(XMLToken.isNCName(ncname)) return new QNm(ncname, uri);
    }

    throw valueError(AtomType.QNAME, eqname, info);
  }
}
