package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.Parser;
import org.basex.build.xml.*;
import org.basex.build.xml.SAXHandler.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.options.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnParseXmlFragment extends Docs {
  /** Function options. */
  public static class ParseXmlFragmentOptions extends Options {
    /** Document node's base URI. */
    public static final StringOption BASE_URI = CommonOptions.BASE_URI;
    /** Remove whitespace-only text nodes. */
    public static final BooleanOption STRIP_SPACE = CommonOptions.STRIP_SPACE;

    /** Custom option (see {@link MainOptions#STRIPNS}). */
    public static final BooleanOption STRIPNS = CommonOptions.STRIPNS;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return parseXml(qc, true, toOptions(arg(1), new ParseXmlFragmentOptions(), qc));
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, exprs);
  }

  /**
   * Returns a document node for the parsed XML input.
   * @param qc query context
   * @param fragment parse fragment
   * @param options options
   * @return result or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item parseXml(final QueryContext qc, final boolean fragment, final Options options)
      throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    if(value.isEmpty()) return Empty.VALUE;

    check(options, fragment, qc);

    final String baseURI = options.contains(CommonOptions.BASE_URI) ?
      options.get(CommonOptions.BASE_URI) : string(info.sc().baseURI().string());
    final IO io = new IOContent(toBytes(value), baseURI, value instanceof Bin ? null :
      Strings.UTF8);

    final MainOptions mopts = new MainOptions(options);
    mopts.set(MainOptions.CATALOG, qc.context.options.get(MainOptions.CATALOG));
    try {
      final boolean ip = fragment || mopts.get(MainOptions.INTPARSE);
      return new DBNode(ip ? new XMLParser(io, mopts, fragment) : Parser.xmlParser(io, mopts));
    } catch(final IOException ex) {
      final Throwable th = ex.getCause();
      final QueryException qe = !(th instanceof ValidationException) ? SAXERR_X.get(info, ex) :
        mopts.get(MainOptions.DTDVALIDATION) ? DTDVALIDATIONERR_X.get(info, ex) :
          XSDVALIDATIONERR_X.get(info, ex);
      if(th instanceof SAXException) qe.value(Str.get(th.toString()));
      throw qe;
    }
  }
}
