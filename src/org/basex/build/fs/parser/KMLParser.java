package org.basex.build.fs.parser;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

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

  /** Supported file suffixes. */
  private static final Set<String> SUFFIXES = new HashSet<String>();

  static {
    SUFFIXES.add("kml");
    for(final String s : SUFFIXES) {
      REGISTRY.put(s, KMLParser.class);
    }
  }

  /** Standard constructor. */
  public KMLParser() {
    super(SUFFIXES, Type.XML, MimeType.KML);
  }

  /** {@inheritDoc} */
  @Override
  boolean check(final FileChannel f, final long limit) {
    // TODO Auto-generated method stub
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void readContent(final FileChannel fc, final long limit,
      final NewFSParser fsParser) throws IOException {
    if(fc.position() != 0 || limit != fc.size()) BaseX.notimplemented("Parsing "
        + "framents of kml files is currently not supported");
    fsParser.parseXML();
  }

  /** {@inheritDoc} */
  @Override
  public void readMeta(final FileChannel fc, final long limit,
      final NewFSParser fsParser) {
  // no metadata to read...
  }
}
