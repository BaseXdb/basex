package org.basex.build.fs.metadata;

import static org.basex.build.fs.FSText.*;
import static org.basex.util.Token.*;
import java.util.HashMap;
import org.basex.BaseX;
import org.basex.util.Array;
import org.basex.util.TokenBuilder;

/**
 * This class indexes EXIF meta data.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class EXIFIndex {
  /** Data Format. */
  static final int STRING = 2;
  /** Data Format. */
  static final int SHORT = 3;
  /** Data Format. */
  static final int LONG = 4;
  /** Data Format. */
  static final int RATIONAL = 5;
  /** Data Format. */
  static final int SIGNEDRATIONAL = 10;
  /** Data Format. */
  static final int UNDEFINED = 7;
  /** EXIF information. */
  HashMap<Integer, EXIFInfo> info = new HashMap<Integer, EXIFInfo>();

  /**
   * Constructor.
   */
  EXIFIndex() {
    info = new HashMap<Integer, EXIFInfo>();
    info.put(0x010E, new EXIFString("ImageDescription"));
    info.put(0x010F, new EXIFString("Make"));
    info.put(0x0110, new EXIFString("Model"));
    info.put(0x0112, new EXIFShort("Orientation",
      bytes("", "Top Left", "Top Right", "Bottom Right", "Bottom Left",
          "Left Top", "Right Top", "Right Bottom", "Left Bottom")));
    info.put(0x011A, new EXIFRational("XResolution"));
    info.put(0x011B, new EXIFRational("YResolution"));
    info.put(0x0128, new EXIFShort("ResolutionUnit",
      bytes("", "None", "Inch", "Cm")));
    info.put(0x0131, new EXIFString("Software"));
    info.put(0x0132, new EXIFString("DateTime"));
    info.put(0x013B, new EXIFString("Artist"));
    info.put(0x013E, new EXIFRational("WhitePoint"));
    info.put(0x013F, new EXIFRational("PrimaryChromaticities"));
    info.put(0x0211, new EXIFRational("YCbCrCoefficients"));
    info.put(0x0213, new EXIFShort("YCbCrPositioning",
      bytes("", "Centered", "Co-Sited")));
    info.put(0x0214, new EXIFRational("ReferenceBlackWhite"));
    info.put(0x8298, new EXIFString("Copyright"));
    info.put(0x829A, new EXIFRational("ExposureTime", 1));
    info.put(0x829D, new EXIFRational("FNumber"));
    info.put(0x8773, new EXIFUndefined("InterColorProfile", 0));
    info.put(0x8822, new EXIFShort("ExposureProgram",
      bytes("", "Manual", "Normal", "Aperture Priority", "Shutter Priority",
          "Slow", "High-Speed", "Portrait", "Landscape")));
    info.put(0x8825, new EXIFLong("GPSInfo"));
    info.put(0x8827, new EXIFShort("ISOSpeedRatings"));
    info.put(0x9000, new EXIFUndefined("ExifVersion", 1));
    info.put(0x9003, new EXIFString("DateTimeOriginal"));
    info.put(0x9004, new EXIFString("DateTimeDigitized"));
    info.put(0x9101, new EXIFUndefined("ComponentsConfiguration", 2));
    info.put(0x9102, new EXIFRational("CompressedBitsPerPixel"));
    info.put(0x9201, new EXIFSignedRational("ShutterSpeedValue", 1));
    info.put(0x9202, new EXIFRational("ApertureValue", 2));
    info.put(0x9203, new EXIFSignedRational("BrightnessValue"));
    info.put(0x9204, new EXIFSignedRational("ExposureBiasValue"));
    info.put(0x9205, new EXIFRational("MaxApertureValue", 2));
    info.put(0x9206, new EXIFRational("SubjectDistance"));
    info.put(0x9207, new EXIFShort("MeteringMode",
      bytes("", "Average", "Center Weighted Average", "Spot",
          "Multi-Spot", "Multi-Segment", "Partial", "Other")));
    info.put(0x9208, new EXIFShort("LightSource",
      bytes("Auto", "Daylight", "Fluorescent", "Tungsten", "Flash",
          "Standard Light A", "Standard Light B", "Standard Light C",
          "D55", "D65", "D75", "Other")));
    info.put(0x9209, new EXIFShort("Flash", bytes(
      "No Flash.", "Flash fired.", "", "", "", "Strobe return light not " +
      "detected.", "", "Strobe return light detected.", "", "Flash fired, " +
      "compulsory flash mode", "", "", "", "Flash fired, compulsory flash " +
      "mode, no return light detected", "", "Flash fired, compulsory flash " +
      "mode, return light detected", "No Flash, compulsory flash mode", "",
      "", "", "", "", "", "", "No Flash, auto mode", "Flash fired, auto mode",
      "", "", "", "Flash fired, auto mode, no return light detected", "",
      "Flash fired, auto mode, return light detected", "No flash function",
      "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
      "", "", "", "", "", "", "", "", "", "", "", "", "", "", "Flash " +
      "fired, red-eye reduction mode", "", "", "", "Flash fired, red-eye " +
      "reduction mode, no return light detected", "", "Flash fired, " +
      "red-eye reduction mode, return light detected", "", "Flash fired, " +
      "compulsory flash mode, red-eye reduction mode", "", "", "", "Flash " +
      "fired, compulsory flash mode, red-eye reduction mode, no return " +
      "light detected", "", "Flash fired, compulsory flash mode, red-eye " +
      "reduction mode, return light detected", "", "", "", "", "", "", "",
      "", "", "Flash fired, auto mode, red-eye reduction mode", "", "", "",
      "Flash fired, auto mode, no return light detected, red-eye reduction " +
      "mode", "", "Flash fired, auto mode, return light detected, red-eye " +
      "reduction mode"))
    );
    //exifinfo.put(0x9209, new EXIFShort("Flash",
    // bytes("None", "Fired", "Fired without Strobe Return Light",
    // "Fired with Strobe Return Light")));
    info.put(0x920A, new EXIFRational("FocalLength", bytes(" mm")));
    info.put(0x920C, new EXIFUndefined("SpatialFrequencyResponse", 0));
    info.put(0x920D, new EXIFUndefined("Noise", 0));
    info.put(0x9286, new EXIFString("UserComment"));
    info.put(0x9291, new EXIFString("SubsecTimeOriginal"));
    info.put(0x9292, new EXIFString("SubsecTimeDigitized"));
    info.put(0xA000, new EXIFUndefined("FlashPixVersion", 0));
    info.put(0xA001, new EXIFShort("ColorSpace",
        bytes("Invalid", "sRGB", "Uncalibrated")));
    info.put(0xA002, new EXIFLong("ExifImageWidth"));
    info.put(0xA003, new EXIFLong("ExifImageHeight"));
    info.put(0xA004, new EXIFString("RelatedSoundFile"));
    info.put(0xA005, new EXIFLong("ExifInteroperabilityOffset"));
    info.put(0xA20E, new EXIFRational("FocalPlaneXResolution"));
    info.put(0xA20F, new EXIFRational("FocalPlaneYResolution"));
    info.put(0xA210, new EXIFShort("FocalPlaneResolutionUnit",
      bytes("", "none", "inch", "cm")));
    info.put(0xA215, new EXIFRational("ExposureIndex"));
    info.put(0xA217, new EXIFShort("SensingMethod", bytes(
      "Undefined", "One-Chip Color Area Sensor", "Two-Chip Color Area Sensor",
      "Three-Chip Color Area Sensor", "Color Sequential Area Sensor",
      "Trilinear Sensor", "Color Sequential Linear Sensor")));
    info.put(0xA300, new EXIFUndefined("FileSource",
      bytes("", "", "", "Digital Still Camera (DSC)", "")));
    info.put(0xA301, new EXIFUndefined("SceneType",
      bytes("", "Directly Photographed")));
    info.put(0xA401, new EXIFShort("CustomRendered",
      bytes("Normal Process", "Custom Process")));
    info.put(0xA402, new EXIFShort("ExposureMode",
      bytes("Auto", "Manual", "Auto Bracket")));
    info.put(0xA403, new EXIFShort("WhiteBalance",
      bytes("Auto", "Manual")));
    info.put(0xA404, new EXIFRational("DigitalZoomRatio", bytes(" x")));
    info.put(0xA405, new EXIFShort("FocalLengthIn35mmFilm"));
    info.put(0xA406, new EXIFShort("SceneCaptureType",
      bytes("Standard", "Landscape", "Portrait", "Night Scene")));
    info.put(0xA407, new EXIFShort("GainControl",
      bytes("None", "Low Gain Up", "High Gain Up", "Low Gain Down",
          "High Gain Down")));
    info.put(0xA408, new EXIFShort("Contrast",
      bytes("Normal", "Soft", "Hard")));
    info.put(0xA409, new EXIFShort("Saturation",
      bytes("Normal", "Low", "High")));
    info.put(0xA40A, new EXIFShort("Sharpness",
      bytes("Normal", "Soft", "Hard")));
    info.put(0xA40C, new EXIFShort("SubjectDistanceRange",
      bytes("Unknown", "Macro", "Close", "Distant")));
  }

  /**
   * Converts a string to a byte array.
   * @param str string array
   * @return byte array
   */
  static byte[] bytes(final String str) {
    return token(str);
  }

  /**
   * Converts strings to byte arrays.
   * @param str string array
   * @return byte array
   */
  static byte[][] bytes(final String... str) {
    final byte[][] val = new byte[str.length][];
    for(int v = 0; v < val.length; v++) val[v] = bytes(str[v]);
    return val;
  }

  /**
   * Returns an EXIF information instance.
   * @param tagnr tag number
   * @return EXIF information instance
   */
  EXIFInfo get(final int tagnr) {
    return info.get(tagnr);
  }

  /**
   * EXIF Information structure.
   */
  abstract static class EXIFInfo {
    /** Tag Name. */
    byte[] tag;
    /** Format. */
    int format;

    /**
     * Constructor.
     * @param t tag name
     * @param f format
     */
    EXIFInfo(final String t, final int f) {
      tag = bytes(t);
      format = f;
    }

    /**
     * Returns a byte array for the specified values.
     * @param obj values
     * @return byte array
     */
    abstract byte[] val(Object... obj);
  }

  /**
   * EXIF Information structure.
   */
  static class EXIFString extends EXIFInfo {
    /**
     * Constructor.
     * @param t tag name
     */
    EXIFString(final String t) {
      super(t, STRING);
    }

    @Override
    byte[] val(final Object... obj) {
      final byte[] buffer = (byte[]) obj[0];
      final int v = ((Integer) obj[1]).intValue();
      final int n = ((Integer) obj[2]).intValue();

      // chop value...
      int s = v - 1;
      int e = v + n;
      while(++s < e) if(buffer[s] > ' ') break;
      while(--e > s) if(buffer[e] > ' ') break;
      if(s >= e) return EMPTY;

      /*if(type == 1) {
        GregorianCalendar gc = new GregorianCalendar(
            toInt(buffer, s, s + 4),
            toInt(buffer, s + 5, s + 7),
            toInt(buffer, s + 8, s + 10),
            toInt(buffer, s + 11, s + 13),
            toInt(buffer, s + 14, s + 16),
            toInt(buffer, s + 17, s + 19));
        return toBytes(gc.getTimeInMillis());
      }*/

      return Array.create(buffer, s, e + 1 - s);
    }
  }

  /**
   * EXIF Information structure.
   */
  static class EXIFShort extends EXIFInfo {
    /** Format. */
    private byte[][] cat;

    /**
     * Constructor.
     * @param t tag name
     */
    EXIFShort(final String t) {
      super(t, SHORT);
    }

    /**
     * Constructor.
     * @param t tag name
     * @param c categories
     */
    EXIFShort(final String t, final byte[][] c) {
      this(t);
      cat = c;
    }

    @Override
    byte[] val(final Object... obj) {
      final int v = ((Integer) obj[0]).intValue();
      return cat != null ? cat[Math.min(cat.length - 1, v)] : token(v);
    }
  }

  /**
   * EXIF Information structure.
   */
  static class EXIFLong extends EXIFShort {
    /**
     * Constructor.
     * @param t tag name
     */
    EXIFLong(final String t) {
      super(t);
      format = LONG;
    }
  }

  /**
   * EXIF Information structure.
   */
  static class EXIFRational extends EXIFInfo {
    /** Root of two. */
    private static final double ROOT2 = Math.pow(2, 0.5);
    /** Info text. */
    private byte[] info;
    /** Format. */
    private int type;

    /**
     * Constructor.
     * @param t tag name
     */
    EXIFRational(final String t) {
      super(t, RATIONAL);
    }

    /**
     * Constructor.
     * @param t tag name
     * @param p type
     */
    EXIFRational(final String t, final int p) {
      this(t);
      type = p;
    }

    /**
     * Constructor.
     * @param t tag name
     * @param i info text
     */
    EXIFRational(final String t, final byte[] i) {
      this(t);
      info = i;
    }

    @Override
    byte[] val(final Object... obj) {
      double v = (double) (Integer) obj[1] / (Integer) obj[0];
      if(info != null) return concat(num(v), info);
      if(type == 0) return num(v);

      // ExposureTime
      if(type == 1) {
        byte[] val;
        if(v < 1) {
          val = concat(bytes("1/"), num(1 / v));
        } else {
          val = num(v);
        }
        return concat(val, bytes(" seconds"));
      }

      // ApertureValue
      if(type == 2) {
        v = Math.round(Math.pow(ROOT2, v) * 100) / 100d;
        return concat(bytes("F "), num(v));
      }

      BaseX.debug(EXIFRATIONAL);
      return EMPTY;
    }

    /**
     * Returns double value as byte array.
     * @param v double value
     * @return byte array
     */
    private byte[] num(final double v) {
      final int vv = (int) v;
      return v == vv ? token(vv) : token(v);
    }
  }

  /**
   * EXIF Information structure.
   */
  static class EXIFSignedRational extends EXIFRational {
    /**
     * Constructor.
     * @param t tag name
     */
    EXIFSignedRational(final String t) {
      super(t);
      format = SIGNEDRATIONAL;
    }

    /**
     * Constructor.
     * @param t tag name
     * @param p type
     */
    EXIFSignedRational(final String t, final int p) {
      super(t, p);
      format = SIGNEDRATIONAL;
    }
  }

  /**
   * EXIF Information structure.
   */
  static class EXIFUndefined extends EXIFInfo {
    /** Undefined EXIF type. */
    private int type;
    /** Format. */
    private byte[][] cat;

    /** RGB Component. */
    private static final byte[] RGBCOMP = { 4, 5, 6, 0 };
    /** RGB String. */
    private static final byte[] RGB = bytes("RGB");
    /** YCBCR Component. */
    private static final byte[] YCBCRCOMP = { 1, 2, 3, 0 };
    /** YCbCr String. */
    private static final byte[] YCBCR = bytes("YCbCr");
    /** Component array. */
    private static final byte[][] COMPCONF =
      bytes("Y", "Cb", "Cr", "R", "G", "B");

    /**
     * Constructor.
     * @param t tag name
     * @param p token type
     */
    EXIFUndefined(final String t, final int p) {
      super(t, UNDEFINED);
      type = p;
    }

    /**
     * Constructor.
     * @param t tag name
     * @param c category
     */
    EXIFUndefined(final String t, final byte[][] c) {
      super(t, UNDEFINED);
      cat = c;
    }

    @Override
    byte[] val(final Object... obj) {
      final byte[] val = (byte[]) obj[0];

      if(cat != null) return cat[val[0]];

      // Default
      if(type == 0) return val;

      // ExifVersion
      if(type == 1) {
        // Alternative: return val;
        final TokenBuilder tb = new TokenBuilder();
        for(int i = 0; i < 4; i++) {
          if(val[i] > 0x30 && val[i] < 0x3A) {
            if(tb.size() != 0) tb.add('.');
            tb.add(val[i]);
          }
        }
        return tb.finish();
      }

      // ComponentsConfiguration
      if(type == 2) {
        if(eq(val, YCBCRCOMP)) return YCBCR;
        if(eq(val, RGBCOMP)) return RGB;
        byte[] tok = {};
        for(int i = 0; i < 4; i++) {
          if(val[i] != 0) tok = concat(tok, COMPCONF[val[i]]);
        }
        return tok;
      }

      BaseX.debug(EXIFUNDEFINED);
      return EMPTY;
    }
  }
}
