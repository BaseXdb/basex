package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.Parser;
import org.basex.build.xml.*;
import org.basex.build.xml.SAXHandler.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.FnParseXml.*;
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
    final Item value = arg(0).atomItem(qc, info);
    if(value.isEmpty()) return Empty.VALUE;

    final String baseURI = options.contains(ParseXmlFragmentOptions.BASE_URI)
        ? options.get(ParseXmlFragmentOptions.BASE_URI) : string(info.sc().baseURI().string());
    final IO io = value instanceof Bin ? new IOContent(toBytes(value), baseURI)
                                       : new IOContent(toBytes(value), baseURI, Strings.UTF8);

    // get option default values from context options
    final MainOptions contextOpts = qc.context.options;
    final MainOptions mainOpts = new MainOptions();
    for(BooleanOption opt : Arrays.asList(MainOptions.STRIPWS, MainOptions.STRIPNS))
      if(contextOpts.contains(opt)) mainOpts.set(opt, contextOpts.get(opt));
    if(!frag) {
      for(BooleanOption opt : Arrays.asList(MainOptions.INTPARSE, MainOptions.DTD,
          MainOptions.DTDVALIDATION, MainOptions.XINCLUDE))
        if(contextOpts.contains(opt)) mainOpts.set(opt, contextOpts.get(opt));
      if(contextOpts.contains(MainOptions.CATALOG))
        mainOpts.set(MainOptions.CATALOG, contextOpts.get(MainOptions.CATALOG));
    }

    // override with explicit options
    Map.of(ParseXmlFragmentOptions.STRIP_SPACE, MainOptions.STRIPWS,
        ParseXmlFragmentOptions.STRIPNS, MainOptions.STRIPNS,
        ParseXmlOptions.INTPARSE, MainOptions.INTPARSE,
        ParseXmlOptions.DTD, MainOptions.DTD,
        ParseXmlOptions.DTD_VALIDATION, MainOptions.DTDVALIDATION,
        ParseXmlOptions.XINCLUDE, MainOptions.XINCLUDE).forEach((opt, mainOpt) -> {
        if(options.contains(opt)) mainOpts.set(mainOpt, options.get(opt));
    });
    if(options.contains(ParseXmlOptions.CATALOG))
      mainOpts.set(MainOptions.CATALOG, options.get(ParseXmlOptions.CATALOG));

    final boolean intParse;
    if(frag) {
      intParse = true;
    } else {
      intParse = mainOpts.get(MainOptions.INTPARSE);
      if(intParse && mainOpts.get(MainOptions.DTDVALIDATION)) throw NODTDVALIDATION.get(info);
    }

    try {
      return new DBNode(intParse
          ? new XMLParser(io, mainOpts, true) : Parser.xmlParser(io, mainOpts));
    } catch(final IOException ex) {
      final Throwable th = ex.getCause();
      final QueryException qe = th instanceof ValidationException ? SAXVALIDATIONERR_X.get(info, ex)
                                                                  : SAXERR_X.get(info, ex);
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

    /** Strip namespaces (default: {@code qc.context.options.get(MainOptions.STRIPNS)}). */
    public static final BooleanOption STRIPNS = new BooleanOption("strip-ns");
  }
}
