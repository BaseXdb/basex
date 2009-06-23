package org.basex.build.fs.parser;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;
import org.basex.BaseX;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.parser.Metadata.DataType;
import org.basex.build.fs.parser.Metadata.Definition;
import org.basex.build.fs.parser.Metadata.Element;
import org.basex.build.fs.parser.Metadata.MimeType;
import org.basex.build.fs.parser.Metadata.Type;
import org.basex.util.Token;

/**
 * Parser for MP3 audio files.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek
 * @author Bastian Lemke
 * 
 * 
 * @see <a href="http://www.id3.org/id3v2.4.0-structure">ID3v2.4.0 structure</a>
 * @see <a href="http://www.id3.org/id3v2.4.0-frames">ID3v2.4.0 frames</a>
 */
public class MP3Parser extends AbstractParser {

  // ---------------------------------------------------------------------------
  // ----- static stuff --------------------------------------------------------
  // ---------------------------------------------------------------------------

  /** Supported file suffixes. */
  private static final Set<String> SUFFIXES = new HashSet<String>();
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
      new byte[] { 73, 110, 115, 116, 114, 117, 109, 101, 110, 116, 97, 108,
          32, 80, 111, 112}, // Instrumental Pop
      new byte[] { 73, 110, 115, 116, 114, 117, 109, 101, 110, 116, 97, 108,
          32, 82, 111, 99, 107}, // Instrumental Rock
      new byte[] { 69, 116, 104, 110, 105, 99}, // Ethnic
      new byte[] { 71, 111, 116, 104, 105, 99}, // Gothic
      new byte[] { 68, 97, 114, 107, 119, 97, 118, 101}, // Darkwave
      new byte[] { 84, 101, 99, 104, 110, 111, 45, 73, 110, 100, 117, 115, 116,
          114, 105, 97, 108}, // Techno-Industrial
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
      new byte[] { 78, 97, 116, 105, 118, 101, 32, 65, 109, 101, 114, 105, 99,
          97, 110}, // Native American
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
      new byte[] { 80, 114, 111, 103, 114, 101, 115, 115, 105, 118, 101, 32,
          82, 111, 99, 107}, // Progressive Rock
      new byte[] { 80, 115, 121, 99, 104, 101, 100, 101, 108, 105, 99, 32, 82,
          111, 99, 107}, // Psychedelic Rock
      new byte[] { 83, 121, 109, 112, 104, 111, 110, 105, 99, 32, 82, 111, 99,
          107}, // Symphonic Rock
      new byte[] { 83, 108, 111, 119, 32, 82, 111, 99, 107}, // Slow Rock
      new byte[] { 66, 105, 103, 32, 66, 97, 110, 100}, // Big Band
      new byte[] { 67, 104, 111, 114, 117, 115}, // Chorus
      new byte[] { 69, 97, 115, 121, 32, 76, 105, 115, 116, 101, 110, 105, 110,
          103}, // Easy Listening
      new byte[] { 65, 99, 111, 117, 115, 116, 105, 99}, // Acoustic
      new byte[] { 72, 117, 109, 111, 117, 114}, // Humour
      new byte[] { 83, 112, 101, 101, 99, 104}, // Speech
      new byte[] { 67, 104, 97, 110, 115, 111, 110}, // Chanson
      new byte[] { 79, 112, 101, 114, 97}, // Opera
      new byte[] { 67, 104, 97, 109, 98, 101, 114, 32, 77, 117, //
          115, 105, 99}, // Chamber Music
      new byte[] { 83, 111, 110, 97, 116, 97}, // Sonata
      new byte[] { 83, 121, 109, 112, 104, 111, 110, 121}, // Symphony
      new byte[] { 66, 111, 111, 116, 121, 32, 66, 114, 97, //
          115, 115}, // Booty Brass
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
      new byte[] { 67, 104, 114, 105, 115, 116, 105, 97, 110, 32, 71, 97, 110,
          103, 115, 116, 97, 32, 82, 97, 112}, // Christian Gangsta Rap
      new byte[] { 72, 101, 97, 118, 121, 32, 77, 101, 116, //
          97, 108}, // Heavy Metal
      new byte[] { 66, 108, 97, 99, 107, 32, 77, 101, 116, 97, // 
          108}, // Black Metal
      new byte[] { 67, 114, 111, 115, 115, 111, 118, 101, 114}, // Crossover
      new byte[] { 67, 111, 110, 116, 101, 109, 112, 111, 114, 97, 114, 121,
          32, 67, 104, 114, 105, 115, 116, 105, //
          97, 110}, // Contemporary Christian
      new byte[] { 67, 104, 114, 105, 115, 116, 105, 97, 110, 32, 82, 111, 99,
          107}, // Christian Rock
      new byte[] { 77, 101, 114, 101, 110, 103, 117, 101}, // Merengue
      new byte[] { 83, 97, 108, 115, 97}, // Salsa
      new byte[] { 84, 114, 97, 115, 104, 32, 77, 101, 116, //
          97, 108}, // Trash Metal
      new byte[] { 65, 110, 105, 109, 101}, // Anime
      new byte[] { 74, 112, 111, 112}, // Jpop
      new byte[] { 83, 121, 110, 116, 104, 112, 111, 112}, // Synthpop
  };

  /** All available picture types for APIC frames. */
  private static final String[] PICTURE_TYPE = new String[] { "Other", //
      "file icon", //
      "Other file icon", //
      "Front cover", //
      "Back cover", //
      "Leaflet page", //
      "Media - e.g. label side of CD", //
      "Lead artist or lead performer or soloist", //
      "Artist or performer", //
      "Conductor", //
      "Band or Orchestra", //
      "Composer", //
      "Lyricist or text writer", //
      "Recording Location", //
      "During recording", //
      "During performance", //
      "Movie or video screen capture", //
      "A bright coloured fish", //
      "Illustration", //
      "Band or artist logotype", //
      "Publisher or Studio logotype"};

  static {
    SUFFIXES.add("mp3");
    for(String s : SUFFIXES) {
      REGISTRY.put(s, MP3Parser.class);
    }
  }

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

  /** Standard constructor. */
  public MP3Parser() {
    super(SUFFIXES, Type.AUDIO, MimeType.MP3);
  }

  // TODO: add support for extended ID3v2 header
  // TODO: add support for extended tag (before ID3v1 tag)

  // ---------------------------------------------------------------------------

  /** The current FileChannel. */
  FileChannel ch;
  /** The maximum number of bytes to read from the {@link FileChannel}. */
  private long fcLimit;
  /** Buffer for the file content. */
  ByteBuffer buf;
  /** The {@link NewFSParser} instance to fire events. */
  NewFSParser fsparser;

  /** {@inheritDoc} */
  @Override
  boolean check(final FileChannel f, final long limit) throws IOException {
    ch = f;
    fcLimit = limit;
    return checkID3v2() || checkID3v1();
  }

  /** {@inheritDoc} */
  @Override
  public void readMeta(final FileChannel f, final long limit,
      final NewFSParser fsParser) throws IOException {
    fsparser = fsParser;
    ch = f;
    fcLimit = limit;
    if(checkID3v2()) readMetaID3v2();
    else if(checkID3v1()) readMetaID3v1();
  }

  /** {@inheritDoc} */
  @Override
  public void readContent(final FileChannel f, final long limit,
      final NewFSParser fsParser) {
  // no textual representation for mp3 content ...
  }

  // ---------------------------------------------------------------------------
  // ----- ID3v1 methods -------------------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Checks if the file contains a ID3v1 tag and sets the file pointer to the
   * beginning of the tag.
   * @return true if the file contains a valid ID3v1 tag.
   * @throws IOException if any error occurs while reading the file.
   */
  private boolean checkID3v1() throws IOException {
    if(fcLimit < 128) return false;
    // ID3v1 tags are located at the end of the file (last 128 bytes)
    // The tag begins with the string "TAG" (first three bytes)
    ch.position(ch.position() + (fcLimit - 128));
    byte[] h = new byte[3];
    fcLimit -= ch.read(ByteBuffer.wrap(h));
    return (h[0] == 'T' && h[1] == 'A' && h[2] == 'G') ? true : false;
  }

  /**
   * Reads the ID3v1 metadata from the file. {@link #checkID3v1()} must be
   * called before.
   * @throws IOException if any error occurs while reading the ID3v1 tag.
   */
  private void readMetaID3v1() throws IOException {
    ByteBuffer[] tag = new ByteBuffer[6];
    tag[0] = ByteBuffer.allocate(30); // title
    tag[1] = ByteBuffer.allocate(30); // artist
    tag[2] = ByteBuffer.allocate(30); // album
    tag[3] = ByteBuffer.allocate(4); // year
    tag[4] = ByteBuffer.allocate(30); // comment
    tag[5] = ByteBuffer.allocate(1); // genre
    fcLimit -= ch.read(tag);
    assert fcLimit == 0;
    byte[] array;
    array = tag[0].array();
    if(array[0] != 0) {
      fsparser.metaEvent(Element.TITLE, DataType.STRING, Definition.NONE, null,
          array);
    }
    array = tag[1].array();
    if(array[0] != 0) {
      fsparser.metaEvent(Element.CREATOR, DataType.STRING, Definition.ARTIST,
          null, array);
    }
    array = tag[2].array();
    if(array[0] != 0) {
      fsparser.metaEvent(Element.ALBUM, DataType.STRING, Definition.NONE, null,
          array);
    }
    array = tag[3].array();
    if(array[0] != 0) {
      fsparser.metaEvent(Element.DATE, DataType.YEAR, Definition.RELEASE_TIME,
          null, ParserUtil.convertYear(array));
    }
    array = tag[4].array();
    if(array[28] == 0) { // detect ID3v1.1, last byte represents track
      if(array[29] != 0) {
        fsparser.metaEvent(Element.TRACK, DataType.INTEGER, Definition.NONE,
            null, Token.token(array[29]));
        array[29] = 0;
      }
    }
    if(array[0] != 0) {
      fsparser.metaEvent(Element.COMMENT, DataType.STRING, Definition.NONE,
          null, array);
    }
    array = tag[5].array();
    if(array[0] != 0) {
      fsparser.metaEvent(Element.GENRE, DataType.STRING, Definition.NONE, null,
          getGenre(array[0] & 0xFF));
    }
  }

  // ---------------------------------------------------------------------------
  // ----- ID3v2 methods -------------------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Checks if the file contains a ID3v2 tag and sets the file pointer to the
   * beginning of the ID3 header fields.
   * @return true if the file contains a ID3v2 tag.
   * @throws IOException if any error occurs while reading the file.
   */
  private boolean checkID3v2() throws IOException {
    if(fcLimit < HEADER_LENGTH + MINIMAL_FRAME_SIZE) return false;
    // ID3v2 tags are usually located at the beginning of the file.
    // The tag begins with the string "ID3" (first three bytes)
    byte[] h = new byte[3];
    fcLimit -= ch.read(ByteBuffer.wrap(h));
    return (h[0] == 'I' && h[1] == 'D' && h[2] == '3') ? true : false;
  }

  /**
   * Reads the ID3v2 metadata from the file. The behaviour is undefined if there
   * is no ID3v2 tag available, therefore {@link #checkID3v1()} should always be
   * called before.
   * @throws IOException if any error occurs while reading the ID3v2 tag.
   */
  private void readMetaID3v2() throws IOException {
    int bufSize = fcLimit < Metadata.DEFAULT_BUFFER_SIZE ? (int) fcLimit
        : Metadata.DEFAULT_BUFFER_SIZE;
    buf = ByteBuffer.allocateDirect(bufSize);
    buf.position(buf.limit());
    int size = readID3v2Header();
    int remainingFrames = Frame.values().length;

    while(size > MINIMAL_FRAME_SIZE) {
      // abort if all "interesting" frames have been read
      if(remainingFrames == 0) break;
      int res = readID3v2Frame();
      if(res > 0) {
        size -= res;
        remainingFrames--;
      } else size += res;
    }
  }

  /**
   * Reads the ID3v2 header and returns the header size.
   * @return the size of the ID3v2 header.
   * @throws IOException if any error occurs while reading the file.
   */
  private int readID3v2Header() throws IOException {
    ch.position(6); // skip tag identifier, ID3 version fields and flags
    int size = readSynchsafeInt();
    return size;
  }

  /**
   * Reads the ID3v2 frame at the current buffer position. Afterwards, the
   * buffer position is set to the first byte after this frame.
   * @return the number of bytes read, {@link Integer#MAX_VALUE} if the end of
   *         the header was detected or the number of bytes read (as negative
   *         number) if the frame was not parsed.
   * @throws IOException if any error occurs while reading the file.
   */
  private int readID3v2Frame() throws IOException {
    checkRemaining(MINIMAL_FRAME_SIZE);
    // padding (some 0x00 bytes) marks correct end of frames.
    if(buf.get(buf.position()) == 0x00) return Integer.MAX_VALUE;
    byte[] frameId = new byte[4];
    buf.get(frameId);
    int frameSize = readSynchsafeInt();
    buf.position(buf.position() + 2); // skip flags
    Frame frame;
    try {
      frame = Frame.valueOf(Token.string(frameId));
      frame.parse(this, frameSize);
      return frameSize;
    } catch(IllegalArgumentException e) {
      skip(frameSize);
      return -frameSize;
    }
  }

  // ---------------------------------------------------------------------------
  // ----- utility methods -----------------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Returns the textual representation of the genre with the code
   * <code>b</code>.
   * @param b the "code" of the genre.
   * @return the textual representation of the genre.
   */
  static byte[] getGenre(final int b) {
    if(b < GENRES.length && b >= 0) return GENRES[b];
    else {
      BaseX.debug("Illegal genre ID detected: " + b);
      return Token.EMPTY;
    }
  }

  /**
   * Returns the current absolute position in the file.
   * @return the current absolute position in the file.
   * @throws IOException if any error occurs while reading from the file.
   */
  long getFilePos() throws IOException {
    assert ch.position() - buf.remaining() > 0;
    return ch.position() - buf.remaining();
  }

  /**
   * Returns the current position in the buffer.
   * @return the current position in the buffer.
   */
  int getBufPos() {
    return buf.position();
  }

  // ---------------------------------------------------------------------------
  // ----- read() methods (methods that read from the file) --------------------
  // ---------------------------------------------------------------------------

  /**
   * Checks if the buffer contains enough data. If the number of remaining bytes
   * in the buffer is <code>< n</code>, data is read from the file to fill the
   * buffer.
   * @param n minimum number of bytes in the buffer.
   * @return <b>true</b> if the buffer is large enough to contain all
   *         <code>n</code> bytes, <b>false</b> if
   *         <code>n > buf.capacity()</code>.
   * @throws IOException if any error occurs while reading the file.
   */
  private boolean checkRemaining(final int n) throws IOException {
    int remaining = buf.remaining();
    if(remaining <= n) {
      if(fcLimit < n - remaining) throw new EOFException();
      if(n > buf.capacity()) return false;
      buf.compact();
      fcLimit -= ch.read(buf);
      buf.flip();
    }
    return true;
  }

  /**
   * Skips <code>n</code> bytes in the ByteBuffer.
   * @param n number of bytes to skip.
   * @throws IOException if any error occurs while reading the file.
   */
  void skip(final int n) throws IOException {
    int remaining = buf.remaining();
    if(remaining < n) {
      ch.position(ch.position() + (n - remaining));
      buf.position(buf.limit());
    } else {
      buf.position(buf.position() + n);
    }
  }

  /**
   * Reads a synchsafe integer (4 bytes) from the ByteBuffer and converts it to
   * a "normal" integer. In ID3 tags, some integers are encoded as "synchsafe"
   * integers to distinguish them from data in other blocks. The most
   * significant bit of each byte is zero, making seven bits out of eight
   * available.
   * @return the integer.
   * @throws IOException if any error occurs while reading the file.
   */
  private int readSynchsafeInt() throws IOException {
    checkRemaining(4);
    int b1, b2, b3, b4;
    b1 = buf.get() & 0xFF;
    b2 = buf.get() & 0xFF;
    b3 = buf.get() & 0xFF;
    b4 = buf.get() & 0xFF;
    return b1 << 21 | b2 << 14 | b3 << 7 | b4;
  }

  /**
   * Reads <code>length</code> bytes from the file and returns a byte array with
   * the content.
   * @param length number of bytes to read.
   * @return the byte array.
   * @throws IOException if any error occurs while reading the file.
   */
  private byte[] read(final int length) throws IOException {
    byte[] value = new byte[length];
    if(checkRemaining(length)) {
      buf.get(value);
    } else { // buf is too small
      int buffered = buf.remaining();
      int remaining = length - buffered;
      if(fcLimit < remaining) throw new EOFException();
      buf.get(value, 0, buffered); // copy buffered bytes
      buf.clear(); // clear buffer
      // read remaining bytes directly into target buffer and fill the buffer
      ByteBuffer buf2 = ByteBuffer.wrap(value, buffered, remaining);
      fcLimit -= ch.read(new ByteBuffer[] { buf2, buf});
      buf.flip();
    }
    return value;
  }

  /**
   * Skip the text encoding description bytes.
   * @return the number of skipped bytes.
   * @throws IOException if any error occurs while reading from the file.
   */
  private int skipEncBytes() throws IOException {
    checkRemaining(3);
    // TODO: handle different encodings
    // skip text encoding description bytes
    int bytesToSkip = 0;
    if((buf.get() & 0xFF) <= 0x04) bytesToSkip++;
    if((buf.get() & 0xFF) >= 0xFE) bytesToSkip++;
    if((buf.get() & 0xFF) >= 0xFE) bytesToSkip++;
    buf.position(buf.position() - (3 - bytesToSkip));
    return bytesToSkip;
  }

  /**
   * Reads and parses text from the file.
   * @param s number of bytes to read.
   * @return byte array with the text.
   * @throws IOException if any error occurs while reading the file.
   */
  byte[] readText(final int s) throws IOException {
    int size = s - skipEncBytes();
    if(size <= 0) return Token.EMPTY;
    return read(size);
  }

  /**
   * Reads and parses the genre from the file.
   * @param s number of bytes to read.
   * @return byte array with the genre.
   * @throws IOException if any error occurs while reading the file.
   */
  byte[] readGenre(final int s) throws IOException {
    byte[] value = readText(s);
    int id;
    if(!Token.ws(value)) {
      if(value[0] == '(') { // ignore brackets around genre id
        int limit = 1;
        while(value[limit] >= '0' && value[limit] <= '9' && limit < s)
          limit++;
        id = Token.toInt(value, 1, limit);
      } else id = Token.toInt(value);
      return id == Integer.MIN_VALUE ? value : getGenre(id);
    }
    return Token.EMPTY;
  }

  /**
   * Removes all illegal chars from the byte array. ID3 track numbers may be of
   * the form <code>X/Y</code> (X is the track number, Y represents the number
   * of tracks in the whole set). Everything after '/' is deleted.
   * @param s number of bytes to read
   * @return a byte array that contains only ASCII bytes that are valid integer
   *         numbers.
   * @throws IOException if any error occurs while reading the file.
   */
  byte[] readTrack(final int s) throws IOException {
    byte[] value = readText(s);
    int size = value.length;
    if(size == 0) return Token.EMPTY;
    int i = 0;
    while(i < size && (value[i] < '0' || value[i] > '9')) {
      value[i] = 0;
      i++;
    }
    if(i >= size - 1) return Token.EMPTY;
    while(i < size && value[i] >= '0' && value[i] <= '9')
      i++;
    while(i < size)
      value[i++] = 0;
    return value;
  }

  /**
   * Reads the file suffix of an embedded picture.
   * @return the file suffix.
   * @throws IOException if any error occurs while reading from the file.
   */
  String readPicSuffix() throws IOException {
    checkRemaining(9);
    skipEncBytes();
    StringBuilder sb = new StringBuilder();
    byte b;
    while((b = buf.get()) != 0)
      sb.append((char) b);
    String string = sb.toString();
    if(string.startsWith("image/")) {
      string = string.substring(6); // skip "image/"
    }
    if(string.length() != 3) {
      BaseX.debug("Unsupported picture MIME type in ID3v2 tag ... "
          + "skipping image.");
      return null;
    }
    return string.toLowerCase();
  }

  /**
   * Reads the picture type id from the APIC frame and returns a textual
   * representation that can be used as file name.
   * @return a textual representation of the picture.
   * @throws IOException if any error occurs while reading from the file.
   */
  String getPicName() throws IOException {
    // there may be more than one APIC frame with the same ID in the ID3 tag
    // TODO: avoid duplicate file names
    checkRemaining(1);
    int typeId = buf.get() & 0xFF;
    if(typeId >= 0 && typeId < PICTURE_TYPE.length) {
      return PICTURE_TYPE[typeId];
    } else return null;
  }

  /**
   * Skip the picture description.
   * @throws IOException if any error occurs while reading from the file.
   */
  void skipPicDescription() throws IOException {
    while(true) {
      try {
        if(buf.get() == 0) break;
      } catch(BufferUnderflowException e) {
        checkRemaining(1);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // ----- Frame enumeration that fires all the events -------------------------
  // ---------------------------------------------------------------------------

  /**
   * Mapping for ID3 frames to xml elements.
   * @author Bastian Lemke
   */
  private enum Frame {
    /** */
    TIT2 {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.fsparser.metaEvent(Element.TITLE, DataType.STRING, Definition.NONE,
            null, obj.readText(size));
      }
    },
    /** */
    TPE1 {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.fsparser.metaEvent(Element.CREATOR, DataType.STRING,
            Definition.ARTIST, null, obj.readText(size));
      }
    },
    /** */
    TALB {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.fsparser.metaEvent(Element.ALBUM, DataType.STRING, Definition.NONE,
            null, obj.readText(size));
      }
    },
    /** */
    TYER {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.fsparser.metaEvent(Element.DATE, DataType.YEAR,
            Definition.RELEASE_TIME, null,
            ParserUtil.convertYear(obj.readText(size)));
      }
    },
    /** */
    TCON {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.fsparser.metaEvent(Element.GENRE, DataType.STRING, Definition.NONE,
            null, obj.readGenre(size));
      }
    },
    /** */
    COMM {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.fsparser.metaEvent(Element.COMMENT, DataType.STRING,
            Definition.NONE, null, obj.readText(size));
      }
    },
    /** */
    TRCK {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.fsparser.metaEvent(Element.TRACK, DataType.INTEGER,
            Definition.NONE, null, obj.readTrack(size));
      }
    },
    /** */
    TLEN {
      @Override
      void parse(final MP3Parser obj, final int size) throws IOException {
        obj.fsparser.metaEvent(Element.DURATION, DataType.DURATION,
            Definition.NONE, null, ParserUtil.msToDuration(obj.readText(size)));
      }
    },
    /** */
    APIC {
      @Override
      void parse(final MP3Parser obj, final int s) throws IOException {
        int position = obj.getBufPos();
        String suffix = obj.readPicSuffix();
        if(suffix == null) {
          obj.skip(s - (obj.getBufPos() - position));
          return;
        }
        obj.skipPicDescription();
        String name = obj.getPicName();
        int size = s - (obj.getBufPos() - position);
        long offset = obj.getFilePos();
        try {
          obj.fsparser.parseFileFragment(obj.ch, size, name, suffix, offset);
        } catch(IOException e) {
          BaseX.debug("Failed to parse APIC frame (%).", e.getMessage());
        }
        obj.ch.position(offset);
        obj.skip(size);
      }
    };

    /**
     * <p>
     * Frame specific parse method.
     * </p>
     * @param obj {@link MP3Parser} instance to send parser events from.
     * @param size the size of the frame in bytes.
     * @throws IOException if any error occurs while reading the file.
     */
    abstract void parse(final MP3Parser obj, final int size) throws IOException;
  }
}
