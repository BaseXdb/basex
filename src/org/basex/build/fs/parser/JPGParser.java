package org.basex.build.fs.parser;

import java.io.EOFException;
import java.io.IOException;

import org.basex.BaseX;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.Metadata;
import org.basex.build.fs.util.Metadata.IntField;
import org.basex.build.fs.util.Metadata.MetaType;
import org.basex.build.fs.util.Metadata.MimeType;
import org.basex.build.fs.util.Metadata.StringField;

/**
 * Parser for JPG files.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Bastian Lemke
 */
public final class JPGParser extends AbstractParser {

  static {
    NewFSParser.register("jpg", JPGParser.class);
    NewFSParser.register("jpeg", JPGParser.class);
  }

  /** Exif header. Null terminated ASCII representation of 'Exif'. */
  private static final byte[] HEADER_EXIF = // 
  { 0x45, 0x78, 0x69, 0x66, 0x00, 0x00};
  /**
   * <p>
   * JFIF header.
   * </p>
   * <ul>
   * <li>5 bytes: ASCII representation of 'JFIF' (null terminated)</li>
   * <li>1 byte: major JFIF version (only v1 supported)</li>
   * </ul>
   */
  private static final byte[] HEADER_JFIF = //
  { 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01};
  /** Extended JFIF header. Null terminated ASCII representation of 'JFXX'. */
  private static final byte[] HEADER_JFXX = { 0x4A, 0x46, 0x58, 0x58, 0x00};
  /** Metadata item. */
  private final Metadata meta;
  /** Parser for Exif data. */
  private final ExifParser exifParser;
  /** The {@link NewFSParser} instance to fire events. */
  private NewFSParser fsparser;
  /** The {@link BufferedFileChannel} to read from. */
  private BufferedFileChannel bfc;

  /** Standard constructor. */
  public JPGParser() {
    super(MetaType.PICTURE, MimeType.JPG);
    meta = new Metadata();
    exifParser = new ExifParser();
  }

  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    try {
      f.buffer(6);
    } catch(final EOFException e) {
      return false;
    }
    int b;
    // 0xFFD8FFE0 or 0xFFD8FFE1
    return f.getShort() == 0xFFD8
        && ((b = f.getShort()) == 0xFFE0 || b == 0xFFE1);
  }

  @Override
  protected void meta(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    if(!check(f)) return;
    bfc = f;
    fsparser = parser;
    bfc.skip(-2);
    while(bfc.get() == 0xFF) {
      final int b = bfc.get(); // segment marker
      final int size = readSize();
      switch(b) {
        case 0xE0: // JFIF
          readJFIF(size);
          break;
        case 0xE1: // Exif
          readExif(size);
          break;
        case 0xFE: // Comment
          readComment(size);
          break;
        default:
          if(b >= 0xC0 && b <= 0xC3) {
            readDimensions();
            return;
          }
          bfc.skip(size);
      }
      bfc.buffer(4); // next segment marker and size value
    }
  }

  @Override
  protected void content(final BufferedFileChannel f, final NewFSParser p) {
  // no textual representation for jpg content ...
  }

  // ---------------------------------------------------------------------------

  @Override
  protected boolean metaAndContent(final BufferedFileChannel f,
      final NewFSParser parser) throws IOException {
    meta(f, parser);
    return true;
  }

  /**
   * Reads two bytes from the {@link BufferedFileChannel} and returns the value
   * as integer.
   * @return the size value.
   */
  private int readSize() {
    return bfc.getShort() - 2;
  }

  /**
   * <p>
   * Reads <code>a.length</code> bytes from the {@link BufferedFileChannel} and
   * checks if they are equals to the array content.
   * </p>
   * <p>
   * <b>Assure that at least <code>a.length</code> bytes are buffered, before
   * calling this method.</b>
   * <p>
   * @param a the array content to check.
   * @return true if the next bytes are equals to the array content, false
   *         otherwise.
   */
  private boolean checkNextBytes(final byte[] a) {
    final int len = a.length;
    final byte[] a2 = new byte[len];
    bfc.get(a2, 0, len);
    for(int i = 0; i < len; i++)
      if(a[i] != a2[i]) return false;
    return true;
  }

  /**
   * Reads image width and height.
   * @throws IOException if any error occurs while reading from the file
   *           channel.
   */
  private void readDimensions() throws IOException {
    bfc.buffer(9);
    if(bfc.get() == 8) {
      final int height = bfc.getShort();
      final int width = bfc.getShort();
      fsparser.metaEvent(meta.setInt(IntField.PIXEL_HEIGHT, height));
      fsparser.metaEvent(meta.setInt(IntField.PIXEL_WIDTH, width));
    } else {
      if(NewFSParser.VERBOSE) BaseX.debug(
          "JPGParser: Wrong data precision field (%).", bfc.getFileName());
    }
  }

  /**
   * Reads an Exif segment.
   * @param size the size of the segment.
   * @throws IOException if any error occurs while reading from the file.
   * @see <a href="http://www.Exif.org/Exif2-2.PDF">Exif 2.2</a>
   */
  private void readExif(final int size) throws IOException {
    final int len = HEADER_EXIF.length;
    bfc.buffer(len);
    if(!checkNextBytes(HEADER_EXIF)) bfc.skip(size - len);
    final BufferedFileChannel sub = bfc.subChannel(size - len);
    try {
      exifParser.parse(sub, fsparser);
    } finally {
      try {
        sub.finish();
      } catch(Exception ex) {
        BaseX.debug(ex);
      }
    }
  }

  /**
   * Reads a JFIF (JPEG File Interchange Format) segment.
   * @param size the size of the segment
   * @throws IOException if any error occurs while reading from the file.
   * @see <a href="http://www.w3.org/Graphics/JPEG/jfif3.pdf">JFIF 1.02 spec</a>
   */
  private void readJFIF(final int size) throws IOException {
    bfc.buffer(16); // standard JFIF segment has 16 bytes
    final long pos = bfc.position();
    if(!checkNextBytes(HEADER_JFIF)) {
      bfc.position(pos);
      readJFXX(size);
      return;
    }
    final int s = size - HEADER_JFIF.length;
    bfc.skip(s);
  }

  /**
   * Reads an extended JFIF segment.
   * @param size the size of the segment.
   * @throws IOException if any error occurs while reading from the file.
   */
  private void readJFXX(final int size) throws IOException {
    // method is only called from readJFIF() which cares about buffering
    final long pos = bfc.position();
    if(!checkNextBytes(HEADER_JFXX)) {
      bfc.position(pos + size);
      return;
    }
    int s = size - HEADER_JFXX.length - 1;
    int width = 0;
    int height = 0;
    switch(bfc.get()) { // extension code
      case 0x10: // Thumbnail coded using JPEG
        fsparser.startContent(bfc.absolutePosition(), s);
        fsparser.metaEvent(meta.setMetaType(MetaType.PICTURE));
        fsparser.metaEvent(meta.setMimeType(MimeType.JPG));
        fsparser.metaEvent(meta.setString(StringField.TITLE, "Thumbnail"));
        break;
      case 0x11: // Thumbnail coded using 1 byte/pixel
        width = bfc.get();
        height = bfc.get();
        s -= 2;
        fsparser.startContent(bfc.absolutePosition(), s);
        fsparser.metaEvent(meta.setMetaType(MetaType.PICTURE));
        fsparser.metaEvent(meta.setString(StringField.TITLE, "Thumbnail"));
        fsparser.metaEvent(meta.setString(StringField.DESCRIPTION,
            "Thumbnail coded using 1 byte/pixel."));
        break;
      case 0x13: // Thumbnail coded using 3 bytes/pixel
        width = bfc.get();
        height = bfc.get();
        s -= 2;
        fsparser.startContent(bfc.absolutePosition(), s);
        fsparser.metaEvent(meta.setMetaType(MetaType.PICTURE));
        fsparser.metaEvent(meta.setString(StringField.TITLE, "Thumbnail"));
        fsparser.metaEvent(meta.setString(StringField.DESCRIPTION,
            "Thumbnail coded using 3 bytes/pixel."));
        break;
      default:
        BaseX.debug("JPGParser: Illegal or unsupported JFIF header (%)",
            bfc.getFileName());
    }
    fsparser.metaEvent(meta.setInt(IntField.PIXEL_WIDTH, width));
    fsparser.metaEvent(meta.setInt(IntField.PIXEL_HEIGHT, height));
    fsparser.endContent();
    bfc.skip(s);
  }

  /**
   * Reads an comment segment.
   * @param size the size of the segment.
   * @throws IOException if any error occurs while reading from the file.
   */
  private void readComment(final int size) throws IOException {
    fsparser.metaEvent(meta.setString(StringField.DESCRIPTION,
        bfc.get(new byte[size])));
  }
}
