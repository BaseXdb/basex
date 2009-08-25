package org.basex.build.fs.parser;

import java.io.IOException;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.Metadata.MetaType;
import org.basex.build.fs.util.Metadata.MimeType;
import org.basex.core.Prop;

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
    super(MetaType.XML, MimeType.XML.get());
  }

  @Override
  public boolean check(final BufferedFileChannel f) {
    // [BL] check if the document is well-formed
    return true;
  }

  @Override
  public void readContent(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    final long size = f.size();
    // parsing of xml fragments inside file is not supported
    if(size > parser.prop.num(Prop.FSTEXTMAX) || f.isSubChannel()) {
      parser.parseWithFallbackParser(f, true);
      return;
    }
    parser.startXMLContent(f.absolutePosition(), f.size());
    parser.parseXML();
    parser.endXMLContent();
  }

  @Override
  public void meta(final BufferedFileChannel bfc, final NewFSParser parser) {
  // no metadata to read...
  }
}
