package org.basex.build.html;

import static org.basex.util.Token.*;
import static org.basex.build.html.HtmlOptions.*;

import java.io.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.ccil.cowan.tagsoup.*;
import org.xml.sax.*;

/**
 * This class uses TagSoup to convert HTML input to well-formed XML.
 * If TagSoup is not found in the classpath, the original document is passed on.
 *
 * TagSoup was written by John Cowan and is based on the Apache 2.0 License:
 * {@code http://home.ccil.org/~cowan/XML/tagsoup/}.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class HtmlParser extends XMLParser {
  /** Name of HTML Parser. */
  private static final String NAME = "TagSoup";
  /** TagSoup URL. */
  private static final String FEATURES = "http://www.ccil.org/~cowan/tagsoup/features/";

  /**
   * Checks if a CatalogResolver is available.
   * @return result of check
   */
  public static boolean available() {
    return Reflect.available("org.ccil.cowan.tagsoup.Parser");
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

      // define output
      final StringWriter sw = new StringWriter();
      final XMLReader reader = new org.ccil.cowan.tagsoup.Parser();
      final XMLWriter writer = new XMLWriter(sw);
      writer.setOutputProperty(ENCODING.name(), Strings.UTF8);
      reader.setContentHandler(writer);

      // set TagSoup options
      if(hopts.get(HTML)) {
        reader.setFeature("http://xml.org/sax/features/namespaces", false);
        writer.setOutputProperty(METHOD.name(), "html");
        writer.setOutputProperty(OMIT_XML_DECLARATION.name(), "yes");
      }
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
      if(hopts.contains(METHOD))
        writer.setOutputProperty(METHOD.name(), hopts.get(METHOD));
      if(hopts.contains(DOCTYPE_SYSTEM))
        writer.setOutputProperty(DOCTYPE_SYSTEM.name(), hopts.get(DOCTYPE_SYSTEM));
      if(hopts.contains(DOCTYPE_PUBLIC))
        writer.setOutputProperty(DOCTYPE_PUBLIC.name(), hopts.get(DOCTYPE_PUBLIC));

      if(hopts.contains(ENCODING))
        enc = hopts.get(ENCODING);
      // end TagSoup options

      // define input
      final InputSource is = new InputSource(new ArrayInput(content));
      is.setEncoding(Strings.supported(enc) ? Strings.normEncoding(enc) : Strings.UTF8);
      reader.parse(is);
      return new IOContent(token(sw.toString()), io.name());

    } catch(final SAXException ex) {
      Util.errln(ex);
      return io;
    }
  }
}
