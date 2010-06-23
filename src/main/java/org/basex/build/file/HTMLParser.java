package org.basex.build.file;

import static org.basex.util.Token.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.basex.build.xml.XMLParser;
import org.basex.core.Main;
import org.basex.core.Prop;
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
public final class HTMLParser extends XMLParser {
  /** HTML reader. */
  private static XMLReader reader;

  /* Check is TagSoup is referenced in the classpath. */
  static {
    try {
      reader = (XMLReader) Class.forName(
          "org.ccil.cowan.tagsoup.Parser").newInstance();
    } catch(final Exception ex) {
      reader = null;
    }
  }

  /**
   * Constructor.
   * @param f file reference
   * @param tar target for collection adding.
   * @param pr database properties
   * @throws IOException I/O exception
   */
  public HTMLParser(final IO f, final String tar, final Prop pr)
      throws IOException {
    super(toXML(f), tar, pr);
  }

  /**
   * Converts an HTML document to XML.
   * @param io io reference
   * @return parser
   */
  private static IO toXML(final IO io) {
    // reader could not be initialized; fall back to XML
    if(reader == null) return io;

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
      reader.setContentHandler((ContentHandler)
          Class.forName("org.ccil.cowan.tagsoup.XMLWriter").getConstructor(
              new Class[] { Writer.class }).newInstance(sw));
      reader.parse(is);
      return new IOContent(token(sw.toString()), io.name());
    } catch(final Exception ex) {
      Main.debug(ex);
      return io;
    }
  }
}
