package org.basex.build;

import static org.basex.util.Token.*;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.basex.core.Main;
import org.basex.io.BufferInput;
import org.basex.io.CachedInput;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * This class uses TagSoup to convert HTML input to well-formed XML.
 * TagSoup was written by John Cowan and licensed under Apache 2.0
 * http://home.ccil.org/~cowan/XML/tagsoup/
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class HTMLParser {
  /**
   * Constructor.
   * @throws Exception if the parser could not be initialized
   */
  HTMLParser() throws Exception {
    getHTMLReader();
  }

  /**
   * Creates a new TagSoup parser that isn't namespace-aware.
   * @return the parser
   * @throws Exception exception
   */
  private static XMLReader getHTMLReader() throws Exception {
    return (XMLReader)
      Class.forName("org.ccil.cowan.tagsoup.Parser").newInstance();
  }

  /**
   * Converts an HTML document to XML, if TagSoup is found in the classpath.
   * @param io io reference
   * @return parser
   */
  IO toXML(final IO io) {
    final String path = io.path();
    if(!path.endsWith(".htm") && !path.endsWith(".html")) return io;

    try {
      // tries to extract the encoding from the input
      byte[] content = io.content();
      final BufferInput bi = new CachedInput(content);
      String enc = bi.encoding();
      content = bi.content().finish();

      // looks for a charset definition
      final byte[] encoding = token("charset=");
      int cs = indexOf(content, encoding);
      if(cs > 0) {
        // extracts the encoding string
        cs += encoding.length;
        int ce = cs;
        while(++ce < content.length && content[ce] > 0x28);
        enc = string(substring(content, cs, ce));
      }

      // define input
      final InputSource is = new InputSource(new ByteArrayInputStream(content));
      is.setEncoding(Charset.isSupported(enc) ? code(enc, null) : UTF8);
      // define output
      final StringWriter sw = new StringWriter();
      final XMLReader parser = getHTMLReader();
      parser.setContentHandler((ContentHandler)
          Class.forName("org.ccil.cowan.tagsoup.XMLWriter").getConstructor(
              new Class[] { Writer.class }).newInstance(sw));
      parser.parse(is);
      return new IOContent(token(sw.toString()), io.name());
    } catch(final Exception ex) {
      Main.debug(ex);
      return io;
    }
  }
}
