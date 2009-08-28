package org.basex.build.fs.parser;

import static org.basex.util.Token.*;

import java.io.EOFException;
import java.io.IOException;

import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.Metadata;
import org.basex.build.fs.util.Metadata.IntField;
import org.basex.build.fs.util.Metadata.MetaType;
import org.basex.build.fs.util.Metadata.MimeType;

/**
 * Parser for BMP files.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Bastian Lemke
 */
public final class BMPParser extends AbstractParser {

  /** BMP header info. */
  byte[] HEADERBMP = token("BM");

  static {
    NewFSParser.register("bmp", BMPParser.class);
  }

  /** Standard constructor. */
  public BMPParser() {
    super(MetaType.PICTURE, MimeType.BMP);
  }

  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    if(f.size() < 2) return false;
    final byte[] header = f.get(new byte[2]);
    return eq(header, HEADERBMP);
  }

  @Override
  protected void meta(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    if(!check(f)) return;
    f.skip(16);
    try {
      f.buffer(8);
    } catch(final EOFException e) {
      return;
    }

    // extract image dimensions
    final Metadata meta = new Metadata();
    final int w = f.get() + (f.get() << 8) + (f.get() << 16) + (f.get() << 24);
    final int h = f.get() + (f.get() << 8) + (f.get() << 16) + (f.get() << 24);
    parser.metaEvent(meta.setInt(IntField.PIXEL_WIDTH, w));
    parser.metaEvent(meta.setInt(IntField.PIXEL_HEIGHT, h));
  }

  @Override
  protected void content(final BufferedFileChannel bfc, final NewFSParser parser) {
  // no content to read...
  }

  @Override
  protected boolean metaAndContent(final BufferedFileChannel bfc, final NewFSParser parser)
      throws IOException {
    meta(bfc, parser);
    return true;
  }
}
