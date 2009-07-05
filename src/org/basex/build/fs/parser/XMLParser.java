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
public class XMLParser extends AbstractParser {

  static {
    NewFSParser.register("xml", XMLParser.class);
  }

  /** Standard constructor. */
  public XMLParser() {
    super(Type.XML, MimeType.XML);
  }

  /** {@inheritDoc} */
  @Override
  public boolean check(final BufferedFileChannel f) {
    // [BL] Auto-generated method stub
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void readContent(final BufferedFileChannel f,
      final NewFSParser fsParser) throws IOException {
    if(f.isSubChannel()) BaseX.notimplemented("Parsing framents of xml files "
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
