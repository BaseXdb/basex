package org.basex.query.func.inspect;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class InspectXqdoc extends StandardFunc {
  /** Name of the result element. */
  public static final QNm Q_XQDOC = new QNm(XQDoc.XQDOC_PREFIX, "xqdoc", XQDoc.XQDOC_URI);

  @Override
  public FNode value(final QueryContext qc) throws QueryException {
    final IOContent content = toContent(toString(arg(0), qc), qc);
    return new XQDoc(qc, info).parse(content);
  }
}
