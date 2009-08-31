/**
 * 
 */
package org.basex.build.fs.parser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.basex.BaseX;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.util.BufferedFileChannel;
import org.basex.build.fs.util.Metadata;
import org.basex.build.fs.util.ParserUtil;
import org.basex.build.fs.util.Metadata.DateField;
import org.basex.build.fs.util.Metadata.MetaType;
import org.basex.build.fs.util.Metadata.MimeType;
import org.basex.build.fs.util.Metadata.StringField;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Parser for EML files.
 * @author Bastian Lemke
 * @author Lukas Kircher
 */
public class EMLParser extends AbstractParser {

  static {
    NewFSParser.register("eml", EMLParser.class);
    NewFSParser.register("emlx", EMLParser.class);
  }

  // ----- enums ---------------------------------------------------------------

  /**
   * Enum to map the mail metadata to deepfs metadata attributes.
   * @author Bastian Lemke
   */
  private enum EML_Meta {
    /** The sender of the mail. */
    FROM {
      @Override
      public boolean parse(final EMLParser obj) throws IOException {
        obj.parseMailAddresses(StringField.SENDER);
        return false;
      }
    },
    /** Date when the mail was sended. */
    DATE {
      @Override
      public boolean parse(final EMLParser obj) throws IOException {
        try {
          final Date d = SDF.parse(obj.mCurrLine);
          obj.meta.setDate(DateField.DATE_CREATED, ParserUtil.convertDate(d));
          obj.fsparser.metaEvent(obj.meta);
        } catch(final ParseException ex) {
          if(NewFSParser.VERBOSE) BaseX.debug("%: %", obj.bfc.getFileName(),
              ex.getMessage());
        }
        return true;
      }
    },
    /** The mail subject. */
    SUBJECT {
      @Override
      public boolean parse(final EMLParser obj) throws IOException {
        final TokenBuilder tb = new TokenBuilder();
        tb.add(obj.mCurrLine);
        while(obj.readLine() && obj.mCurrLine.length() != 0
            && !obj.mCurrLine.contains(": ")) {
          tb.add(obj.mCurrLine);
        }
        byte[] text = tb.finish();

        final TokenBuilder tmp = new TokenBuilder();
        final int len = text.length;
        int i = 0;
        while(i < len) {
          // add ASCII text
          while(i < len && text[i] != '=')
            tmp.add(text[i++]);
          if(i + 4 >= len) {
            i = len;
            break;
          }
          // char '=' detected -> must be encoded text
          if(text[i++] == '=' && text[i++] == '?') {
            // read the charset of the encoded text
            final TokenBuilder subjEnc = new TokenBuilder();
            while(i < len) {
              final byte b = text[i++];
              if(b == '?') break;
              subjEnc.add(b);
            }
            final boolean utf = subjEnc.toString().equalsIgnoreCase(Token.UTF8);
            // read the encoding flag
            final byte flag = text[i++];
            ++i; // skip '?'
            Encoding enc;
            if(flag == 'Q' || flag == 'q') enc = Encoding.Q_ENC;
            else if(flag == 'B' || flag == 'b') enc = Encoding.BASE64;
            else enc = Encoding.NONE;
            final TokenBuilder tok = new TokenBuilder();
            while(i < len && text[i] != '?')
              tok.add(text[i++]);
            ++i; // skip '?'
            tmp.add(enc.decode(tok.finish(), utf));
            assert text[i] == '=';
            ++i;
          } else {
            BaseX.debug("EMLParser: Found invalid chars in subject (%)",
                obj.bfc.getFileName());
            break; // stop reading
          }
        }
        text = tmp.finish();
        text = Token.replace(text, '_', ' ');
        obj.meta.setString(StringField.SUBJECT, text);
        obj.fsparser.metaEvent(obj.meta);
        return false;
      }
    },
    /** Receiver. */
    TO {
      @Override
      public boolean parse(final EMLParser obj) throws IOException {
        obj.parseMailAddresses(StringField.RECEIVER);
        return false;
      }
    },
    /** Carbon copy receiver. */
    CC {
      @Override
      public boolean parse(final EMLParser obj) throws IOException {
        obj.parseMailAddresses(StringField.COPY_RECEIVER);
        return false;
      }
    },
    /** Blind carbon copy receiver. */
    BCC {
      @Override
      public boolean parse(final EMLParser obj) throws IOException {
        obj.parseMailAddresses(StringField.HIDDEN_RECEIVER);
        return false;
      }
    },
    /** The content type. */
    CONTENT_TYPE {
      @Override
      public boolean parse(final EMLParser obj) throws IOException {
        obj.getContentType();
        return true;
      }
    },
    /** The content transfer encoding. */
    CONTENT_TRANSFER_ENCODING {
      @Override
      public boolean parse(final EMLParser obj) {
        obj.getEncoding();
        return true;
      }
    };

    /**
     * Parses the current (and perhaps the following) line(s) and fire meta
     * events.
     * @param obj the {@link EMLParser} to fire events from.
     * @return flag if a new line has to be read afterwards. If true, a new line
     *         must be read, if false, a new line is already stored to
     *         <code>mCurrLine</code>
     * @throws IOException if any error occurs while reading from the file.
     */
    abstract boolean parse(EMLParser obj) throws IOException;
  }

  /**
   * The different encodings.
   * @author Bastian Lemke
   */
  private enum Encoding {
    /** plaintext. */
    NONE {
      @Override
      public byte[] decode(final byte[] text, final boolean utf) {
        return text;
      }
    },
    /** Q-Encoding. */
    Q_ENC {
      @Override
      public byte[] decode(final byte[] text, final boolean utf) {
        return decodeQ(text, utf);
      }
    },
    /** base64 encoding. */
    BASE64 {
      @Override
      public byte[] decode(final byte[] text, final boolean utf) {
        return decodeBase64(text, utf);
      }
    };

    /**
     * Decodes the text.
     * @param text the text to decode.
     * @param utf flag if its utf-encoded
     * @return the decoded text.
     */
    abstract byte[] decode(final byte[] text, final boolean utf);
  }

  // ----- static stuff --------------------------------------------------------

  /** The pattern to isolate email addresses. */
  private static final Pattern MAILPATTERN = Pattern.compile(//
  /* */"[_a-zA-Z0-9-.]+(\\.[_a-zA-Z0-9-])*@([_a-zA-Z0-9-]+\\.)+([a-zA-Z]*)");
  /** Header key. */
  private static final Pattern KEYPAT = Pattern.compile("^([A-Za-z-]+): (.*)");
  /** Mac OS X plist. */
  private static final String PLIST = "<!DOCTYPE plist PUBLIC \"-//Apple//DTD "
      + "PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">";
  /** Q-Encoding. */
  private static final String Q_ENCODING = "quoted-printable";
  /** base64 encoding. */
  private static final String BASE64_ENCODING = "base64";
  /** Table for mapping ASCII chars to base64 values. */
  private static final byte[] BASE64MAPPING = { 62, -1, -1, -1, 63, 52, 53, 54,
      55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5,
      6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
      25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
      37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};
  /** The format of the date values. */
  static final SimpleDateFormat SDF = new SimpleDateFormat(
      "EEE, d MMM yyyy HH:mm:ss Z", Locale.US);

  // ---------------------------------------------------------------------------

  /** The content-type of the email. */
  private String mContentType;
  /** Holds the MIME boundary sequence. */
  private String mBoundary;
  /** Encoding of the mail body. */
  private Encoding bodyEnc = Encoding.NONE;
  /** Email body character encoding. */
  private String mBodyCharset = "";

  /** The line which is actually processed. */
  String mCurrLine;
  /** The {@link BufferedFileChannel} to read from. */
  BufferedFileChannel bfc;
  /** The {@link NewFSParser} instance to fire events. */
  NewFSParser fsparser;
  /** Metadata item. */
  Metadata meta = new Metadata();

  /** Standard constructor. */
  public EMLParser() {
    super(MetaType.MESSAGE, MimeType.EML);
  }

  @Override
  public boolean check(final BufferedFileChannel f) throws IOException {
    bfc = f;
    return readLine();
  }

  @Override
  protected void meta(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    if(!check(f)) return;
    fsparser = parser;
    mBoundary = "";
    do {
      String type = "";
      final Matcher m = KEYPAT.matcher(mCurrLine);
      if(m.matches()) {
        type = m.group(1).toUpperCase().replace('-', '_');
        mCurrLine = m.group(2).trim();
      }
      boolean readNext = true;
      try {
        readNext = EML_Meta.valueOf(type).parse(this);
      } catch(final IllegalArgumentException ex) {
        getBoundary();
        getCharset();
      }
      if(readNext && !readLine()) return;
    } while(mCurrLine.length() != 0);
  }

  @Override
  protected void content(final BufferedFileChannel f, final NewFSParser parser)
      throws IOException {
    if(!check(f)) return;
    fsparser = parser;

    do {
      String type = "";
      final Matcher m = KEYPAT.matcher(mCurrLine);
      if(m.matches()) {
        type = m.group(1);
        mCurrLine = m.group(2).trim();
        if(type.equalsIgnoreCase("content-type")) getContentType();
        else if(type.equalsIgnoreCase("content-transfer-encoding")) {
          getEncoding();
        }
      } else {
        getBoundary();
        getCharset();
      }
      if(!readLine()) return;
    } while(mCurrLine.length() != 0);

    parseContent();
  }

  @Override
  protected boolean metaAndContent(final BufferedFileChannel f,
      final NewFSParser parser) throws IOException {
    meta(f, parser);
    if(mCurrLine == null) return true;
    parseContent();
    return true;
  }

  /**
   * Parses the mail content.
   * @throws IOException if any I/O error occurs.
   */
  private void parseContent() throws IOException {
    boolean multipart = false;
    if(mBoundary != null && mBoundary.length() != 0) {
      // multipart
      if(mContentType != null && mContentType.startsWith("multipart")) {
        multipart = true;
        while(mCurrLine != null && !mCurrLine.contains(mBoundary))
          readLine();
      }
    }

    // parse all mail parts (can be just one)
    while(true)
      if(!getBodyData(multipart)) break;
  }

  /**
   * Extracts the body text of an email.
   * @param multipart flag if it's a multipart message.
   * @return true if more parts are found
   * @throws IOException I/O exception
   */
  private boolean getBodyData(final boolean multipart) throws IOException {
    final long bodyStartPos = bfc.absolutePosition();

    if(multipart) fsparser.startContent(bodyStartPos);
    // if we have a multipart message, extract text only if it is plaintext.
    if(multipart ? readSectionHeader() : mContentType == null
        || mContentType.startsWith("text")) {
      final long pos2 = bfc.absolutePosition();
      final TokenBuilder tb = new TokenBuilder();
      boolean first = true;
      final boolean emlx = bfc.getFileName().endsWith(".emlx");
      final boolean utf = mBodyCharset.equalsIgnoreCase(Token.UTF8);
      while(readLine()) {
        if(multipart && mCurrLine.contains(mBoundary)) break;
        // .emlx files contain a Mac OS X property list after the body data.
        if(emlx && mCurrLine.startsWith("<?xml")) {
          final String oldLine = mCurrLine;
          readLine();
          if(mCurrLine.equals(PLIST)) break;
          if(first) first = false;
          else tb.add('\n');
          tb.add(oldLine);
        }
        if(first) first = false;
        else tb.add('\n');
        tb.add(mCurrLine);
      }

      // fire parser event
      final byte[] text = tb.finish();
      final byte[] data = bodyEnc.decode(text, utf);
      final int size = data.length;
      if(size > 0) fsparser.textContent(pos2, text.length, data, true);
    } else {
      while(readLine()) {
        if(multipart) if(mCurrLine.contains(mBoundary)) break;
      }
    }

    if(multipart) {
      bodyEnc = Encoding.NONE;
      final int readAhead = mCurrLine == null ? 0 : mCurrLine.length();
      fsparser.setContentSize(bfc.absolutePosition() - bodyStartPos - readAhead
          - 1);
      fsparser.endContent();
    }

    return mCurrLine != null && multipart && !mCurrLine.endsWith("--");
  }

  /**
   * Extracts content-type information of the body or the attachment. true means
   * body, false means attachment.
   * @return true if content is plaintext, false otherwise.
   * @throws IOException I/O exception
   */
  private boolean readSectionHeader() throws IOException {
    boolean plaintext = false;
    do {
      String type = "";
      if(mCurrLine == null) break;
      final Matcher m = KEYPAT.matcher(mCurrLine);
      if(m.matches()) {
        type = m.group(1).toLowerCase();
        mCurrLine = m.group(2).trim();
      }

      // check transfer encoding for mail section...
      if(type.equals("content-transfer-encoding")) {
        getEncoding();
      } else if(type.equals("content-type")) {
        final String mimeString = mCurrLine.split(";")[0].toLowerCase();
        MimeType mime = null;
        if(mimeString.startsWith("multipart")) {
          mContentType = mimeString;
          if(mimeString.startsWith("multipart/related")) multipartRelated();
          break;
        }
        mime = MimeType.getItem(mimeString);
        if(mime != null) {
          for(final MetaType mt : mime.getMetaTypes()) {
            fsparser.metaEvent(meta.setMetaType(mt));
            // [BL] handle mail attachments that are not plaintext
            if(mt == MetaType.TEXT) plaintext = true;
          }
          fsparser.metaEvent(meta.setMimeType(mime));
          getCharset();
        }
      } else getCharset();
    } while(readLine() && mCurrLine.length() != 0);
    if(!plaintext) return false;
    return true;
  }

  /**
   * Parses a multipart/related section.
   * @throws IOException if any I/O error occurs.
   */
  void multipartRelated() throws IOException {
    // [BL] test multipart mails
    readLine();
    getBoundary();
    readLine();
    parseContent();
  }

  /**
   * Parses mail addresses and fires parser events.
   * @param field the address field to set.
   * @throws IOException if any error occurs while reading from the file.
   */
  void parseMailAddresses(final StringField field) throws IOException {
    final StringBuilder addresses = new StringBuilder();
    addresses.append(mCurrLine);
    while(readLine() && mCurrLine.length() != 0 && !mCurrLine.contains(": ")) {
      addresses.append(mCurrLine);
    }
    for(final Matcher m = MAILPATTERN.matcher(addresses); m.find();) {
      meta.setString(field, m.group());
      fsparser.metaEvent(meta);
    }
  }

  /** Gets the encoding. */
  void getEncoding() {
    if(mCurrLine.equals(Q_ENCODING)) bodyEnc = Encoding.Q_ENC;
    else if(mCurrLine.equals(BASE64_ENCODING)) bodyEnc = Encoding.BASE64;
    else bodyEnc = Encoding.NONE;
  }

  /**
   * Gets the content-type, boundary and charset. Fires a parser event if the
   * charset was found.
   * @throws IOException if any I/O error occurs.
   */
  void getContentType() throws IOException {
    mContentType = mCurrLine;
    getBoundary();
    getCharset();
  }

  /**
   * Gets the charset and fires a parser event.
   * @throws IOException if any I/O error occurs.
   */
  void getCharset() throws IOException {
    if(mCurrLine.contains("charset=")) {
      mBodyCharset = mCurrLine.split("charset=")[1].split(";")[0].replace("\"",
          "").trim();
      fsparser.metaEvent(meta.setString(StringField.ENCODING, mBodyCharset));
    }
  }

  /** Gets the boundary. */
  void getBoundary() {
    if(mCurrLine.contains("boundary=")) {
      mBoundary = mCurrLine.split("boundary=")[1].split(";")[0].replace("\"",
          "").trim();
    }
  }

  /**
   * Reads in the next line.
   * @return true if next line exists
   * @throws IOException I/O exception
   */
  boolean readLine() throws IOException {
    mCurrLine = bfc.readLine(mBodyCharset);
    return mCurrLine != null;
  }

  /**
   * Adds a byte to the TokenBuilder.
   * @param tb the TokenBuilder to add the byte to.
   * @param b the byte to add.
   * @param utf true, if the byte is part of an utf-encoded string, false
   *          otherwise.
   */
  private static void addByte(final TokenBuilder tb, final int b,
      final boolean utf) {
    if(utf) tb.add((byte) b);
    else tb.addUTF(b);
  }

  /**
   * Decodes a Q-encoded text and returns the result.
   * @param text the text to be decoded
   * @param utf flag if the text is utf-encoded
   * @return the decoded text
   */
  static byte[] decodeQ(final byte[] text, final boolean utf) {
    final TokenBuilder tmp = new TokenBuilder();
    final int len = text.length;
    for(int i = 0; i < len; i++) {
      final byte c = text[i];
      if(c == '=') {
        if(i + 2 >= len) break;
        if(text[++i] == 0xA) continue; // ignore line feed
        final int n1 = hex2num(text[i]);
        final int n2 = hex2num(text[++i]);
        if(n1 < 0 || n2 < 0) continue;
        addByte(tmp, n1 << 4 | n2, utf);
      } else addByte(tmp, c, utf);
    }
    return tmp.finish();
  }

  /**
   * Converts a hexadecimal character into a number.
   * @param b character
   * @return byte value
   */
  private static int hex2num(final byte b) {
    if(b >= '0' && b <= '9') return b - '0';
    if(b >= 'A' && b <= 'F') return b - 0x37;
    // won't happen in correctly encoded mails
    BaseX.debug("EMLExtractor.hex2num: " + (char) b);
    return -1;
  }

  /**
   * Decodes a base64 encoded text.
   * @param text the text to be decoded.
   * @param utf flag if the text is utf-encoded
   * @return the decoded text.
   */
  static byte[] decodeBase64(final byte[] text, final boolean utf) {
    final TokenBuilder tmp = new TokenBuilder();

    final byte[] data = Token.delete(text, 0xA); // delete line feeds
    final int size = data.length;
    assert size % 4 == 0;
    byte b1, b2, b3, b4;
    int i, max;
    try {
      for(i = 0, max = size - 4; i < max;) {
        b1 = base64Val(data[i++]);
        b2 = base64Val(data[i++]);
        b3 = base64Val(data[i++]);
        b4 = base64Val(data[i++]);
        addByte(tmp, b1 << 2 | b2 >> 4, utf);
        addByte(tmp, b2 << 4 & 0xF0 | b3 >> 2, utf);
        addByte(tmp, b3 << 6 & 0xC0 | b4, utf);
      }
      b1 = base64Val(data[i++]);
      assert b1 != -2; // there may be at most 3 empty bytes
      b2 = base64Val(data[i++]);
      if(b2 == -2) {
        final int b = b1 << 2;
        if(b != 0) addByte(tmp, b, utf);
      } else {
        addByte(tmp, b1 << 2 | b2 >> 4, utf);
        b3 = base64Val(data[i++]);
        if(b3 == -2) {
          final int b = b2 << 4 & 0xF0;
          if(b != 0) addByte(tmp, b, utf);
        } else {
          addByte(tmp, b2 << 4 & 0xF0 | b3 >> 2, utf);
          b4 = base64Val(data[i++]);
          if(b4 == -2) {
            final byte b = (byte) (b3 << 6 & 0xC0);
            if(b != 0) addByte(tmp, b, utf);
          } else addByte(tmp, b3 << 6 & 0xC0 | b4, utf);
        }
      }
    } catch(final Exception ex) {
      BaseX.debug("EMLParser: invalid base64 encoding");
    }
    return tmp.finish();
  }

  /**
   * Translates an ascii char to the base64 value.
   * @param b the char to convert.
   * @return the base64 value;
   */
  private static byte base64Val(final byte b) {
    final byte val = BASE64MAPPING[b - 0x2B];
    assert val != -1;
    return val;
  }
}
