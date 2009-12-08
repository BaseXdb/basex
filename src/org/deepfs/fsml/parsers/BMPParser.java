package org.deepfs.fsml.parsers;

import static org.basex.util.Token.*;
import java.io.EOFException;
import java.io.IOException;
import org.basex.core.Main;
import org.deepfs.fsml.BufferedFileChannel;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.FileType;
import org.deepfs.fsml.MetaElem;
import org.deepfs.fsml.MimeType;
import org.deepfs.fsml.ParserRegistry;

/**
 * Parser for BMP files.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Bastian Lemke
 */
public class BMPParser implements IFileParser {

  /** BMP header info. */
  private static final byte[] HEADERBMP = token("BM");

  static {
    ParserRegistry.register("bmp", BMPParser.class);
  }

  /**
   * Checks the header bytes.
   * @param f the {@link BufferedFileChannel} to read from
   * @return true if the header is valid
   * @throws IOException if any error occurs while reading from the channel
   */
  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    if(f.size() < 2) return false;
    final byte[] header = f.get(new byte[2]);
    return eq(header, HEADERBMP);
  }

  @Override
  public void extract(final DeepFile deepFile) throws IOException {
    if(!deepFile.extractMeta()) return; // no content to extract

    final BufferedFileChannel f = deepFile.getBufferedFileChannel();
    if(!check(f)) return;

    f.skip(16);
    try {
      f.buffer(8);
    } catch(final EOFException e) {
      return;
    }

    deepFile.setFileType(FileType.PICTURE);
    deepFile.setFileFormat(MimeType.BMP);

    // extract image dimensions
    final int w = f.get() + (f.get() << 8) + (f.get() << 16) + (f.get() << 24);
    final int h = f.get() + (f.get() << 8) + (f.get() << 16) + (f.get() << 24);
    deepFile.addMeta(MetaElem.PIXEL_WIDTH, w);
    deepFile.addMeta(MetaElem.PIXEL_HEIGHT, h);
  }

  @Override
  public void propagate(final DeepFile deepFile) {
    Main.notimplemented();
  }
}
