package org.deepfs.fsml.parsers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.util.ByteList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.deepfs.fsml.BufferedFileChannel;
import org.deepfs.fsml.DeepFile;
import org.deepfs.fsml.FileType;
import org.deepfs.fsml.MetaElem;
import org.deepfs.fsml.MimeType;
import org.deepfs.fsml.ParserRegistry;
import org.deepfs.fsml.util.ParserUtil;

/**
 * Parser for EML files.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Bastian Lemke
 * @author Lukas Kircher
 */
public final class EMLParser implements IFileParser {

  static {
    ParserRegistry.register("eml", EMLParser.class);
    ParserRegistry.register("emlx", EMLParser.class);
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
        obj.parseMailAddresses(MetaElem.SENDER_NAME, MetaElem.SENDER_EMAIL);
        return false;
      }
    },
        /** Date when the mail was sended. */
    DATE {
      @Override
      public boolean parse(final EMLParser obj) {
        try {
          final Date d = SDF.parse(obj.mCurrLine);
          obj.deepFile.addMeta(MetaElem.DATETIME_CREATED,
              ParserUtil.convertDateTime(d));
        } catch(final ParseException ex) {
          obj.deepFile.debug("EMLParser: Failed to parse date", ex);
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
        while(obj.readLine() && !obj.mCurrLine.isEmpty()
            && !obj.mCurrLine.contains(": ")) {
          tb.add(obj.mCurrLine);
        }
        final byte[] text = tb.finish();
        try {
          obj.deepFile.addMeta(MetaElem.SUBJECT, obj.decode(text));
        } catch(final DecodingException ex) {
          obj.deepFile.debug("EMLParser: Failed to decode subject (%)", ex);
        }
        return false;
      }
    },
        /** Receiver. */
    TO {
      @Override
      public boolean parse(final EMLParser obj) throws IOException {
        obj.parseMailAddresses(MetaElem.RECEIVER_NAME, MetaElem.RECEIVER_EMAIL);
        return false;
      }
    },
        /** Carbon copy receiver. */
    CC {
      @Override
      public boolean parse(final EMLParser obj) throws IOException {
        obj.parseMailAddresses(MetaElem.COPY_RECEIVER_NAME,
            MetaElem.COPY_RECEIVER_EMAIL);
        return false;
      }
    },
        /** Blind carbon copy receiver. */
    BCC {
      @Override
      public boolean parse(final EMLParser obj) throws IOException {
        obj.parseMailAddresses(MetaElem.HIDDEN_RECEIVER_NAME,
            MetaElem.HIDDEN_RECEIVER_EMAIL);
        return false;
      }
    },
        /** The content type. */
    CONTENT_TYPE {
      @Override
      public boolean parse(final EMLParser obj) {
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
     * @param obj the {@link EMLParser} to fire events from
     * @return flag if a new line has to be read afterwards. If true, a new line
     *         must be read, if false, a new line is already stored to
     *         {@code mCurrLine}
     * @throws IOException if any error occurs while reading from the file
     */
    abstract boolean parse(final EMLParser obj) throws IOException;
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
      public byte[] decode(final byte[] text, final boolean utf)
          throws DecodingException {
        return decodeBase64(text, utf);
      }
    };

    /**
     * Decodes the text.
     * @param text the text to decode
     * @param utf flag if its utf-encoded
     * @return the decoded text
     * @throws DecodingException if any error occurs
     */
    abstract byte[] decode(final byte[] text, final boolean utf)
        throws DecodingException;
  }

  // ----- static stuff --------------------------------------------------------

  /** The pattern to isolate email addresses. */
  private static final Pattern MAILPATTERN = Pattern.compile(
      "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9_.-]+\\.[a-zA-Z]{2,6}");
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
      37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };
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

  /** File reference, representing the eml file. */
  DeepFile deepFile;

  @Override
  public boolean check(final DeepFile df) throws IOException {
    while(readLine())
      if(mCurrLine.startsWith("From:") && MAILPATTERN.matcher(mCurrLine).find())
        return true;
    return false;
  }

  @Override
  public void propagate(final DeepFile df) {
    Util.notimplemented();
  }

  @Override
  public void extract(final DeepFile df) throws IOException {
    deepFile = df;
    bfc = df.getBufferedFileChannel();
    if(!readLine()) return;

    if(df.extractMeta()) {
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
      } while(!mCurrLine.isEmpty());

      if(deepFile.isMetaSet(MetaElem.SENDER_EMAIL)
          || deepFile.isMetaSet(MetaElem.SENDER_NAME)) {
        deepFile.setFileType(FileType.MESSAGE);
        deepFile.setFileFormat(MimeType.EML);
        deepFile.addMeta(MetaElem.ENCODING, mBodyCharset);
      } else deepFile.debug("EMLParser: Invalid mail file (no sender found).");

      if(df.extractText()) parseContent();
    } else if(df.extractText()) {
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
      } while(!mCurrLine.isEmpty());
      parseContent();
    }
  }

  /**
   * Checks if the given string contains a valid email address.
   * @param str the string to check
   * @return true if the string contains a valid email address
   */
  public static boolean isEmailAddress(final String str) {
    final Matcher m = MAILPATTERN.matcher(str);
    return m.find() && m.start() == 0 && m.end() == str.length();
  }

  /**
   * Parses the mail content.
   * @throws IOException if any I/O error occurs
   */
  private void parseContent() throws IOException {
    boolean multipart = false;
    if(mBoundary != null && !mBoundary.isEmpty()) {
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
   * @param multipart flag if it's a multipart message
   * @return true if more parts are found
   * @throws IOException I/O exception
   */
  private boolean getBodyData(final boolean multipart) throws IOException {
    final long bodyStartPos = bfc.absolutePosition();

    final DeepFile content;
    if(multipart) content = deepFile.newContentSection(bodyStartPos);
    else content = deepFile;

    // if we have a multipart message, extract text only if it is plaintext
    if(multipart ? readSectionHeader(content) : mContentType == null
        || mContentType.startsWith("text")) {
      final long pos2 = bfc.absolutePosition();
      final TokenBuilder tb = new TokenBuilder();
      boolean first = true;
      final boolean emlx = bfc.getFileName().endsWith(".emlx");
      final boolean utf = mBodyCharset.equalsIgnoreCase(Token.UTF8);
      while(readLine()) {
        if(multipart && mCurrLine.contains(mBoundary)) break;
        // .emlx files contain a Mac OS X property list after the body data
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
      try {
        final byte[] text = tb.finish();
        final byte[] data = bodyEnc.decode(text, utf);
        final int size = data.length;
        if(size > 0) deepFile.addText(pos2, text.length, Token.string(data));
      } catch(final DecodingException ex) {
        deepFile.debug("EMLParser: Failed to decode text (%)", ex);
      }
    } else {
      while(readLine()) {
        if(multipart) if(mCurrLine.contains(mBoundary)) break;
      }
    }

    if(multipart) {
      bodyEnc = Encoding.NONE;
      final int readAhead = mCurrLine == null ? 0 : mCurrLine.length();
      content.setSize(bfc.absolutePosition() - bodyStartPos - readAhead
          - 1);
    }

    return mCurrLine != null && multipart && !mCurrLine.endsWith("--");
  }

  /**
   * Extracts content-type information of the body or the attachment. true means
   * body, false means attachment.
   * @param contentMeta metadata store for the current section
   * @return true if content is plaintext, false otherwise
   * @throws IOException I/O exception
   */
  private boolean readSectionHeader(final DeepFile contentMeta)
      throws IOException {
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
          for(final FileType mt : mime.getMetaTypes()) {
            contentMeta.setFileType(mt);
            if(mt == FileType.TEXT) plaintext = true;
          }
          contentMeta.setFileFormat(mime);
          contentMeta.addMeta(MetaElem.ENCODING, getCharset());
        }
      } else getCharset();
    } while(readLine() && !mCurrLine.isEmpty());
    if(!plaintext) return false;
    return true;
  }

  /**
   * Parses a multipart/related section.
   * @throws IOException if any I/O error occurs
   */
  void multipartRelated() throws IOException {
    readLine();
    getBoundary();
    readLine();
    parseContent();
  }

  /**
   * Parses mail addresses and fires parser events.
   * @param name the metadata element to set (name)
   * @param email the metadata element to set (email address)
   * @throws IOException if any error occurs while reading from the file
   */
  void parseMailAddresses(final MetaElem name, final MetaElem email)
      throws IOException {
    final StringBuilder addresses = new StringBuilder();
    addresses.append(mCurrLine);
    while(readLine() && !mCurrLine.isEmpty() && !mCurrLine.contains(": "))
      addresses.append(mCurrLine);
    int pos = 0;
    String lastMatch = "";
    for(final Matcher m = MAILPATTERN.matcher(addresses); m.find();) {
      final String match = m.group();
      if(match.equals(lastMatch)) continue;
      lastMatch = match;
      deepFile.addMeta(email, m.group());

      final int end = m.start() - 2;
      if(end > pos) {
        final byte[] text = chop(Token.token(addresses.substring(pos, end)));
        try {
          deepFile.addMeta(name, chop(decode(text)));
        } catch(final DecodingException ex) {
          deepFile.debug("EMLParser: Failed to decode mail address (% - %)",
              name, ex);
        }
      }
      pos = m.end() + 2;
    }
  }

  /**
   * Removes leading and trailing whitespaces and quoting signs.
   * @param text the text to chop
   * @return the chopped text
   */
  private byte[] chop(final byte[] text) {
    int start = 0;
    int end = text.length - 1;
    boolean finished = false;
    while(!finished && start < end) {
      final byte b = text[start];
      switch(b) {
        case ' ':
        case '\t':
        case '\'':
        case '"':
          ++start;
          break;
        default:
          finished = true;
      }
    }
    finished = false;
    while(!finished && start < end) {
      final byte b = text[end];
      switch(b) {
        case ' ':
        case '\t':
        case '\'':
        case '"':
          --end;
          break;
        default:
          finished = true;
      }
    }
    if(start != 0 || end != text.length - 1) {
      final int size = end - start + 1;
      final byte[] newText = new byte[size];
      System.arraycopy(text, start, newText, 0, size);
      return newText;
    }
    return text;
  }

  /** Gets the encoding. */
  void getEncoding() {
    if(mCurrLine.equals(Q_ENCODING)) bodyEnc = Encoding.Q_ENC;
    else if(mCurrLine.equals(BASE64_ENCODING)) bodyEnc = Encoding.BASE64;
    else bodyEnc = Encoding.NONE;
  }

  /**
   * Gets the content-type, boundary and charset.
   */
  void getContentType() {
    mContentType = mCurrLine;
    getBoundary();
    getCharset();
  }

  /**
   * Returns the charset.
   * @return the charset
   */
  String getCharset() {
    if(mCurrLine.contains("charset=")) {
      mBodyCharset = mCurrLine.split("charset=")[1].split(";")[0].replace("\"",
          "").trim();
      return mBodyCharset;
    }
    return "";
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
   * Decodes a text.
   * @param text the text to decode
   * @return the decoded text as byte array
   * @throws DecodingException if the text could not be decoded
   */
  byte[] decode(final byte[] text) throws DecodingException {
    final int len = text.length;
    final ByteList bl = new ByteList();
    int i = 0;
    while(i < len) {
      // add ASCII text
      while(i < len && text[i] != '=') bl.add(text[i++]);
      if(i + 4 >= len) break;
      // char '=' detected -> must be encoded text
      if(text[i++] == '=' && text[i++] == '?') {
        // read the charset of the encoded text
        final TokenBuilder subjEnc = new TokenBuilder();
        while(i < len) {
          final byte b = text[i++];
          if(b == '?') break;
          subjEnc.addLong(b);
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
        while(i < len && text[i] != '?') tok.addLong(text[i++]);
        ++i; // skip '?'
        for(final byte b : enc.decode(tok.finish(), utf)) bl.add(b);
        assert text[i] == '=';
        ++i;
      } else {
        deepFile.debug("EMLParser: Found invalid chars in subject.");
        break; // stop reading
      }
    }
    return Token.replace(bl.toArray(), '_', ' ');
  }

  /**
   * Adds a byte to the TokenBuilder.
   * @param tb the TokenBuilder to add the byte to
   * @param b the byte to add
   * @param utf true, if the byte is part of an utf-encoded string, false
   *          otherwise
   */
  private static void addByte(final TokenBuilder tb, final int b,
      final boolean utf) {
    if(utf) tb.addByte((byte) b);
    else tb.add(b);
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
    for(int i = 0; i < len; ++i) {
      final byte c = text[i];
      if(c == '=') {
        if(i + 2 >= len) break;
        if(text[++i] == 0xA) continue; // ignore line feed
        final int n1 = hex2num(text[i]);
        final int n2 = hex2num(text[++i]);
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
    if(b >= 'a' && b <= 'f') return b - 0x57;
    // won't happen in correctly encoded mails
    return b;
  }

  /**
   * Decodes a base64 encoded text.
   * @param text the text to be decoded
   * @param utf flag if the text is utf-encoded
   * @return the decoded text
   * @throws DecodingException if any error occurs
   */
  static byte[] decodeBase64(final byte[] text, final boolean utf)
      throws DecodingException {
    final TokenBuilder tmp = new TokenBuilder();

    final byte[] data = Token.delete(text, (char) 0xA); // delete line feeds
    final int size = data.length;
    if(size % 4 != 0)
      throw new DecodingException("Invalid number of bytes (" + size + ")");
    byte b1, b2, b3, b4;
    int i, max;
    boolean valid = true;
    try {
      for(i = 0, max = size - 4; i < max;) {
        b1 = base64Val(data[i++]);
        b2 = base64Val(data[i++]);
        b3 = base64Val(data[i++]);
        b4 = base64Val(data[i++]);
        if(b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1) {
          valid = false;
          addByte(tmp, ' ', true);
          continue;
        }
        addByte(tmp, b1 << 2 | b2 >> 4, utf);
        addByte(tmp, b2 << 4 & 0xF0 | b3 >> 2, utf);
        addByte(tmp, b3 << 6 & 0xC0 | b4, utf);
      }
      b1 = base64Val(data[i++]);
      if(b1 < 0) valid = false;
      else {
        b2 = base64Val(data[i++]);
        if(b2 == -2) {
          final int b = b1 << 2;
          if(b != 0) addByte(tmp, b, utf);
        } else if(b2 == -1) valid = false;
        else {
          addByte(tmp, b1 << 2 | b2 >> 4, utf);
          b3 = base64Val(data[i++]);
          if(b3 == -2) {
            final int b = b2 << 4 & 0xF0;
            if(b != 0) addByte(tmp, b, utf);
          } else if(b3 == -1) valid = false;
          else {
            addByte(tmp, b2 << 4 & 0xF0 | b3 >> 2, utf);
            b4 = base64Val(data[i++]);
            if(b4 == -2) {
              final byte b = (byte) (b3 << 6 & 0xC0);
              if(b != 0) addByte(tmp, b, utf);
            } else if(b4 == -1) valid = false;
            else addByte(tmp, b3 << 6 & 0xC0 | b4, utf);
          }
        }
      }
    } catch(final Exception ex) {
      throw new DecodingException(ex);
    }
    if(!valid) throw new DecodingException();
    return tmp.finish();
  }

  /**
   * Translates an ascii char to the base64 value.
   * @param b the char to convert
   * @return the base64 value
   */
  private static byte base64Val(final byte b) {
    final byte val = BASE64MAPPING[b - 0x2B];
    return val;
  }
}
