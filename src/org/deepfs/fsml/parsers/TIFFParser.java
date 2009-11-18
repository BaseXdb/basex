package org.deepfs.fsml.parsers;

import java.io.IOException;

import org.basex.core.Main;
import org.deepfs.fsml.util.BufferedFileChannel;
import org.deepfs.fsml.util.DeepFile;
import org.deepfs.fsml.util.FileType;
import org.deepfs.fsml.util.MimeType;
import org.deepfs.fsml.util.ParserRegistry;

/**
 * Parser for TIF files.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Bastian Lemke
 */
public final class TIFFParser implements IFileParser {

  static {
    ParserRegistry.register("tiff", TIFFParser.class);
    ParserRegistry.register("tif", TIFFParser.class);
  }

  /** Parser for Exif data. */
  private final ExifParser exifParser = new ExifParser();

  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    return exifParser.check(f);
  }

  @Override
  public void extract(final DeepFile deepFile) throws IOException {
    if(deepFile.extractMeta()) {
      final BufferedFileChannel bfc = deepFile.getBufferedFileChannel();
      if(!check(bfc)) return;
      deepFile.setFileType(FileType.PICTURE);
      deepFile.setFileFormat(MimeType.TIFF);
      exifParser.extract(deepFile);
    }
  }

  @Override
  public void propagate(final DeepFile deepFile) {
    Main.notimplemented();
  }
}
