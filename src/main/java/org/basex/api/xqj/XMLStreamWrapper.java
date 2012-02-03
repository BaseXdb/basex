package org.basex.api.xqj;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.util.Atts;
import org.basex.util.Util;

/**
 * This class parses an XML document via a conventional SAX parser.
 * Would be the easiest solution, but some large file cannot be parsed
 * with the default parser.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class XMLStreamWrapper extends Parser {
  /** Element counter. */
  private int nodes;
  /** XML stream reader. */
  private final XMLStreamReader reader;

  /**
   * Constructor.
   * @param sr stream reader
   */
  XMLStreamWrapper(final XMLStreamReader sr) {
    super("");
    reader = sr;
  }

  @Override
  public void parse(final Builder builder) throws IOException {
    try {
      builder.startDoc(token(src.name()));
      while(reader.hasNext()) {
        final int kind = reader.next();
        switch(kind) {
          case XMLStreamConstants.START_ELEMENT:
            final int as = reader.getAttributeCount();
            final Atts att = new Atts();
            for(int a = 0; a < as; ++a) {
              att.add(token(reader.getAttributeLocalName(a)),
                  token(reader.getAttributeValue(a)));
            }
            builder.startElem(token(reader.getLocalName()), att);
            ++nodes;
            break;
          case XMLStreamConstants.END_ELEMENT:
            builder.endElem();
            break;
          case XMLStreamConstants.CHARACTERS:
            builder.text(token(reader.getText()));
            ++nodes;
            break;
          case XMLStreamConstants.PROCESSING_INSTRUCTION:
            builder.pi(token(reader.getPITarget() + ' ' + reader.getPIData()));
            ++nodes;
            break;
          case XMLStreamConstants.COMMENT:
            builder.comment(token(reader.getText()));
            ++nodes;
            break;
          case XMLStreamConstants.END_DOCUMENT:
            break;
          default:
            throw new IOException("Unknown node kind " + kind);
        }
      }
      builder.endDoc();
    } catch(final IOException ex) {
      Util.stack(ex);
      throw ex;
    } catch(final XMLStreamException ex) {
      final IOException ioe = new IOException(ex.getMessage());
      ioe.setStackTrace(ex.getStackTrace());
      throw ioe;
    }
  }

  @Override
  public String det() {
    return Util.info(NODES_PARSED_X, src.name(), nodes);
  }
}
