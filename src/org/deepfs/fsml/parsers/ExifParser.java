package org.deepfs.fsml.parsers;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.basex.core.Main;
import org.basex.util.Token;
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
   * Converts strings to byte arrays.
   * @param str string array
   * @return byte array
   */
  static byte[][] bytes(final String... str) {
    final byte[][] val = new byte[str.length][];
    for(int v = 0; v < val.length; v++)
      val[v] = Token.token(str[v]);
    return val;
  }

  /**
   * Format of an IFD field.
   * @author Bastian Lemke
   */
  private enum Format {
    /** 8-bit unsigned integer. */
    BYTE(1),
    /** 8-bit byte containing on 7-bit ASCII code (NULL-terminated). */
    ASCII(1),
    /** 16-bit unsigned integer. */
    SHORT(2),
    /** 32-bit unsigned integer. */
    LONG(4),
    /**
     * Two LONGs. The first LONG is the numerator and the second LONG expresses
     * the denominator.
     */
    RATIONAL(8),
    /** 8-bit byte that can take any value depending on the field definition. */
    UNDEFINED(1),
    /** 32-bit signed integer (2's complement notation). */
    SLONG(4),
    /**
     * two SLONGs, The first SLONG is the numerator and the second SLONG is the
     * denominator.
     */
    SRATIONAL(8);
    
    /** The size in bytes. */
    private final int size;
    
    /**
     * Constructor.
     * @param s the size in bytes.
     */
    private Format(final int s) {
      size = s;
    }
    
    /**
     * Returns the size in bytes.
     * @return the size in bytes.
     */
    int getSize() {
      return size;
    }

    /**
     * Returns the field format for a id.
     * @param i the id.
     * @return the field format.
     */
    static Format getForId(final int i) {
      switch(i) {
        case 1:  return BYTE;
        case 2:  return ASCII;
        case 3:  return SHORT;
        case 4:  return LONG;
        case 5:  return RATIONAL;
        case 6:  return null;
        case 7:  return UNDEFINED;
        case 8:  return null;
        case 9:  return SLONG;
        case 10: return SRATIONAL;
        default: return null;
      }
    }
  }

  /**
   * <p>
   * Enum for all IFD tags that should be parsed. Each enum constant is the
   * (lower case) hex code of the corresponding IFD tag, preceded by 'h'
   * (because java enum constants must not start with a number) and without
   * leading zeroes.
   * </p>
   * @author Bastian Lemke
   */
  private enum Tag {
    /** Image width. */
    h100(1, MetaElem.PIXEL_WIDTH, Format.SHORT, Format.LONG) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        if(d.isMetaSet(elem)) return;
        metaSimple(d, b);
      }
    },
    /** Image length. */
    h101(1, MetaElem.PIXEL_HEIGHT, Format.SHORT, Format.LONG) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        if(d.isMetaSet(elem)) return;
        metaSimple(d, b);
      }
    },
    /** Image description/title. */
    h10e(MetaElem.TITLE, Format.ASCII),
    /** Image input equipment manufacturer. */
    h10f(MetaElem.MAKE, Format.ASCII),
    /** Image input equipment model. */
    h110(MetaElem.MODEL, Format.ASCII),
    /** Orientation. */
    h112(1, MetaElem.ORIENTATION, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 1:  s = "top left";     break;
          case 2:  s = "top right";    break;
          case 3:  s = "bottom right"; break;
          case 4:  s = "bottom left";  break;
          case 5:  s = "left top";     break;
          case 6:  s = "right top";    break;
          case 7:  s = "right bottom"; break;
          case 8:  s = "left bottom";  break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** XResolution. */
    h11a(1, MetaElem.X_RESOLUTION, Format.RATIONAL),
    /** YResolution. */
    h11b(1, MetaElem.Y_RESOLUTION, Format.RATIONAL),
    /** resolution unit. */
    h128(1, MetaElem.RESOLUTION_UNIT, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 2:  s = "inches";      break;
          case 3:  s = "centimeters"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Software. */
    h131(MetaElem.SOFTWARE, Format.ASCII),
    /** DateTime of image creation. */
    h132(20, MetaElem.DATE_CREATED, Format.ASCII) {
      @Override
      void meta(final DeepFile d, final BufferedFileChannel b) {
        metaDate(d, b);
      }
    },
    /** Creator. */
    h13b(MetaElem.CREATOR_NAME, Format.ASCII),
    /** White point. */
    h13e(2, MetaElem.WHITE_POINT, Format.RATIONAL) {
      @Override
      public void meta(final DeepFile d, final BufferedFileChannel b) {
        d.addMeta(elem, readRational(b) + " " + readRational(b));
      }
    },
    /** Exif Image Width. */
    ha002(1, MetaElem.PIXEL_WIDTH, Format.SHORT, Format.LONG) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        if(d.isMetaSet(elem)) return;
        metaSimple(d, b);
      }
    },
    /** Exif Image Height. */
    ha003(1, MetaElem.PIXEL_HEIGHT, Format.SHORT, Format.LONG) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        if(d.isMetaSet(elem)) return;
        metaSimple(d, b);
      }
    },
    /** Exposure time in seconds. */
    h829a(1, MetaElem.EXPOSURE_TIME, Format.RATIONAL) {
      @Override
      void meta(final DeepFile d, final BufferedFileChannel b) {
        final double sec = readRational(b);
        d.addMeta(MetaElem.EXPOSURE_TIME_MS, sec * 1000);
        final StringBuilder str = new StringBuilder();
        if(sec < 1) str.append("1/").append((int) (1 / sec));
        else str.append((int) sec);
        str.append(" seconds");
        d.addMeta(MetaElem.EXPOSURE_TIME, str.toString());
      }
    },
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
    h8769(1, null, Format.LONG) {
      @Override
      void parse(final ExifParser o, final ByteBuffer buf) {
        if(!check(o, buf)) err(o);
        try {
          o.bfc.position(buf.getInt());
          o.readIFD();
        } catch(IOException e) {
          err(o.deepFile, e);
        }
      }
    },
    /** Aperture value. */
    h9202(1, MetaElem.APERTURE_VALUE, Format.RATIONAL),
    /** Max aperture value. */
    h9205(1, MetaElem.APERTURE_VALUE_MAX, Format.RATIONAL);

    /** The format of the date values. */
    private static final SimpleDateFormat SDF = new SimpleDateFormat(
        "yyyy:MM:dd HH:mm:ss");
    /** The allowed field formats. */
    private final Format[] aFormats;
    /** The actual format of the field. */
    Format format;
    /** The corresponding {@link MetaElem} (for simple fields only). */
    final MetaElem elem;
    /** The default number of values. */
    final int defaultCount;
    /** The number of values. */
    int count;
    /** Flag if the value of the field is inlined. */
    boolean inlined;

    /**
     * Initializes a tag.
     * @param metaElem the corresponding {@link MetaElem}.
     * @param allowedFormats formats that are allowed for the tag field.
     */
    private Tag(final MetaElem metaElem, final Format... allowedFormats) {
      this(-1, metaElem, allowedFormats);
    }
    
    /**
     * Initializes a tag.
     * @param expectedCount the expected number of values.
     * @param metaElem the corresponding {@link MetaElem}.
     * @param allowedFormats formats that are allowed for the tag field.
     */
    private Tag(final int expectedCount, final MetaElem metaElem,
        final Format... allowedFormats) {
      assert expectedCount > 0 || expectedCount == -1;
      defaultCount = expectedCount;
      elem = metaElem;
      aFormats = allowedFormats;
    }

    /**
     * Parses the tag.
     * @param o {@link ExifParser} instance to send parser events from.
     * @param buf the {@link ByteBuffer} to read from.
     */
    void parse(final ExifParser o, final ByteBuffer buf) {
      if(check(o, buf)) {
        if(inlined) meta(o.deepFile, buf);
        else meta(o.deepFile, o.bfc);
      } else err(o);
    }

    /**
     * Reads the metadata from the ByteBuffer and adds it to the DeepFile. Must
     * be overridden for complex fields that occupy at most 4 bytes (and are
     * inlined in the IFD field).
     * @param d the DeepFile to add the metadata to.
     * @param b the ByteBuffer to read from.
     */
    void meta(final DeepFile d, final ByteBuffer b) {
      metaSimple(d, b);
    }

    /**
     * Reads the metadata from the BufferedFileChannel and adds it to the
     * DeepFile. Must be overridden for complex fields that occupy at least 5
     * bytes (and are not inlined in the IFD field).
     * @param d the DeepFile to add the metadata to.
     * @param b the {@link BufferedFileChannel} to read from.
     */
    void meta(final DeepFile d, final BufferedFileChannel b) {
      metaSimple(d, b);
    }

    /**
     * Checks if the field is valid.
     * @param o the ExifParser instance.
     * @param buf the ByteBuffer to read from.
     * @return true if the current IFD field is valid.
     */
    boolean check(final ExifParser o, final ByteBuffer buf) {
      format = Format.getForId(buf.getShort());
      for(final Format f : aFormats) {
        if(f.equals(format)) {
          final long c = buf.getInt() & 0xFFFFFFFFL;
          if (c > Integer.MAX_VALUE || c < 1) {
            err(o, "Invalid item count.");
            return false;
          }
          if(defaultCount == -1) count = (int) c;
          else if(defaultCount != c) return false;
          else count = defaultCount; // defaultCount == c
          final int size = count * format.getSize();
          if(size <= 4) inlined = true;
          else {
            try {
              o.bfc.position(buf.getInt());
              o.bfc.buffer(size);
            } catch(IOException e) {
              err(o, e);
              return false;
            }
            inlined = false;
          }
          return true;
        }
      }
      err(o);
      return false;
    }
    
    /**
     * Reads a rational value from a IFD field.
     * @param b the {@link BufferedFileChannel} to read the rational from.
     * @return the rational value as double.
     */
    double readRational(final BufferedFileChannel b) {
      final long numerator = b.getInt() & 0xFFFFFFFFL;
      final long denominator = b.getInt() & 0xFFFFFFFFL;
      return (double) numerator / denominator;
    }
    
    // [BL] implement parsing of UNDEFINED fields.
    // [BL] implement parsing of SLONG fields.
    // [BL] implement parsing of SRATIONAL fields.

    /**
     * Parses a field that is not inlined and adds the metadata to the deep
     * file.
     * @param d the {@link DeepFile} to add the metadata to.
     * @param b the {@link BufferedFileChannel} to read from.
     */
    void metaSimple(final DeepFile d, final BufferedFileChannel b) {
      try {
        switch(format) {
          case ASCII:    d.addMeta(elem, b.get(new byte[count])); break;
          case RATIONAL: d.addMeta(elem, readRational(b));        break;
          default:
            Main.debug("ExifParser: Unknown or unsupported field type for " +
                "non-inlined data (%)", format);
        }
      } catch(final IOException e) {
        err(d, e);
      }
    }
    
    /**
     * Parses an inlined field and adds the metadata to the deep file.
     * @param d the {@link DeepFile} to add the metadata to.
     * @param buf the buffer to read from.
     */
    void metaSimple(final DeepFile d, final ByteBuffer buf) {
      switch(format) {
        case BYTE:  d.addMeta(elem, (short) (buf.get() & 0xFF));        break;
        case ASCII: d.addMeta(elem, buf.get(new byte[count]).array());  break;
        case SHORT: d.addMeta(elem, buf.getShort() & 0xFFFF);           break;
        case LONG:  d.addMeta(elem, buf.getInt() & 0xFFFFFFFFL);        break;
        default:
          Main.debug("ExifParser: Unknown or unsupported field type for " +
              "inlined data (%)", format);
      }
    }

    /**
     * Converts the date to the correct format and adds it to the DeepFile.
     * @param d the {@link DeepFile} to add the date to.
     * @param b the {@link BufferedFileChannel} to read from.
     */
    void metaDate(final DeepFile d, final BufferedFileChannel b) {
      try {
        final byte[] data = new byte[20];
        b.get(data);
        final Date date = SDF.parse(Token.string(data));
        d.addMeta(elem, ParserUtil.convertDateTime(date));
        return;
      } catch(final ParseException ex) {
        err(d, ex);
      } catch(final IOException e) {
        err(d, e);
      }
    }

    /**
     * Generates a debug message.
     * @param o the ExifParser.
     */
    void err(final ExifParser o) {
      Main.debug("ExifParser: Invalid field found (%, file: %)", toString(),
          o.bfc.getFileName());
    }

    /**
     * Generates a debug message.
     * @param o the ExifParser.
     * @param ex the exception to log.
     */
    void err(final ExifParser o, final Exception ex) {
      Main.debug(
          "ExifParser: Invalid field found (%, file: %, error: %)",
          toString(), o.bfc.getFileName(), ex);
    }
    
    /**
     * Generates a debug message.
     * @param d the DeepFile.
     * @param ex the exception to log.
     */
    void err(final DeepFile d, final Exception ex) {
      Main.debug(
          "ExifParser: Invalid field found (%, file: %, error message: %)",
          toString(), d.getBufferedFileChannel().getFileName(), ex);
    }
    
    /**
     * Generates a debug message.
     * @param o the ExifParser.
     * @param ex the message to log.
     */
    void err(final ExifParser o, final String ex) {
      Main.debug(
          "ExifParser: Invalid field found (%, file: %, error message: %)",
          toString(), o.bfc.getFileName(), ex);
    }

    @Override
    public String toString() {
      return name().replace("h", "0x");
    }
  }

  /** The {@link BufferedFileChannel} to read from. */
  BufferedFileChannel bfc;
  /** The metadata and content store. */
  DeepFile deepFile;

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
      final Tag tag = Tag.valueOf("h" + Integer.toHexString(tagNr));
      tag.parse(this, data);
    } catch(final IllegalArgumentException ex) { /* tag is not registered. */}
  }
}
