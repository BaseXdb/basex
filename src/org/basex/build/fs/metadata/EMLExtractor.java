package org.basex.build.fs.metadata;

import static org.basex.build.fs.FSText.*;
import static org.basex.util.Token.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.BaseX;
import org.basex.build.Builder;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * EML meta data extractor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class EMLExtractor extends AbstractExtractor {
  /** The pattern to isolate email addresses. */
  private static final Pattern MAILPATTERN = Pattern.compile(
      "[_a-zA-Z0-9-.]+(\\.[_a-zA-Z0-9-])*@([_a-zA-Z0-9-]+\\.)+([a-zA-Z]*)");
  /** Header key. */
  private static final Pattern KEYPAT = Pattern.compile("^([A-Za-z-]+): (.*)");

  /** Mapping the month to numbers. */
  private static final byte[][] MMONTHS = { token("Jan"), token("Feb"),
      token("Mar"), token("Apr"), token("May"), token("Jun"), token("Jul"),
      token("Aug"), token("Sep"), token("Oct"), token("Nov"), token("Dec") };

  /** The line which is actually processed. */
  private String mCurrLine;
  /** The content-type of the email. */
  private String mContentType;
  /** Holds the MIME boundary sequence. */
  private String mBoundary;
  /** Input. */
  private BufferedReader mIn;
  /** Email has attachment. */
  private boolean mMultiPart;
  /** Email format is mbox. */
  private boolean mIsMultiMail;
  /** Quoted-Printable encoding. */
  private boolean quotePrint;

  /** Email body character encoding. */
  private String mBodyCharset;
  /** Email body content type. */
  private String mBodyType = "";

  @Override
  public void extract(final Builder listener, final File f) throws IOException {
    mIn = new BufferedReader(new FileReader(f));

    // check for mbox format
    readLine();
    mIsMultiMail = mCurrLine.startsWith("From ") && mCurrLine.contains("@");

    while(getSingleMailData(listener));

    mIn.close();
    mContentType = null;
  }

  /**
   * Extracts metadata of a single email.
   * @param listener Builder reference
   * @return false if no more mails are found
   * @throws IOException I/O exception
   */
  private boolean getSingleMailData(final Builder listener) throws IOException {
    listener.startElem(EMAIL, atts.reset());

    // catch exceptions and insert them into tree to find the part of
    // the mail which caused problem
    mBoundary = "";
    getHeaderData(listener);

    if(mBoundary.length() != 0) {
      // currently, mContextType can still be empty..
      mMultiPart = mContentType != null && mContentType.contains("multipart");

      // find beginning of first part
      if(mMultiPart) while(!mCurrLine.contains(mBoundary))
        readLine();
    }

    // parse all mail parts (can be just one)
    while(getBodyData(listener));

    // need to reset if email is mbox format
    mMultiPart = false;

    listener.endElem(EMAIL);

    while(readLine() && mCurrLine.length() == 0);
    return mCurrLine != null;
  }

  /**
   * Extracts the body text of an email.
   * @param listener Builder reference
   * @return true if more parts are found
   * @throws IOException I/O exception
   */
  private boolean getBodyData(final Builder listener) throws IOException {
    final TokenBuilder tb = new TokenBuilder();

    final boolean bound = mBoundary.length() != 0;
    if(bound) getSectionContentType();

    while(readLine()) {
      if(bound) {
        if(mCurrLine.contains(mBoundary)) break;
      } else if(mIsMultiMail) {
        if(mCurrLine.startsWith("From ") && mCurrLine.contains("@")) break;
      }
      tb.add(mCurrLine);
      tb.add('\n');
    }

    // write body tag & content
    listener.startElem(EMLBODY, atts.set(TYPE, token(mBodyType)));
    listener.text(quotePrint ?
        new TokenBuilder(decodeQP(tb.finish())) : tb, false);
    listener.endElem(EMLBODY);

    // reset encoding flag
    quotePrint = false;

    return mCurrLine != null && bound && !mCurrLine.endsWith("--");
  }

  /**
   * A method to locate problems and add the error
   * message to the xml tree. (for testing only)
   * @param listener Builder reference
   * @param s the error message to be printed
   * @throws IOException I/O exception
   */
  private void printException(final Builder listener, final String s)
      throws IOException {
    listener.nodeAndText(token("Exception"), atts.reset(), token(s));
  }

  /**
   * Extracts the header information of an email.
   * @param listener Builder reference
   * @throws IOException I/O exception
   */
  private void getHeaderData(final Builder listener) throws IOException {
    do {
      checkHeaderLine(listener);
    } while(mCurrLine.length() != 0);
  }

  /**
   * Checks the current header line for attributes.
   * @param listener Builder reference
   * @throws IOException I/O exception
   */
  private void checkHeaderLine(final Builder listener) throws IOException {
    // some attributes treated separately due to syntax e.g.

    String type = "";
    final Matcher m = KEYPAT.matcher(mCurrLine);
    if(m.matches()) {
      type = m.group(1).toLowerCase();
      mCurrLine = m.group(2).trim();
    }

    boolean readNext = true;
    if(type.equals("from")) {
      getSender(listener);
    } else if(type.equals("date")) {
      getDate(listener);
    } else if(type.equals("subject")) {
      getSubject(listener);
      readNext = false;
    } else if(type.equals("to")) {
      getReceivers(listener);
      readNext = false;
    } else if(type.equals("content-type") || mCurrLine.contains("boundary=")) {
      getEmailContentType(listener);
    } else if(type.equals("content-transfer-encoding")) {
      getEncoding();
    } else {
      // check simple attributes
      for(int i = 0; i < EMLATTR.length; i++) {
        if(type.equals(EMLATTR[i])) {
          listener.nodeAndText(ATTRIBUTETOKENS[i], atts.reset(),
              token(mCurrLine));
          return;
        }
      }
    }
    if(readNext) readLine();
  }

  /**
   * Decodes a text with quoted-printables and returns the result.
   * @param text the text to be decoded
   * @return the decoded text
   */
  private byte[] decodeQP(final byte[] text) {
    final TokenBuilder tmp = new TokenBuilder();

    // access bounds of text[] aren't checked..
    // could cause problems if mail encoding is pretty weird
    for(int i = 0; i < text.length; i++) {
      byte c = text[i];
      if(c == '=') {
        if(text[++i] == 0x0a) continue;
        final int n1 = hex2num(text[i++]);
        final int n2 = hex2num(text[i]);
        if(n1 < 0 || n2 < 0) continue;
        c = (byte) ((n1 << 4) | n2);
      }
      tmp.add(c);
    }
    return tmp.finish();
  }

  /**
   * Converts a hexadecimal character into a number.
   * @param b character
   * @return byte value
   */
  private int hex2num(final byte b) {
    if(b >= '0' && b <= '9') return b - '0';
    if(b >= 'A' && b <= 'F') return b - 0x37;
    // won't happen in correctly encoded mails
    BaseX.debug("EMLExtractor.hex2num: " + (char) b);
    return -1;
  }

  /**
   * Extracts content-type information of the body or the
   * attachment.
   * true means body, false means attachment
   * @throws IOException I/O exception
   */
  private void getSectionContentType() throws IOException {
    mBodyType = null;

    do {
      String type = "";
      final Matcher m = KEYPAT.matcher(mCurrLine);
      if(m.matches()) {
        type = m.group(1).toLowerCase();
        mCurrLine = m.group(2).trim();
      }

      // check transfer encoding for mail section...
      if(type.equals("content-transfer-encoding")) {
        getEncoding();
      } else if(type.equals("content-type")) {
        mBodyType = mCurrLine.split(";")[0].toLowerCase();
      }
      if(mCurrLine.contains("charset=")) {
        mBodyCharset = mCurrLine.split("charset=")[1];
        mBodyCharset = mBodyCharset.replace("\"", "").trim();
      }
    } while(readLine() && mCurrLine.length() != 0);
  }

  /**
   * Gets the overall content-type of an email.
   * @param listener Builder reference
   * @throws IOException I/O exception
   */
  private void getEmailContentType(final Builder listener) throws IOException {
    for(final String type : mCurrLine.split(";")) {
      if(!type.contains("=")) {
        // store content type
        final String contentType = type.toLowerCase();
        mContentType = contentType.split("/")[0];
        listener.nodeAndText(EMLCONTENTTYPE, atts.reset(), token(contentType));
      } else if(type.trim().startsWith("boundary=")) {
        // store boundary
        mBoundary = type.split("boundary=")[1];
        mBoundary = mBoundary.replace("\"", "").trim();
      }
    }
  }

  /**
   * Gets the overall content-type of an email.
   */
  private void getEncoding() {
    quotePrint = mCurrLine.equals("quoted-printable");
  }

  /**
   * Extracts the addresses the email was sent to.
   * @param listener Builder reference
   * @throws IOException I/O exception
   */
  private void getReceivers(final Builder listener) throws IOException {
    final StringBuilder addressPool = new StringBuilder();
    addressPool.append(mCurrLine);

    // get all lines up to the next attribute
    while(readLine() && mCurrLine.length() != 0 && !mCurrLine.contains(": ")) {
      addressPool.append(mCurrLine);
    }

    for(final Matcher m = MAILPATTERN.matcher(addressPool); m.find();) {
      listener.nodeAndText(EMLTO, atts.reset(), token(m.group()));
    }
  }

  /**
   * Extracts the subject information and decodes it, if MIME-encoded.
   * @param listener Builder reference
   * @throws IOException I/O exception
   */
  private void getSubject(final Builder listener) throws IOException {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(mCurrLine);
    while(readLine() && mCurrLine.length() != 0 && !mCurrLine.contains(": ")) {
      tb.add(mCurrLine);
    }
    byte[] text = tb.finish();
    // byte representation for "=?"
    final byte[] sig = { 0x3d, 0x3f };

    if(Token.startsWith(text, sig)) {
      final TokenBuilder tmp = new TokenBuilder();
      final byte[][] atoms = Token.split(text, 0x3f);
      // quoted-printable encoded
      if(atoms[2][0] == 0x51) {
        for(int i = 3; i < atoms.length - 1; i += 4) {
          tmp.add(decodeQP(atoms[i]));
        }
      }
      // base64 encoded
      if(atoms[2][0] == 0x42) {
        for(int i = 3; i < atoms.length - 1; i += 4) {
          tmp.add(atoms[i]);
        }
      }
      text = tmp.finish();
      for(int i = 0; i < text.length; i++) {
        if(text[i] == 0x5f) text[i] = 0x20;
      }
    }
    listener.nodeAndText(EMLSUBJECT, atts.reset(), text);
  }

  /**
   * Extracts the address of the email sender.
   * @param listener Builder reference
   * @throws IOException I/O exception
   */
  private void getSender(final Builder listener) throws IOException {
    final Matcher m = MAILPATTERN.matcher(mCurrLine);
    if(m.find()) {
      listener.nodeAndText(EMLFROM, atts.reset(), token(m.group()));
    } else {
      printException(listener, mCurrLine);
    }
  }

  /**
   * Extracts the date information of an email.
   * @param listener Builder reference
   */
  private void getDate(final Builder listener) {
    final byte[][] date = split(token(mCurrLine), ' ');

    // supported date format: (day,)? dd (mm|month) yyyy tt:tt:tt (zone)?
    try {
      final int off = contains(date[0], ',') ? 1 : 0;
      final byte[] day = date[off + 0];
      byte[] mon = date[off + 1];
      final byte[] yea = date[off + 2];
      final byte[][] tim = split(date[off + 3], ':');

      // change named months to numbered months
      for(int i = 0; i < MMONTHS.length; i++) {
        if(eq(mon, MMONTHS[i])) {
          mon = token(i);
          break;
        }
      }
      // calculate and store time in minutes from 1.1.1970
      final GregorianCalendar cal = new GregorianCalendar(toInt(yea),
          toInt(mon), toInt(day), toInt(tim[0]), toInt(tim[1]), toInt(tim[2]));
      final long min = cal.getTimeInMillis() / 60000;
      listener.emptyElem(EMLDATE, atts.set(EMLTIME, token(min)));
    } catch(final Exception e) {
      BaseX.debug("EMLExtractor.getDate: " + mCurrLine);
    }
  }

  /**
   * Reads in the next line.
   * @return true if next line exists
   * @throws IOException I/O exception
   */
  private boolean readLine() throws IOException {
    mCurrLine = mIn.readLine();
    return mCurrLine != null;
  }
}
