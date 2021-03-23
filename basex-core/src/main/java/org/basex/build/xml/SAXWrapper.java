package org.basex.build.xml;

import static org.basex.core.Text.*;

import java.io.*;

import javax.xml.transform.sax.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.parse.xml.*;
import org.basex.util.*;
import org.xml.sax.*;

/**
 * This class parses an XML document with Java's internal SAX parser. Note that
 * not all files cannot be parsed with the default parser; for example, the
 * DBLP documents contain too many entities and cause an out of memory error.
 * The internal {@link XMLParser} can be used as alternative.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class SAXWrapper extends SingleParser {
  /** Processed bytes. */
  private long bytes;
  /** Processed lines. */
  private int lines;

  /** SAX handler reference. */
  private SAXHandler saxh;
  /** File length (real or estimated). */
  private long length;

  /**
   * Constructor.
   * @param source input source
   * @param opts database options
   */
  public SAXWrapper(final IO source, final MainOptions opts) {
    super(source, opts);
  }

  @Override
  public void parse() throws IOException {
    final InputSource is = inputSource();
    final SAXSource saxs = new SAXSource(is);
    try {
      XMLReader reader = saxs.getXMLReader();
      if(reader == null) {
        reader = XmlParser.reader(options.get(MainOptions.DTD), options.get(MainOptions.XINCLUDE));
      }

      saxh = new SAXHandler(builder, options.get(MainOptions.CHOP),
          options.get(MainOptions.STRIPNS));
      final EntityResolver er = CatalogWrapper.getEntityResolver(options);
      if(er != null) reader.setEntityResolver(er);

      reader.setDTDHandler(saxh);
      reader.setContentHandler(saxh);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", saxh);
      reader.setErrorHandler(saxh);

      reader.parse(is);
    } catch(final SAXParseException ex) {
      final String msg = Util.info(SCANPOS_X_X, source.path(), ex.getLineNumber(),
          ex.getColumnNumber()) + COLS + Util.message(ex);
      throw new IOException(msg, ex);
    } catch(final JobException ex) {
      throw ex;
    } catch(final Exception ex) {
      // occurs, e.g. if document encoding is invalid:
      // prefix message with source id
      // wrap and return original message
      throw new IOException('"' + source.path() + '"' + Util.message(ex), ex);
    } finally {
      try(Reader r = is.getCharacterStream()) { /* no action */ }
      try(InputStream ist = is.getByteStream()) { /* no action */ }
    }
  }

  /**
   * Returns an input source. Wraps the input source with a stream which counts the number of
   * read bytes and parsed lines.
   * @return resulting SAX source
   * @throws IOException I/O exception
   */
  @SuppressWarnings("resource")
  private InputSource inputSource() throws IOException {
    final InputStream input = source.inputStream();

    // retrieve/estimate number of bytes to be read
    length = source.length();
    try {
      if(length <= 0) length = input.available();
    } catch(final IOException ex) {
      input.close();
      throw ex;
    }

    // create input source with wrapped input stream
    final InputStream wrapped = new InputStream() {
      final InputStream buffer = input instanceof ByteArrayInputStream ||
          input instanceof ArrayInput ? input : BufferInput.get(input);

      @Override
      public int read() throws IOException {
        final int i = buffer.read();
        if(i == '\n') ++lines;
        ++bytes;
        return i;
      }

      @Override
      public void close() throws IOException {
        buffer.close();
      }
    };

    final InputSource is = new InputSource(wrapped);
    is.setSystemId(source.url());
    return is;
  }

  @Override
  public String detailedInfo() {
    return length == 0 ? super.detailedInfo() : Util.info(SCANPOS_X_X, source.name(), lines + 1);
  }

  @Override
  public double progressInfo() {
    return length == 0 ? saxh == null ? 0 : saxh.nodes / 3000000.0d % 1 : (double) bytes / length;
  }
}
