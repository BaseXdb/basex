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

  /** Image orientation viewed in terms of rows and columns. */
  static final byte[][] ORIENTATION = bytes("", "top left", "top right",
      "bottom right", "bottom left", "left top", "right top", "right bottom",
      "left bottom");

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
    BYTE,

    /** 8-bit byte containing on 7-bit ASCII code (NULL-terminated). */
    ASCII,

    /** 16-bit unsigned integer. */
    SHORT,

    /** 32-bit unsigned integer. */
    LONG,

    /**
     * Two LONGs. The first LONG is the numerator and the second LONG expresses
     * the denominator.
     */
    RATIONAL,

    /** 8-bit byte that can take any value depending on the field definition. */
    UNDEFINED,

    /** 32-bit signed integer (2's complement notation). */
    SLONG,

    /**
     * two SLONGs, The first SLONG is the numerator and the second SLONG is the
     * denominator.
     */
    SRATIONAL;

    /**
     * Returns the field format for a id.
     * @param i the id.
     * @return the field format.
     */
    static Format getForId(final int i) {
      switch(i) {
        case 1:
          return BYTE;
        case 2:
          return ASCII;
        case 3:
          return SHORT;
        case 4:
          return LONG;
        case 5:
          return RATIONAL;
        case 6:
          return null;
        case 7:
          return UNDEFINED;
        case 8:
          return null;
        case 9:
          return SLONG;
        case 10:
          return SRATIONAL;
        default:
          return null;
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
    h100(1, Format.SHORT, Format.LONG) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        final MetaElem elem = MetaElem.PIXEL_WIDTH;
        if(o.deepFile.isMetaSet(elem)) return;
        metaSimple(o, buf, f, elem);
      }
    },

    /** Image length. */
    h101(1, Format.SHORT, Format.LONG) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        final MetaElem elem = MetaElem.PIXEL_HEIGHT;
        if(o.deepFile.isMetaSet(elem)) return;
        metaSimple(o, buf, f, elem);
      }
    },

    /** Image description/title. */
    h10e(null, Format.ASCII) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        metaSimple(o, buf, f, MetaElem.TITLE);
      }
    },

    /** Image input equipment manufacturer. */
    h10f(null, Format.ASCII) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        metaSimple(o, buf, f, MetaElem.MAKE);
      }
    },

    /** Image input equipment model. */
    h110(null, Format.ASCII) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        metaSimple(o, buf, f, MetaElem.MODEL);
      }
    },

    /** Orientation. */
    h112(1, Format.SHORT) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        final int val = buf.getShort();
        if(val >= 0 && val < ORIENTATION.length) {
          o.deepFile.addMeta(MetaElem.ORIENTATION, ORIENTATION[val]);
          return;
        }
        err(o);
      }
    },

    /** XResolution. */
    h11a(1, Format.RATIONAL) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        metaSimple(o, buf, f, MetaElem.X_RESOLUTION);
      }
    },

    /** YResolution. */
    h11b(1, Format.RATIONAL) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        metaSimple(o, buf, f, MetaElem.Y_RESOLUTION);
      }
    },

    /** resolution unit. */
    h128(1, Format.SHORT) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        final MetaElem e = MetaElem.RESOLUTION_UNIT;
        final String v;
        switch(buf.getShort()) {
          case 2:  v = "inches";      break;
          case 3:  v = "centimeters"; break;
          default: v = null;
        }
        if(v != null) o.deepFile.addMeta(e, v);
      }
    },

    /** Software. */
    h131(null, Format.ASCII) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        metaSimple(o, buf, f, MetaElem.SOFTWARE);
      }
    },

    /** DateTime of image creation. */
    h132(20, Format.ASCII) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        metaDate(o, buf, MetaElem.DATE_CREATED);
      }
    },
    /** Creator. */
    h13b(null, Format.ASCII) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        metaSimple(o, buf, f, MetaElem.CREATOR_NAME);
      }
    },
    /** Exif Image Width. */
    ha002(1, Format.SHORT, Format.LONG) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        final MetaElem elem = MetaElem.PIXEL_WIDTH;
        if(o.deepFile.isMetaSet(elem)) return;
        metaSimple(o, buf, f, elem);
      }
     },
     /** Exif Image Height. */
     ha003(1, Format.SHORT, Format.LONG) {
       @Override
       void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
         final MetaElem elem = MetaElem.PIXEL_HEIGHT;
         if(o.deepFile.isMetaSet(elem)) return;
         metaSimple(o, buf, f, elem);
       }
     },
    /** Exposure time in seconds. */
    h829a(1, Format.RATIONAL) {
      @Override
      public void parse0(final ExifParser o, final ByteBuffer buf,
          final Format f) {
        try {
          final double sec = readRational(o.bfc, buf);
          o.deepFile.addMeta(MetaElem.EXPOSURE_TIME_MS, sec * 1000);
          final StringBuilder str = new StringBuilder();
          if(sec < 1) str.append("1/").append((int) (1 / sec));
          else str.append((int) sec);
          str.append(" seconds");
          o.deepFile.addMeta(MetaElem.EXPOSURE_TIME, str.toString());
        } catch(final IOException e) {
          err(o, e);
        }
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
    h8769(1, Format.LONG) {
      @Override
      void parse0(final ExifParser o, final ByteBuffer buf, final Format f) {
        try {
          o.bfc.position(buf.getInt());
          o.readIFD();
        } catch(final IOException e) {
          err(o, e);
        }
      }
    },
    /** Aperture value. */
    h9202(1, Format.RATIONAL) {
      @Override
      public void parse0(final ExifParser o, final ByteBuffer buf,
          final Format f) {
        metaSimple(o, buf, f, MetaElem.APERTURE_VALUE);
      }
    },
    /** Max aperture value. */
    h9205(1, Format.RATIONAL) {
      @Override
      public void parse0(final ExifParser o, final ByteBuffer buf,
          final Format f) {
        metaSimple(o, buf, f, MetaElem.APERTURE_VALUE_MAX);
      }
    };

    /** The format of the date values. */
    private static final SimpleDateFormat SDF = new SimpleDateFormat(
        "yyyy:MM:dd HH:mm:ss");
    /** The expected value. */
    final Integer expVal;
    /** The allowed field formats. */
    final Format[] aFormats;

    /**
     * Initilizes a tag.
     * @param expectedValue the expected value.
     * @param allowedFormats formats that are allowed for the tag field.
     */
    private Tag(final Integer expectedValue, final Format... allowedFormats) {
      expVal = expectedValue;
      aFormats = allowedFormats;
    }

    /**
     * Parses the tag.
     * @param o {@link ExifParser} instance to send parser events from.
     * @param buf the {@link ByteBuffer} to read from.
     */
    void parse(final ExifParser o, final ByteBuffer buf) {
      final Format f = check(o, buf);
      if(f != null) parse0(o, buf, f);
    }

    /**
     * Tag specific parse method.
     * @param o {@link ExifParser} instance to send parser events from.
     * @param buf the {@link ByteBuffer} to read from.
     * @param f the format of the field.
     */
    abstract void parse0(final ExifParser o, final ByteBuffer buf,
        final Format f);

    /**
     * Checks if the field is valid.
     * @param o the ExifParser instance.
     * @param buf the ByteBuffer to read from.
     * @return the field format if the field is valid, <code>null</code>
     *         otherwise.
     */
    private Format check(final ExifParser o, final ByteBuffer buf) {
      final Format format = Format.getForId(buf.getShort());
      for(final Format f : aFormats) {
        if(f.equals(format)) {
          if(expVal == null || (buf.getInt() & 0xFFFFFFFFL) == expVal) return f;
        }
      }
      err(o);
      return null;
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
          "ExifParser: Invalid field found (%, file: %, error message: %)",
          toString(), o.bfc.getFileName(), ex);
    }

    /**
     * Reads an IFD ASCII field value.
     * @param b the {@link BufferedFileChannel} to read from, if the data is not
     *          inlined.
     * @param buf the {@link ByteBuffer}, containing the IFD ASCII field.
     * @return the value.
     * @throws IOException if any error occurs while reading from the file.
     */
    private byte[] readAscii(final BufferedFileChannel b, final ByteBuffer buf)
        throws IOException {
      final int size = buf.getInt();
      final byte[] data = new byte[size];
      if(size <= 4) { // data is inlined
        buf.get(data);
      } else {
        b.position(buf.getInt());
        b.buffer(size);
        b.get(data);
      }
      return data;
    }
    
    /**
     * Reads a rational value from a IFD field.
     * @param b the {@link BufferedFileChannel} to read the rational from.
     * @param buf the buffer containing the IFD field.
     * @return the rational value as double.
     * @throws IOException if any error occurs while reading from the file.
     */
    double readRational(final BufferedFileChannel b,
        final ByteBuffer buf) throws IOException {
      // [BL] are there RATIONAL fields with size > 1?
      // size was already read
      b.position(buf.getInt());
      b.buffer(8); // 2 LONGs => 8 bytes
      final long numerator = b.getInt() & 0xFFFFFFFFL;
      final long denominator = b.getInt() & 0xFFFFFFFFL;
      return (double) numerator / denominator;
    }

    /**
     * Converts the date to the correct format and adds it to the DeepFile.
     * @param o the ExifParser.
     * @param buf the ByteBuffer to read from.
     * @param elem the metadata element to use for the metadata item.
     */
    void metaDate(final ExifParser o, final ByteBuffer buf,
        final MetaElem elem) {
      try {
        o.bfc.position(buf.getInt());
        o.bfc.buffer(20);
        final byte[] data = new byte[20];
        o.bfc.get(data);
        final Date d = SDF.parse(Token.string(data));
        o.deepFile.addMeta(elem, ParserUtil.convertDateTime(d));
        return;
      } catch(final ParseException ex) {
        err(o, ex);
      } catch(final IOException e) {
        err(o, e);
      }
    }

    /**
     * Parses a field and adds the metadata to the deep file of the exif parser.
     * @param o the ExifParser.
     * @param buf the buffer to read from.
     * @param f the format of the field.
     * @param elem the MetaElement.
     */
    void metaSimple(final ExifParser o, final ByteBuffer buf, final Format f,
        final MetaElem elem) {
      final DeepFile d = o.deepFile;
      switch(f) {
        case BYTE:  d.addMeta(elem, (short) (buf.get() & 0xFF)); break;
        case ASCII:
          try {
            d.addMeta(elem, readAscii(o.bfc, buf));
          } catch(final IOException e) {
            err(o, e);
          }
          break;
        case SHORT: d.addMeta(elem, buf.getShort() & 0xFFFF); break;
        case LONG:  d.addMeta(elem, buf.getInt() & 0xFFFFFFFFL); break;
        case RATIONAL:  try {
            d.addMeta(elem, readRational(o.bfc, buf));
          } catch(final IOException e) {
            err(o, e);
          }
          break;
        case UNDEFINED: break; // [BL] implement parsing of UNDEFINED fields.
        case SLONG:     break; // [BL] implement parsing of SLONG fields.
        case SRATIONAL: break; // [BL] implement parsing of SRATIONAL fields.
        default: Main.debug("ExifParser: Unknown field type (%)", f);
      }
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
