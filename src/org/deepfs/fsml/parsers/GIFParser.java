package org.deepfs.fsml.parsers;

import static org.basex.util.Token.*;

import java.io.EOFException;
import java.io.IOException;

import org.basex.core.Main;
import org.deepfs.fsml.util.BufferedFileChannel;
import org.deepfs.fsml.util.DeepFile;
import org.deepfs.fsml.util.FileType;
import org.deepfs.fsml.util.MetaElem;
import org.deepfs.fsml.util.MimeType;
import org.deepfs.fsml.util.ParserRegistry;

/**
 * Parser for GIF files.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Bastian Lemke
 */
public final class GIFParser implements IFileParser {

  /** GIF87a header info. */
  private static final byte[] HEADERGIF87 = token("GIF87a");
  /** GIF89a header info. */
  private static final byte[] HEADERGIF89 = token("GIF89a");

  static {
    ParserRegistry.register("gif", GIFParser.class);
  }

  /**
   * Checks if the gif header is valid.
   * @param f the file channel to read from
   * @return true if the header is valid, false otherwise.
   * @throws IOException if any error occurs while reading from the channel.
   */
  public boolean check(final BufferedFileChannel f) throws IOException {
    final int len = HEADERGIF87.length;
    if(f.size() < len) return false;
    final byte[] header = f.get(new byte[len]);
    return eq(header, HEADERGIF87) || eq(header, HEADERGIF89);
  }

  @Override
  public void extract(final DeepFile deepFile) throws IOException {
    final BufferedFileChannel f = deepFile.getBufferedFileChannel();
    if(deepFile.extractMeta()) {
      try {
        f.buffer(10);
      } catch(final EOFException e) {
        return;
      }
      if(!check(f)) return;

      deepFile.setFileType(FileType.PICTURE);
      deepFile.setFileFormat(MimeType.GIF);

      // extract image dimensions
      deepFile.addMeta(MetaElem.PIXEL_WIDTH, f.get() + (f.get() << 8));
      deepFile.addMeta(MetaElem.PIXEL_HEIGHT, f.get() + (f.get() << 8));
    }
  }

  @Override
  public void propagate(final DeepFile deepFile) {
    Main.notimplemented();
  }
}
