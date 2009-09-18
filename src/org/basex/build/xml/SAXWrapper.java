package org.basex.build.xml;

import static org.basex.core.Text.*;

import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.Main;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * This class parses an XML document with Java's default SAX parser.
 * Note that large file cannot be parsed with the default parser due to
 * entity handling (e.g. the DBLP data).
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class SAXWrapper extends Parser {
  /** SAX Handler reference. */
  private SAXHandler sax;
  /** Optional XML reader. */
  private final SAXSource source;
  /** File length. */
  private long length;
  /** File counter. */
  long counter;
  /** Current line. */
  int line = 1;

  /**
   * Constructor.
   * @param s sax source
   * @param pr database properties
   */
  public SAXWrapper(final SAXSource s, final Prop pr) {
    super(IO.get(s.getSystemId()), pr);
    source = s;
  }

  @Override
  public void parse(final Builder build) throws IOException {
    final InputSource is = wrap(source.getInputSource());
    try {
      XMLReader r = source.getXMLReader();
      if(r == null) {
        final SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        f.setValidating(false);
        r = f.newSAXParser().getXMLReader();
      }
      sax = new SAXHandler(build, io.name());
      sax.doc = doc;
      r.setDTDHandler(sax);
      r.setContentHandler(sax);
      r.setProperty("http://xml.org/sax/properties/lexical-handler", sax);
      r.setErrorHandler(sax);

      if(is != null) r.parse(is);
      else r.parse(source.getSystemId());
    } catch(final SAXParseException ex) {
      final String msg = Main.info(SCANPOS, ex.getSystemId(),
          ex.getLineNumber(), ex.getColumnNumber()) + ": " + ex.getMessage();
      final IOException ioe = new IOException(msg);
      ioe.setStackTrace(ex.getStackTrace());
      throw ioe;
    } catch(final ProgressException ex) {
      throw ex;
    } catch(final Exception ex) {
      final IOException ioe = new IOException(ex.getMessage());
      ioe.setStackTrace(ex.getStackTrace());
      throw ioe;
    }
  }

  /**
   * Wraps the input stream to track the number of read bytes.
   * @param is input stream
   * @return resulting stream
   * @throws IOException I/O exception
   */
  private InputSource wrap(final InputSource is) throws IOException {
    if(is == null) return null;
    final String id = is.getSystemId();
    if(!Prop.gui || is.getByteStream() != null || id == null ||
        id.length() == 0) return is;

    length = IO.get(id).length();
    final FileInputStream fis = new FileInputStream(io.path()) {
      @Override
      public int read(final byte[] b, final int off, final int len)
          throws IOException {
        final int i = super.read(b, off, len);
        for(int o = off; o < len; o++) if(b[off + o] == '\n') line++;
        counter += i;
        return i;
      }
    };
    final InputSource input = new InputSource(fis);
    source.setInputSource(input);
    return input;
  }

  @Override
  public String tit() {
    return PROGCREATE;
  }

  @Override
  public String det() {
    return length == 0 ? super.det() : Main.info(SCANPOS, io.name(), line);
  }

  @Override
  public double prog() {
    return length == 0 ? sax == null ? 0 : sax.nodes / 1000000d % 1 :
      (double) counter / length;
  }
}
