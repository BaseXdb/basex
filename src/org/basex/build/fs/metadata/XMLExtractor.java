package org.basex.build.fs.metadata;

import static org.basex.build.fs.FSText.*;
import java.io.File;
import java.io.IOException;
import org.basex.build.Builder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.io.IO;

/**
 * XML extractor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XMLExtractor extends AbstractExtractor {
  @Override
  public void extract(final Builder build, final File f) throws IOException {
    // skip too large files
    if(f.length() > build.parser.prop.num(Prop.FSTEXTMAX)) return;

    final IO io = IO.get(f.getPath());
    try {
      // parse file in main memory first
      final Parser p = Parser.xmlParser(io, build.parser.prop);
      new MemBuilder(p).build();
    } catch(final IOException ex) {
      // XML parsing exception...
      return;
    }
    build.startElem(XML, atts.reset());
    final Parser parser = Parser.xmlParser(io, build.parser.prop);
    parser.doc = false;
    parser.parse(build);
    build.endElem(XML);
  }
}
