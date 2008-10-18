package org.basex.build.xml;

import static org.basex.Text.*;
import java.io.IOException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.basex.BaseX;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.ProgressException;
import org.basex.io.IO;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * This class parses an XML document with Java's default SAX parser.
 * Note that large file cannot be parsed with the default parser due to
 * entity handling (e.g. the DBLP data).
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class SAXWrapper extends Parser {
  /** Optional XML reader. */
  SAXSource source;
  /** Parser reference. */
  SAX2BaseX parse;

  /**
   * Constructor.
   * @param s sax source
   */
  public SAXWrapper(final SAXSource s) {
    super(IO.get(s.getSystemId()));
    source = s;
  }

  @Override
  public void parse(final Builder build) throws IOException {
    try {
      XMLReader r = source != null ? source.getXMLReader() : null;
      if(r == null) {
        final SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        f.setValidating(false);
        r = f.newSAXParser().getXMLReader();
      }
      parse = new SAX2BaseX(build, io.name());
      r.setDTDHandler(parse);
      r.setContentHandler(parse);
      r.setProperty("http://xml.org/sax/properties/lexical-handler", parse);
      r.setErrorHandler(parse);

      final InputSource is = source.getInputSource();
      if(is != null) r.parse(is);
      else r.parse(source.getSystemId());

    } catch(final SAXParseException ex) {
      final String msg = BaseX.info(SCANPOS, ex.getSystemId(),
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

  @Override
  public String head() {
    return PROGCREATE;
  }

  @Override
  public String det() {
    return BaseX.info(NODESPARSED, io.name(), parse != null ? parse.nodes : 0);
  }

  @Override
  public double percent() {
    return (parse != null ? parse.nodes : 0 / 1000000d) % 1;
  }
}
