package org.deepfs.fsml.parsers;

import java.io.IOException;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.MetaStore.MetaType;
import org.basex.build.fs.util.MetaStore.MimeType;

/**
 * Parser for TIF files.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Bastian Lemke
 */
public final class TIFFParser extends AbstractParser {

  static {
    NewFSParser.register("tiff", TIFFParser.class);
    NewFSParser.register("tif", TIFFParser.class);
  }

  /** Parser for Exif data. */
  private final ExifParser exifParser = new ExifParser(meta);

  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    return exifParser.check(f);
  }

  @Override
  protected void meta(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    if(!check(f)) return;
    meta.setType(MetaType.PICTURE);
    meta.setFormat(MimeType.TIFF);
    exifParser.parse(f, parser);
  }

  @Override
  protected void content(final BufferedFileChannel bfc, //
      final NewFSParser parser) {
  // no content to read...
  }

  @Override
  protected boolean metaAndContent(final BufferedFileChannel bfc,
      final NewFSParser parser) {
    return false;
  }
}
