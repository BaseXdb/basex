package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.Parser;
import org.basex.build.xml.*;
import org.basex.build.xml.SAXHandler.*;
import org.basex.core.*;
import org.basex.core.users.*;
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
  /** Function options. */
  public static class ParseXmlFragmentOptions extends Options {
    /** Document node's base URI. */
    public static final StringOption BASE_URI = new StringOption("base-uri");
    /** Remove whitespace-only text nodes. */
    public static final BooleanOption STRIP_SPACE = new BooleanOption("strip-space", false);

    /** Custom option (see {@link MainOptions#STRIPNS}). */
    public static final BooleanOption STRIPNS = new BooleanOption("stripns", false);
  }

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
   * @param fragment parse fragment
   * @param options options
   * @return result or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item parseXml(final QueryContext qc, final boolean fragment,
      final ParseXmlFragmentOptions options) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    if(value.isEmpty()) return Empty.VALUE;

    final String baseURI = options.contains(ParseXmlFragmentOptions.BASE_URI)
        ? options.get(ParseXmlFragmentOptions.BASE_URI) : string(info.sc().baseURI().string());
    final IO io = new IOContent(toBytes(value), baseURI, value instanceof Bin ? null :
      Strings.UTF8);

    // assign options
    final MainOptions mopts = new MainOptions();
    Map.of(ParseXmlFragmentOptions.STRIP_SPACE, MainOptions.STRIPWS,
        ParseXmlFragmentOptions.STRIPNS, MainOptions.STRIPNS,
        ParseXmlOptions.INTPARSE, MainOptions.INTPARSE,
        ParseXmlOptions.ALLOW_EXTERNAL_ENTITIES, MainOptions.ALLOWEXTERNALENTITIES,
        ParseXmlOptions.DTD, MainOptions.DTD,
        ParseXmlOptions.DTD_VALIDATION, MainOptions.DTDVALIDATION,
        ParseXmlOptions.XINCLUDE, MainOptions.XINCLUDE).forEach((opt, mopt) -> {
      if(options.contains(opt)) mopts.set(mopt, options.get(opt));
    });
    Map.of(ParseXmlOptions.XSD_VALIDATION, MainOptions.XSDVALIDATION,
        ParseXmlOptions.CATALOG, MainOptions.CATALOG).forEach((opt, mopt) -> {
      if(options.contains(opt)) mopts.set(mopt, options.get(opt));
    });
    if(options.contains(ParseXmlOptions.ENTITY_EXPANSION_LIMIT)) {
      mopts.set(MainOptions.ENTITYEXPANSIONLIMIT,
          options.get(ParseXmlOptions.ENTITY_EXPANSION_LIMIT));
    }
    if(mopts.get(MainOptions.DTD) || mopts.get(MainOptions.XINCLUDE)
        || mopts.get(MainOptions.ALLOWEXTERNALENTITIES)
        || !mopts.get(MainOptions.CATALOG).isEmpty()) {
      checkPerm(qc, Perm.CREATE);
    }

    final boolean dtdVal = mopts.get(MainOptions.DTDVALIDATION);
    final String xsdVal = mopts.get(MainOptions.XSDVALIDATION);
    final boolean skip = MainOptions.SKIP.equals(xsdVal);
    final boolean strict = MainOptions.STRICT.equals(xsdVal);
    final boolean intparse = fragment || mopts.get(MainOptions.INTPARSE);
    if(intparse) {
      if(dtdVal) throw NODTDVALIDATION.get(info);
      if(!skip) throw NOXSDVALIDATION_X.get(info, xsdVal);
    } else if(!skip) {
      if(!strict) throw INVALIDXSDOPT_X.get(info, xsdVal);
      if(dtdVal) throw NOXSDANDDTD_X.get(info, xsdVal);
    }

    try {
      return new DBNode(intparse ? new XMLParser(io, mopts, fragment) :
        Parser.xmlParser(io, mopts));
    } catch(final IOException ex) {
      final Throwable th = ex.getCause();
      final QueryException qe = !(th instanceof ValidationException) ? SAXERR_X.get(info, ex) :
        dtdVal ? DTDVALIDATIONERR_X.get(info, ex) : XSDVALIDATIONERR_X.get(info, ex);
      if(th instanceof SAXException) qe.value(Str.get(th.toString()));
      throw qe;
    }
  }
}
