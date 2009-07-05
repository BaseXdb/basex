package org.basex.build.fs.parser;

import java.io.IOException;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.parser.Metadata.DataType;
import org.basex.build.fs.parser.Metadata.Definition;
import org.basex.build.fs.parser.Metadata.Element;
import org.basex.build.fs.parser.Metadata.MimeType;
import org.basex.build.fs.parser.Metadata.Type;
import org.basex.util.Token;

/**
 * Parser for PNG files.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public class PNGParser extends AbstractParser {

  static {
    NewFSParser.register("png", PNGParser.class);
  }

  /** Standard constructor. */
  public PNGParser() {
    super(Type.IMAGE, MimeType.PNG);
  }

  /** PNG header. */
  private static final byte[] HEADER = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D,
      0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52};
  /** Lenght of the PNG header. */
  private static final int HEADER_LENGTH = HEADER.length + 8;

  /** {@inheritDoc} */
  @Override
  public boolean check(final BufferedFileChannel bfc) throws IOException {
    if(bfc.size() < HEADER_LENGTH) return false;
    int len = HEADER.length;
    byte[] h = new byte[len];
    bfc.get(h);
    for(int i = 0; i < len; i++) {
      if(h[i] != HEADER[i]) return false;
    }
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void readContent(final BufferedFileChannel bfc,
      final NewFSParser fsParser) {
  // no textual representation for png content ...
  }

  /** {@inheritDoc} */
  @Override
  public void readMeta(final BufferedFileChannel bfc, //
      final NewFSParser fsParser) throws IOException {
    if(!check(bfc)) return;
    fsParser.metaEvent(Element.WIDTH, DataType.INTEGER, Definition.PIXEL, null,
        Token.token(bfc.getInt()));
    fsParser.metaEvent(Element.HEIGHT, DataType.INTEGER, Definition.PIXEL,
        null, Token.token(bfc.getInt()));
  }
}
