package org.basex.build;

import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * This class uses TagSoup to convert HTML input to well-formed XML.
 * If TagSoup is not found in the classpath, the original document is passed on.
 *
 * TagSoup was written by John Cowan and is based on the Apache 2.0 License:
 * {@code http://home.ccil.org/~cowan/XML/tagsoup/}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class HtmlParser extends XMLParser {
  /** Name of HTML Parser. */
  private static final String NAME = "TagSoup";
  /** TagSoup URL. */
  private static final String FEATURES = "http://www.ccil.org/~cowan/tagsoup/features/";

  /** XML parser class string. */
  private static final String PCLASS = "org.ccil.cowan.tagsoup.Parser";
  /** XML writer class string. */
  private static final String WCLASS = "org.ccil.cowan.tagsoup.XMLWriter";
  /** HTML reader. */
  private static final Class<?> READER = Reflect.find(PCLASS);
  /** HTML writer. */
  private static final Constructor<?> WRITER = Reflect.find(Reflect.find(WCLASS), Writer.class);
  /** XML writer output property method. */
  private static final Method METHOD = Reflect.method(
    Reflect.find(WCLASS), "setOutputProperty", new Class[] { String.class, String.class });

  /**
   * Checks if a CatalogResolver is available.
   * @return result of check
   */
  public static boolean available() {
    return READER != null;
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
   * @param opts database options
   * @throws IOException I/O exception
   */
  public HtmlParser(final IO source, final MainOptions opts) throws IOException {
    this(source, opts, opts.get(MainOptions.HTMLPARSER));
  }

  /**
   * Constructor.
   * @param source document source
   * @param opts database options
   * @param hopts html options
   * @throws IOException I/O exception
   */
  public HtmlParser(final IO source, final MainOptions opts, final HtmlOptions hopts)
      throws IOException {
    super(toXML(source, hopts), opts);
  }

  /**
   * Converts an HTML document to XML.
   * @param io io reference
   * @param opts html options
   * @return parser
   * @throws IOException I/O exception
   */
  private static IO toXML(final IO io, final HtmlOptions opts) throws IOException {
    // reader could not be initialized; fall back to XML
    if(READER == null) return io;

    try {
      // tries to extract the encoding from the input
      final TextInput ti = new TextInput(io);
      String enc = ti.encoding();
      final byte[] content = ti.content();

      // looks for a charset definition
      final byte[] encoding = token("charset=");
      int cs = indexOf(content, encoding);
      if(cs > 0) {
        // extracts the encoding string
        cs += encoding.length;
        int ce = cs;
        final int cl = content.length;
        while(++ce < cl && content[ce] > 0x28);
        enc = string(substring(content, cs, ce));
      }

      // define input
      final InputSource is = new InputSource(new ArrayInput(content));
      is.setEncoding(Strings.supported(enc) ? Strings.normEncoding(enc) : Strings.UTF8);
      // define output
      final StringWriter sw = new StringWriter();
      final XMLReader reader = (XMLReader) Reflect.get(READER);
      final Object writer = Reflect.get(WRITER, sw);

      // set TagSoup options
      if(opts.get(HtmlOptions.HTML)) {
        reader.setFeature("http://xml.org/sax/features/namespaces", false);
        opt(writer, "method", "html");
        opt(writer, "omit-xml-declaration", "yes");
      }
      if(opts.get(HtmlOptions.NONS))
        reader.setFeature("http://xml.org/sax/features/namespaces", false);
      if(opts.get(HtmlOptions.OMITXML))
        opt(writer, "omit-xml-declaration", "yes");
      if(opts.get(HtmlOptions.NOBOGONS))
        reader.setFeature(FEATURES + "ignore-bogons", true);
      if(opts.get(HtmlOptions.NODEFAULTS))
        reader.setFeature(FEATURES + "default-attributes", false);
      if(opts.get(HtmlOptions.NOCOLONS))
        reader.setFeature(FEATURES + "translate-colons", true);
      if(opts.get(HtmlOptions.NORESTART))
        reader.setFeature(FEATURES + "restart-elements", false);
      if(opts.get(HtmlOptions.IGNORABLE))
        reader.setFeature(FEATURES + "ignorable-whitespace", true);
      if(opts.get(HtmlOptions.EMPTYBOGONS))
        reader.setFeature(FEATURES + "bogons-empty", true);
      if(opts.get(HtmlOptions.ANY))
        reader.setFeature(FEATURES + "bogons-empty", false);
      if(opts.get(HtmlOptions.NOROOTBOGONS))
        reader.setFeature(FEATURES + "root-bogons", false);
      if(opts.get(HtmlOptions.NOCDATA))
        reader.setFeature(FEATURES + "cdata-elements", false);
      if(opts.get(HtmlOptions.LEXICAL))
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", writer);
      if(opts.contains(HtmlOptions.METHOD))
        opt(writer, "method", opts.get(HtmlOptions.METHOD));
      if(opts.contains(HtmlOptions.DOCTYPESYS))
        opt(writer, "doctype-system", opts.get(HtmlOptions.DOCTYPESYS));
      if(opts.contains(HtmlOptions.DOCTYPEPUB))
        opt(writer, "doctype-public", opts.get(HtmlOptions.DOCTYPEPUB));
      if(opts.contains(HtmlOptions.ENCODING))
        is.setEncoding(opts.get(HtmlOptions.ENCODING));
      // end TagSoup options

      reader.setContentHandler((ContentHandler) writer);
      reader.parse(is);
      return new IOContent(token(sw.toString()), io.name());

    } catch(final SAXException ex) {
      Util.errln(ex);
      return io;
    }
  }

  /**
   * Reflection invoke XMLWriter.setOutputProperty().
   * @param writer writer instance
   * @param name property
   * @param value value
   */
  private static void opt(final Object writer, final String name, final String value) {
    Reflect.invoke(METHOD, writer, name, value);
  }
}
