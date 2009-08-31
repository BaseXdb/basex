package org.basex.build.fs.parser;

import java.io.IOException;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.Metadata.MetaType;
import org.basex.build.fs.util.Metadata.MimeType;
import org.basex.core.Prop;

/**
 * Parser for KML files.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class KMLParser extends AbstractParser {

  static {
    NewFSParser.register("kml", KMLParser.class);
  }

  /** Standard constructor. */
  public KMLParser() {
    super(MetaType.XML, MimeType.KML);
  }

  @Override
  public boolean check(final BufferedFileChannel f) {
    // [BL] check if the document is well-formed
    return true;
  }

  @Override
  protected void content(final BufferedFileChannel f, final NewFSParser parser)
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
  protected void meta(final BufferedFileChannel bfc, final NewFSParser parser) {
  // no metadata to read...
  }

  @Override
  protected boolean metaAndContent(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException {
    content(bfc, parser);
    return true;
  }
}
