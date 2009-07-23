package org.basex.build.fs.parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.basex.BaseX;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.parser.Metadata.DataType;
import org.basex.build.fs.parser.Metadata.Definition;
import org.basex.build.fs.parser.Metadata.Element;
import org.basex.build.fs.parser.Metadata.MimeType;
import org.basex.build.fs.parser.Metadata.Type;
import static org.basex.util.Token.*;

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

  /** IFD field format: 8-bit unsigned integer. */
  // private static final int IFD_TYPE_BYTE = 1;
  /**
   * IFD field format: 8-bit byte containing on 7-bit ASCII code
   * (NULL-terminated).
   */
  private static final int IFD_TYPE_ASCII = 2;
  /** IFD field format: 16-bit unsigned integer. */
  private static final int IFD_TYPE_SHORT = 3;
  /** IFD field format: 32-bit unsigned integer. */
  private static final int IFD_TYPE_LONG = 4;

  /**
   * IFD field format: two LONGs. The first LONG is the numerator and the second
   * LONG expresses the denominator.
   */
  // private static final int IFD_TYPE_RATIONAL = 5;
  /**
   * IFD field format: 8-bit byte that can take any value depending on the field
   * definition.
   */
  // private static final int IFD_TYPE_UNDEFINED = 7;
  /** IFD field format: 32-bit signed integer (2's complement notation). */
  // private static final int IFD_TYPE_SLONG = 9;
  /**
   * IFD field format: two SLONGs, The first SLONG is the numerator and the
   * second SLONG is the denominator.
   */
  // private static final int IFD_TYPE_SRATIONAL = 10;
  // ---------------------------------------------------------------------------
  /**
   * <p>
   * Enum for all IFD tags that should be parsed. Each enum constant is the
   * (lower case) hex code of the corresponding IFD tag, preceded by 'h'
   * (because java enum constants may not start with a number) and without
   * leading zeroes.
   * </p>
   * @author Bastian Lemke
   */
  private enum IFD_TAG {
    /** Image width. */
    h100 {
      @Override
      void parse(final JPGParser obj, final long ifdOff, final ByteBuffer buf)
          throws IOException {
        final int type = buf.getShort();
        if((type == IFD_TYPE_LONG || type == IFD_TYPE_SHORT)
            && buf.getInt() == 1) {
          obj.fsparser.metaEvent(Element.WIDTH, DataType.INTEGER,
              Definition.PIXEL, null,
              token((long) buf.getInt() & 0xFFFFFFFF));
        } else error(obj, "Image width (0x0100)");
      }
    },
    /** Image length. */
    h101 {
      @Override
      void parse(final JPGParser obj, final long ifdOff, final ByteBuffer buf)
          throws IOException {
        final int type = buf.getShort();
        if((type == IFD_TYPE_LONG || type == IFD_TYPE_SHORT)
            && buf.getInt() == 1) {
          obj.fsparser.metaEvent(Element.HEIGHT, DataType.INTEGER,
              Definition.PIXEL, null,
              token((long) buf.getInt() & 0xFFFFFFFF));
        } else error(obj, "Image length (0x0101)");
      }
    },
    /** Image description. */
    h10e {
      @Override
      void parse(final JPGParser obj, final long ifdOff, final ByteBuffer buf)
          throws IOException {
        if(buf.getShort() == IFD_TYPE_ASCII) {
          obj.fsparser.metaEvent(Element.DESCRIPTION, DataType.STRING,
              Definition.NONE, null, obj.readAscii(buf, ifdOff));
        } else error(obj, "Image description (0x010E)");
      }
    },
    /** DateTime of image creation. */
    h132 {
      @Override
      void parse(final JPGParser obj, final long ifdOff, final ByteBuffer buf)
          throws IOException {
        if(buf.getShort() == IFD_TYPE_ASCII && buf.getInt() == 20) {
          obj.bfc.position(ifdOff + buf.getInt());
          obj.bfc.buffer(20);
          final byte[] data = new byte[20];
          obj.bfc.get(data);
          obj.fsparser.metaEvent(Element.DATE, DataType.DATETIME,
              Definition.CREATE_TIME, null, ParserUtil.convertDateTime(data));
        } else error(obj, "DateTime (0x0132)");
      }
    },
    /** Creator. */
    h13b {
      @Override
      void parse(final JPGParser obj, final long ifdOff, final ByteBuffer buf)
          throws IOException {
        if(buf.getShort() == IFD_TYPE_ASCII) {
          obj.fsparser.metaEvent(Element.CREATOR, DataType.STRING,
              Definition.NONE, null, obj.readAscii(buf, ifdOff));
        } else error(obj, "Creator (0x013B)");
      }
    },
    /** Exif Image Width. */
    ha002 {
      @Override
      void parse(final JPGParser obj, final long ifdOff, final ByteBuffer buf)
          throws IOException {
        final int type = buf.getShort();
        byte[] value = null;
        if(buf.getInt() == 1) {
          if(type == IFD_TYPE_LONG) {
            value = token((long) buf.getInt() & 0xFFFFFFFF);
          } else if(type == IFD_TYPE_SHORT) {
            value = token(buf.getShort() & 0xFFFF);
          }
        }
        if(value != null) {
          obj.fsparser.metaEvent(Element.WIDTH, DataType.INTEGER,
              Definition.PIXEL, null, value);
        } else error(obj, "Exif Image Width (0xA002)");
      }
    },
    /** Exif Image Height. */
    ha003 {
      @Override
      void parse(final JPGParser obj, final long ifdOff, final ByteBuffer buf)
          throws IOException {
        final int type = buf.getShort();
        if((type == IFD_TYPE_LONG || type == IFD_TYPE_SHORT)
            && buf.getInt() == 1) {
          obj.fsparser.metaEvent(Element.HEIGHT, DataType.INTEGER,
              Definition.PIXEL, null,
              token((long) buf.getInt() & 0xFFFFFFFF));
        } else error(obj, "Exif Image Height (0xA003)");
      }
    },
    /** GPS IFD. */
    h8825 {
      @Override
      void parse(final JPGParser obj, final long ifdOff, final ByteBuffer buf)
          throws IOException {
        if(buf.getShort() == IFD_TYPE_LONG && buf.getInt() == 1) {
          obj.bfc.position(ifdOff + buf.getInt());
          obj.readIFD();
        } else error(obj, "GPS (0x8825)");
      }
    },
    /** EXIF IFD. */
    h8769 {
      @Override
      void parse(final JPGParser obj, final long ifdOff, final ByteBuffer buf)
          throws IOException {
        if(buf.getShort() == IFD_TYPE_LONG && buf.getInt() == 1) {
          obj.bfc.position(ifdOff + buf.getInt());
          obj.readIFD();
        } else error(obj, "Exif (0x8769)");
      }
    };

    /**
     * <p>
     * Tag specific parse method.
     * </p>
     * @param obj {@link JPGParser} instance to send parser events from.
     * @param ifdOff offset of the IFD the current tag belongs to.
     * @param buf the {@link ByteBuffer} to read from.
     * @throws IOException if any error occurs while reading additional data
     *           from the file channel.
     */
    abstract void parse(final JPGParser obj, final long ifdOff,
        final ByteBuffer buf) throws IOException;

    /**
     * Log error.
     * @param obj the current {@link JPGParser} instance.
     * @param fieldName the name of the current IFD field.
     */
    void error(final JPGParser obj, final String fieldName) {
      if(NewFSParser.VERBOSE) BaseX.debug("JPGParser: Invalid " + fieldName
          + " field (%)", obj.bfc.getFileName());
    }
  }

  /**
   * Reads variable size ascii data from a IFD field.
   * @param buf the {@link ByteBuffer} to read from.
   * @param ifdOff the offset of the current IFD.
   * @return the ascii data.
   * @throws IOException if any error occurs while reading from the file
   *           channel.
   */
  byte[] readAscii(final ByteBuffer buf, final long ifdOff) throws IOException {
    final int size = buf.getInt();
    final byte[] data = new byte[size];
    if(size <= 4) { // data is inlined
      buf.get(data);
    } else {
      bfc.position(ifdOff + buf.getInt());
      bfc.buffer(size);
      bfc.get(data);
    }
    return trim(data);
  }

  /** Exif header. Null terminated ASCII representation of 'Exif'. */
  private static final byte[] HEADER_EXIF =
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
  private static final byte[] HEADER_JFIF =
    { 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01};

  /** Extended JFIF header. Null terminated ASCII representation of 'JFXX'. */
  private static final byte[] HEADER_JFXX = { 0x4A, 0x46, 0x58, 0x58, 0x00};

  /** Standard constructor. */
  public JPGParser() {
    super(Type.IMAGE, MimeType.JPG);
  }

  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    f.buffer(4);
    int b;
    // 0xFFD8FFE0 or 0xFFD8FFE1
    return f.get() == 0xFF
        && f.get() == 0xD8
        && f.get() == 0xFF
        && ((b = f.get()) == 0xE0 || b == 0xE1);
  }

  @Override
  public void readContent(final BufferedFileChannel f,
      final NewFSParser parser) {
    // no textual representation for jpg content ...
  }

  /** The {@link NewFSParser} instance to fire events. */
  NewFSParser fsparser;
  /** The {@link BufferedFileChannel} to read from. */
  BufferedFileChannel bfc;

  /**
   * Reads two bytes from the {@link BufferedFileChannel} and returns the value
   * as integer.
   * @return the size value.
   */
  private int readSize() {
    return (bfc.get() << 8 | bfc.get()) - 2;
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

  @Override
  public void readMeta(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {

    bfc = f;
    fsparser = parser;
    bfc.buffer(6);
    if(bfc.get() != 0xFF || bfc.get() != 0xD8) {
      if(NewFSParser.VERBOSE) BaseX.debug("JPGParser: Not a jpg file (%).",
          bfc.getFileName());
      return;
    }
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

  /**
   * Reads image width and height.
   * @throws IOException if any error occurs while reading from the file
   *           channel.
   */
  private void readDimensions() throws IOException {
    if(bfc.get() == 8) {
      bfc.buffer(8);
      final int height = bfc.getShort();
      final int width = bfc.getShort();
      fsparser.metaEvent(Element.HEIGHT, DataType.INTEGER, Definition.PIXEL,
          null, token(height));
      fsparser.metaEvent(Element.WIDTH, DataType.INTEGER, Definition.PIXEL,
          null, token(width));
    } else {
      if(NewFSParser.VERBOSE) BaseX.debug("Wrong data precision field (%).",
          bfc.getFileName());
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
        fsparser.fileStartEvent("Thumbnail", "jpg", Type.IMAGE, MimeType.JPG,
            bfc.absolutePosition(), s);
        break;
      case 0x11: // Thumbnail coded using 1 byte/pixel
        width = bfc.get();
        height = bfc.get();
        s -= 2;
        fsparser.fileStartEvent("Thumbnail", null, Type.IMAGE,
            MimeType.UNKNOWN, bfc.absolutePosition(), s);
        fsparser.metaEvent(Element.DESCRIPTION, DataType.STRING,
            Definition.NONE, null,
            token("Thumbnail coded using 1 byte/pixel."));
        break;
      case 0x13: // Thumbnail coded using 3 bytes/pixel
        width = bfc.get();
        height = bfc.get();
        s -= 2;
        fsparser.fileStartEvent("Thumbnail", null, Type.IMAGE,
            MimeType.UNKNOWN, bfc.absolutePosition(), s);
        fsparser.metaEvent(Element.DESCRIPTION, DataType.STRING,
            Definition.NONE, null,
            token("Thumbnail coded using 3 bytes/pixel."));
        break;
      default:
        BaseX.debug("JPGParser: Illegal or unsupported JFIF header (%)",
            bfc.getFileName());
    }
    fsparser.metaEvent(Element.WIDTH, DataType.INTEGER, Definition.PIXEL, null,
        token(width));
    fsparser.metaEvent(Element.HEIGHT, DataType.INTEGER, Definition.PIXEL,
        null, token(height));
    fsparser.fileEndEvent();
    bfc.skip(s);
  }

  /**
   * Reads an comment segment.
   * @param size the size of the segment.
   * @throws IOException if any error occurs while reading from the file.
   */
  private void readComment(final int size) throws IOException {
    final byte[] array = new byte[size];
    bfc.get(array);
    fsparser.metaEvent(Element.COMMENT, DataType.STRING, Definition.NONE, null,
        ParserUtil.checkAscii(array));
  }

  /**
   * Reads an Exif segment.
   * @param size the size of the segment.
   * @throws IOException if any error occurs while reading from the file.
   * @see <a href="http://www.Exif.org/Exif2-2.PDF">Exif 2.2</a>
   */
  private void readExif(final int size) throws IOException {
    final int len = HEADER_EXIF.length;
    bfc.buffer(len + 8);
    final long pos = bfc.position();
    if(checkNextBytes(HEADER_EXIF)
    	&& checkEndianness()
    	&& bfc.get() == 0x00
    	&& bfc.get() == 0x2A) {

      final int ifdOffset = bfc.getInt();
      /*
       * The IFD offset value counts the number of bytes from the first byte
       * after the Exif header. At this point, 8 more bytes have been read
       * (endian bytes (2), 0x002A, IFD offset itself (4)).
       */
      // ifdStartPos = bfc.position() - 8;
      assert ifdOffset >= 8;
      bfc.skip(ifdOffset - 8);
      readIFD();
      bfc.setByteOrder(ByteOrder.BIG_ENDIAN); // reset byte order to default
    } else {
      bfc.position(pos + size); // skip remaining bytes
      return;
    }
  }

  /**
   * First byte of the IFD. All offset values inside the IFD are relative to
   * this position.
   */
  long ifdStartPos;

  /**
   * Reads an IFD (an array of fixed length fields) from the current file
   * channel position. At least 2 bytes have to be buffered.
   * @throws IOException if any error occurs while reading from the file
   *           channel.
   */
  void readIFD() throws IOException {
    bfc.buffer(2);
    final long pos = bfc.position() - 8;
    final int numFields = bfc.getShort();
    // one field contains 12 bytes
    final byte[] buf = new byte[numFields * 12];
    bfc.get(buf);
    for(int i = 0; i < numFields; i++) {
      readField(pos, ByteBuffer.wrap(buf, i * 12, 12));
    }
  }

  /**
   * Read a single tag field from the IFD array.
   * @param ifdOffset position of the first IFD byte.
   * @param data the {@link ByteBuffer} containing the field data.
   * @throws IOException if any error occurs while reading from the file
   *           channel.
   */
  private void readField(final long ifdOffset, final ByteBuffer data)
      throws IOException {
    final int tagNr = data.getShort() & 0xFFFF;
    try {
      // System.out.println("h" + Integer.toHexString(tagNr));
      final IFD_TAG tag = IFD_TAG.valueOf("h" + Integer.toHexString(tagNr));
      tag.parse(this, ifdOffset, data);
    } catch(final IllegalArgumentException e) { /* */}
  }

  /**
   * Check if the two endianness bytes are correct and set the ByteBuffer's
   * endianness according to these bytes.
   * @return true if the endianness bytes are valid, false otherwise.
   */
  private boolean checkEndianness() {
    final int b1 = bfc.get();
    final int b2 = bfc.get();
    if(b1 == 0x49 && b2 == 0x49) {
      bfc.setByteOrder(ByteOrder.LITTLE_ENDIAN);
      return true;
    }
    return b1 == 0x4d && b2 == 0x4d; // default byte order is big endian
  }
}
