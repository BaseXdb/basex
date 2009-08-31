package org.basex.build.fs.parser;

import java.io.IOException;

import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.Metadata.MetaType;
import org.basex.build.fs.util.Metadata.MimeType;

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
  private final ExifParser exifParser;

  /** Standard constructor. */
  public TIFFParser() {
    super(MetaType.PICTURE, MimeType.TIFF);
    exifParser = new ExifParser();
  }

  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    return exifParser.check(f);
  }

  @Override
  protected void meta(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    exifParser.parse(f, parser);
  }

  @Override
  protected void content(final BufferedFileChannel bfc, //
      final NewFSParser parser) {
  // no content to read...
  }

  @Override
  protected boolean metaAndContent(final BufferedFileChannel bfc,
      final NewFSParser parser) throws IOException {
    meta(bfc, parser);
    return true;
  }
}
