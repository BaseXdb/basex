package org.basex.build.xml;

import static org.basex.core.Text.*;

import java.io.*;

import javax.xml.transform.sax.*;

import org.basex.build.*;
import org.basex.build.xml.SAXHandler.*;
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
 * @author BaseX Team, BSD License
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
   * @param options main options
   */
  public SAXWrapper(final IO source, final MainOptions options) {
    super(source, options);
  }

  @Override
  public void parse() throws IOException {
    final InputSource is = inputSource();
    final SAXSource saxs = new SAXSource(is);
    try {
      XMLReader reader = saxs.getXMLReader();
      if(reader == null) {
        reader = XmlParser.reader(options);
      }
      if(reader.getEntityResolver() == null) {
        final EntityResolver er = Resolver.entities(options);
        if(er != null) reader.setEntityResolver(er);
      }

      saxh = new SAXHandler(builder, options.get(MainOptions.STRIPWS),
          options.get(MainOptions.STRIPNS));
      reader.setDTDHandler(saxh);
      reader.setContentHandler(saxh);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", saxh);
      reader.setErrorHandler(saxh);

      reader.parse(is);
    } catch(final SAXParseException | ValidationException ex) {
      final SAXParseException spex =
          (SAXParseException) (ex instanceof ValidationException ? ex.getCause() : ex);
      final String msg = Util.info(SCANPOS_X_X, source.path(), spex.getLineNumber(),
          spex.getColumnNumber()) + COLS + Util.message(spex);
      throw new IOException(msg, ex);
    } catch(final JobException ex) {
      throw ex;
    } catch(final Exception ex) {
      // invalid document encoding, catalog raises an error, ...
      final String msg = ex.getCause() != null ? ex.getCause().getMessage() : Util.message(ex);
      throw new IOException(source.path() + ": " + msg, ex);
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
      final InputStream buffer = input instanceof ByteArrayInputStream ? input :
        BufferInput.get(input);

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
    is.setEncoding(source.encoding());
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
