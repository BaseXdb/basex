package org.basex.build.fs.parser;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.build.fs.parser.Metadata.MimeType;
import org.basex.build.fs.parser.Metadata.Type;
import org.basex.io.IO;

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
  void readContent(final FileChannel f, final long limit) throws IOException {
    Builder builder = getBuilder();
    final IO io = IO.get(file.getPath());
    final Parser parser = Parser.getXMLParser(io);
    parser.doc = false;
    parser.parse(builder);
  }

  /** {@inheritDoc} */
  @Override
  void readMeta(final FileChannel f, final long limit) {
  // TODO Auto-generated method stub
  }

}
