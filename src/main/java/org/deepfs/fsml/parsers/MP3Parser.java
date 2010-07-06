package org.deepfs.fsml.parsers;

import static org.basex.util.Token.*;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import org.basex.core.Main;
import org.deepfs.fsml.BufferedFileChannel;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.FileType;
import org.deepfs.fsml.MetaElem;
import org.deepfs.fsml.MimeType;
import org.deepfs.fsml.ParserException;
import org.deepfs.fsml.ParserRegistry;
import org.deepfs.fsml.util.ParserUtil;

/**
 * <p>
 * Parser for MP3 audio files.
 * </p>
 * <p>
 * Currently not supported:
 * <ul>
 * <li>extended ID3v2 header</li>
 * <li>extended tag (before ID3v1 tag)</li>
 * </ul>
 * </p>
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Alexander Holupirek
 * @author Bastian Lemke
 * @see <a href="http://www.id3.org/id3v2.4.0-structure">ID3v2.4.0 structure</a>
 * @see <a href="http://www.id3.org/id3v2.4.0-frames">ID3v2.4.0 frames</a>
 */
public final class MP3Parser implements IFileParser {

  // ---------------------------------------------------------------------------
  // ----- static stuff --------------------------------------------------------
  // ---------------------------------------------------------------------------

  /** ID3v2 header lenght. */
  private static final int HEADER_LENGTH = 10;
  /**
   * ID3v2 frame header length.
   * @see "ID3v2.4.0 structure"
   */
  private static final int FRAME_HEADER_LENGTH = 10;
  /**
   * A tag MUST contain at least one frame. A frame must be at least 1 byte big,
   * excluding the header.)
   * @see "ID3v2.4.0 structure"
   */
  private static final int MINIMAL_FRAME_SIZE = FRAME_HEADER_LENGTH + 1;
  /**
   * All available ID3v1.1 genres. Order is important! ID3v1.1 genres are stored
   * as a one byte value (as last byte in the ID3v1.1 tag). The position in the
   * array represents the "code" of the genre, the textual representation of the
   * genre X is stored at GENRES[X].
   */
  private static final byte[][] GENRES = tokens("Blues", "Classic Rock",
      "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop", "Jazz",
      "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae",
      "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal",
      "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal",
      "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid",
      "House", "Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass",
      "Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
      "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial",
      "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock",
      "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk",
      "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic",
      "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk",
      "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock",
      "Folk", "Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob",
      "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock",
      "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock",
      "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech",
      "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Brass",
      "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba",
      "Folklore", "Ballad", "Poweer Ballad", "Rhytmic Soul", "Freestyle",
      "Duet", "Punk Rock", "Drum Solo", "A Capela", "Euro-House", "Dance Hall",
      "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror", "Indie",
      "BritPop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta Rap",
      "Heavy Metal", "Black Metal", "Crossover", "Contemporary Christian",
      "Christian Rock", "Merengue", "Salsa", "Trash Metal", "Anime", "Jpop",
      "Synthpop");

  /** All available picture types for APIC frames. */
  static final String[] PICTURE_TYPE = new String[] { "Other", "file icon",
      "Other file icon", "Front cover", "Back cover", "Leaflet page",
      "Media - e.g. label side of CD",
      "Lead artist or lead performer or soloist", "Artist or performer",
      "Conductor", "Band or Orchestra", "Composer", "Lyricist or text writer",
      "Recording Location", "During recording", "During performance",
      "Movie or video screen capture", "A bright coloured fish",
      "Illustration", "Band or artist logotype", "Publisher or Studio logotype"
  };

  /** Flag for ISO-8859-1 encoding. */
  private static final int ENC_ISO_8859_1 = 0;
  /**
   * Flag for UTF-16 encoding (with BOM).
   * @see <a href="http://en.wikipedia.org/wiki/UTF-16/UCS-2">Wikipedia</a>
   */
  private static final int ENC_UTF_16_WITH_BOM = 1;
  /**
   * Flag for UTF-16 encoding (without BOM).
   * @see <a href="http://en.wikipedia.org/wiki/UTF-16/UCS-2">Wikipedia</a>
   */
  private static final int ENC_UTF_16_NO_BOM = 2;
  /** Flag for UTF-8 encoding. */
  private static final int ENC_UTF_8 = 3;

  /** MP3 modes. */
  private static final byte[][] MODES = 
    tokens("Stereo", "Joint Stereo", "Dual Channel", "Mono");
  /** MP3 emphases. */
  private static final byte[][] EMPH =
    tokens("None", "5015MS", "Illegal", "CCITT");
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
  /** MP3 samples per frame. */
  private static final int[][] SPF = new int[][] {
    { 384, 1152, 1152 }, { 384, 1152, 576 }
  };
  /** MP3 types. */
  private static final byte[][] VERSIONS =
    tokens("MPEG-2.5 ", "MPEG-2 ", "MPEG-1 ");
  /** MP3 types. */
  private static final byte[][] LAYERS =
    tokens("Layer 1", "Layer 2", "Layer 3", "");
  /** MP3 encoding. */
  private static final byte[][] ENCODE = tokens("CBR", "VBR");

  static {
    ParserRegistry.register("mp3", MP3Parser.class);
  }

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

  /** The {@link BufferedFileChannel} to read from. */
  BufferedFileChannel bfc;
  /** File reference to store metadata and file contents. */
  DeepFile deepFile;

  @Override
  public boolean check(final DeepFile df) throws IOException {
    bfc = df.getBufferedFileChannel();
    deepFile = df;
    return checkID3v2() || checkID3v1();
  }

  @Override
  public void extract(final DeepFile df) throws IOException {
    if(df.extractMeta()) {
      bfc = df.getBufferedFileChannel();
      deepFile = df;
      if(checkID3v2()) readMetaID3v2();
      else if(checkID3v1()) readMetaID3v1();
    }
  }

  /** Sets {@link FileType} and {@link MimeType}. */
  private void setTypeAndFormat() {
    deepFile.setFileType(FileType.AUDIO);
    deepFile.setFileFormat(MimeType.MP3);
  }

  /**
   * Reads technical information and adds it to the deep file.
   * @param size the size of the ID3 tags
   */
  private void techInfo(final int size) {
    try {
      int b0, b1, b2, b3;
      do {
        bfc.buffer(1);
        b0 = bfc.get();
      } while(b0 != 0xFF);

      bfc.buffer(3);
      b1 = bfc.get();
      b2 = bfc.get();
      b3 = bfc.get();

      // check if technical bits are correct... if not, parse next bytes
      while(b0 != 0xFF || (b1 & 0xE0) != 0xE0 || (b1 & 0x18) == 0x08
          || (b1 & 0x06) == 0x00 || (b2 & 0xF0) == 0xF0 || (b2 & 0xF0) == 0x00
          || (b2 & 0x0C) == 0x0C) {
        bfc.buffer(1);
        b0 = b1; b1 = b2; b2 = b3; b3 = bfc.get();
      }

      final int vers = b1 >> 3 & 0x03;
      final int layr = 3 - (b1 >> 1 & 0x03);
      final int rate = b2 >> 4 & 0x0F;
      final int smpl = b2 >> 2 & 0x03;
      final int emph = b3 & 0x03;
      final int version = vers == 3 ? 0 : 1;

      final int samples = SAMPLES[vers][smpl];
      final int mode = b3 >> 6 & 0x03;
      int bitrate = BITRATES[version][layr][rate];
      int seconds = (int) ((bfc.size() - size) * 8 / bitrate) / 1000;

      // look for VBR XING header to correct track length and bitrate
      int encoding = 0;
      final int fsize = FSIZE[version][mode == 3 ? 1 : 0];
      bfc.skip(fsize);
      final byte[] vbrh = new byte[4];
      bfc.get(vbrh);
      if(eq(token("Xing"), vbrh)) {
        bfc.skip(3);
        bfc.buffer(5);
        if((bfc.get() & 0x01) != 0) {
          final int nf = (bfc.get() << 24) + (bfc.get() << 16)
              + (bfc.get() << 8) + bfc.get();
          seconds = nf * SPF[version][layr] / samples;
          encoding++;
          if(seconds != 0) bitrate = (int) ((bfc.size() - size) * 8 /
              seconds / 1000);
        }
      }

      if(!deepFile.isMetaSet(MetaElem.DURATION)) deepFile.addMeta(
          MetaElem.DURATION, ParserUtil.convertMsDuration(seconds * 1000));
      deepFile.addMeta(MetaElem.CODEC, concat(VERSIONS[vers], LAYERS[layr]));
      deepFile.addMeta(MetaElem.BITRATE_KBIT, bitrate);
      deepFile.addMeta(MetaElem.SAMPLE_RATE, samples);
      deepFile.addMeta(MetaElem.MODE, MODES[mode]);
      deepFile.addMeta(MetaElem.EMPHASIS, EMPH[emph]);
      deepFile.addMeta(MetaElem.ENCODING, ENCODE[encoding]);
    } catch(final IOException ex) { /* end of file ... */ }
  }

  // ---------------------------------------------------------------------------
  // ----- ID3v1 methods -------------------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Checks if the file contains a ID3v1 tag and sets the file pointer to the
   * beginning of the tag.
   * @return true if the file contains a valid ID3v1 tag
   * @throws IOException if any error occurs while reading the file
   */
  private boolean checkID3v1() throws IOException {
    final long size = bfc.size();
    if(size < 128) return false;
    // ID3v1 tags are located at the end of the file (last 128 bytes)
    // The tag begins with the string "TAG" (first three bytes)
    bfc.position(size - 128);
    bfc.buffer(128);
    return bfc.get() == 'T' && bfc.get() == 'A' && bfc.get() == 'G';
  }

  /**
   * Reads the ID3v1 metadata from the file. {@link #checkID3v1()} must be
   * called before (and must return {@code true}).
   * @throws IOException if any error occurs while reading from the file
   */
  private void readMetaID3v1() throws IOException {
    setTypeAndFormat();
    // tag is already buffered by checkID3v1()
    final byte[] array = new byte[30];
    bfc.get(array, 0, 30);
    if(!ws(array)) deepFile.addMeta(MetaElem.TITLE, trim(array));
    bfc.get(array, 0, 30);
    if(!ws(array)) deepFile.addMeta(MetaElem.CREATOR_NAME, trim(array));
    bfc.get(array, 0, 30);
    if(!ws(array)) deepFile.addMeta(MetaElem.ALBUM, trim(array));
    final byte[] a2 = new byte[4];
    bfc.get(a2, 0, 4);
    if(!ws(a2)) deepFile.addMeta(MetaElem.YEAR,
        ParserUtil.convertYear(toInt(a2)));
    bfc.get(array, 0, 30);
    if(array[28] == 0) { // detect ID3v1.1, last byte represents track
      if(array[29] != 0) {
        deepFile.addMeta(MetaElem.TRACK, array[29]);
        array[29] = 0;
      }
    }
    if(!ws(array)) deepFile.addMeta(MetaElem.DESCRIPTION, trim(array));
    final int genreId = bfc.get() & 0xFF;
    if(genreId != 0) deepFile.addMeta(MetaElem.GENRE, getGenre(genreId));
    bfc.position(0);
    techInfo(128);
  }

  // ---------------------------------------------------------------------------
  // ----- ID3v2 methods -------------------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Checks if the file contains a ID3v2 tag and sets the file pointer to the
   * beginning of the ID3 header fields.
   * @return true if the file contains a ID3v2 tag
   * @throws IOException if any error occurs while reading the file
   */
  private boolean checkID3v2() throws IOException {
    final int size = HEADER_LENGTH + MINIMAL_FRAME_SIZE;
    if(bfc.size() < size) return false;
    // ID3v2 tags are usually located at the beginning of the file.
    // The tag begins with the string "ID3" (first three bytes)
    bfc.buffer(size);
    return bfc.get() == 'I' && bfc.get() == 'D' && bfc.get() == '3';
  }

  /**
   * Reads the ID3v2 metadata from the file. The behavior is undefined if there
   * is no ID3v2 tag available, therefore {@link #checkID3v1()} should always be
   * called before.
   * @throws IOException if any error occurs while reading the ID3v2 tag
   */
  private void readMetaID3v2() throws IOException {
    setTypeAndFormat();
    final int size = readID3v2Header();
    int s = size;
    while(s >= MINIMAL_FRAME_SIZE) {
      final int res = readID3v2Frame();
      if(res > 0) {
        s -= res;
      } else {
        s += res;
        if(s < MINIMAL_FRAME_SIZE) break;
        bfc.skip(-res);
      }
    }
    final long pos = bfc.position();
    s = size;
    if(checkID3v1()) s += 128;
    bfc.position(pos);
    techInfo(s);
  }

  /**
   * Reads the ID3v2 header and returns the header size.
   * @return the size of the ID3v2 header
   * @throws IOException if any error occurs while reading the file
   */
  private int readID3v2Header() throws IOException {
    // already buffered by checkID3v2()
    bfc.position(6); // skip tag identifier, ID3 version fields and flags
    return readSynchsafeInt();
  }

  /**
   * Reads the ID3v2 frame at the current buffer position. Afterwards, the
   * buffer position is set to the first byte after this frame.
   * @return the number of bytes read, {@link Integer#MAX_VALUE} if the end of
   *         the header was detected or the number of bytes read (as negative
   *         number) if the frame was not parsed
   * @throws IOException if any error occurs while reading the file
   */
  private int readID3v2Frame() throws IOException {
    bfc.buffer(MINIMAL_FRAME_SIZE);
    final byte[] frameId = new byte[4];
    // padding (some 0x00 bytes) marks correct end of frames.
    if((frameId[0] = (byte) bfc.get()) == 0) return Integer.MAX_VALUE;
    bfc.get(frameId, 1, 3);
    final int frameSize = readSynchsafeInt();
    bfc.skip(2); // skip flags
    Frame frame;
    try {
      frame = Frame.valueOf(string(frameId));
      frame.parse(this, frameSize);
      return frameSize;
    } catch(final IllegalArgumentException ex) {
      return -frameSize;
    }
  }

  // ---------------------------------------------------------------------------
  // ----- utility methods -----------------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Returns the textual representation of the genre with the code {@code b}.
   * @param b the "code" of the genre
   * @return the textual representation of the genre
   */
  public static byte[] getGenre(final int b) {
    return b < GENRES.length && b >= 0 ? GENRES[b] : EMPTY;
  }

  /**
   * Reads a synchsafe integer (4 bytes) from the channel and converts it to a
   * "normal" integer. In ID3 tags, some integers are encoded as "synchsafe"
   * integers to distinguish them from data in other blocks. The most
   * significant bit of each byte is zero, making seven bits out of eight
   * available.
   * @return the integer
   * @throws IOException if any error occurs
   */
  private int readSynchsafeInt() throws IOException {
    final int b1 = bfc.get();
    final int b2 = bfc.get();
    final int b3 = bfc.get();
    final int b4 = bfc.get();
    if(b1 > 127 || b2 > 127 || b3 > 127 || b4 > 127) {
      // integer is not synchsafe
      return b1 << 24 | b2 << 16 | b3 << 8 | b4;
    }
    return b1 << 21 | b2 << 14 | b3 << 7 | b4;
  }

  /**
   * Skips the text encoding description bytes.
   * @return the number of skipped bytes
   * @throws IOException if any error occurs while reading from the file
   */
  int skipEncBytes() throws IOException {
    bfc.buffer(3);
    // skip text encoding description bytes
    int bytesToSkip = 0;
    if((bfc.get() & 0xFF) <= 0x04) bytesToSkip++;
    if((bfc.get() & 0xFF) >= 0xFE) bytesToSkip++;
    if((bfc.get() & 0xFF) >= 0xFE) bytesToSkip++;
    bfc.skip(bytesToSkip - 3);
    return bytesToSkip;
  }

  /**
   * Reads the text encoding of the following frame from the file channel.
   * Assure that at least one byte is buffered before calling this method.
   * @return a string with the name of the encoding that was detected or
   *         {@code null} if an invalid or unsupported encoding was
   *         detected. If no encoding is set, an empty string is returned
   * @throws IOException if any error occurs while reading from the file
   */
  String readEncoding() throws IOException {
    final int c = bfc.get();
    switch(c) {
      case ENC_ISO_8859_1:
        return "ISO-8859-1";
      case ENC_UTF_8:
        return "UTF-8";
      case ENC_UTF_16_NO_BOM:
        deepFile.debug(
            "MP3Parser: Unsupported text encoding (UTF-16 without BOM)");
        return null;
      case ENC_UTF_16_WITH_BOM:
        return "UTF-16";
      default: // no encoding specified
        bfc.skip(-1);
        return "";
    }
  }

  /**
   * Reads and parses text from the file. Assure that at least {@code s}
   * bytes are buffered before calling this method.
   * @param s number of bytes to read
   * @return byte array with the text
   * @throws IOException if any error occurs while reading from the file
   */
  byte[] readText(final int s) throws IOException {
    return s <= 1 ? EMPTY : readText(s, readEncoding());
  }

  /**
   * Reads and parses text with the given encoding from the file. Assure that at
   * least {@code s} bytes are buffered before calling this method.
   * @param s number of bytes to read
   * @param encoding the encoding of the text
   * @return byte array with the text
   * @throws IOException if any error occurs while reading from the file
   */
  byte[] readText(final int s, final String encoding) throws IOException {
    int size = s;
    if(size <= 1 || encoding == null) return EMPTY;
    if(bfc.get() != 0) bfc.skip(-1); // skip leading zero byte
    else size--;
    if(encoding.isEmpty()) // no encoding specified
      return bfc.get(new byte[size]);
    final byte[] array = new byte[size - 1];
    return token(new String(bfc.get(array), encoding));
  }

  /**
   * Reads and parses the genre from the file and fires events for each genre.
   * @param s number of bytes to read
   * @throws IOException if any error occurs while reading the file
   */
  void fireGenreEvents(final int s) throws IOException {
    final byte[] value = readText(s);
    int id;
    if(ws(value)) return;
    if(value[0] == '(') { // ignore brackets around genre id
      int limit = 1;
      while(value[limit] >= '0' && value[limit] <= '9' && limit < s)
        limit++;
      id = toInt(value, 1, limit);
    } else id = toInt(value);
    if(id == Integer.MIN_VALUE) {
      final byte[][] arrays = split(value, ',');
      for(final byte[] a : arrays) {
        deepFile.addMeta(MetaElem.GENRE, trim(a));
      }
    } else {
      deepFile.addMeta(MetaElem.GENRE, getGenre(id));
    }
  }

  /**
   * Removes all illegal chars from the byte array. ID3 track numbers may be of
   * the form {@code X/Y} (X is the track number, Y represents the number
   * of tracks in the whole set). Everything after '/' is deleted.
   * @param s number of bytes to read
   * @return a byte array that contains only ASCII bytes that are valid integer
   *         numbers
   * @throws IOException if any error occurs while reading the file
   * @throws ParserException if the track number could not be parsed
   */
  int readTrack(final int s) throws IOException, ParserException {
    final byte[] value = readText(s);
    final int size = value.length;
    int i = 0;
    while(i < size && (value[i] < '1' || value[i] > '9'))
      value[i++] = 0;

    final int start = i; // first byte of the number
    while(i < size && value[i] >= '0' && value[i] <= '9')
      i++;
    // number of bytes of the number
    final int track = toInt(value, start, i);
    if(track == Integer.MIN_VALUE) throw new ParserException(
        "Failed to parse track number");
    return track;
  }

  /**
   * Parses a date and returns the corresponding xml calendar.
   * @param d the date to parse
   * @return the xml date
  XMLGregorianCalendar parseDate(final byte[] d) {
    final int len = d.length;
    if(len >= 4) { // yyyy
      final int year = toInt(d, 0, 4);
      if(len >= 7) { // yyyy-MM
        if(d[4] != '-') return null;
        final int month = toInt(d, 5, 7);
        if(len >= 10) { // yyyy-MM-dd
          if(d[7] != '-') return null;
          final int day = toInt(d, 8, 10);
          if(len >= 13) { // yyyy-MM-ddTHH
            if(d[10] != 'T') return null;
            final GregorianCalendar gc = new GregorianCalendar();
            gc.set(year, month, day);
            gc.set(Calendar.HOUR_OF_DAY, toInt(d, 11, 13));
            if(len >= 16) {
              if(d[13] != ':') return null;
              gc.set(Calendar.MINUTE, toInt(d, 14, 16));
              if(len >= 19) {
                if(d[16] != ':') return null;
                gc.set(Calendar.SECOND, toInt(d, 17, 19));
              }
            }
            return ParserUtil.convertDateTime(gc);
          }
          return ParserUtil.convertDate(year, month, day);
        }
        return ParserUtil.convertYearMonth(year, month);
      }
      return ParserUtil.convertYear(year);
    }
    return null;
  }
   */

  // ---------------------------------------------------------------------------
  // ----- Frame enumeration that fires all the events -------------------------
  // ---------------------------------------------------------------------------

  /**
   * Mapping for ID3 frames to xml elements.
   * @author Bastian Lemke
   */
  private enum Frame {

    /** Embedded picture. */
    APIC {
      @Override
      void parse(final MP3Parser obj, final int s) throws IOException {
        final long pos = obj.bfc.position(); // beginning of the APIC frame

        // read the file suffix of an embedded picture
        obj.bfc.buffer(9);
        obj.skipEncBytes();
        final StringBuilder sb = new StringBuilder();
        int b;
        while((b = obj.bfc.get()) != 0)
          sb.append((char) b);
        String string = sb.toString();
        if(string.startsWith("image/")) { // string may be a MIME type
          string = string.substring(6); // skip "image/"
        }
        String[] suffixes;
        if(string.equals("jpeg")) string = "jpg";
        if(string.length() != 3) suffixes = new String[] { "png", "jpg"};
        else suffixes = new String[] { string};

        // read the picture type id and convert it to a text
        obj.bfc.buffer(1);
        final int typeId = obj.bfc.get() & 0xFF;
        String name = null;
        if(typeId >= 0 && typeId < PICTURE_TYPE.length)
          name = PICTURE_TYPE[typeId];

        // skip the picture description
        while(true) {
          try {
            if(obj.bfc.get() == 0) break;
          } catch(final BufferUnderflowException ex) {
            obj.bfc.buffer(1);
          }
        }
        final int size = (int) (s - (obj.bfc.position() - pos));
        obj.deepFile.subfile(name, size, suffixes);
      }
    },

    /** Comments. */
    COMM {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        final String encoding = obj.readEncoding();
        obj.bfc.skip(3); // skip language
        int pos = 4;
        // ignore short content description
        while(obj.bfc.get() != 0 && ++pos < size);
        if(pos >= size) return;
        obj.deepFile.addMeta(MetaElem.DESCRIPTION, obj.readText(size - pos,
            encoding));
      }
    },

    /** Album title. */
    TALB {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.deepFile.addMeta(MetaElem.ALBUM, obj.readText(size));
      }
    },

    /** Beats per minute. */
    TBPM {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.BEATS_PER_MINUTE,
            Integer.parseInt(string(obj.readText(size))));
      }
    },

    /** iTunes Compilation flag. */
    TCMP {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.ITUNES_COMPILATION,
            Integer.parseInt(string(obj.readText(size - 1))));
      }
    },

    /** Composer. */
    TCOM {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.COMPOSER, obj.readText(size));
      }
    },

    /** Content type (genre). */
    TCON {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.fireGenreEvents(size);
      }
    },

    /** Copyright message. */
    TCOP {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.RIGHTS, obj.readText(size));
      }
    },

    /** Recording time. */
    TDRC {
      @Override
      public void parse(final MP3Parser obj, final int size) {
      }
    },

    /** Tagging time. */
    TDTG {
      @Override
      public void parse(final MP3Parser obj, final int size) {
      }
    },

    /** Encoded by... */
    TENC {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.ENCODER, obj.readText(size));
      }
    },

    /** File type. */
    TFLT {
      @Override
      public void parse(final MP3Parser obj, final int size) {

      // TFLT
      // The 'File type' frame indicates which type of audio this tag defines.
      // The following types and refinements are defined:
      //
      // MIME MIME type follows
      // MPG MPEG Audio
      // /1 MPEG 1/2 layer I
      // /2 MPEG 1/2 layer II
      // /3 MPEG 1/2 layer III
      // /2.5 MPEG 2.5
      // /AAC Advanced audio compression
      // VQF Transform-domain Weighted Interleave Vector Quantisation
      // PCM Pulse Code Modulated audio
      //
      // but other types may be used, but not for these types though. This is
      // used in a similar way to the predefined types in the "TMED" frame,
      // but without parentheses. If this frame is not present audio type is
      // assumed to be "MPG".
      }
    },

    /** Content group description. */
    TIT1 {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.DESCRIPTION, obj.readText(size));
      }
    },

    /** Title/songname/content description. */
    TIT2 {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.deepFile.addMeta(MetaElem.TITLE, obj.readText(size));
      }
    },

    /** Subtitle/Description refinement. */
    TIT3 {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.DESCRIPTION, obj.readText(size));
      }
    },

    /** Language. */
    TLAN {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.LANGUAGE, obj.readText(size));
      }
    },

    /** Length. */
    TLEN {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.deepFile.addMeta(MetaElem.DURATION,
            ParserUtil.convertDuration(obj.readText(size)));
      }
    },

    /** Media type. */
    TMED {
      @Override
      public void parse(final MP3Parser obj, final int size) {

      // TMED
      // The 'Media type' frame describes from which media the sound
      // originated. This may be a text string or a reference to the
      // predefined media types found in the list below. Example:
      // "VID/PAL/VHS" $00.
      //
      // DIG Other digital media
      // /A Analogue transfer from media
      //
      // ANA Other analogue media
      // /WAC Wax cylinder
      // /8CA 8-track tape cassette
      //
      // CD CD
      // /A Analogue transfer from media
      // /DD DDD
      // /AD ADD
      // /AA AAD
      //
      // LD Laserdisc
      //
      // TT Turntable records
      // /33 33.33 rpm
      // /45 45 rpm
      // /71 71.29 rpm
      // /76 76.59 rpm
      // /78 78.26 rpm
      // /80 80 rpm
      //
      // MD MiniDisc
      // /A Analogue transfer from media
      //
      // DAT DAT
      // /A Analogue transfer from media
      // /1 standard, 48 kHz/16 bits, linear
      // /2 mode 2, 32 kHz/16 bits, linear
      // /3 mode 3, 32 kHz/12 bits, non-linear, low speed
      // /4 mode 4, 32 kHz/12 bits, 4 channels
      // /5 mode 5, 44.1 kHz/16 bits, linear
      // /6 mode 6, 44.1 kHz/16 bits, 'wide track' play
      //
      // DCC DCC
      // /A Analogue transfer from media
      //
      // DVD DVD
      // /A Analogue transfer from media
      //
      // TV Television
      // /PAL PAL
      // /NTSC NTSC
      // /SECAM SECAM
      //
      // VID Video
      // /PAL PAL
      // /NTSC NTSC
      // /SECAM SECAM
      // /VHS VHS
      // /SVHS S-VHS
      // /BETA BETAMAX
      //
      // RAD Radio
      // /FM FM
      // /AM AM
      // /LW LW
      // /MW MW
      //
      // TEL Telephone
      // /I ISDN
      //
      // MC MC (normal cassette)
      // /4 4.75 cm/s (normal speed for a two sided cassette)
      // /9 9.5 cm/s
      // /I Type I cassette (ferric/normal)
      // /II Type II cassette (chrome)
      // /III Type III cassette (ferric chrome)
      // /IV Type IV cassette (metal)
      //
      // REE Reel
      // /9 9.5 cm/s
      // /19 19 cm/s
      // /38 38 cm/s
      // /76 76 cm/s
      // /I Type I cassette (ferric/normal)
      // /II Type II cassette (chrome)
      // /III Type III cassette (ferric chrome)
      // /IV Type IV cassette (metal)
      }
    },

    /** Original artist(s)/performer(s). */
    TOPE {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.ORIGINAL_ARTIST, obj.readText(size));
      }
    },

    /** Lead performer(s)/Soloist(s) */
    TPE1 {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.deepFile.addMeta(MetaElem.ARTIST, obj.readText(size));
      }
    },

    /** Band/orchestra/accompaniment. */
    TPE2 {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.deepFile.addMeta(MetaElem.ARTIST, obj.readText(size));
      }
    },

    /** Conductor/performer refinement. */
    TPE3 {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.deepFile.addMeta(MetaElem.ARTIST, obj.readText(size));
      }
    },

    /** Interpreted, remixed, or otherwise modified by. */
    TPE4 {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.deepFile.addMeta(MetaElem.ARTIST, obj.readText(size));
      }
    },

    /** Part of a set. */
    TPOS {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.SET, obj.readText(size));
      }
    },

    /** Publisher. */
    TPUB {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.PUBLISHER, obj.readText(size));
      }
    },

    /** Track number/Position in set. */
    TRCK {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        try {
          obj.deepFile.addMeta(MetaElem.TRACK, obj.readTrack(size));
        } catch(final ParserException ex) {
          obj.deepFile.debug("MP3Parser: Failed to parse track number (%).",
              ex);
        }
      }
    },

    /** Software/Hardware and settings used for encoding. */
    TSSE {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.SOFTWARE, obj.readText(size));
      }
    },

    /** Recording year. */
    TYER {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.deepFile.addMeta(MetaElem.YEAR,
            ParserUtil.convertYear(toInt(obj.readText(size))));
      }
    },

    /** User defined text information frame. */
    TXXX {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.COMMENT, obj.readText(size));
      }
    },

    /** Lyrics. */
    USLT {
      @Override
      public void parse(final MP3Parser obj, final int size)
          throws IOException {
        obj.deepFile.addMeta(MetaElem.LYRICS, obj.readText(size));
      }
    };

    /**
     * <p>
     * Frame specific parse method.
     * </p>
     * @param obj {@link MP3Parser} instance to send parser events from
     * @param size the size of the frame in bytes
     * @throws IOException if any error occurs while reading the file
     */
    abstract void parse(final MP3Parser obj, final int size) throws IOException;
  }

  @Override
  public void propagate(final DeepFile df) {
    Main.notimplemented();
  }
}
