package org.deepfs.fsml.parsers;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.datatype.XMLGregorianCalendar;
import org.basex.query.item.Type;
import org.basex.util.Token;
import org.deepfs.fsml.BufferedFileChannel;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.MetaElem;
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
     * @param s the size in bytes
     */
    private Format(final int s) {
      size = s;
    }

    /**
     * Returns the size in bytes.
     * @return the size in bytes
     */
    int getSize() {
      return size;
    }

    /**
     * Returns the field format for a id.
     * @param i the id
     * @return the field format
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
    h100(1, MetaElem.PIXEL_WIDTH, true, Format.SHORT, Format.LONG),
    /** Image length. */
    h101(1, MetaElem.PIXEL_HEIGHT, true, Format.SHORT, Format.LONG),
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
        if(d.isMetaSet(elem)) return;
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
    h132(20, MetaElem.DATETIME_CREATED, Format.ASCII),
    /** Creator. */
    h13b(MetaElem.CREATOR_NAME, Format.ASCII),
    /** White point. */
    h13e(2, MetaElem.WHITE_POINT, Format.RATIONAL),
    /** White point. */
    h13f(6, MetaElem.PRIMARY_CHROMATICITIES, Format.RATIONAL),
    /** YCbCrCoefficients. */
    h211(3, MetaElem.YCBCR_COEFFICIENTS, Format.RATIONAL),
    /** YCbCrPositioning. */
    h213(1, MetaElem.YCBCR_POSITIONING, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 1:  s = "centered"; break;
          case 2:  s = "co-sited"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** ReferenceBlackWhite. */
    h214(6, MetaElem.REFERENCE_BLACK_WHITE, Format.RATIONAL),
    /** Copyright. */
    h8298(MetaElem.RIGHTS, Format.ASCII),
    /** Exposure time in seconds. */
    h829a(1, MetaElem.EXPOSURE_TIME, Format.RATIONAL) {
      @Override
      void meta(final DeepFile d, final BufferedFileChannel b)
          throws IOException {
        final double sec = readRational(b);
        d.addMeta(MetaElem.EXPOSURE_TIME_MS, sec * 1000);
        final StringBuilder str = new StringBuilder();
        if(sec < 1) str.append("1/").append((int) (1 / sec));
        else str.append((int) sec);
        str.append(" seconds");
        d.addMeta(MetaElem.EXPOSURE_TIME, str.toString());
      }
    },
    /** FNumber. */
    h829d(1, MetaElem.F_NUMBER, Format.RATIONAL),
    /** ExposureProgram. */
    h8822(1, MetaElem.EXPOSURE_PROGRAM, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 0:  s = "not defined";       break;
          case 1:  s = "manual";            break;
          case 2:  s = "normal program";    break;
          case 3:  s = "aperture priority"; break;
          case 4:  s = "shutter priority";  break;
          case 5:
            s = "creative program (biased toward depth of field)";
            break;
          case 6:
            s = "action program (biased toward fast shutter speed)";
            break;
          case 7:
            s = "portrait mode (for closeup photos with the background out " +
                "of focus)"; break;
          case 8:
            s = "landscape mode (for landscape photos with the background in " +
                "focus)"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** GPS IFD. */
    h8825(1, null, Format.LONG) {
      @Override
      void parse(final ExifParser o, final ByteBuffer buf) {
        if(!check(o, buf)) return;
        try {
          o.bfc.position(buf.getInt());
          o.readIFD();
        } catch(final IOException e) {
          err(o.deepFile, e);
        }
      }
    },
    /** ISO speed ratings. */
    h8827(MetaElem.ISO_SPEED_RATINGS, Format.SHORT),
    /** EXIF IFD. */
    h8769(1, null, Format.LONG) {
      @Override
      void parse(final ExifParser o, final ByteBuffer buf) {
        if(!check(o, buf));
        try {
          o.bfc.position(buf.getInt());
          o.readIFD();
        } catch(final IOException e) {
          err(o.deepFile, e);
        }
      }
    },
    /** DateTimeOriginal. */
    h9003(20, MetaElem.DATETIME_ORIGINAL, Format.ASCII),
    /** DateTimeDigitized. */
    h9004(20, MetaElem.DATETIME_DIGITIZED, Format.ASCII),
    /** CompressedBitsPerPixel. */
    h9102(1, MetaElem.COMPRESSED_BITS_PER_PIXEL, Format.RATIONAL),
    /** Shutter speed value. */
    h9201(1, MetaElem.SHUTTER_SPEED_VALUE, Format.SRATIONAL),
    /** Aperture value. */
    h9202(1, MetaElem.APERTURE_VALUE, Format.RATIONAL),
    /** Brightness value. */
    h9203(1, MetaElem.BRIGHTNESS_VALUE, Format.SRATIONAL),
    /** Exposure bias value. */
    h9204(1, MetaElem.EXPOSURE_BIAS_VALUE, Format.SRATIONAL),
    /** Max aperture value. */
    h9205(1, MetaElem.APERTURE_VALUE_MAX, Format.RATIONAL),
    /** Subject distance. */
    h9206(1, MetaElem.SUBJECT_DISTANCE, Format.RATIONAL),
    /** Metering mode. */
    h9207(1, MetaElem.METERING_MODE, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        final int v = b.getShort();
        switch(v) {
          case 0:   s = "unknown";                 break;
          case 1:   s = "average";                 break;
          case 2:   s = "center weighted average"; break;
          case 3:   s = "spot";                    break;
          case 4:   s = "multi spot";              break;
          case 5:   s = "pattern";                 break;
          case 6:   s = "partial";                 break;
          default:  s = v == 255 ? "other" : null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Light source. */
    h9208(1, MetaElem.LIGHT_SOURCE, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        final int v = b.getShort();
        switch(v) {
          case 0:  s = "unknown"; break;
          case 1:  s = "daylight"; break;
          case 2:  s = "fluorescent"; break;
          case 3:  s = "tungsten (incandescent light)"; break;
          case 4:  s = "flash"; break;
          case 5:  s = null; break;
          case 6:  s = null; break;
          case 7:  s = null; break;
          case 8:  s = null; break;
          case 9:  s = "fine weather"; break;
          case 10: s = "cloudy weather"; break;
          case 11: s = "shade"; break;
          case 12: s = "daylight fluorescent (D 5700 - 7100K)"; break;
          case 13: s = "day white fluorescent ( N 4600 - 5400K)"; break;
          case 14: s = "cool white fluorescent (W 3900 - 4500K)"; break;
          case 15: s = "white fluorescent (WW 3200 - 3700K)"; break;
          case 16: s = null; break;
          case 17: s = "standard light A"; break;
          case 18: s = "standard light B"; break;
          case 19: s = "standard light C"; break;
          case 20: s = "D55"; break;
          case 21: s = "D65"; break;
          case 22: s = "D75"; break;
          case 23: s = "D50"; break;
          case 24: s = "ISO studio tungsten"; break;
          default: s = v == 255 ? "other light source" : null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Flash. */
    h9209(1, MetaElem.FLASH, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        final int v = b.getShort();
        switch(v) {
          case 0:  s = "flash did not fire."; break;
          case 1:  s = "flash fired."; break;
          case 5:  s = "strobe return light not detected."; break;
          case 7:  s = "strobe return light detected."; break;
          case 9:  s = "flash fired, compulsory flash mode"; break;
          case 13: s = "flash fired, compulsory flash mode, return light not " +
              "detected"; break;
          case 15: s = "flash fired, compulsory flash mode, return light " +
              "detected"; break;
          case 16: s = "flash did not fire, compulsory flash mode"; break;
          case 24: s = "flash did not fire, auto mode"; break;
          case 25: s = "flash fired, auto mode"; break;
          case 29: s = "flash fired, auto mode, return light not " +
              "detected"; break;
          case 31: s = "flash fired, auto mode, return light detected"; break;
          case 32: s = "no flash function"; break;
          case 65: s = "flash fired, red-eye reduction mode"; break;
          case 69: s = "flash fired, red-eye reduction mode, return light " +
              "not detected"; break;
          case 71: s = "flash fired, red-eye reduction mode, return light " +
              "detected"; break;
          case 73: s = "flash fired, compulsory flash mode, red-eye " +
              "reduction mode"; break;
          case 77: s = "flash fired, compulsory flash mode, red-eye " +
              "reduction mode, return light not detected"; break;
          case 79: s = "flash fired, compulsory flash mode, red-eye " +
              "reduction mode, return light detected"; break;
          case 89: s = "flash fired, auto mode, red-eye reduction mode"; break;
          case 93: s = "flash fired, auto mode, return light not detected, " +
              "red-eye reduction mode"; break;
          case 95: s = "flash fired, auto mode, return light detected, " +
              "red-eye reduction mode"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Focal length in mm. */
    h920a(1, MetaElem.FOCAL_LENGTH, Format.RATIONAL),
    /** User comment. */
    h9286(MetaElem.COMMENT, Format.ASCII),
    // [BL] parse subsecond data
    // /** Subsecond data - fractions of seconds for the original datetime. */
    // h9291(null, Format.ASCII),
    // /**
    // * Subsecond data - fractions of seconds for the date time digitalized
    // tag.
    // */
    // h9292(null, Format.ASCII),
    /** Color space. */
    ha001(1, MetaElem.COLOR_SPACE, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort() & 0xFFFF) {
          case 1:      s = "sRGB";         break;
          case 0xFFFF: s = "uncalibrated"; break;
          default:     s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Exif Image Width. */
    ha002(1, MetaElem.PIXEL_WIDTH, true, Format.SHORT, Format.LONG),
    /** Exif Image Height. */
    ha003(1, MetaElem.PIXEL_HEIGHT, true, Format.SHORT, Format.LONG),
    /** Related sound file. */
    ha004(13, MetaElem.RELATED_SOUND_FILE, Format.ASCII),
    /** Focal plane X resolution. */
    ha20e(1, MetaElem.FOCAL_PLANE_X_RESOLUTION, Format.RATIONAL),
    /** Focal plane y resolution. */
    ha20f(1, MetaElem.FOCAL_PLANE_Y_RESOLUTION, Format.RATIONAL),
    /** Focal plane resolution unit. */
    ha210(1, MetaElem.FOCAL_PLANE_RESOLUTION_UNIT, Format.SHORT) {
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
    /** Exposure index. */
    ha215(1, MetaElem.EXPOSURE_INDEX, Format.RATIONAL),
    /** Sensing method. */
    ha217(1, MetaElem.SENSING_METHOD, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 1:  s = "not defined";                    break;
          case 2:  s = "one-chip color area sensor";     break;
          case 3:  s = "two-chip color area sensor";     break;
          case 4:  s = "three-chip color area sensor";   break;
          case 5:  s = "color sequential area sensor";   break;
          case 6:  s = null; break;
          case 7:  s = "trilinear sensor";               break;
          case 8:  s = "color sequential linear sensor"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Custom rendered. */
    ha401(1, MetaElem.CUSTOM_RENDERED, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 0:  s = "normal process"; break;
          case 1:  s = "custom process"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Exposure mode. */
    ha402(1, MetaElem.EXPOSURE_MODE, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 0:  s = "auto exposure";   break;
          case 1:  s = "manual exposure"; break;
          case 2:  s = "auto bracket";    break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** White balance. */
    ha403(1, MetaElem.WHITE_BALANCE, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 0:  s = "auto white balance";   break;
          case 1:  s = "manual white balance"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Digital zoom ratio. */
    ha404(1, MetaElem.DIGITAL_ZOOM_RATIO, Format.RATIONAL),
    /** Focal length in 35mm film. */
    ha405(1, MetaElem.FOCAL_LENGTH_IN_35MM_FILM, Format.SHORT),
    /** Scene capture type. */
    ha406(1, MetaElem.SCENE_CAPTURE_TYPE, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 0:  s = "standard";    break;
          case 1:  s = "landscape";   break;
          case 2:  s = "portrait";    break;
          case 3:  s = "night scene"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Gain control. */
    ha407(1, MetaElem.GAIN_CONTROL, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 0:  s = "none";           break;
          case 1:  s = "low gain up";    break;
          case 2:  s = "high gain up";   break;
          case 3:  s = "low gain down";  break;
          case 4:  s = "high gain down"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Contrast. */
    ha408(1, MetaElem.CONTRAST, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 0:  s = "normal"; break;
          case 1:  s = "soft";   break;
          case 2:  s = "hard";   break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Saturation. */
    ha409(1, MetaElem.SATURATION, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 0:  s = "normal";          break;
          case 1:  s = "low saturation";  break;
          case 2:  s = "high saturation"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Sharpness. */
    ha40a(1, MetaElem.SHARPNESS, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 0:  s = "normal"; break;
          case 1:  s = "soft"; break;
          case 2:  s = "hard"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    },
    /** Subject distance range. */
    ha40c(1, MetaElem.SUBJECT_DISTANCE_RANGE, Format.SHORT) {
      @Override
      void meta(final DeepFile d, final ByteBuffer b) { // always inlined
        final String s;
        switch(b.getShort()) {
          case 0:  s = "unknown";      break;
          case 1:  s = "macro";        break;
          case 2:  s = "close view";   break;
          case 3:  s = "distant view"; break;
          default: s = null;
        }
        if(s != null) d.addMeta(elem, s);
      }
    };

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
    /** Flag if the meta element must be checked to be unique. */
    final boolean unique;

    /**
     * Initializes a tag.
     * @param metaElem the corresponding {@link MetaElem}
     * @param allowedFormats formats that are allowed for the tag field
     */
    private Tag(final MetaElem metaElem, final Format... allowedFormats) {
      this(-1, metaElem, false, allowedFormats);
    }

    /**
     * Initializes a tag.
     * @param expectedCount the expected number of values
     * @param metaElem the corresponding {@link MetaElem}
     * @param allowedFormats formats that are allowed for the tag field
     */
    private Tag(final int expectedCount, final MetaElem metaElem,
        final Format... allowedFormats) {
      this(expectedCount, metaElem, false, allowedFormats);
    }

    /**
     * Initializes a tag.
     * @param expectedCount the expected number of values
     * @param metaElem the corresponding {@link MetaElem}
     * @param checkExisting if true, the value for the metaElem is only set if
     *          there was no value set previously
     * @param allowedFormats formats that are allowed for the tag field
     */
    private Tag(final int expectedCount, final MetaElem metaElem,
        final boolean checkExisting, final Format... allowedFormats) {
      assert expectedCount > 0 || expectedCount == -1;
      defaultCount = expectedCount;
      elem = metaElem;
      aFormats = allowedFormats;
      unique = checkExisting;
    }

    /**
     * Parses the tag.
     * @param o {@link ExifParser} instance to send parser events from
     * @param buf the {@link ByteBuffer} to read from
     * @throws IOException if any error occurs
     */
    void parse(final ExifParser o, final ByteBuffer buf) throws IOException {
      if(!check(o, buf)) return;
      if(inlined) meta(o.deepFile, buf);
      else meta(o.deepFile, o.bfc);
    }

    /**
     * Checks if the field is valid.
     * @param o the ExifParser instance
     * @param buf the ByteBuffer to read from
     * @return true if the current IFD field is valid
     */
    boolean check(final ExifParser o, final ByteBuffer buf) {
      format = Format.getForId(buf.getShort());
      for(final Format f : aFormats) {
        if(f.equals(format)) {
          final long c = buf.getInt() & 0xFFFFFFFFL; // read count value
          if (c > Integer.MAX_VALUE || c < 1) {
            err(o.deepFile, "Invalid item count (" + c + ")");
            return false;
          }
          if(defaultCount == -1) count = (int) c;
          else if(defaultCount != c) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Invalid item count (expected: ");
            sb.append(defaultCount).append(", found: ");
            sb.append(c).append(")");
            err(o.deepFile, sb.toString());
            return false;
          } else count = defaultCount;
          final int size = count * format.getSize();
          if(size <= 4) inlined = true;
          else {
            try { // prepare the byte buffer
              o.bfc.position(buf.getInt());
              o.bfc.buffer(size);
            } catch(final IOException e) {
              err(o.deepFile, e);
              return false;
            }
            inlined = false;
          }
          return true;
        }
      }
      final StringBuilder sb = new StringBuilder();
      sb.append("Invalid field format (expected: ");
      int i = 0;
      final int numFormats = aFormats.length;
      sb.append(aFormats[i++]);
      for(final int max = numFormats - 1; i < max; i++)
        sb.append(", ").append(aFormats[i]);
      if (i < numFormats) sb.append(" or ").append(aFormats[i]);
      sb.append(", found: ").append(format).append(")");
      err(o.deepFile, sb.toString());
      return false;
    }

    /**
     * Reads the metadata from the ByteBuffer and adds it to the DeepFile. May
     * be overridden for complex fields that occupy at most 4 bytes (and are
     * inlined in the IFD field)
     * @param d the DeepFile to add the metadata to
     * @param b the ByteBuffer to read from
     */
    void meta(final DeepFile d, final ByteBuffer b) {
      if(unique && d.isMetaSet(elem)) return;
      metaSimple(d, b);
    }

    /**
     * Reads the metadata from the BufferedFileChannel and adds it to the
     * DeepFile. May be overridden for complex fields that occupy at least 5
     * bytes (and are not inlined in the IFD field).
     * @param d the DeepFile to add the metadata to
     * @param b the {@link BufferedFileChannel} to read from
     * @throws IOException if any error occurs
     */
    @SuppressWarnings("unused")
    void meta(final DeepFile d, final BufferedFileChannel b)
        throws IOException {
      if(unique && d.isMetaSet(elem)) return;
      metaSimple(d, b);
    }

    /**
     * Parses an inlined field and adds the metadata to the deep file.
     * @param d the {@link DeepFile} to add the metadata to
     * @param b the buffer to read from
     */
    void metaSimple(final DeepFile d, final ByteBuffer b) {
      switch(format) {
        case BYTE:
          if(count == 1) d.addMeta(elem, readByte(b));
          else {
            final StringBuilder sbb = new StringBuilder();
            for(int i = 0, max = count - 1; i < max; i++)
              sbb.append(readByte(b)).append(", ");
            sbb.append(readByte(b));
            d.addMeta(elem, sbb.toString());
          }
          break;
        case ASCII: d.addMeta(elem, readAscii(b)); break;
        case SHORT:
          if(count == 1) d.addMeta(elem, readShort(b));
          else {
            final StringBuilder sbs = new StringBuilder();
            for(int i = 0, max = count - 1; i < max; i++)
              sbs.append(readShort(b)).append(", ");
            sbs.append(readShort(b));
            d.addMeta(elem, sbs.toString());
          }
          break;
        case LONG:  d.addMeta(elem, readLong(b));  break;
        case SLONG: d.addMeta(elem, readSLong(b)); break;
        default:
          d.debug("ExifParser: Unknown or unsupported field type for " +
              "inlined data (%)", format);
      }
    }

    /**
     * Parses a field that is not inlined and adds the metadata to the deep
     * file.
     * @param d the {@link DeepFile} to add the metadata to
     * @param b the {@link BufferedFileChannel} to read from
     */
    void metaSimple(final DeepFile d, final BufferedFileChannel b) {
      try {
        switch(format) {
          case BYTE:
            if(count == 1) d.addMeta(elem, readByte(b));
            else {
              final StringBuilder sbb = new StringBuilder();
              for(int i = 0, max = count - 1; i < max; i++)
                sbb.append(readByte(b)).append(", ");
              sbb.append(readByte(b));
              d.addMeta(elem, sbb.toString());
            }
            break;
          case ASCII:
            if(elem.getType().instance(Type.DTM)) d.addMeta(elem, readDate(b));
            else d.addMeta(elem, readAscii(b));
            break;
          case SHORT:
            if(count == 1) d.addMeta(elem, readShort(b));
            else {
              final StringBuilder sbs = new StringBuilder();
              for(int i = 0, max = count - 1; i < max; i++)
                sbs.append(readShort(b)).append(", ");
              sbs.append(readShort(b));
              d.addMeta(elem, sbs.toString());
            }
            break;
          case LONG:
            if(count == 1) d.addMeta(elem, readLong(b));
            else {
              final StringBuilder sbl = new StringBuilder();
              for(int i = 0, max = count - 1; i < max; i++)
                sbl.append(readLong(b)).append(", ");
              sbl.append(readLong(b));
              d.addMeta(elem, sbl.toString());
            }
            break;
          case RATIONAL:
            if(count == 1) d.addMeta(elem, readRational(b));
            else {
              final StringBuilder sbr = new StringBuilder();
              for(int i = 0; i < count; i++) {
                final double db = readRational(b);
                final long rounded = Math.round(db);
                if(rounded == db) sbr.append(rounded);
                else sbr.append(db);
                sbr.append(", ");
              }
              d.addMeta(elem, sbr.substring(0, sbr.length() - 2));
            }
            break;
          case SLONG:
            if(count == 1) d.addMeta(elem, readSLong(b));
            else {
              final StringBuilder sbsl = new StringBuilder();
              for(int i = 0, max = count - 1; i < max; i++)
                sbsl.append(readSLong(b)).append(", ");
              sbsl.append(readSLong(b));
              d.addMeta(elem, sbsl.toString());
            }
            break;
          case SRATIONAL:
            if(count == 1) d.addMeta(elem, readSRational(b));
            else {
              final StringBuilder sbsr = new StringBuilder();
              for(int i = 0; i < count; i++) {
                final double db = readSRational(b);
                final long rounded = Math.round(db);
                if(rounded == db) sbsr.append(rounded);
                else sbsr.append(db);
                sbsr.append(", ");
              }
              d.addMeta(elem, sbsr.substring(0, sbsr.length() - 2));
            }
            break;
          default:
            d.debug("ExifParser: Unknown or unsupported field type for " +
                "non-inlined data (%)", format);
        }
      } catch(final Exception e) {
        err(d, e.getMessage());
      }
    }

    /**
     * Reads a byte from the {@link ByteBuffer}.
     * @param b the {@link ByteBuffer} to read from
     * @return the byte value
     */
    short readByte(final ByteBuffer b) {
      return (short) (b.get() & 0xFF);
    }

    /**
     * Reads a byte from the {@link BufferedFileChannel}.
     * @param b the {@link BufferedFileChannel} to read from
     * @return the byte value
     * @throws IOException if any error occurs
     */
    short readByte(final BufferedFileChannel b) throws IOException {
      return (short) b.get();
    }

    /**
     * Reads a short from the {@link ByteBuffer}.
     * @param b the {@link ByteBuffer} to read from
     * @return the short value
     */
    int readShort(final ByteBuffer b) {
      return b.getShort() & 0xFFFF;
    }

    /**
     * Reads a short from the {@link BufferedFileChannel}.
     * @param b the {@link BufferedFileChannel} to read from
     * @return the short value
     * @throws IOException if any error occurs
     */
    int readShort(final BufferedFileChannel b) throws IOException {
      return b.getShort();
    }

    /**
     * Reads an unsigned long from the {@link ByteBuffer}.
     * @param b the {@link ByteBuffer} to read from
     * @return the long value
     */
    long readLong(final ByteBuffer b) {
      return b.getInt() & 0xFFFFFFFFL;
    }

    /**
     * Reads an unsigned long from the {@link BufferedFileChannel}.
     * @param b the {@link BufferedFileChannel} to read from
     * @return the long value
     * @throws IOException if any error occurs
     */
    long readLong(final BufferedFileChannel b) throws IOException {
      return b.getInt() & 0xFFFFFFFFL;
    }

    /**
     * Reads a signed long from the {@link ByteBuffer}.
     * @param b the {@link ByteBuffer} to read from
     * @return the long value
     */
    int readSLong(final ByteBuffer b) {
      return b.getInt();
    }

    /**
     * Reads a signed long from the {@link BufferedFileChannel}.
     * @param b the {@link BufferedFileChannel} to read from
     * @return the long value
     * @throws IOException if any error occurs
     */
    int readSLong(final BufferedFileChannel b) throws IOException {
      return b.getInt();
    }

    /**
     * Reads an ASCII value from the {@link ByteBuffer} and returns it as byte
     * array.
     * @param b the {@link ByteBuffer} to read from
     * @return the ASCII value as byte array
     */
    byte[] readAscii(final ByteBuffer b) {
      return b.get(new byte[count]).array();
    }

    /**
     * Reads an ASCII value from the {@link BufferedFileChannel} and returns it
     * as byte array.
     * @param b the {@link BufferedFileChannel} to read from
     * @return the ASCII value as byte array
     * @throws IOException if any error occurs while reading from the channel
     */
    byte[] readAscii(final BufferedFileChannel b)
        throws IOException {
      return b.get(new byte[count]);
    }

    /**
     * Reads a rational value from a IFD field.
     * @param b the {@link BufferedFileChannel} to read the rational from
     * @return the rational value as double
     * @throws IOException if any error occurs 
     */
    double readRational(final BufferedFileChannel b) throws IOException {
      final long numerator = readLong(b);
      final long denominator = readLong(b);
      return (double) numerator / denominator;
    }

    /**
     * Reads a signed rational value from a IFD field.
     * @param b the {@link BufferedFileChannel} to read the rational from
     * @return the rational value as double
     * @throws IOException if any error occurs
     */
    double readSRational(final BufferedFileChannel b) throws IOException {
      final int numerator = readSLong(b);
      final int denominator = readSLong(b);
      return (double) numerator / denominator;
    }

    /**
     * Reads a date value from the {@link BufferedFileChannel} and returns it as
     * {@link XMLGregorianCalendar}.
     * @param b the {@link BufferedFileChannel} to read from
     * @return the date value
     * @throws ParseException if the date could not be parsed
     * @throws IOException if any error occurs while reading from the channel
     */
    XMLGregorianCalendar readDate(final BufferedFileChannel b)
        throws ParseException, IOException {
      final Date date = SDF.parse(Token.string(readAscii(b)));
      return ParserUtil.convertDateTime(date);
    }

    /**
     * Generates a debug message.
     * @param d the {@link DeepFile}
     * @param ex the exception to log
     */
    void err(final DeepFile d, final Exception ex) {
      err(d, ex.getMessage());
    }

    /**
     * Generates a debug message.
     * @param d the {@link DeepFile}
     * @param ex the message to log
     */
    void err(final DeepFile d, final String ex) {
      d.debug("ExifParser: Invalid field (field id: % (%), error message: %)",
          this, elem, ex);
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
   * @param f the {@link BufferedFileChannel} to read from
   * @return true if the IFD is valid, false otherwise
   * @throws IOException if any error occurs while reading from the channel
   */
  boolean check(final BufferedFileChannel f) throws IOException {
    try {
      f.buffer(4);
    } catch(final EOFException e) {
      return false;
    }
    if(checkEndianness(f) && f.getShort() == 0x2A) return true;
    f.setByteOrder(ByteOrder.BIG_ENDIAN);
    return false;
  }

  /**
   * Parses the Exif data.
   * @param df the DeepFile to store metadata and file content
   * @throws IOException if any error occurs while reading from the channel
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
    try {
      readIFD();
    } catch(final IOException e) {
      throw e;
    } finally {
      bfc.setByteOrder(ByteOrder.BIG_ENDIAN); // set byte order to default value
    }
  }

  /**
   * Reads an IFD (an array of fixed length fields) from the current file
   * channel position. At least 2 bytes have to be buffered.
   * @throws IOException if any error occurs while reading from the file
   *           channel
   */
  void readIFD() throws IOException {
    bfc.buffer(2);
    final int numFields = bfc.getShort();
    // one field contains 12 bytes
    final byte[] buf = bfc.get(new byte[numFields * 12]);
    for(int i = 0; i < numFields; i++) {
      readField(ByteBuffer.wrap(buf, i * 12, 12).order(bfc.getByteOrder()));
    }
  }

  // ----- utility methods -----------------------------------------------------

  /**
   * Checks if the two endianness bytes are correct and set the ByteBuffer's
   * endianness according to these bytes.
   * @param f the {@link BufferedFileChannel} to read from
   * @return true if the endianness bytes are valid, false otherwise
   * @throws IOException if any error occurs
   */
  private boolean checkEndianness(final BufferedFileChannel f)
      throws IOException {
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
   * @param data the {@link ByteBuffer} containing the field data
   * @throws IOException if any error occurs
   */
  private void readField(final ByteBuffer data) throws IOException {
    final int tagNr = data.getShort() & 0xFFFF;
    try {
      final Tag tag = Tag.valueOf("h" + Integer.toHexString(tagNr));
      tag.parse(this, data);
    } catch(final IllegalArgumentException ex) { /* tag is not registered. */}
  }
}
