package org.basex.query.func.html;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.html.*;
import org.basex.build.html.HtmlParser.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class HtmlParse extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return parse(htmlInput(qc), Parser.DEFAULT, qc);
  }

  /**
   * Converts the HTML input in the first argument to an IOContent instance from a binary or string
   * item.
   * @param qc query context
   * @return input as an IOContent instance ({@code null}, if empty)
   * @throws QueryException query exception
   */
  protected IOContent htmlInput(final QueryContext qc) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    if(value.isEmpty()) return null;
    return value instanceof Bin ? new IOContent(toBytes(value))
                                : new IOContent(toBytes(value), "", Strings.UTF8);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Parses the input and creates an XML document.
   * @param io input data
   * @param defaultParser default HTML parser to be used in absence of the METHOD option (can be
   *          {@code null})
   * @param qc query context
   * @return node
   * @throws QueryException query exception
   */
  protected final Item parse(final IO io, final Parser defaultParser, final QueryContext qc)
      throws QueryException {
    if(io == null) return Empty.VALUE;
    final HtmlOptions options = toOptions(arg(1), new HtmlOptions(), qc);
    final Parser parser = Parser.of(options, defaultParser);
    if(parser != null) parser.ensureAvailable(options, definition.local(), info);
    try {
      return new DBNode(
          new org.basex.build.html.HtmlParser(io, parser, new MainOptions(), options));
    } catch(final IOException ex) {
      throw INVHTML_X.get(info, ex);
    }
  }
}
