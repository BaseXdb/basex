package org.basex.build.xml;

import static org.basex.core.Text.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.basex.build.SingleParser;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.IOFile;
import org.basex.util.Util;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * This class parses an XML document with Java's internal SAX parser. Note that
 * not all files cannot be parsed with the default parser; for example, the
 * DBLP documents contain too many entities and cause an out of memory error.
 * The internal {@link XMLParser} can be used as alternative.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class SAXWrapper extends SingleParser {
  /** External DTD parsing. */
  private static final String EXTDTD =
    "http://apache.org/xml/features/nonvalidating/load-external-dtd";
  /** Lexical handler. */
  private static final String LEXHANDLER =
    "http://xml.org/sax/properties/lexical-handler";

  /** File counter. */
  long counter;
  /** Current line. */
  int line = 1;

  /** SAX handler reference. */
  private SAXHandler saxh;
  /** Optional XML reader. */
  private final SAXSource saxs;
  /** File length. */
  private long length;
  /** Properties. */
  private final Prop prop;

  /**
   * Constructor.
   * @param source sax source
   * @param pr Properties
   */
  public SAXWrapper(final SAXSource source, final Prop pr) {
    this(source, "", "", pr);
  }

  /**
   * Constructor.
   * @param source sax source
   * @param name name
   * @param path target path
   * @param pr Properties
   */
  public SAXWrapper(final SAXSource source, final String name,
      final String path, final Prop pr) {

    super(IO.get(source.getSystemId()), path);
    if(!name.isEmpty()) src.name(name);
    saxs = source;
    prop = pr;
  }

  /**
   * Constructor.
   * @param source sax source
   * @param path target path
   * @param pr Properties
   */
  public SAXWrapper(final IO source, final String path, final Prop pr) {
    super(source, path);
    saxs = new SAXSource(source.inputSource());
    prop = pr;
  }

  @Override
  public void parse() throws IOException {
    final InputSource is = wrap(saxs.getInputSource());
    final String in = saxs.getSystemId() == null ? DOTS : saxs.getSystemId();

    try {
      XMLReader r = saxs.getXMLReader();
      if(r == null) {
        final SAXParserFactory f = SAXParserFactory.newInstance();
        f.setFeature(EXTDTD, prop.is(Prop.DTD));
        f.setFeature("http://xml.org/sax/features/use-entity-resolver2", false);
        f.setNamespaceAware(true);
        f.setValidating(false);
        f.setXIncludeAware(true);
        r = f.newSAXParser().getXMLReader();
      }

      saxh = new SAXHandler(builder);
      final String cat = prop.get(Prop.CATFILE);
      if(!cat.isEmpty()) CatalogWrapper.set(r, cat);

      r.setDTDHandler(saxh);
      r.setContentHandler(saxh);
      r.setProperty(LEXHANDLER, saxh);
      r.setErrorHandler(saxh);

      if(is != null) r.parse(is);
      else r.parse(saxs.getSystemId());
    } catch(final SAXParseException ex) {
      final String msg = Util.info(SCANPOS_X_X, in, ex.getLineNumber(),
          ex.getColumnNumber()) + COLS + ex.getMessage();
      final IOException ioe = new IOException(msg);
      ioe.setStackTrace(ex.getStackTrace());
      throw ioe;
    } catch(final ProgressException ex) {
      throw ex;
    } catch(final Exception ex) {
      // occurs, e.g. if document encoding is invalid:
      // prefix message with source id
      String msg = ex.getMessage();
      if(in != null) msg = '"' + in + '"' + COLS + msg;
      // wrap and return original message
      final IOException ioe = new IOException(msg);
      ioe.setStackTrace(ex.getStackTrace());
      throw ioe;
    } finally {
      if(is == null) return;
      try {
        final Reader r = is.getCharacterStream();
        if(r != null) r.close();
        final InputStream ist = is.getByteStream();
        if(ist != null) ist.close();
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    }
  }

  /**
   * Wraps the input source with a stream which counts the number of read bytes
   * and parsed lines.
   * @param is input source
   * @return resulting stream
   * @throws IOException I/O exception
   */
  private InputSource wrap(final InputSource is) throws IOException {
    if(is == null) return is;

    // choose input stream
    final InputStream in;
    if(is.getByteStream() != null) {
      in = is.getByteStream();
    } else if(is.getSystemId() == null || is.getSystemId().isEmpty()) {
      return is;
    } else if(src instanceof IOFile) {
      in = new FileInputStream(src.path());
    } else if(src instanceof IOContent) {
      in = new ByteArrayInputStream(src.read());
    } else {
      return is;
    }
    // retrieve/estimate number of bytes to be read
    length = src.length();
    if(length <= 0) length = in.available();

    // create wrapper
    final InputSource tmp = new InputSource(new InputStream() {
      final InputStream buffer = in instanceof ByteArrayInputStream ? in :
        new BufferedInputStream(in);

      @Override
      public int read() throws IOException {
        final int i = buffer.read();
        if(i == '\n') ++line;
        ++counter;
        return i;
      }

      @Override
      public void close() throws IOException {
        buffer.close();
      }
    });

    saxs.setInputSource(tmp);
    saxs.setSystemId(is.getSystemId());
    return tmp;
  }

  @Override
  public String det() {
    return length == 0 ? super.det() : Util.info(SCANPOS_X_X, src.name(), line);
  }

  @Override
  public double prog() {
    return length == 0 ? saxh == null ? 0 : saxh.nodes / 3000000d % 1
        : (double) counter / length;
  }
}
