package org.basex.build.html;

import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.options.*;
import org.xml.sax.*;

/**
 * This class uses TagSoup to convert HTML input to well-formed XML.
 * If TagSoup is not found in the classpath, the original document is passed on.
 *
 * TagSoup was written by John Cowan and is based on the Apache 2.0 License:
 * {@code http://home.ccil.org/~cowan/XML/tagsoup/}.
 *
 * @author BaseX Team 2005-23, BSD License
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
    Reflect.find(WCLASS), "setOutputProperty", String.class, String.class);

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
   * @param options database options
   * @throws IOException I/O exception
   */
  public HtmlParser(final IO source, final MainOptions options) throws IOException {
    this(source, options, options.get(MainOptions.HTMLPARSER));
  }

  /**
   * Constructor.
   * @param source document source
   * @param options database options
   * @param hopts html options
   * @throws IOException I/O exception
   */
  public HtmlParser(final IO source, final MainOptions options, final HtmlOptions hopts)
      throws IOException {
    super(toXML(source, hopts), options);
  }

  /**
   * Converts an HTML document to XML.
   * @param io io reference
   * @param hopts html options
   * @return parser
   * @throws IOException I/O exception
   */
  private static IO toXML(final IO io, final HtmlOptions hopts) throws IOException {
    // reader could not be initialized; fall back to XML
    if(READER == null) return io;

    try(TextInput ti = new TextInput(io)) {
      // tries to extract the encoding from the input
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
      set(writer, HtmlOptions.ENCODING, Strings.UTF8);

      // set TagSoup options
      if(hopts.get(HtmlOptions.HTML)) {
        reader.setFeature("http://xml.org/sax/features/namespaces", false);
        set(writer, HtmlOptions.METHOD, "html");
        set(writer, HtmlOptions.OMIT_XML_DECLARATION, "yes");
      }
      if(hopts.get(HtmlOptions.NONS))
        reader.setFeature("http://xml.org/sax/features/namespaces", false);
      if(hopts.get(HtmlOptions.OMIT_XML_DECLARATION))
        set(writer, HtmlOptions.OMIT_XML_DECLARATION, "yes");
      if(hopts.get(HtmlOptions.NOBOGONS))
        reader.setFeature(FEATURES + "ignore-bogons", true);
      if(hopts.get(HtmlOptions.NODEFAULTS))
        reader.setFeature(FEATURES + "default-attributes", false);
      if(hopts.get(HtmlOptions.NOCOLONS))
        reader.setFeature(FEATURES + "translate-colons", true);
      if(hopts.get(HtmlOptions.NORESTART))
        reader.setFeature(FEATURES + "restart-elements", false);
      if(hopts.get(HtmlOptions.IGNORABLE))
        reader.setFeature(FEATURES + "ignorable-whitespace", true);
      if(hopts.get(HtmlOptions.EMPTYBOGONS))
        reader.setFeature(FEATURES + "bogons-empty", true);
      if(hopts.get(HtmlOptions.ANY))
        reader.setFeature(FEATURES + "bogons-empty", false);
      if(hopts.get(HtmlOptions.NOROOTBOGONS))
        reader.setFeature(FEATURES + "root-bogons", false);
      if(hopts.get(HtmlOptions.NOCDATA))
        reader.setFeature(FEATURES + "cdata-elements", false);
      if(hopts.get(HtmlOptions.LEXICAL))
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", writer);
      if(hopts.contains(HtmlOptions.METHOD))
        set(writer, HtmlOptions.METHOD, hopts.get(HtmlOptions.METHOD));
      if(hopts.contains(HtmlOptions.DOCTYPE_SYSTEM))
        set(writer, HtmlOptions.DOCTYPE_SYSTEM, hopts.get(HtmlOptions.DOCTYPE_SYSTEM));
      if(hopts.contains(HtmlOptions.DOCTYPE_PUBLIC))
        set(writer, HtmlOptions.DOCTYPE_PUBLIC, hopts.get(HtmlOptions.DOCTYPE_PUBLIC));
      if(hopts.contains(HtmlOptions.ENCODING))
        is.setEncoding(hopts.get(HtmlOptions.ENCODING));
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
   * @param option property
   * @param value value
   */
  private static void set(final Object writer, final Option<?> option, final String value) {
    Reflect.invoke(METHOD, writer, option.name(), value);
  }
}
