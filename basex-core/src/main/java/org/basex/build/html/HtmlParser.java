package org.basex.build.html;

import static org.basex.build.html.HtmlOptions.*;
import static org.basex.build.html.HtmlOptions.NOCDATA;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.ccil.cowan.tagsoup.*;
import org.xml.sax.*;

import nu.validator.htmlparser.common.Heuristics;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.*;

/**
 * This class uses the TagSoup or Validator.nu HTML parser to convert HTML input to well-formed
 * XML. If TagSoup should be used, and it is not found in the classpath, the original document
 * is passed on.
 *
 * TagSoup was written by John Cowan and is based on the Apache 2.0 License:
 * {@code http://vrici.lojban.org/~cowan/tagsoup/}
 *
 * The Validator.nu HTML parser was written by Henri Sivonen and is based on the MIT License:
 * {@code https://about.validator.nu/htmlparser/}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HtmlParser extends XMLParser {
  /**
   * Constructor.
   * @param source document source
   * @param options main options
   * @param hopts HTML options
   * @throws IOException I/O exception
   */
  public HtmlParser(final IO source, final MainOptions options, final HtmlOptions hopts)
      throws IOException {
    this(source, Parser.of(hopts), options, hopts);
  }

  /**
   * Constructor.
   * @param source document source
   * @param parser parser to be used (can be {@code null})
   * @param options main options
   * @param hopts HTML options
   * @throws IOException I/O exception
   */
  public HtmlParser(final IO source, final Parser parser, final MainOptions options,
      final HtmlOptions hopts) throws IOException {
    super(toXml(source, parser, hopts), options);
  }

  /**
   * Converts an HTML document to XML.
   * @param io io reference
   * @param parser parser to be used (can be {@code null})
   * @param hopts HTML options
   * @return parser
   * @throws IOException I/O exception
   */
  private static IO toXml(final IO io, final Parser parser, final HtmlOptions hopts)
      throws IOException {
    // parser unavailable: fall back to XML
    if(parser == null) return io;
    try {
      // define output
      final StringWriter sw = new StringWriter();
      final XMLReader reader = parser.reader(hopts, sw);

      // define input
      try(InputStream in = io.inputStream()) {
        final InputSource is = new InputSource(in);
        final String enc = io.encoding() != null ? io.encoding() : hopts.get(ENCODING);
        if(enc != null) {
          if(!Strings.encodingSupported(enc)) throw INVALIDOPTION_X.getIO(
              "Unknown encoding: " + enc + '.');
          is.setEncoding(Strings.normEncoding(enc));
        }
        reader.parse(is);
      }
      return new IOContent(token(sw.toString()), io.name());
    } catch(final SAXException ex) {
      Util.errln(ex);
      throw INVHTML_X.getIO(ex.getLocalizedMessage());
    }
  }

  /** Method option values. */
  public enum Method {
    /** TagSoup parser. */
    tagsoup(Parser.TAGSOUP),
    /** Validator.nu parser. */
    nu(Parser.NU);

    /** Parser associated with this method. */
    public final Parser parser;

    /**
     * Constructor.
     * @param parser parser associated with this method
     */
    Method(final Parser parser) {
      this.parser = parser;
    }
  }

  /** Parser type. */
  public enum Parser {
    /** TagSoup parser. */
    TAGSOUP("TagSoup", "org.ccil.cowan.tagsoup.Parser") {

      /** TagSoup URL. */
      private static final String FEATURES = "http://www.ccil.org/~cowan/tagsoup/features/";

      @Override
      XMLReader reader(final HtmlOptions hopts, final StringWriter sw) throws SAXException {
        final XMLReader reader = new org.ccil.cowan.tagsoup.Parser();
        final XMLWriter writer = new XMLWriter(sw);
        writer.setOutputProperty(ENCODING.name(), Strings.UTF8);
        reader.setContentHandler(writer);

        // set TagSoup options
        if(hopts.get(NONS))
          reader.setFeature("http://xml.org/sax/features/namespaces", false);
        if(hopts.get(NOBOGONS))
          reader.setFeature(FEATURES + "ignore-bogons", true);
        if(hopts.get(NODEFAULTS))
          reader.setFeature(FEATURES + "default-attributes", false);
        if(hopts.get(NOCOLONS))
          reader.setFeature(FEATURES + "translate-colons", true);
        if(hopts.get(NORESTART))
          reader.setFeature(FEATURES + "restart-elements", false);
        if(hopts.get(IGNORABLE))
          reader.setFeature(FEATURES + "ignorable-whitespace", true);
        if(hopts.get(EMPTYBOGONS))
          reader.setFeature(FEATURES + "bogons-empty", true);
        if(hopts.get(ANY))
          reader.setFeature(FEATURES + "bogons-empty", false);
        if(hopts.get(NOROOTBOGONS))
          reader.setFeature(FEATURES + "root-bogons", false);
        if(hopts.get(NOCDATA))
          reader.setFeature(FEATURES + "cdata-elements", false);
        if(hopts.get(LEXICAL))
          reader.setProperty("http://xml.org/sax/properties/lexical-handler", writer);
        if(hopts.get(OMIT_XML_DECLARATION))
          writer.setOutputProperty(OMIT_XML_DECLARATION.name(), "yes");
        if(hopts.contains(DOCTYPE_SYSTEM))
          writer.setOutputProperty(DOCTYPE_SYSTEM.name(), hopts.get(DOCTYPE_SYSTEM));
        if(hopts.contains(DOCTYPE_PUBLIC))
          writer.setOutputProperty(DOCTYPE_PUBLIC.name(), hopts.get(DOCTYPE_PUBLIC));
        return reader;
      }
    },

    /** Validator.nu parser. */
    NU("Validator.nu", "nu.validator.htmlparser.sax.HtmlParser",
        "nu.validator.htmlparser.sax.XmlSerializer",
        "nu.validator.htmlparser.common.XmlViolationPolicy",
        "nu.validator.htmlparser.common.Heuristics") {

      /** Class needed for option heuristics=ICU. */
      private static final String ICU_CLASS_NAME = "com.ibm.icu.text.CharsetDetector";
      /** Class needed for option heuristics=CHARDET. */
      private static final String CHARDET_CLASS_NAME =
          "org.mozilla.intl.chardet.nsICharsetDetectionObserver";

      @Override
      XMLReader reader(final HtmlOptions hopts, final StringWriter sw) throws SAXException {
        final nu.validator.htmlparser.sax.HtmlParser reader =
            new nu.validator.htmlparser.sax.HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
        final ContentHandler writer = new XmlSerializer(sw);
        reader.setContentHandler(writer);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", writer);

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
        return reader;
      }

      @Override
      public void ensureAvailable(final HtmlOptions options, final QNm name, final InputInfo info)
          throws QueryException {
        super.ensureAvailable(options, name, info);
        if(options.contains(HEURISTICS)) {
          switch(options.get(HEURISTICS)) {
            case ALL:
              ensureAvailable(ICU_CLASS_NAME, name, info);
              ensureAvailable(CHARDET_CLASS_NAME, name, info);
              break;
            case ICU:
              ensureAvailable(ICU_CLASS_NAME, name, info);
              break;
            case CHARDET:
              ensureAvailable(CHARDET_CLASS_NAME, name, info);
              break;
            default:
          }
        }
      }

      @Override
      public boolean available(final HtmlOptions options) {
        if(!super.available(options)) return false;
        if(!options.contains(HEURISTICS)) return true;
        switch(options.get(HEURISTICS)) {
          case ALL:
            if(!Reflect.available(ICU_CLASS_NAME)) return false;
            if(!Reflect.available(CHARDET_CLASS_NAME)) return false;
            break;
          case ICU:
            if(!Reflect.available(ICU_CLASS_NAME)) return false;
            break;
          case CHARDET:
            if(!Reflect.available(CHARDET_CLASS_NAME)) return false;
            break;
          default:
        }
        return true;
      }
    };

    /** The default parser: TAGSOUP if available, NU if available, {@code null} otherwise. */
    public static final Parser DEFAULT;
    static {
      final HtmlOptions opts = new HtmlOptions();
      DEFAULT = TAGSOUP.available(opts) ? TAGSOUP : NU.available(opts) ? NU : null;
    }

    /** String representation. */
    private final String string;
    /** Required classes. */
    private final String[] classes;

    /**
     * Return a reader instance for this parser.
     * @param options HTML options
     * @param writer string writer
     * @return reader
     * @throws SAXException SAX exception
     */
    abstract XMLReader reader(HtmlOptions options, StringWriter writer) throws SAXException;

    /**
     * Constructor.
     * @param string string representation
     * @param classes required classes
     */
    Parser(final String string, final String... classes) {
      this.string = string;
      this.classes = classes;
    }

    /**
     * Checks if this parser is available.
     * @param options HTML options
     * @return result of check
     */
    public boolean available(@SuppressWarnings("unused") final HtmlOptions options) {
      for(final String cl : classes) if(!Reflect.available(cl)) return false;
      return true;
    }

    /**
     * Throws an exception if any of the classes required for this parser are unavailable.
     * @param options HTML options
     * @param name name of function that is asking for this parser
     * @param info input info (can be {@code null})
     * @throws QueryException query exception
     */
    public void ensureAvailable(@SuppressWarnings("unused") final HtmlOptions options,
        final QNm name, final InputInfo info) throws QueryException {
      for(final String cl : classes) ensureAvailable(cl, name, info);
    }

    /**
     * Throws an exception if a class required for this parser is unavailable.
     * @param className the class name
     * @param name name of function that is asking for this parser
     * @param info input info (can be {@code null})
     * @throws QueryException query exception,
     */
    static void ensureAvailable(final String className, final QNm name,
        final InputInfo info) throws QueryException {
      if(!Reflect.available(className)) throw BASEX_CLASSPATH_X_X.get(info, name, className);
    }

    /**
     * Returns the parser associated with the specified HTML options.
     * @param options HTML options.
     * @return parser (can be {@code null})
     */
    public static Parser of(final HtmlOptions options) {
      return of(options, Parser.DEFAULT);
    }

    /**
     * Returns the parser associated with the specified HTML options, if any, or the specified
     * default parser.
     * @param options HTML options.
     * @param defaultParser default parser (can be {@code null})
     * @return parser (can be {@code null})
     */
    public static Parser of(final HtmlOptions options, final Parser defaultParser) {
      return options.contains(METHOD) ? options.get(METHOD).parser : defaultParser;
    }

    @Override
    public String toString() {
      return string;
    }
  }
}
