package org.basex.build.fs.parser;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.parser.Metadata.MimeType;
import org.basex.build.fs.parser.Metadata.Type;

/**
 * Parser for KML files.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public class KMLParser extends AbstractParser {

  static {
    NewFSParser.register("kml", KMLParser.class);
  }

  /** Standard constructor. */
  public KMLParser() {
    super(Type.XML, MimeType.KML);
  }

  /** {@inheritDoc} */
  @Override
  public boolean check(final BufferedFileChannel f) {
    // [BL] Auto-generated method stub
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void readContent(final BufferedFileChannel bfc,
      final NewFSParser fsParser) throws IOException {
    if(bfc.isSubChannel()) BaseX.notimplemented("Parsing framents of kml files "
        + "is currently not supported");
    fsParser.parseXML();
  }

  /** {@inheritDoc} */
  @Override
  public void readMeta(final BufferedFileChannel bfc, //
      final NewFSParser fsParser) {
  // no metadata to read...
  }
}
