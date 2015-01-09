package org.basex.query.func.zip;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.html.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class ZipXmlEntry extends ZipBinaryEntry {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return xmlEntry(qc, false);
  }

  /**
   * Returns a document node, created from an XML or HTML file.
   * @param qc query context
   * @param html html flag
   * @return binary result
   * @throws QueryException query exception
   */
  final ANode xmlEntry(final QueryContext qc, final boolean html) throws QueryException {
    final MainOptions opts = qc.context.options;
    final IO io = new IOContent(entry(qc));
    try {
      return new DBNode(html ? new HtmlParser(io, opts) : Parser.xmlParser(io));
    } catch(final IOException ex) {
      throw SAXERR_X.get(info, ex);
    }
  }
}
