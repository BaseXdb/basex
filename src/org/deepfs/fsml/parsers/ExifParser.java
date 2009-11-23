package org.deepfs.fsml.parsers;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.basex.core.Main;
import org.deepfs.fsml.util.BufferedFileChannel;
import org.deepfs.fsml.util.DeepFile;
import org.deepfs.fsml.util.MetaElem;
import org.deepfs.fsml.util.ParserUtil;

/**
 * Parser for Exif data. This is not a standalone parser. Only used for parsing
 * Exif data that is embedded in e.g. a TIFF file.
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Bastian Lemke
 */
public final class ExifParser {
  /**
   * <p>
   * Enum for all IFD tags that should be parsed. Each enum constant is the
   * (lower case) hex code of the corresponding IFD tag, preceded by 'h'
   * (because java enum constants must not start with a number) and without
   * leading zeroes.
   * </p>
   * @author Bastian Lemke
   */
  private enum IFD_TAG {
        /*
     * HOWTO add more fields: . . . . . . . . . . . . . . . . . . . . . . . . ..
     * 1. Read Exif spec: http://www.Exif.org/Exif2-2.PDF and search for the . .
     * .. metadata that should be extracted. . . . . . . . . . . . . . . . . . .
     * 2. Pick the corresponding hexadecimal TAG ID (e.g. 100 for "Image width")
     * 3. add an item to this enum - the item name has to be the char 'h' . . ..
     * .. followed by the lowercase hex value of the TAG ID. . . . . . . . . . .
     * 4. implement the abstract parse() method, fill metadata container . . . .
     * .. o.meta with the metadata and fire metadata events . . . . . . . . ..
     * .. (o.fsparser.metaEvent(o.meta))
     */
    /** Image width. */
    h100 {
      @Override
      void parse(final ExifParser o, final ByteBuffer buf) {
        final int type = buf.getShort();
        if(buf.getInt() == 1) {
          if(type == IFD_TYPE_SHORT) {
            o.deepFile.addMeta(MetaElem.PIXEL_WIDTH, buf.getShort() & 0xFFFF);
            buf.getShort(); // empty two bytes
          } else if(type == IFD_TYPE_LONG) {
            o.deepFile.addMeta(MetaElem.PIXEL_WIDTH, buf.getInt());
          } else error(o, "Image width (0x0100)");
        } else error(o, "Image width (0x0100)");
      }
    },
        /** Image length. */
    h101 {
      @Override
      void parse(final ExifParser o, final ByteBuffer buf) {
        final int type = buf.getShort();
        if(buf.getInt() == 1) {
          if(type == IFD_TYPE_SHORT) {
            o.deepFile.addMeta(MetaElem.PIXEL_HEIGHT, buf.getShort() & 0xFFFF);
          } else if(type == IFD_TYPE_LONG) {
            o.deepFile.addMeta(MetaElem.PIXEL_HEIGHT, buf.getInt());
          } else error(o, "Image length (0x0101)");
        } else error(o, "Image length (0x0101)");
      }
    },
        /** Image description. */
    h10e {
      @Override
      void parse(final ExifParser o, final ByteBuffer buf) throws IOException {
        if(buf.getShort() == IFD_TYPE_ASCII) {
          o.deepFile.addMeta(MetaElem.DESCRIPTION, o.readAscii(buf));
        } else error(o, "Image description (0x010E)");
      }
    },
        /** DateTime of image creation. */
    h132 {
      @Override
      void parse(final ExifParser o, final ByteBuffer buf) throws IOException {
        if(buf.getShort() == IFD_TYPE_ASCII && buf.getInt() >= 20) {
          o.bfc.position(buf.getInt());
          o.bfc.buffer(20);
          final byte[] data = new byte[20];
          o.bfc.get(data);
          o.dateEvent(MetaElem.DATE_CREATED, data);
        } else error(o, "DateTime (0x0132)");
      }
    },
        /** Creator. */
    h13b {
      @Override
      void parse(final ExifParser o, final ByteBuffer buf) throws IOException {
        if(buf.getShort() == IFD_TYPE_ASCII) {
          o.deepFile.addMeta(MetaElem.CREATOR_NAME, o.readAscii(buf));
        } else error(o, "Creator (0x013B)");
      }
    },
        // /** Exif Image Width. */
    // ha002 {
    // @Override
    // void parse(final ExifParser o, final ByteBuffer buf)
    // throws IOException {
    // final int type = buf.getShort();
    // if(buf.getInt() == 1) {
    // if(type == IFD_TYPE_LONG) {
    // o.meta.setLong(IntField.PIXEL_WIDTH,
    // (long) buf.getInt() & 0xFFFFFFFF);
    // } else if(type == IFD_TYPE_SHORT) {
    // o.meta.setShort(IntField.PIXEL_WIDTH,
    // (short) (buf.getShort() & 0xFFFF));
    // } else error(o, "Exif Image Width (0xA002)");
    // } else error(o, "Exif Image Width (0xA002)");
    // o.fsparser.metaEvent(o.meta);
    // }
    // },
    // /** Exif Image Height. */
    // ha003 {
    // @Override
    // void parse(final ExifParser o, final ByteBuffer buf)
    // throws IOException {
    // final int type = buf.getShort();
    // if(buf.getInt() == 1) {
    // if(type == IFD_TYPE_LONG) {
    // o.meta.setLong(IntField.PIXEL_HEIGHT,
    // (long) buf.getInt() & 0xFFFFFFFF);
    // } else if(type == IFD_TYPE_SHORT) {
    // o.meta.setShort(IntField.PIXEL_HEIGHT,
    // (short) (buf.getShort() & 0xFFFF));
    // } else error(o, "Exif Image Height (0xA003)");
    // } else error(o, "Exif Image Height (0xA003)");
    // o.fsparser.metaEvent(o.meta);
    // }
    // },
    // /** GPS IFD. */
    // h8825 {
    // @Override
    // void parse(final ExifParser o, final ByteBuffer buf)
    // throws IOException {
    // if(buf.getShort() == IFD_TYPE_LONG && buf.getInt() == 1) {
    // o.bfc.position(buf.getInt());
    // o.readIFD();
    // } else error(o, "GPS (0x8825)");
    // }
    // },
    /** EXIF IFD. */
    h8769 {
      @Override
      void parse(final ExifParser o, final ByteBuffer buf) throws IOException {
        if(buf.getShort() == IFD_TYPE_LONG && buf.getInt() == 1) {
          o.bfc.position(buf.getInt());
          o.readIFD();
        } else error(o, "Exif (0x8769)");
      }
    };

    /**
     * <p>
     * Tag specific parse method.
     * </p>
     * @param exifParser {@link ExifParser} instance to send parser events from.
     * @param buf the {@link ByteBuffer} to read from.
     * @throws IOException if any error occurs while reading additional data
     *           from the file channel.
     */
    abstract void parse(final ExifParser exifParser, final ByteBuffer buf)
        throws IOException;

    /**
     * Log error.
     * @param o the current {@link ExifParser} instance.
     * @param fieldName the name of the current IFD field.
     */
    void error(final ExifParser o, final String fieldName) {
      Main.debug("ExifParser: Invalid " + fieldName + " field (%)",
          o.bfc.getFileName());
    }
  }

  // /** IFD field format: 8-bit unsigned integer. */
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
  // /**
  // * IFD field format: two LONGs. The first LONG is the numerator and the
  // second
  // * LONG expresses the denominator.
  // */
  // private static final int IFD_TYPE_RATIONAL = 5;
  // /**
  // * IFD field format: 8-bit byte that can take any value depending on the
  // field
  // * definition.
  // */
  // private static final int IFD_TYPE_UNDEFINED = 7;
  // /** IFD field format: 32-bit signed integer (2's complement notation). */
  // private static final int IFD_TYPE_SLONG = 9;
  // /**
  // * IFD field format: two SLONGs, The first SLONG is the numerator and the
  // * second SLONG is the denominator.
  // */
  // private static final int IFD_TYPE_SRATIONAL = 10;
  // ---------------------------------------------------------------------------

  /** The {@link BufferedFileChannel} to read from. */
  BufferedFileChannel bfc;
  /** The metadata and content store. */
  DeepFile deepFile;
  /** The format of the date values. */
  private static final SimpleDateFormat SDF = new SimpleDateFormat(
      "yyyy:MM:dd HH:mm:ss");

  /**
   * Checks if the Exif IFD is valid.
   * @param f the {@link BufferedFileChannel} to read from.
   * @return true if the IFD is valid, false otherwise.
   * @throws IOException if any error occurs while reading from the channel.
   */
  boolean check(final BufferedFileChannel f) throws IOException {
    try {
      f.buffer(4);
    } catch(final EOFException e) {
      return false;
    }
    if(!checkEndianness(f)) return false;
    if(f.getShort() != 0x2A) return false; // magic number
    return true;
  }

  /**
   * Parse the Exif data.
   * @param df the DeepFile to store metadata and file content.
   * @throws IOException if any error occurs while reading from the channel.
   */
  public void extract(final DeepFile df)
      throws IOException {
    deepFile = df;
    bfc = df.getBufferedFileChannel();
    try {
      bfc.buffer(8);
    } catch(final EOFException e) {
      return;
    }
    if(!check(bfc)) return;

    final int ifdOffset = bfc.getInt();
    /*
     * The IFD offset value counts the number of bytes from the first byte after
     * the Exif header. At this point, 8 more bytes have been read (endian bytes
     * (2), 0x002A, IFD offset itself (4)).
     */
    assert ifdOffset >= 8;
    bfc.position(ifdOffset);
    readIFD();
    bfc.setByteOrder(ByteOrder.BIG_ENDIAN); // set byte order to default value
  }

  /**
   * Reads an IFD (an array of fixed length fields) from the current file
   * channel position. At least 2 bytes have to be buffered.
   * @throws IOException if any error occurs while reading from the file
   *           channel.
   */
  void readIFD() throws IOException {
    bfc.buffer(2);
    final int numFields = bfc.getShort();
    // one field contains 12 bytes
    final byte[] buf = new byte[numFields * 12];
    bfc.get(buf);
    for(int i = 0; i < numFields; i++) {
      readField(ByteBuffer.wrap(buf, i * 12, 12).order(bfc.getByteOrder()));
    }
  }

  // ----- methods used by IFD_TAG parse methods -------------------------------

  /**
   * Reads variable size ascii data from a IFD field.
   * @param buf the {@link ByteBuffer} to read from.
   * @return the ascii data.
   * @throws IOException if any error occurs while reading from the file
   *           channel.
   */
  byte[] readAscii(final ByteBuffer buf) throws IOException {
    final int size = buf.getInt();
    final byte[] data = new byte[size];
    if(size <= 4) { // data is inlined
      buf.get(data);
    } else {
      bfc.position(buf.getInt());
      bfc.buffer(size);
      bfc.get(data);
    }
    return data;
  }

  /**
   * Converts the date to the correct format and fires a date event.
   * @param elem the metadata element to use for the metadata item.
   * @param date the date value.
   */
  void dateEvent(final MetaElem elem, final byte[] date) {
    try {
      final Date d = SDF.parse(new String(date));
      deepFile.addMeta(elem, ParserUtil.convertDateTime(d));
    } catch(final ParseException ex) {
      Main.debug(ex.getMessage());
    }
  }

  // ----- utility methods -----------------------------------------------------

  /**
   * Checks if the two endianness bytes are correct and set the ByteBuffer's
   * endianness according to these bytes.
   * @param f the {@link BufferedFileChannel} to read from.
   * @return true if the endianness bytes are valid, false otherwise.
   */
  private boolean checkEndianness(final BufferedFileChannel f) {
    final int b1 = f.get();
    final int b2 = f.get();
    if(b1 == 0x49 && b2 == 0x49) {
      f.setByteOrder(ByteOrder.LITTLE_ENDIAN);
      return true;
    }
    return b1 == 0x4d && b2 == 0x4d; // default byte order is big endian
  }

  /**
   * Reads a single tag field from the IFD array.
   * @param data the {@link ByteBuffer} containing the field data.
   */
  private void readField(final ByteBuffer data) {
    final int tagNr = data.getShort() & 0xFFFF;
    try {
      final IFD_TAG tag = IFD_TAG.valueOf("h" + Integer.toHexString(tagNr));
      tag.parse(this, data);
    } catch(final IOException ex) {
      Main.debug("%", ex);
    } catch(final IllegalArgumentException ex) { /* */}
  }
}
