package org.basex.build.fs.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.basex.BaseX;
import org.basex.build.fs.FSText;
import org.basex.build.fs.metadata.EXIFIndex.EXIFInfo;
import org.basex.util.Array;
import org.basex.util.Token;
import static org.basex.build.fs.FSText.*;

/**
 * This is an abstract class for defining EXIF extractors.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class EXIFExtractor extends AbstractExtractor {
  /** EXIF information. */
  ArrayList<byte[]> exif = new ArrayList<byte[]>();
  /** File endian for numeric values. */
  private boolean littleEndian;
  /** EXIF buffer. */
  private byte[] buffer = new byte[65536];
  /** EXIF information. */
  private final EXIFIndex index = new EXIFIndex();
  /** Name of current file. */
  private String file;

  /**
   * Checks the endian of numeric values in the input stream.
   * @param in input stream
   * @return false if TIFF endian is invalid.
   * @throws IOException I/O exception
   */
  protected boolean checkEndian(final InputStream in) throws IOException {
    final int b1 = in.read();
    final int b2 = in.read();
    return checkEndian(b1, b2);
  }

  /**
   * Checks the endian of numeric values for the specified bytes.
   * @param b1 first byte
   * @param b2 second byte
   * @return false if TIFF endian is invalid.
   */
  private boolean checkEndian(final int b1, final int b2) {
    // check if the header is valid
    littleEndian = b1 == 0x49;
    return b1 == 0x49 && b2 == 0x49 || b1 == 0x4d && b2 == 0x4d;
  }

  /**
   * Scans EXIF data.
   * @param in input stream
   * @param skip header size
   * @param f current filename
   * @return number of skipped bytes
   * @throws IOException I/O exception
   */
  protected int scanEXIF(final InputStream in, final int skip, final String f)
      throws IOException {

    file = f;
    int i = 0;
    final int is = HEADEREXIF.length;
    while(i < is && in.read() == HEADEREXIF[i++]);
    final int sk = skip - i;
    if(i != is) return sk;

    final int size = sk;
    if(size > buffer.length) buffer = new byte[sk];
    for(int s = 0; s < size; s++) buffer[s] = (byte) in.read();

    // check endian & magic number
    if(!checkEndian(buffer[0], buffer[1]) || getShort(2) != 0x2a) return 0;

    final int start = getInt(4);

    scan(start);
    return 0;
  }

  /**
   * Scans the specified buffer range.
   * @param start start position
   * @throws MetaDataException MetaData exception
   */
  private void scan(final int start) throws MetaDataException {
    final int entries = getShort(start);
    final int s = start + 2;
    final int end = s + (entries * 0x0C);

    for(int e = s; e < end; e += 0x0C) {
      final int tagnr = getShort(e); // Tag Number
      int format = getShort(e + 2); // Data Format

      // Exit Offset - SubIFD
      if(tagnr == 0x8769) {
        scan(getInt(e + 8));
        continue;
      }

      // 0x927C (MakerNotes) and 0x9286 (UserComments) not supported yet

      final EXIFInfo inf = index.get(tagnr);
      if(inf == null) {
        BaseX.debug(FSText.EXIFIGNORED, Integer.toHexString(tagnr), file);
        continue;
      }

      // UserComment - kinda hacky as it's actually of type 'Undefined'
      if(tagnr == 0x9286) format = EXIFIndex.STRING;

      if(format != inf.format) {
        if(inf.format == EXIFIndex.UNDEFINED) {
          BaseX.debug(FSText.EXIFFORMAT1, Integer.toHexString(tagnr),
            inf.tag, inf.format, format, file);
          continue;
        }
        BaseX.debug(FSText.EXIFFORMAT2, Integer.toHexString(tagnr),
          inf.tag, inf.format, format, file);
      }

      byte[] v;
      if(format == EXIFIndex.STRING) {
        if(tagnr == 0x9286) {
          // Hack continued...
          final int l = getInt(e + 4) - 8;
          final int o = getInt(e + 8) + 8;
          v = inf.val(buffer, o, l);
        } else {
          final int l = getInt(e + 4) - 1;
          final int o = getInt(e + 8);

          if(o > buffer.length) {
            // copied backwards.. not correct for all exif codes
            v = new byte[l];
            for(int i = 0; i < l; i++) v[i] = buffer[e + 7 + l - i];
          } else {
            v = inf.val(buffer, o, l);
          }
        }
      } else if(format == EXIFIndex.SHORT) {
        v = inf.val(getShort(e + 8));
      } else if(format == EXIFIndex.LONG) {
        v = inf.val(getInt(e + 8));
      } else if(format == EXIFIndex.RATIONAL ||
          format == EXIFIndex.SIGNEDRATIONAL) {
        final int n = getInt(e + 8);
        v = inf.val(getInt(n + 4), getInt(n));
      } else if(format == EXIFIndex.UNDEFINED) {
        v = inf.val(Array.create(buffer, e + 8, 4));
      } else {
        throw new MetaDataException(BaseX.info(FSText.EXIFUNKNOWN,
            Integer.toHexString(tagnr), format));
      }
      if(v.length > 0) {
        exif.add(inf.tag);
        exif.add(v);
      }

      // add exposure time in milliseconds
      try {
        if(tagnr == 0x829A) {
          final String[] vv = Token.string(v).
            replaceAll(" seconds", "").split("/");
          if(vv.length == 1) {
            v = Token.token(Double.parseDouble(vv[0]) * 1000);
          } else {
            final int i = Integer.parseInt(vv[0]);
            final double d = Double.parseDouble(vv[1]);
            v = Token.token((int) ((i / d) * 10000) / 10.0);
          }
          exif.add(EXPOS);
          exif.add(v);
        }
      } catch(final Exception ex) {
        throw new MetaDataException("Could not parse '%'", v);
      }
    }
  }

  /**
   * Returns a number for the specified endian byte order.
   * @param in input stream
   * @return number
   * @throws IOException I/O exception
   */
  protected int getShort(final InputStream in) throws IOException {
    final int b1 = in.read();
    final int b2 = in.read();
    return littleEndian ? b1 + (b2 << 8) : (b1 << 8) + b1;
  }

  /**
   * Returns a number for the specified endian byte order.
   * @param in input stream
   * @return number
   * @throws IOException I/O exception
   */
  protected int getInt(final InputStream in) throws IOException {
    final int b1 = getShort(in);
    final int b2 = getShort(in);
    return littleEndian ? b1 + (b2 << 16) : (b1 << 16) + b1;
  }

  /**
   * Returns a number for the specified endian byte order.
   * @param pos buffer position
   * @return number
   */
  private int getShort(final int pos) {
    final int b1 = buffer[pos] & 0xFF;
    final int b2 = buffer[pos + 1] & 0xFF;
    return littleEndian ? b1 + (b2 << 8) : (b1 << 8) + b1;
  }

  /**
   * Returns a number for the specified endian byte order.
   * @param pos buffer position
   * @return number
   */
  private int getInt(final int pos) {
    final int b1 = getShort(pos);
    final int b2 = getShort(pos + 2);
    return (littleEndian ? b1 + (b2 << 16) : (b1 << 16) + b1) & 0x7FFFFFFF;
  }
}
