package org.basex.build.fs;

import static org.basex.util.Token.*;

/**
 * This interface assembles textual file system information.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public interface FSText {
  /** Image tag. */
  byte[] IMAGE = token("Image");
  /** Audio tag. */
  byte[] AUDIO = token("Audio");
  /** XML tag. */
  byte[] XML = token("XML");

  /** Image attributes. */
  byte[] TYPE = token("type");
  /** GIF suffix. */
  byte[] TYPEGIF = token("gif");
  /** JPG type. */
  byte[] TYPEJPG = token("jpg");
  /** PNG type. */
  byte[] TYPEPNG = token("png");
  /** BMP type. */
  byte[] TYPEBMP = token("bmp");
  /** JPEG suffix. */
  byte[] TYPEJPEG = token("jpeg");
  /** TIF suffix. */
  byte[] TYPETIF = token("tif");
  /** EML suffix. */
  byte[] TYPEEML = token("eml");
  /** MBS suffix. */
  byte[] TYPEMBS = token("mbs");
  /** MBX suffix. */
  byte[] TYPEMBX = token("mbx");
  /** MP3 suffix. */
  byte[] TYPEMP3 = token("mp3");
  /** XML suffix. */
  byte[] TYPEXML = token("xml");

  /** Width tag. */
  byte[] WIDTH = token("Width");
  /** Height tag. */
  byte[] HEIGHT = token("Height");
  /** EXIF information. */
  byte[] EXIF = token("EXIF");
  /** EXIF Exposure. */
  byte[] EXPOS = token("ExposureTimeMS");

  /** GIF87a header info. */
  byte[] HEADERGIF87 = token("GIF87a");
  /** GIF89a header info. */
  byte[] HEADERGIF89 = token("GIF89a");
  /** PNG header info. */
  byte[] HEADERPNG = { -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72,
      68, 82 };
  /** JPG header info. */
  int[] HEADERJPG = { 0xFF, 0xD8 };
  /** JPG header info. */
  int[] HEADEREXIF = { 0x45, 0x78, 0x69, 0x66, 0, 0 };
  /** BMP header info. */
  byte[] HEADERBMP = token("BM");

  /** ID3v2 error message. */
  String ID3V2ERROR = "- ID3 Tags not found (%)";
  /** ID3v2 parsing stopped. */
  String ID3SIZE1 = "- ID3 '%' -> '%' Tag Size = % (%)";
  /** ID3v2 parsing stopped. */
  String ID3SIZE2 = "- ID3 '%' Tag Size = % (%)";
  /** ID3v2 invalid. */
  String ID3INVALID = "- ID3 Header invalid (%)";
  /** ID3v2 parsing stopped. */
  String ID3NULL = "- ID3 '%' Tag contains 'zero' bytes (%)";
  /** ID3v2 parsing stopped. */
  String ID3UNKNOWN = "- ID3 '%' Tag unknown (%)";

  /** MP3 info. */
  byte[] MP3CODEC = token("Codec");
  /** MP3 info. */
  byte[] MP3RATE = token("Bitrate");
  /** MP3 info. */
  byte[] MP3SAMPLE = token("Samplerate");
  /** MP3 info. */
  byte[] MP3MODE = token("Mode");
  /** MP3 info. */
  byte[] MP3EMPH = token("Emphasis");
  /** MP3 info. */
  byte[] MP3SEC = token("Seconds");
  /** MP3 info. */
  byte[] MP3PAD = token("Padding");
  /** MP3 info. */
  byte[] MP3ENCODE = token("Encoding");

  /** MP3 tag. */
  byte[] PERSON = token("Person");
  /** MP3 tag. */
  byte[] TITLE = token("Title");
  /** MP3 tag. */
  byte[] ALBUM = token("Album");
  /** MP3 tag. */
  byte[] BITRATE = token("Bitrate");
  /** MP3 tag. */
  byte[] SECONDS = token("Seconds");

  /** ID3v1 header info. */
  byte[] HEADERID3V1 = token("TAG");
  /** ID3v1 tag. */
  byte[] ID3TITLE = token("Title");
  /** ID3v1 tag. */
  byte[] ID3ALBUM = token("Album");
  /** ID3v1 tag. */
  byte[] ID3ARTIST = token("Artist");
  /** ID3v1 tag. */
  byte[] ID3YEAR = token("Year");
  /** ID3v1 tag. */
  byte[] ID3COMMENT = token("Comment");

  /** ID3v2 tag. */
  byte[] ID3 = token("id3");
  /** MP3 XING header. */
  byte[] MP3XING = token("Xing");
  /** ID3v2.x.y version number. */
  byte[] ID3VERS = token("version");
  /** ID3v2.x.y flag info id3v2.4.0-structure.txt l.144ff. */
  byte[] ID3FLAG_UNSYNC = token("unsynchronised");
  /** ID3v2.x.y flag info id3v2.4.0-structure.txt l.151ff. */
  byte[] ID3FLAG_EXTHEA = token("extendedheader");
  /** ID3v2.x.y flag info id3v2.4.0-structure.txt l.159ff. */
  byte[] ID3FLAG_EXPERI = token("experimental");
  /** ID3v2.x.y flag info id3v2.4.0-structure.txt l.165ff. */
  byte[] ID3FLAG_FOOTER = token("footer");

  /** EXIF Number ignored. */
  String EXIFIGNORED = "- EXIF Code 0x% ignored (%)";
  /** EXIF Format Mismatch. */
  String EXIFFORMAT1 = "- EXIF Code 0x%: Format Mismatch %, % instead of % (%)";
  /** EXIF Format Mismatch. */
  String EXIFFORMAT2 = "- EXIF Code 0x%: Format ignored %, % instead of % (%)";
  /** EXIF Unknown type. */
  String EXIFUNKNOWN = "Unknown data format: %  -> %";
  /** EXIF Undefined Type. */
  String EXIFUNDEFINED = "- EXIF method missing for EXIFUndefined.type";
  /** EXIF Undefined Type. */
  String EXIFRATIONAL = "- EXIF Method missing for specified EXIFRational.type";

  /** Email tag. */
  byte[] EMAIL = token("Mail");
  /** Email section. */
  byte[] EMLBODY = token("Section");
  /** Email section. */
  byte[] EMLATTACHMENT = token("Section");

  /** Email subject info. */
  byte[] EMLSUBJECT = token("Subject");
  /** Email sender info. */
  byte[] EMLFROM = token("From");
  /** Email receiver info. */
  byte[] EMLTO = token("To");
  /** Email date info. */
  byte[] EMLDATE = token("Date");
  /** Email time info in minutes from 1.1.1970. */
  byte[] EMLTIME = token("time");
  /** Email content-type info. */
  byte[] EMLCONTENTTYPE = token("Content-Type");

  /** Email attributes. */
  String[] EMLATTR = { "cc", "user-agent", "organization" };
  /** Email attributes. */
  byte[][] ATTRIBUTETOKENS = { token("CC"), token("User-Agent"),
      token("Organization") };
  
  /** Error message 'Import FS...' if backing storage exists. */
  String BACKINGEXISTS = "Backing storage exists, please delete first: ";
}
