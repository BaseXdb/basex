package org.basex.query.func.html;

import static org.basex.build.html.HtmlOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.html.*;
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

import nu.validator.htmlparser.sax.*;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.common.Heuristics;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Gunther Rademacher
 */
public class FnParseHtml extends StandardFunc {
  // TODO: handle second argument (method, html-version, encoding), produce error code FODC0012

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    final HtmlOptions options = toOptions(arg(1), new HtmlOptions(), true, qc);
    return value.isEmpty() ? Empty.VALUE : parse(new IOContent(toBytes(value)), options);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }

  /**
   * Parses the input and creates an XML document.
   * @param io input data
   * @param options HTML options
   * @return node
   * @throws QueryException query exception
   */
  protected final Item parse(final IO io, final HtmlOptions options) throws QueryException {
    try {
      if (!ParserImpl.available()) {
        // reader could not be initialized; fall back to html:parse
        final HtmlOptions htmlOptions = new HtmlOptions();
        htmlOptions.set(HtmlOptions.LEXICAL, true);
        htmlOptions.set(HtmlOptions.NONS, false);
        return new DBNode(new org.basex.build.html.HtmlParser(io, new MainOptions(), htmlOptions));
      }
      return new DBNode(new ParserImpl(info, io, options));
    } catch(final IOException ex) {
      throw INVHTML_X.get(info, ex);
    }
  }

  /**
   * Parser implementation.
   */
  private static class ParserImpl extends XMLParser {

    /**
     * Checks if Validator.nu is available.
     * @return result of check
     */
    public static boolean available() {
      return Reflect.available("nu.validator.htmlparser.sax.HtmlParser")
          && Reflect.available("nu.validator.htmlparser.sax.XmlSerializer")
          && Reflect.available("nu.validator.htmlparser.common.Heuristics")
          && Reflect.available("nu.validator.htmlparser.common.XmlViolationPolicy");
    }

    /**
     * Constructor.
     * @param info input info
     * @param source document source
     * @param options HTML options
     * @throws IOException I/O exception
     * @throws QueryException query exception
     */
    ParserImpl(final InputInfo info, final IO source, final HtmlOptions options)
        throws IOException, QueryException {
      super(toXml(info, source, options), new MainOptions());
    }

    /**
     * Converts an HTML document to XML.
     * @param info input info
     * @param io io reference
     * @param hopts HTML options
     * @return parser
     * @throws IOException I/O exception
     * @throws QueryException query exception
     */
    private static IO toXml(final InputInfo info, final IO io, final HtmlOptions hopts)
        throws IOException, QueryException {

      try {
        // define output
        final StringWriter sw = new StringWriter();
        final nu.validator.htmlparser.sax.HtmlParser reader =
            new nu.validator.htmlparser.sax.HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
        final ContentHandler writer = new XmlSerializer(sw);
        reader.setContentHandler(writer);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", writer);

        // define input
        final InputSource is = new InputSource(io.inputStream());

        // set Validator.nu options
        if(hopts.get(UNICODE_NORMALIZATION_CHECKING))
          reader.setCheckingNormalization(true);
        if(hopts.get(MAPPING_LANG_TO_XML_LANG))
          reader.setMappingLangToXmlLang(true);
        if(hopts.get(SCRIPTING_ENABLED))
          reader.setScriptingEnabled(true);
        if(hopts.contains(CONTENT_SPACE_POLICY))
          reader.setContentSpacePolicy(
              XmlViolationPolicy.valueOf(hopts.get(CONTENT_SPACE_POLICY).name()));
        if(hopts.contains(CONTENT_NON_XML_CHAR_POLICY))
          reader.setContentNonXmlCharPolicy(XmlViolationPolicy.valueOf(
              hopts.get(CONTENT_NON_XML_CHAR_POLICY).name()));
        if(hopts.contains(COMMENT_POLICY))
          reader.setCommentPolicy(XmlViolationPolicy.valueOf(hopts.get(COMMENT_POLICY).name()));
        if(hopts.contains(XMLNS_POLICY))
          reader.setXmlnsPolicy(XmlViolationPolicy.valueOf(hopts.get(XMLNS_POLICY).name()));
        if(hopts.contains(NAME_POLICY))
          reader.setNamePolicy(XmlViolationPolicy.valueOf(hopts.get(NAME_POLICY).name()));
        if(hopts.contains(STREAMABILITY_VIOLATION_POLICY))
          reader.setStreamabilityViolationPolicy(
              XmlViolationPolicy.valueOf(hopts.get(STREAMABILITY_VIOLATION_POLICY).name()));
        if(hopts.contains(XML_POLICY))
          reader.setXmlPolicy(XmlViolationPolicy.valueOf(hopts.get(XML_POLICY).name()));

        if(hopts.contains(HEURISTICS))
          reader.setHeuristics(Heuristics.valueOf(hopts.get(HEURISTICS).name()));
        // end Validator.nu options

        if (hopts.contains(ENCODING)) {
          String enc = hopts.get(HtmlOptions.ENCODING);
          if (!Strings.supported(enc))
            throw INVALIDOPT_X.get(info, "Unsupported encoding: " + enc + '.');
          is.setEncoding(Strings.supported(enc) ? Strings.normEncoding(enc) : Strings.UTF8);
        }

        reader.parse(is);
        return new IOContent(token(sw.toString()), io.name());

      } catch(final SAXException ex) {
        Util.errln(ex);
        throw INVHTML_X.get(info, ex.getLocalizedMessage());
      }
    }
  }
}
