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
 * Parser for XML files.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public class XMLParser extends AbstractParser {

  /** Supported file suffixes. */
  private static final Set<String> SUFFIXES = new HashSet<String>();

  static {
    SUFFIXES.add("xml");
    for(final String s : SUFFIXES) {
      REGISTRY.put(s, XMLParser.class);
    }
  }

  /** Standard constructor. */
  public XMLParser() {
    super(SUFFIXES, Type.XML, MimeType.XML);
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
        + "framents of xml files is currently not supported");
    fsParser.parseXML();
  }

  /** {@inheritDoc} */
  @Override
  public void readMeta(final FileChannel fc, final long limit,
      final NewFSParser fsParser) {
  // no metadata to read...
  }
}
