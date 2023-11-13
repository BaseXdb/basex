package org.basex.build.html;

import static org.basex.build.html.HtmlOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;
import org.xml.sax.*;

import nu.validator.htmlparser.common.Heuristics;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.*;

/**
 * This class uses the Validator.nu HTML parser to convert HTML input to well-formed XML.
 * If the Validator.nu HTML parser is not found in the classpath, the original document is
 * passed on.
 *
 * The Validator.nu HTML parser was written by Henri Sivonen and is based on the MIT License:
 * {@code https://about.validator.nu/htmlparser/}.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class HtmlParser extends XMLParser {
  /** Name of HTML Parser. */
  private static final String NAME = "Validator.nu";

  /**
   * Checks if Validator.nu is available.
   * @return result of check
   */
  public static boolean available() {
    return firstUnavailableClass() == null;
  }

  /**
   * Check whether Validator.nu classes are available on the class path.
   * @return the name of the first class that is not available, or null if all classes are available
   */
  public static String firstUnavailableClass() {
    for(final String className : Arrays.asList("nu.validator.htmlparser.sax.HtmlParser",
        "nu.validator.htmlparser.sax.XmlSerializer",
        "nu.validator.htmlparser.common.XmlViolationPolicy",
        "nu.validator.htmlparser.common.Heuristics")) {
      if(!Reflect.available(className)) return className;
    }
    return null;
  }

  /**
   * Returns the name of the parser, or an empty string.
   * @return name of parser
   */
  public static String parser() {
    return available() ? NAME : "";
  }

  /**
   * Constructor.
   * @param source document source
   * @param options main options
   * @throws IOException I/O exception
   */
  public HtmlParser(final IO source, final MainOptions options) throws IOException {
    this(source, options, options.get(MainOptions.HTMLPARSER));
  }

  /**
   * Constructor.
   * @param source document source
   * @param options main options
   * @param hopts html options
   * @throws IOException I/O exception
   */
  public HtmlParser(final IO source, final MainOptions options, final HtmlOptions hopts)
      throws IOException {
    super(toXml(source, hopts), options);
  }

  /**
   * Converts an HTML document to XML.
   * @param io io reference
   * @param hopts html options
   * @return parser
   * @throws IOException I/O exception
   */
  private static IO toXml(final IO io, final HtmlOptions hopts) throws IOException {
    // reader could not be initialized; fall back to XML
    if(!available()) return io;

    try {
      // define output
      final StringWriter sw = new StringWriter();
      final nu.validator.htmlparser.sax.HtmlParser reader =
          new nu.validator.htmlparser.sax.HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
      final ContentHandler writer = new XmlSerializer(sw);
      reader.setContentHandler(writer);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", writer);

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

      // define input
      final InputSource is = new InputSource(io.inputStream());
      String enc = io.encoding() != null
          ? io.encoding()
          : hopts.contains(ENCODING)
            ? hopts.get(HtmlOptions.ENCODING)
            : null;
      if (enc != null) {
        if (!Strings.supported(enc))
          throw INVALIDOPT_X.getIO("Unsupported encoding: " + enc + '.');
        is.setEncoding(Strings.normEncoding(enc));
      }

      reader.parse(is);
      return new IOContent(token(sw.toString()), io.name());

    } catch(final SAXException ex) {
      Util.errln(ex);
      throw INVHTML_X.getIO(ex.getLocalizedMessage());
    }
  }
}
