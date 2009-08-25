package org.basex.build.fs.parser;

import java.io.IOException;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.parser.Metadata.IntField;
import org.basex.build.fs.parser.Metadata.MimeType;
import org.basex.build.fs.parser.Metadata.MetaType;
import static org.basex.util.Token.*;

/**
 * Parser for PNG files.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class PNGParser extends AbstractParser {

  static {
    NewFSParser.register("png", PNGParser.class);
  }

  /** Standard constructor. */
  public PNGParser() {
    super(MetaType.PICTURE, MimeType.PNG.get());
  }

  /** PNG header. */
  private static final byte[] HEADER = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D,
      0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52};
  /** Length of the PNG header. */
  private static final int HEADER_LENGTH = HEADER.length + 8;

  @Override
  public boolean check(final BufferedFileChannel bfc) throws IOException {
    return bfc.size() >= HEADER_LENGTH
        && eq(bfc.get(new byte[HEADER.length]), HEADER);
  }

  @Override
  public void readContent(final BufferedFileChannel bfc,
      final NewFSParser parser) {
  // no textual representation for png content ...
  }

  @Override
  public void meta(final BufferedFileChannel bfc, final NewFSParser parser)
      throws IOException {
    if(!check(bfc)) return;
    final Metadata meta = new Metadata();
    parser.metaEvent(meta.setInt(IntField.PIXEL_WIDTH, bfc.getInt()));
    parser.metaEvent(meta.setInt(IntField.PIXEL_HEIGHT, bfc.getInt()));
  }
}
