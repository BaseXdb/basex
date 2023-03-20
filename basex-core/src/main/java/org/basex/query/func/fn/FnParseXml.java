package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.Parser;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnParseXml extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return parseXml(qc, false);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Returns a document node for the parsed XML input.
   * @param qc query context
   * @param frag parse fragments
   * @return result or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item parseXml(final QueryContext qc, final boolean frag) throws QueryException {
    final byte[] value = toTokenOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    final IO io = new IOContent(value, string(sc.baseURI().string()));
    try {
      return new DBNode(frag ? new XMLParser(io, new MainOptions(), true) : Parser.xmlParser(io));
    } catch(final IOException ex) {
      final QueryException qe = SAXERR_X.get(info, ex);
      final Throwable th = ex.getCause();
      if(th instanceof SAXException) qe.value(Str.get(th.toString()));
      throw qe;
    }
  }
}
