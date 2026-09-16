package org.basex.build.xml;

import static org.basex.core.Text.*;

import java.io.*;

import javax.xml.catalog.*;
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

  /** SAX handler reference (can be {@code null}). */
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
      final boolean trusted = options.isTrusted();
      final boolean dtd = options.get(MainOptions.DTD);
      final boolean dtdValidation = options.get(MainOptions.DTDVALIDATION);
      if(!trusted && options.get(MainOptions.XINCLUDE))
        throw new TrustedViolationException("xinclude");

      final EntityResolver er = options.resolver().entityResolver();
      if(!trusted && (dtd || dtdValidation)) {
        // block external resource access, unless the resource is mapped by a catalog
        reader.setEntityResolver((pubId, sysId) -> {
          final InputSource mapped = mapped(er, pubId, sysId);
          if(mapped == null) throw TrustedViolationException.entity(
              sysId != null ? sysId : pubId != null ? pubId : "", dtdValidation);
          saxh.resource = mapped.getSystemId();
          return mapped;
        });
      } else if(reader.getEntityResolver() == null) {
        reader.setEntityResolver((pubId, sysId) -> {
          saxh.resource = sysId;
          return er != null ? er.resolveEntity(pubId, sysId) : null;
        });
      }

      saxh = new SAXHandler(builder, options.get(MainOptions.STRIPWS),
          options.get(MainOptions.STRIPNS));
      reader.setDTDHandler(saxh);
      reader.setContentHandler(saxh);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", saxh);
      reader.setProperty("http://xml.org/sax/properties/declaration-handler", saxh);
      reader.setErrorHandler(saxh);

      reader.parse(is);
    } catch(final SAXParseException | ValidationException ex) {
      final SAXParseException spex =
          (SAXParseException) (ex instanceof ValidationException ? ex.getCause() : ex);
      final String msg = Util.info(SCANPOS_X_X, source.path(), spex.getLineNumber(),
          spex.getColumnNumber()) + COLS + Util.message(spex);
      throw new IOException(msg, ex);
    } catch(final TrustedViolationException ex) {
      throw ex.wrap();
    } catch(final JobException ex) {
      throw ex;
    } catch(final Exception ex) {
      // invalid document encoding, catalog raises an error, external resource cannot be opened...
      final String msg = ex.getCause() != null ? ex.getCause().getMessage() : Util.message(ex);
      final String path = saxh != null && saxh.resource != null ? saxh.resource : source.path();
      throw new IOException(path + ": " + msg, ex);
    } finally {
      try(Reader r = is.getCharacterStream()) { /* no action */ }
      try(InputStream ist = is.getByteStream()) { /* no action */ }
    }
  }

  /**
   * Returns the input source of a resource that is mapped by a catalog.
   * @param er entity resolver (can be {@code null})
   * @param pubId public ID (can be {@code null})
   * @param sysId system ID (can be {@code null})
   * @return input source, or {@code null} if the resource is not mapped
   * @throws SAXException SAX exception
   * @throws IOException I/O exception
   */
  private static InputSource mapped(final EntityResolver er, final String pubId,
      final String sysId) throws SAXException, IOException {
    if(er == null) return null;
    final InputSource is;
    try {
      is = er.resolveEntity(pubId, sysId);
    } catch(final CatalogException ex) {
      Util.debug(ex);
      return null;
    }
    // resolvers may return the original resource if no mapping exists
    final String id = is != null ? is.getSystemId() : null;
    return id != null && !id.equals(sysId) ? is : null;
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
