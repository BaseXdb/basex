package org.basex.build.fs.parser;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.parser.Metadata.MimeType;
import org.basex.build.fs.parser.Metadata.Type;

/**
 * Parser for XML files.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class XMLParser extends AbstractParser {

  static {
    NewFSParser.register("xml", XMLParser.class);
  }

  /** Standard constructor. */
  public XMLParser() {
    super(Type.XML, MimeType.XML);
  }

  @Override
  public boolean check(final BufferedFileChannel f) {
    return true;
  }

  @Override
  public void readContent(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {

    if(f.isSubChannel()) BaseX.notimplemented("Parsing framents of xml files "
        + "is currently not supported");
    parser.parseXML();
  }

  @Override
  public void readMeta(final BufferedFileChannel bfc,
      final NewFSParser parser) {
    // no metadata to read...
  }
}
