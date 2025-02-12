package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

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
import org.basex.util.options.*;
import org.xml.sax.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnParseXmlFragment extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return parseXml(qc, true, toOptions(arg(1), new ParseXmlFragmentOptions(), qc));
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Returns a document node for the parsed XML input.
   * @param qc query context
   * @param frag parse fragments
   * @param options options
   * @return result or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item parseXml(final QueryContext qc, final boolean frag,
      final ParseXmlFragmentOptions options) throws QueryException {
    final byte[] value = toTokenOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    final String baseURI = options.contains(ParseXmlFragmentOptions.BASE_URI)
        ? options.get(ParseXmlFragmentOptions.BASE_URI) : string(info.sc().baseURI().string());
    final IO io = new IOContent(value, baseURI);

    final MainOptions mainOpts = new MainOptions(qc.context.options, true);
    Map.of(ParseXmlFragmentOptions.STRIP_SPACE, MainOptions.STRIPWS,
        ParseXmlFragmentOptions.INTPARSE, MainOptions.INTPARSE,
        ParseXmlFragmentOptions.STRIPNS, MainOptions.STRIPNS,
        ParseXmlFragmentOptions.DTD, MainOptions.DTD,
        ParseXmlFragmentOptions.XINCLUDE, MainOptions.XINCLUDE).forEach((opt, mainOpt) -> {
        if(options.contains(opt)) mainOpts.set(mainOpt, options.get(opt));
    });
    if(options.contains(ParseXmlFragmentOptions.CATALOG))
      mainOpts.set(MainOptions.CATALOG, options.get(ParseXmlFragmentOptions.CATALOG));

    try {
      return new DBNode(frag || mainOpts.get(MainOptions.INTPARSE) ?
          new XMLParser(io, mainOpts, true) : Parser.xmlParser(io, mainOpts));
    } catch(final IOException ex) {
      final QueryException qe = SAXERR_X.get(info, ex);
      final Throwable th = ex.getCause();
      if(th instanceof SAXException) qe.value(Str.get(th.toString()));
      throw qe;
    }
  }

  /**
   * Options for fn:parse-xml-fragment.
   */
  public static class ParseXmlFragmentOptions extends Options {
    /** Document node's base URI. */
    public static final StringOption BASE_URI = new StringOption("base-uri");
    /** Remove whitespace-only text nodes. */
    public static final BooleanOption STRIP_SPACE = new BooleanOption("strip-space", false);

    /** Use internal XML parser (default: {@code qc.context.options.get(MainOptions.INTPARSE)}). */
    public static final BooleanOption INTPARSE = new BooleanOption("int-parse");
    /** Strip namespaces (default: {@code qc.context.options.get(MainOptions.STRIPNS)}). */
    public static final BooleanOption STRIPNS = new BooleanOption("strip-ns");
    /** Flag for parsing DTDs (default: {@code qc.context.options.get(MainOptions.DTD)}). */
    public static final BooleanOption DTD = new BooleanOption("dtd");
    /** Flag for using XInclude (default: {@code qc.context.options.get(MainOptions.XINCLUDE)}). */
    public static final BooleanOption XINCLUDE = new BooleanOption("xinclude");
    /** Path to XML Catalog file (default: {@code qc.context.options.get(MainOptions.CATALOG)}). */
    public static final StringOption CATALOG = new StringOption("catalog");
  }
}
