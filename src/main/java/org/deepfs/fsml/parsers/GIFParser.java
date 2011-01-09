package org.deepfs.fsml.parsers;

import static org.basex.util.Token.*;
import java.io.EOFException;
import java.io.IOException;
import org.basex.util.Util;
import org.deepfs.fsml.BufferedFileChannel;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.FileType;
import org.deepfs.fsml.MetaElem;
import org.deepfs.fsml.MimeType;
import org.deepfs.fsml.ParserRegistry;

/**
 * Parser for GIF files.
 *
 * @author BaseX Team 2005-11, ISC License
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

  @Override
  public boolean check(final DeepFile deepFile) throws IOException {
    final int len = HEADERGIF87.length;
    final BufferedFileChannel f = deepFile.getBufferedFileChannel();
    if(f.size() < len) return false;
    try { f.buffer(10); } catch(final EOFException ex) { return false; }
    final byte[] header = f.get(new byte[len]);
    return eq(header, HEADERGIF87) || eq(header, HEADERGIF89);
  }

  @Override
  public void extract(final DeepFile deepFile) throws IOException {
    final BufferedFileChannel f = deepFile.getBufferedFileChannel();
    if(deepFile.extractMeta()) {
      if(!check(deepFile)) return;

      deepFile.setFileType(FileType.PICTURE);
      deepFile.setFileFormat(MimeType.GIF);

      // extract image dimensions
      deepFile.addMeta(MetaElem.PIXEL_WIDTH, f.get() + (f.get() << 8));
      deepFile.addMeta(MetaElem.PIXEL_HEIGHT, f.get() + (f.get() << 8));
    }
  }

  @Override
  public void propagate(final DeepFile deepFile) {
    Util.notimplemented();
  }
}
