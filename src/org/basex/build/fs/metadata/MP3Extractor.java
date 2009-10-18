package org.basex.build.fs.metadata;

import static org.basex.build.fs.FSText.*;
import static org.basex.util.Token.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.basex.build.Builder;
import org.basex.build.fs.FSText;
import org.basex.core.Main;

/**
 * Extractor for MP3s and ID3v2 meta data.
 * http://www.id3.org/id3v2.4.0-structure [2.4.0-s]
 * http://www.id3.org/id3v2.4.0-frames [2.4.0-f]
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek
 */
public final class MP3Extractor extends AbstractExtractor {
  /** ID3v2 frame header length ([2.4.0-s], l.324). */
  static final int FRAME_HEADER_LENGTH = 10;
  /** A tag MUST contain at least one frame. A frame must be at least 1
   * byte big, excluding the header. ([2.4.0-s], l.351)*/
  static final int MINIMAL_FRAME_SIZE = FRAME_HEADER_LENGTH + 1;

  /** MP3 Types. */
  private static final byte[][][] TAGS = new byte[][][] {
    { token("COMM"), token("Comment"), null },
    { token("TALB"), token("Album") },
    { token("TBPM"), token("BPM") },
    { token("TCOM"), token("Composer") },
    { token("TCON"), token("Genre") },
    { token("TCOP"), token("Copyright") },
    { token("TDAT"), token("Date") },
    { token("TDRC"), token("RecordingTime") },
    { token("TDTG"), token("TaggingTime") },
    { token("TENC"), token("Encoder") },
    { token("TFLT"), token("FileType") },
    { token("TIT1"), token("Content") },
    { token("TIT2"), TITLE },
    { token("TLAN"), token("Language") },
    { token("TLEN"), token("Milliseconds") },
    { token("TMED"), token("MediaType") },
    { token("TOPE"), token("Original") },
    { token("TPE1"), PERSON },
    { token("TPE2"), PERSON },
    { token("TPE3"), PERSON },
    { token("TPOS"), token("Set") },
    { token("TPUB"), token("Publisher") },
    { token("TSIZ"), token("Size") },
    { token("TRCK"), token("Track") },
    { token("TSSE"), token("Software") },
    { token("TYER"), token("Year") },
    { token("USLT"), token("Lyrics"), null },
  };

  /** MP3 Types. */
  private static final byte[][] VERSIONS = new byte[][] {
    token("MPEG-2.5 "), EMPTY, token("MPEG-2 "), token("MPEG-1 "),
  };
  /** MP3 Types. */
  private static final byte[][] LAYERS = new byte[][] {
    token("Layer 1"), token("Layer 2"), token("Layer 3"), EMPTY
  };
  /** Available bit rates. */
  private static final int[][][] BITRATES = { {
    { 0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448 },
    { 0, 32, 48, 56,  64,  80,  96, 112, 128, 160, 192, 224, 256, 320, 384 },
    { 0, 32, 40, 48,  56,  64,  80,  96, 112, 128, 160, 192, 224, 256, 320 },
  }, {
    { 0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256 },
    { 0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160 },
    { 0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 16 },
  } };
  /** MP3 sample rates. */
  private static final int[][] SAMPLES = {
    { 11025, 12000, 8000, }, { 0, 0, 0 },
    { 22050, 24000, 16000 }, { 44100, 48000, 32000 }
  };
  /** MP3 sample rates. */
  private static final int[][] FSIZE = { { 32, 17, }, { 17, 9 } };

  /** MP3 modes. */
  private static final byte[][] MODES = { token("Stereo"),
    token("Joint Stereo"), token("Dual Channel"), token("Mono") };
  /** MP3 emphases. */
  private static final byte[][] EMPH = { token("None"),
    token("5015MS"), token("Illegal"), token("CCITT") };
  /** MP3 samples per frame. */
  private static final int[][] SPF = new int[][] {
    { 384, 1152, 1152 }, { 384, 1152, 576 }
  };
  /** MP3 emphases. */
  private static final byte[][] ENCODE = { token("CBR"), token("VBR") };
  /** Reference to the mp3 file. */
  private BufferedInputStream in;
  /** Filename (used for debugging). */
  private File file;

  /** Buffer for ID3V1 Info. */
  private final byte[] id3v1 = new byte[128];
  /** Byte container. */
  byte[] content = new byte[8];

  /**
   * Reads a single byte from buffered input stream (mp3 file).
   * @return int, the read byte.
   * @exception IOException in case of premature EOF or IO error.
   */
  private int read() throws IOException {
    int i = 0;
    if((i = in.read()) != -1) return i;
    in.close();
    throw new MetaDataException(FSText.ID3V2ERROR, file.getName());
  }

  /**
   * Returns the frames in a list.
   * @param size number of frames
   * @return frames in a list structure
   * @throws IOException I/O exception
   */
  private List<byte[]> getID3V2(final int size) throws IOException {
    final List<byte[]> frames = new ArrayList<byte[]>();

    int s = size;
    while(s > MINIMAL_FRAME_SIZE) {
      int b;
      // padding marks correct end of frames.
      if((b = read()) == 0x00) break;
      // get tag
      final byte[] tag = { (byte) b, (byte) read(), (byte) read(),
          (byte) read() };
      // get length of entry
      final int fsize = read() << 24 | read() << 16 | read() << 8 | read();

      // analyze flags?
      read(); read();

      // this is just a first stub.
      // diff. frames have diff. layouts, Txxx frames may have diff. enc. ...
      final int enc = read();

      // read in tag information
      boolean valid = true;
      for(int i = 0; i < fsize - 1; i++) {
        b = read();
        if(i == content.length) content = Arrays.copyOf(content, i << 1);
        content[i] = (byte) b;
        valid &= b != 0;
      }

      // get readable version of ID3 tag name
      byte[][] tt = null;
      for(final byte[][] t : TAGS) {
        if(eq(tag, t[0])) {
          tt = t;
          break;
        }
      }

      // no readable version found..
      if(tt == null) {
        Main.debug(ID3UNKNOWN, tag, file.getName());
      } else if(enc == 0 || enc == 3) {
        // UTF16 not supported (0: ISO8859, 1: LE, 2: BW, 3: UTF8)
        final byte[] cont = chop(fsize - 1, tt.length == 3);

        if(cont == null) {
          Main.debug(ID3NULL, tag, file.getName());
        } else if(cont.length != 0) {
          frames.add(tt[1]);
          frames.add(cont);
        }
      }
      s -= FRAME_HEADER_LENGTH + fsize;

      // to be done: handle unicode encodings
    }
    skip(in, s - 1);
    return frames;
  }

  /**
   * Chops remaining zero bytes from the current content.
   * @param s content size
   * @param conv convert null bytes to spaces
   * @return resulting content or null if content is invalid
   */
  private byte[] chop(final int s, final boolean conv) {
    // find last valid character
    int i = s;
    while(--i >= 0 && content[i] == 0);
    if(i == -1) return EMPTY;

    // check if there are still zero bytes
    int j = i;
    while(--j >= 0 && content[j] != 0);
    if(j == -1) return Arrays.copyOf(content, i + 1);

    // return null if zero bytes have been found
    if(!conv) return null;

    for(int k = i; k > j; k--) if(content[k] == 0) content[k] = ' ';
    return Arrays.copyOfRange(content, j + 1, i + 1);
  }

  @Override
  public void extract(final Builder builder, final File f) throws IOException {
    in = new BufferedInputStream(new FileInputStream(f));
    int major, minor, hflag, size, sync1, sync2, sync3, sync4;
    boolean exthea = false;
    boolean experi = false;
    boolean footer = false;
    boolean found = false;

    try {
      final long fileLen = f.length();
      file = f;

      final int first = read();

      // ID3 header
      if(first == 0x49 // ID3V2...
         && read() == 0x44
         && read() == 0x33
         && (major = read()) < 0xFF // major, minor version
         && (minor = read()) < 0xFF
         && (hflag = read()) < 0xFF // header flags
         && (sync1 = read()) < 0x80 // synchsafe tag size
         && (sync2 = read()) < 0x80
         && (sync3 = read()) < 0x80
         && (sync4 = read()) < 0x80) {

        // ID3 frames
        found = true;
        builder.startElem(AUDIO, atts.set(TYPE, TYPEMP3));

        exthea = (hflag & 0x40) != 0;
        experi = (hflag & 0x20) != 0;
        footer = (hflag & 0x10) != 0;
        size = sync1 << 21 | sync2 << 14 | sync3 << 7 | sync4;

        final List<byte[]> frames = getID3V2(size);

        // scans for the technical info and adds the audio header
        while(read() != 0xFF);
        getTechInfo(builder, size);

        // insert ID3 info
        atts.reset();
        atts.add(ID3VERS, token("2." + major + '.' + minor));
        atts.add(ID3FLAG_EXTHEA, token(exthea));
        atts.add(ID3FLAG_EXPERI, token(experi));
        atts.add(ID3FLAG_FOOTER, token(footer));
        builder.startElem(ID3, atts);

        for(int i = 0; i < frames.size() - 1; i += 2) {
          builder.nodeAndText(frames.get(i), atts.reset(), frames.get(i + 1));
        }
        builder.endElem(ID3);

      } else if(fileLen > 0x80) { // ID3V1...
        found = true;
        builder.startElem(AUDIO, atts.set(TYPE, TYPEMP3));

        if(first != 0xFF) while(read() != 0xFF);

        // scans for the technical info and adds the audio header
        getTechInfo(builder, 0x80);
        in.close();

        // parse ID3V1 tags..
        final RandomAccessFile raf = new RandomAccessFile(f, "r");
        raf.seek(fileLen - 0x80);
        raf.read(id3v1);
        raf.close();

        // ...header tag always upper case?
        if(startsWith(id3v1, HEADERID3V1)) {
          final byte[] title = getTag(id3v1, 3, 30);
          final byte[] artist = getTag(id3v1, 33, 30);
          final byte[] album = getTag(id3v1, 63, 30);
          final byte[] year = getTag(id3v1, 93, 4);
          final byte[] comment = getTag(id3v1, 97, 30);

          builder.startElem(ID3, atts.set(ID3VERS, new byte[] { '1' }));
          add(builder, ID3TITLE, title);
          add(builder, ID3ARTIST, artist);
          add(builder, ID3ALBUM, album);
          add(builder, ID3YEAR, year);
          add(builder, ID3COMMENT, comment);
          builder.endElem(ID3);
        }
      } else {
        Main.debug(ID3INVALID, file.getName());
      }
    } catch(final MetaDataException ex) {
      Main.debug(ex);
    }

    if(found) builder.endElem(AUDIO);
  }

  /**
   * Adds a single tag and a text value.
   * @param builder builder reference
   * @param tag tag
   * @param value text value
   * @throws IOException I/O exception
   */
  private void add(final Builder builder, final byte[] tag, final byte[] value)
      throws IOException {
    if(value == null) return;
    builder.nodeAndText(tag, atts.reset(), value);
  }

  /**
   * Adds the header and technical info to the MP3 file.
   * @param builder builder reference
   * @param size header size
   * @throws IOException I/O exception
   */
  private void getTechInfo(final Builder builder, final int size)
      throws IOException {

    int byte0 = 0xFF;
    int byte1 = read();
    int byte2 = read();
    int byte3 = read();

    // check if technical bits are correct... if not, parse next bytes
    while(byte0 != 0xFF || (byte1 & 0xE0) != 0xE0 || (byte1 & 0x18) == 0x08 ||
        (byte1 & 0x06) == 0x00 || (byte2 & 0xF0) == 0xF0 ||
        (byte2 & 0xF0) == 0x00 || (byte2 & 0x0C) == 0x0C) {
      byte0 = byte1; byte1 = byte2; byte2 = byte3; byte3 = read();
    }

    final int vers = byte1 >> 3 & 0x03;
    final int layr = 3 - (byte1 >> 1 & 0x03);
    final int rate = byte2 >> 4 & 0x0F;
    final int smpl = byte2 >> 2 & 0x03;
    final int emph = byte3 & 0x03;
    final int version = vers == 3 ? 0 : 1;

    /*
    boolean priv = (byte2 & 0x01) != 0;
    boolean copy = (byte3 & 0x08) != 0;
    boolean orig = (byte3 & 0x04) != 0;
    */

    final int samples = SAMPLES[vers][smpl];
    final int mode = byte3 >> 6 & 0x03;
    int bitrate = BITRATES[version][layr][rate];
    int seconds = (int) ((file.length() - size) * 8 / bitrate) / 1000;

    // look for VBR XING header to correct track length and bitrate
    int encoding = 0;
    final int fsize = FSIZE[version][mode == 3 ? 1 : 0];
    skip(in, fsize);
    final byte[] vbrh = new byte[4];
    in.read(vbrh);
    if(eq(MP3XING, vbrh)) {
      skip(in, 3);
      if((read() & 0x01) != 0) {
        final int nf = (read() << 24) + (read() << 16) + (read() << 8) + read();
        seconds = nf * SPF[version][layr] / samples;
        encoding++;
        if(seconds != 0) bitrate = (int) ((file.length() - size) * 8 /
            seconds / 1000);
      }
    }

    builder.nodeAndText(MP3CODEC, atts.reset(),
        concat(VERSIONS[vers], LAYERS[layr]));
    builder.nodeAndText(MP3RATE, atts, token(bitrate));
    builder.nodeAndText(MP3SAMPLE, atts, token(samples));
    builder.nodeAndText(MP3MODE, atts, MODES[mode]);
    builder.nodeAndText(MP3EMPH, atts, EMPH[emph]);
    builder.nodeAndText(MP3ENCODE, atts, ENCODE[encoding]);
    builder.nodeAndText(MP3SEC, atts, token(seconds));
  }

  /**
   * Returns an ID3V1 tag.
   * @param input input byte array
   * @param off offset to look at
   * @param max maximum length
   * @return resulting tag
   */
  private byte[] getTag(final byte[] input, final int off, final int max) {
    int i = -1;
    while(++i < max && input[off + i] != 0);
    while(--i >= 0 && input[off + i] == 0x20) i--;
    return i < 0 ? null : Arrays.copyOfRange(input, off, off + i + 1);
  }
}
