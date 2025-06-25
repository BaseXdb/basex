package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.build.html.*;
import org.basex.build.html.HtmlParser.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * HTML parse helper functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ParseHtml extends ParseFn {
  /**
   * Returns the default parser.
   * @return parser
   */
  protected abstract Parser parser();

  @Override
  protected final Value doc(final QueryContext qc) throws QueryException {
    final String source = toStringOrNull(arg(0), qc);
    return source == null ? Empty.VALUE : parse(toIO(source, false), qc);
  }

  @Override
  protected final Value parse(final QueryContext qc) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    return value.isEmpty() ? Empty.VALUE :
      parse(new IOContent(toBytes(value), "", value instanceof Bin ? null : Strings.UTF8), qc);
  }

  /**
   * Parses the specified input and returns a document.
   * @param io input reference
   * @param qc query context
   * @return document
   * @throws QueryException query exception
   */
  private DBNode parse(final IO io, final QueryContext qc) throws QueryException {
    final HtmlOptions options = options(qc);
    final Parser prsr = Parser.of(options, parser());
    if(prsr != null) prsr.ensureAvailable(options, definition.name, info);
    try {
      return new DBNode(new HtmlParser(io, prsr, new MainOptions(), options));
    } catch(final IOException ex) {
      throw error().get(info, ex);
    }
  }

  @Override
  final Value parse(final TextInput ti, final Options options, final QueryContext qc) {
    // may be applicable once input is streamed by HTML parser
    throw Util.notExpected();
  }

  @Override
  final QueryError error() {
    return INVHTML_X;
  }

  @Override
  protected final HtmlOptions options(final QueryContext qc) throws QueryException {
    return toOptions(arg(1), new HtmlOptions(), qc);
  }
}
