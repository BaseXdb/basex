package org.deepfs.fsml.parsers;

import static org.basex.util.Token.*;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.datatype.XMLGregorianCalendar;
import org.basex.core.Main;
import org.basex.util.Token;
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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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
  static final int HEADER_LENGTH = 10;
  /**
   * ID3v2 frame header length.
   * @see "ID3v2.4.0 structure"
   */
  static final int FRAME_HEADER_LENGTH = 10;
  /**
   * A tag MUST contain at least one frame. A frame must be at least 1 byte big,
   * excluding the header.)
   * @see "ID3v2.4.0 structure"
   */
  static final int MINIMAL_FRAME_SIZE = FRAME_HEADER_LENGTH + 1;
  /**
   * All available ID3v1.1 genres. Order is important! ID3v1.1 genres are stored
   * as a one byte value (as last byte in the ID3v1.1 tag). The position in the
   * array represents the "code" of the genre, the textual representation of the
   * genre X is stored at GENRES[X].
   */
  static final byte[][] GENRES = new byte[][] {
      new byte[] { 66, 108, 117, 101, 115}, // Blues
      new byte[] { 67, 108, 97, 115, 115, 105, 99, 32, 82, 111, //
          99, 107}, // Classic Rock
      new byte[] { 67, 111, 117, 110, 116, 114, 121}, // Country
      new byte[] { 68, 97, 110, 99, 101}, // Dance
      new byte[] { 68, 105, 115, 99, 111}, // Disco
      new byte[] { 70, 117, 110, 107}, // Funk
      new byte[] { 71, 114, 117, 110, 103, 101}, // Grunge
      new byte[] { 72, 105, 112, 45, 72, 111, 112}, // Hip-Hop
      new byte[] { 74, 97, 122, 122}, // Jazz
      new byte[] { 77, 101, 116, 97, 108}, // Metal
      new byte[] { 78, 101, 119, 32, 65, 103, 101}, // New Age
      new byte[] { 79, 108, 100, 105, 101, 115}, // Oldies
      new byte[] { 79, 116, 104, 101, 114}, // Other
      new byte[] { 80, 111, 112}, // Pop
      new byte[] { 82, 38, 66}, // R&B
      new byte[] { 82, 97, 112}, // Rap
      new byte[] { 82, 101, 103, 103, 97, 101}, // Reggae
      new byte[] { 82, 111, 99, 107}, // Rock
      new byte[] { 84, 101, 99, 104, 110, 111}, // Techno
      new byte[] { 73, 110, 100, 117, 115, 116, 114, 105, //
          97, 108}, // Industrial
      new byte[] { 65, 108, 116, 101, 114, 110, 97, 116, 105, //
          118, 101}, // Alternative
      new byte[] { 83, 107, 97}, // Ska
      new byte[] { 68, 101, 97, 116, 104, 32, 77, 101, 116, //
          97, 108}, // Death Metal
      new byte[] { 80, 114, 97, 110, 107, 115}, // Pranks
      new byte[] { 83, 111, 117, 110, 100, 116, 114, 97, 99, 107}, // Soundtrack
      new byte[] { 69, 117, 114, 111, 45, 84, 101, 99, 104, 110, //
          111}, // Euro-Techno
      new byte[] { 65, 109, 98, 105, 101, 110, 116}, // Ambient
      new byte[] { 84, 114, 105, 112, 45, 72, 111, 112}, // Trip-Hop
      new byte[] { 86, 111, 99, 97, 108}, // Vocal
      new byte[] { 74, 97, 122, 122, 43, 70, 117, 110, 107}, // Jazz+Funk
      new byte[] { 70, 117, 115, 105, 111, 110}, // Fusion
      new byte[] { 84, 114, 97, 110, 99, 101}, // Trance
      new byte[] { 67, 108, 97, 115, 115, 105, 99, 97, 108}, // Classical
      new byte[] { 73, 110, 115, 116, 114, 117, 109, 101, 110, //
          116, 97, 108}, // Instrumental
      new byte[] { 65, 99, 105, 100}, // Acid
      new byte[] { 72, 111, 117, 115, 101}, // House
      new byte[] { 71, 97, 109, 101}, // Game
      new byte[] { 83, 111, 117, 110, 100, 32, 67, 108, 105, 112}, // Sound Clip
      new byte[] { 71, 111, 115, 112, 101, 108}, // Gospel
      new byte[] { 78, 111, 105, 115, 101}, // Noise
      new byte[] { 65, 108, 116, 101, 114, 110, 82, 111, 99, 107}, // AlternRock
      new byte[] { 66, 97, 115, 115}, // Bass
      new byte[] { 83, 111, 117, 108}, // Soul
      new byte[] { 80, 117, 110, 107}, // Punk
      new byte[] { 83, 112, 97, 99, 101}, // Space
      new byte[] { 77, 101, 100, 105, 116, 97, 116, 105, //
          118, 101}, // Meditative
      new byte[] { 73, 110, 115, 116, 114, 117, 109, 101, 110, 116, 97, 108, //
          32, 80, 111, 112}, // Instrumental Pop
      new byte[] { 73, 110, 115, 116, 114, 117, 109, 101, 110, 116, 97, 108, //
          32, 82, 111, 99, 107}, // Instrumental Rock
      new byte[] { 69, 116, 104, 110, 105, 99}, // Ethnic
      new byte[] { 71, 111, 116, 104, 105, 99}, // Gothic
      new byte[] { 68, 97, 114, 107, 119, 97, 118, 101}, // Darkwave
      new byte[] { 84, 101, 99, 104, 110, 111, 45, 73, 110, 100, 117, 115, //
          116, 114, 105, 97, 108}, // Techno-Industrial
      new byte[] { 69, 108, 101, 99, 116, 114, 111, 110, 105, 99}, // Electronic
      new byte[] { 80, 111, 112, 45, 70, 111, 108, 107}, // Pop-Folk
      new byte[] { 69, 117, 114, 111, 100, 97, 110, 99, 101}, // Eurodance
      new byte[] { 68, 114, 101, 97, 109}, // Dream
      new byte[] { 83, 111, 117, 116, 104, 101, 114, 110, 32, 82, //
          111, 99, 107}, // Southern Rock
      new byte[] { 67, 111, 109, 101, 100, 121}, // Comedy
      new byte[] { 67, 117, 108, 116}, // Cult
      new byte[] { 71, 97, 110, 103, 115, 116, 97}, // Gangsta
      new byte[] { 84, 111, 112, 32, 52, 48}, // Top 40
      new byte[] { 67, 104, 114, 105, 115, 116, 105, 97, 110, 32, 82, //
          97, 112}, // Christian Rap
      new byte[] { 80, 111, 112, 47, 70, 117, 110, 107}, // Pop/Funk
      new byte[] { 74, 117, 110, 103, 108, 101}, // Jungle
      new byte[] { 78, 97, 116, 105, 118, 101, 32, 65, 109, 101, 114, 105, //
          99, 97, 110}, // Native American
      new byte[] { 67, 97, 98, 97, 114, 101, 116}, // Cabaret
      new byte[] { 78, 101, 119, 32, 87, 97, 118, 101}, // New Wave
      new byte[] { 80, 115, 121, 99, 104, 97, 100, 101, 108, 105, //
          99}, // Psychadelic
      new byte[] { 82, 97, 118, 101}, // Rave
      new byte[] { 83, 104, 111, 119, 116, 117, 110, 101, 115}, // Showtunes
      new byte[] { 84, 114, 97, 105, 108, 101, 114}, // Trailer
      new byte[] { 76, 111, 45, 70, 105}, // Lo-Fi
      new byte[] { 84, 114, 105, 98, 97, 108}, // Tribal
      new byte[] { 65, 99, 105, 100, 32, 80, 117, 110, 107}, // Acid Punk
      new byte[] { 65, 99, 105, 100, 32, 74, 97, 122, 122}, // Acid Jazz
      new byte[] { 80, 111, 108, 107, 97}, // Polka
      new byte[] { 82, 101, 116, 114, 111}, // Retro
      new byte[] { 77, 117, 115, 105, 99, 97, 108}, // Musical
      new byte[] { 82, 111, 99, 107, 32, 38, 32, 82, 111, 108, //
          108}, // Rock & Roll
      new byte[] { 72, 97, 114, 100, 32, 82, 111, 99, 107}, // Hard Rock
      new byte[] { 70, 111, 108, 107}, // Folk
      new byte[] { 70, 111, 108, 107, 45, 82, 111, 99, 107}, // Folk-Rock
      new byte[] { 78, 97, 116, 105, 111, 110, 97, 108, 32, 70, //
          111, 108, 107}, // National Folk
      new byte[] { 83, 119, 105, 110, 103}, // Swing
      new byte[] { 70, 97, 115, 116, 32, 70, 117, 115, 105, 111, //
          110}, // Fast Fusion
      new byte[] { 66, 101, 98, 111, 98}, // Bebob
      new byte[] { 76, 97, 116, 105, 110}, // Latin
      new byte[] { 82, 101, 118, 105, 118, 97, 108}, // Revival
      new byte[] { 67, 101, 108, 116, 105, 99}, // Celtic
      new byte[] { 66, 108, 117, 101, 103, 114, 97, 115, 115}, // Bluegrass
      new byte[] { 65, 118, 97, 110, 116, 103, 97, 114, 100, 101}, // Avantgarde
      new byte[] { 71, 111, 116, 104, 105, 99, 32, 82, 111, 99, //
          107}, // Gothic Rock
      new byte[] { 80, 114, 111, 103, 114, 101, 115, 115, 105, 118, 101, 32, //
          82, 111, 99, 107}, // Progressive Rock
      new byte[] { 80, 115, 121, 99, 104, 101, 100, 101, 108, 105, 99, 32, //
          82, 111, 99, 107}, // Psychedelic Rock
      new byte[] { 83, 121, 109, 112, 104, 111, 110, 105, 99, 32, 82, 111, //
          99, 107}, // Symphonic Rock
      new byte[] { 83, 108, 111, 119, 32, 82, 111, 99, 107}, // Slow Rock
      new byte[] { 66, 105, 103, 32, 66, 97, 110, 100}, // Big Band
      new byte[] { 67, 104, 111, 114, 117, 115}, // Chorus
      new byte[] { 69, 97, 115, 121, 32, 76, 105, 115, 116, 101, 110, 105, //
          110, 103}, // Easy Listening
      new byte[] { 65, 99, 111, 117, 115, 116, 105, 99}, // Acoustic
      new byte[] { 72, 117, 109, 111, 117, 114}, // Humour
      new byte[] { 83, 112, 101, 101, 99, 104}, // Speech
      new byte[] { 67, 104, 97, 110, 115, 111, 110}, // Chanson
      new byte[] { 79, 112, 101, 114, 97}, // Opera
      new byte[] { 67, 104, 97, 109, 98, 101, 114, 32, 77, 117, //
          115, 105, 99}, // Chamber Music
      new byte[] { 83, 111, 110, 97, 116, 97}, // Sonata
      new byte[] { 83, 121, 109, 112, 104, 111, 110, 121}, // Symphony
      new byte[] { 66, 111, 111, 116, 121, 32, 66, 114, 97, 115, //
          115}, // Booty Brass
      new byte[] { 80, 114, 105, 109, 117, 115}, // Primus
      new byte[] { 80, 111, 114, 110, 32, 71, 114, 111, 111, //
          118, 101}, // Porn Groove
      new byte[] { 83, 97, 116, 105, 114, 101}, // Satire
      new byte[] { 83, 108, 111, 119, 32, 74, 97, 109}, // Slow Jam
      new byte[] { 67, 108, 117, 98}, // Club
      new byte[] { 84, 97, 110, 103, 111}, // Tango
      new byte[] { 83, 97, 109, 98, 97}, // Samba
      new byte[] { 70, 111, 108, 107, 108, 111, 114, 101}, // Folklore
      new byte[] { 66, 97, 108, 108, 97, 100}, // Ballad
      new byte[] { 80, 111, 119, 101, 101, 114, 32, 66, 97, 108, 108, //
          97, 100}, // Poweer Ballad
      new byte[] { 82, 104, 121, 116, 109, 105, 99, 32, 83, 111, 117, //
          108}, // Rhytmic Soul
      new byte[] { 70, 114, 101, 101, 115, 116, 121, 108, 101}, // Freestyle
      new byte[] { 68, 117, 101, 116}, // Duet
      new byte[] { 80, 117, 110, 107, 32, 82, 111, 99, 107}, // Punk Rock
      new byte[] { 68, 114, 117, 109, 32, 83, 111, 108, 111}, // Drum Solo
      new byte[] { 65, 32, 67, 97, 112, 101, 108, 97}, // A Capela
      new byte[] { 69, 117, 114, 111, 45, 72, 111, 117, 115, 101}, // Euro-House
      new byte[] { 68, 97, 110, 99, 101, 32, 72, 97, 108, 108}, // Dance Hall
      new byte[] { 71, 111, 97}, // Goa
      new byte[] { 68, 114, 117, 109, 32, 38, 32, 66, 97, 115, //
          115}, // Drum & Bass
      new byte[] { 67, 108, 117, 98, 45, 72, 111, 117, 115, 101}, // Club-House
      new byte[] { 72, 97, 114, 100, 99, 111, 114, 101}, // Hardcore
      new byte[] { 84, 101, 114, 114, 111, 114}, // Terror
      new byte[] { 73, 110, 100, 105, 101}, // Indie
      new byte[] { 66, 114, 105, 116, 80, 111, 112}, // BritPop
      new byte[] { 78, 101, 103, 101, 114, 112, 117, 110, 107}, // Negerpunk
      new byte[] { 80, 111, 108, 115, 107, 32, 80, 117, 110, 107}, // Polsk Punk
      new byte[] { 66, 101, 97, 116}, // Beat
      new byte[] { 67, 104, 114, 105, 115, 116, 105, 97, 110, 32, 71, 97, //
          110, 103, 115, 116, 97, 32, 82, 97, 112}, // Christian Gangsta Rap
      new byte[] { 72, 101, 97, 118, 121, 32, 77, 101, 116, //
          97, 108}, // Heavy Metal
      new byte[] { 66, 108, 97, 99, 107, 32, 77, 101, 116, 97, //
          108}, // Black Metal
      new byte[] { 67, 114, 111, 115, 115, 111, 118, 101, 114}, // Crossover
      new byte[] { 67, 111, 110, 116, 101, 109, 112, 111, 114, 97, 114, 121, //
          32, 67, 104, 114, 105, 115, 116, 105, //
          97, 110}, // Contemporary Christian
      new byte[] { 67, 104, 114, 105, 115, 116, 105, 97, 110, 32, 82, 111, //
          99, 107}, // Christian Rock
      new byte[] { 77, 101, 114, 101, 110, 103, 117, 101}, // Merengue
      new byte[] { 83, 97, 108, 115, 97}, // Salsa
      new byte[] { 84, 114, 97, 115, 104, 32, 77, 101, 116, //
          97, 108}, // Trash Metal
      new byte[] { 65, 110, 105, 109, 101}, // Anime
      new byte[] { 74, 112, 111, 112}, // Jpop
      new byte[] { 83, 121, 110, 116, 104, 112, 111, 112}, // Synthpop
  };

  /** All available picture types for APIC frames. */
  static final String[] PICTURE_TYPE = new String[] { "Other",
      "file icon", "Other file icon", "Front cover", "Back cover",
      "Leaflet page", "Media - e.g. label side of CD",
      "Lead artist or lead performer or soloist", "Artist or performer",
      "Conductor", "Band or Orchestra", "Composer", "Lyricist or text writer",
      "Recording Location", "During recording", "During performance",
      "Movie or video screen capture", "A bright coloured fish",
      "Illustration", "Band or artist logotype", //
      "Publisher or Studio logotype"};

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
  private static final byte[][] MODES = { token("Stereo"),
    token("Joint Stereo"), token("Dual Channel"), token("Mono") };
  /** MP3 emphases. */
  private static final byte[][] EMPH = { token("None"),
    token("5015MS"), token("Illegal"), token("CCITT") };
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
  /** MP3 Types. */
  private static final byte[][] VERSIONS = new byte[][] {
    token("MPEG-2.5 "), EMPTY, token("MPEG-2 "), token("MPEG-1 "),
  };
  /** MP3 Types. */
  private static final byte[][] LAYERS = new byte[][] {
    token("Layer 1"), token("Layer 2"), token("Layer 3"), EMPTY
  };
  /** MP3 encoding. */
  private static final byte[][] ENCODE = { token("CBR"), token("VBR") };

  static {
    ParserRegistry.register("mp3", MP3Parser.class);
  }

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

  /** The {@link BufferedFileChannel} to read from. */
  BufferedFileChannel bfc;
  /** The DeepFile to store metadata and file contents. */
  DeepFile deepFile;

  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    bfc = f;
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
   * @param size the size of the ID3 tags.
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
      if(Token.eq(token("Xing"), vbrh)) {
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
      deepFile.addMeta(MetaElem.CODEC, Token.concat(VERSIONS[vers],
          LAYERS[layr]));
      deepFile.addMeta(MetaElem.BITRATE_KBIT, bitrate);
      deepFile.addMeta(MetaElem.SAMPLE_RATE, samples);
      deepFile.addMeta(MetaElem.MODE, MODES[mode]);
      deepFile.addMeta(MetaElem.EMPHASIS, EMPH[emph]);
      deepFile.addMeta(MetaElem.ENCODING, ENCODE[encoding]);
    } catch(IOException e) { /* end of file ... */ }
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
   * called before (and must return <code>true</code>).
   * @throws IOException if any error occurs while reading from the file.
   */
  private void readMetaID3v1() throws IOException {
    setTypeAndFormat();
    // tag is already buffered by checkID3v1()
    final byte[] array = new byte[30];
    bfc.get(array, 0, 30);
    if(!ws(array)) deepFile.addMeta(MetaElem.TITLE, Token.trim(array));
    bfc.get(array, 0, 30);
    if(!ws(array)) deepFile.addMeta(MetaElem.CREATOR_NAME, Token.trim(array));
    bfc.get(array, 0, 30);
    if(!ws(array)) deepFile.addMeta(MetaElem.ALBUM, Token.trim(array));
    final byte[] a2 = new byte[4];
    bfc.get(a2, 0, 4);
    if(!ws(a2)) deepFile.addMeta(MetaElem.YEAR,
        ParserUtil.convertYear(Token.toInt(a2)));
    bfc.get(array, 0, 30);
    if(array[28] == 0) { // detect ID3v1.1, last byte represents track
      if(array[29] != 0) {
        deepFile.addMeta(MetaElem.TRACK, array[29]);
        array[29] = 0;
      }
    }
    if(!ws(array)) deepFile.addMeta(MetaElem.DESCRIPTION, Token.trim(array));
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
    if(checkID3v1()) {
      bfc.position(pos);
      techInfo(size + 128);
    } else techInfo(size);
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
   * Returns the textual representation of the genre with the code
   * <code>b</code>.
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
   */
  private int readSynchsafeInt() {
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
   *         <code>null</code> if an invalid or unsupported encoding was
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
   * Reads and parses text from the file. Assure that at least <code>s</code>
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
   * least <code>s</code> bytes are buffered before calling this method.
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
    if(encoding.isEmpty()) { // no encoding specified
      return bfc.get(new byte[size]);
    }
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
      final byte[][] arrays = Token.split(value, ',');
      for(final byte[] a : arrays) {
        deepFile.addMeta(MetaElem.GENRE, Token.trim(a));
      }
    } else {
      deepFile.addMeta(MetaElem.GENRE, getGenre(id));
    }
  }

  /**
   * Removes all illegal chars from the byte array. ID3 track numbers may be of
   * the form <code>X/Y</code> (X is the track number, Y represents the number
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
    final int track = Token.toInt(value, start, i);
    if(track == Integer.MIN_VALUE) throw new ParserException(
        "Failed to parse track number");
    return track;
  }

  /**
   * Parses a date and returns the corresponding xml calendar.
   * @param d the date to parse
   * @return the xml date
   */
  XMLGregorianCalendar parseDate(final byte[] d) {
    final int len = d.length;
    if(len >= 4) { // yyyy
      final int year = Token.toInt(d, 0, 4);
      if(len >= 7) { // yyyy-MM
        if(d[4] != '-') return null;
        final int month = Token.toInt(d, 5, 7);
        if(len >= 10) { // yyyy-MM-dd
          if(d[7] != '-') return null;
          final int day = Token.toInt(d, 8, 10);
          if(len >= 13) { // yyyy-MM-ddTHH
            if(d[10] != 'T') return null;
            final GregorianCalendar gc = new GregorianCalendar();
            gc.set(year, month, day);
            gc.set(Calendar.HOUR_OF_DAY, Token.toInt(d, 11, 13));
            if(len >= 16) {
              if(d[13] != ':') return null;
              gc.set(Calendar.MINUTE, Token.toInt(d, 14, 16));
              if(len >= 19) {
                if(d[16] != ':') return null;
                gc.set(Calendar.SECOND, Token.toInt(d, 17, 19));
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
        if(typeId >= 0 && typeId < PICTURE_TYPE.length) {
          name = PICTURE_TYPE[typeId];
        }

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
        byte[] lang = obj.readText(3, "");
        for(final byte b : lang) {
          if(ws(b) || b == 0) {
            lang = EMPTY;
            break;
          }
        }
        int pos = 4;
        // ignore short content description
        while(obj.bfc.get() != 0 && ++pos < size)
          ;
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
          // [BL] different elements for gYear, gYearMonth, ...
      }
    },

    /** Tagging time. */
    TDTG {
      @Override
      public void parse(final MP3Parser obj, final int size) {
          // [BL] different elements for gYear, gYearMonth, ...
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
          // [BL] parse file type

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
          // [BL] parse media type

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

    // [BL] distinguish between different TPE fields

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
        } catch(ParserException e) {
          obj.deepFile.debug("MP3Parser: Failed to parse track number (%).", e);
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
            ParserUtil.convertYear(Token.toInt(obj.readText(size))));
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
