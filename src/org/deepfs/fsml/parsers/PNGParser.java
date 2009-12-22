package org.deepfs.fsml.parsers;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Main;
import org.deepfs.fsml.BufferedFileChannel;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.FileType;
import org.deepfs.fsml.MetaElem;
import org.deepfs.fsml.MimeType;
import org.deepfs.fsml.ParserRegistry;

/**
 * Parser for PNG files.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
public final class PNGParser implements IFileParser {

  static {
    ParserRegistry.register("png", PNGParser.class);
  }

  /** PNG header. */
  private static final byte[] HEADER = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D,
      0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52};
  /** Length of the PNG header. */
  private static final int HEADER_LENGTH = HEADER.length + 8;

  @Override
  public boolean check(final DeepFile df) throws IOException {
    final BufferedFileChannel bfc = df.getBufferedFileChannel();
    return bfc.size() >= HEADER_LENGTH
        && eq(bfc.get(new byte[HEADER.length]), HEADER);
  }

  @Override
  public void extract(final DeepFile deepFile) throws IOException {
    if(deepFile.extractMeta()) {
      if(!check(deepFile)) return;

      deepFile.setFileType(FileType.PICTURE);
      deepFile.setFileFormat(MimeType.PNG);

      final BufferedFileChannel bfc = deepFile.getBufferedFileChannel();
      deepFile.addMeta(MetaElem.PIXEL_WIDTH, bfc.getInt());
      deepFile.addMeta(MetaElem.PIXEL_HEIGHT, bfc.getInt());
    }
  }

  @Override
  public void propagate(final DeepFile deepFile) {
    Main.notimplemented();
  }
}
