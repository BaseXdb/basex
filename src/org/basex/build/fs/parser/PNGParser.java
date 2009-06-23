package org.basex.build.fs.parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

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

  /** Supported file suffixes. */
  private static final Set<String> SUFFIXES = new HashSet<String>();

  static {
    SUFFIXES.add("png");
    for(final String s : SUFFIXES) {
      REGISTRY.put(s, PNGParser.class);
    }
  }

  /** Standard constructor. */
  public PNGParser() {
    super(SUFFIXES, Type.IMAGE, MimeType.PNG);
  }

  /** PNG header. */
  private static final byte[] HEADER = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D,
      0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52};
  /** Lenght of the PNG header. */
  private static final int HEADER_LENGTH = HEADER.length + 8;

  /** Buffer for the file content. */
  private ByteBuffer buf;

  /** {@inheritDoc} */
  @Override
  boolean check(final FileChannel f, final long limit) throws IOException {
    if(limit < HEADER_LENGTH) return false;
    byte[] h = new byte[HEADER_LENGTH];
    buf = ByteBuffer.wrap(h);
    f.read(buf);
    int len = HEADER.length;
    for(int i = 0; i < len; i++) {
      if(h[i] != HEADER[i]) return false;
    }
    buf.position(len);
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void readContent(final FileChannel fc, final long limit,
      final NewFSParser fsParser) {
  // no textual representation for png content ...
  }

  /** {@inheritDoc} */
  @Override
  public void readMeta(final FileChannel fc, final long limit,
      final NewFSParser fsParser) throws IOException {
    if(!check(fc, limit)) return;
    byte[] width = Token.token(buf.getInt());
    byte[] height = Token.token(buf.getInt());
    fsParser.metaEvent(Element.WIDTH, DataType.INTEGER, Definition.PIXEL, null,
        width);
    fsParser.metaEvent(Element.HEIGHT, DataType.INTEGER, Definition.PIXEL,
        null, height);
  }
}
