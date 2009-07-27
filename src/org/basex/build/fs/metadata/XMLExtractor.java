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
  public void extract(final Builder build, final File f) throws IOException {
    build.startElem(XML, atts.reset());
    final IO io = IO.get(f.getPath());
    final Parser parser = Parser.xmlParser(io, build.parser.prop);
    parser.doc = false;
    parser.parse(build);
    build.endElem(XML);
  }
}
