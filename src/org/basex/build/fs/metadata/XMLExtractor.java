package org.basex.build.fs.metadata;

import static org.basex.build.fs.FSText.*;
import java.io.File;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.io.IO;

/**
 * BMP meta data extractor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XMLExtractor extends AbstractExtractor {
  @Override
  public void extract(final Builder listener, final File f) throws IOException {
    listener.startElem(XML, atts.reset());
    final IO io = IO.get(f.getPath());
    final Parser parser = Parser.getXMLParser(io);
    parser.doc = false;
    parser.parse(listener);
    listener.endElem(XML);
  }
}
