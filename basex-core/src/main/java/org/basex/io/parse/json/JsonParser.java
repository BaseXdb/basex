package org.basex.io.parse.json;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.build.json.JsonParserOptions.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A JSON parser generating parse events similar to a SAX XML parser.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class JsonParser {
  /** Names of control characters not allowed in string literals. */
  private static final String[] CTRL = {
    // U+0000 -- U+001F
    "NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL",
    "BS",  "TAB", "LF",  "VT",  "FF",  "CR",  "SO",  "SI",
    "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB",
    "CAN", "EM",  "SUB", "ESC", "FS",  "GS",  "RS",  "US",
  };

  /** Converter. */
  private final JsonConverter conv;
  /** Liberal flag. */
  private final boolean liberal;
  /** Escape flag. */
  private final boolean escape;
  /** Duplicates. */
  private final JsonDuplicates duplicates;
  /** Token builder for string literals. */
  private final TokenBuilder tb = new TokenBuilder();
  /** Input stream. */
  private final TextInput input;
  /** Current code point. */
  private int current;
  /** Input position. */
  private long pos;
  /** Current line number. */
  private long line = 1;
  /** Current column. */
  private long col = 1;
  /** Input buffer, 12 code points needed to hold one complete Unicode-escaped surrogate pair. */
  private final int[] buf = new int[16];

  /**
   * Constructor taking the input string and the spec according to which it is parsed.
   * @param input input stream
   * @param opts options
   * @param conv converter
   */
  public JsonParser(final TextInput input, final JsonParserOptions opts, final JsonConverter conv) {
    liberal = opts.get(JsonParserOptions.LIBERAL);
    escape = opts.get(JsonParserOptions.ESCAPE);
    final JsonDuplicates dupl = opts.get(JsonParserOptions.DUPLICATES);
    final JsonFormat jf = opts.get(JsonOptions.FORMAT);
    duplicates = dupl != null ? dupl : jf == JsonFormat.W3_XML || jf == JsonFormat.BASIC ?
      JsonDuplicates.RETAIN : JsonDuplicates.USE_FIRST;
    this.conv = conv;
    this.input = input;
  }

  /**
   * Parses a JSON expression.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public void parse() throws QueryException, IOException {
    try {
      current = input.read();
      consume('\uFEFF');
      skipWs();
      value();
    } catch(InputException ex) {
      if(ex instanceof DecodingException) throw WHICHCHARS_X.get(info(), ex);
      throw PARSE_JSON_X.get(info(), ex.getMessage());
    } catch(final StackOverflowError er) {
      Util.debug(er);
      throw error("Input is too deeply nested");
    }
    if(more()) throw error("Unexpected trailing content: %", remaining());
  }

  /**
   * Parses a JSON value.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void value() throws QueryException, IOException {
    if(!more()) throw eof(", expected JSON value");
    switch(current) {
      case '[':
        array();
        break;
      case '{':
        object();
        break;
      case '"':
        // string
        conv.stringLit(string());
        break;
      case '-':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        // number
        conv.numberLit(number());
        break;
      case 't':
        consume("true");
        conv.booleanLit(Token.TRUE);
        break;
      case 'f':
        consume("false");
        conv.booleanLit(Token.FALSE);
        break;
      case 'n':
        consume("null");
        conv.nullLit();
        break;
      default:
        throw error("Unexpected JSON value: '%'", remaining());
    }
  }

  /**
   * Parses a JSON object.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void object() throws QueryException, IOException {
    consumeWs('{', true);
    conv.openObject();
    if(!consumeWs('}', false)) {
      final TokenSet set = new TokenSet();
      do {
        final byte[] key = !liberal || current == '"' ? string() : unquoted();
        final boolean dupl = set.contains(key);
        if(dupl && duplicates == JsonDuplicates.REJECT)
          throw error(DUPLICATE_JSON_X, "Key \"%\" occurs more than once", key);

        final boolean add = !(dupl && duplicates == JsonDuplicates.USE_FIRST);
        conv.openPair(key, add);
        consumeWs(':', true);
        value();
        conv.closePair(add);
        set.put(key);
      } while(consumeWs(',', false) && !(liberal && current == '}'));
      consumeWs('}', true);
    }
    conv.closeObject();
  }

  /**
   * Parses a JSON array.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void array() throws QueryException, IOException {
    consumeWs('[', true);
    conv.openArray();
    if(!consumeWs(']', false)) {
      do {
        conv.openItem();
        value();
        conv.closeItem();
      } while(consumeWs(',', false) && !(liberal && current == ']'));
      consumeWs(']', true);
    }
    conv.closeArray();
  }

  /**
   * Reads an unquoted string literal.
   * @return the string
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private byte[] unquoted() throws QueryException, IOException {
    if(!Character.isJavaIdentifierStart(current))
      throw error("Expected unquoted string, found %", remaining());
    tb.reset();
    do {
      tb.add(consume());
    } while(Character.isJavaIdentifierPart(current));
    skipWs();
    return tb.toArray();
  }

  /**
   * Parses a number literal.
   * @return string representation
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private byte[] number() throws QueryException, IOException {
    tb.reset();

    // integral part
    int cp = consume();
    tb.add(cp);
    if(cp == '-') {
      cp = consume();
      if(cp < '0' || cp > '9') throw error("Number expected after '-'");
      tb.add(cp);
    }

    final boolean zero = cp == '0';
    if(zero && current >= '0' && current <= '9') throw error("No digit allowed after '0'");

    LOOP:
    while(true) {
      switch(current) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          tb.add(consume());
          break;
        case '.':
        case 'e':
        case 'E':
          break LOOP;
        default:
          skipWs();
          return tb.toArray();
      }
    }

    if(consume('.')) {
      tb.add('.');
      if(current < '0' || current > '9') throw error("Number expected after '.'");
      do {
        tb.add(consume());
      } while(current >= '0' && current <= '9');
      if(current != 'e' && current != 'E') {
        skipWs();
        return tb.toArray();
      }
    }

    // 'e' or 'E'
    tb.add(consume());
    if(current == '-' || current == '+') {
      tb.add(consume());
    }

    if(current < '0' || current > '9') throw error("Exponent expected");
    do tb.add(consume());
    while(current >= '0' && current <= '9');
    skipWs();
    return tb.toArray();
  }

  /**
   * Parses a string literal.
   * @return the string
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private byte[] string() throws QueryException, IOException {
    if(!consume('"')) throw error("Expected: string, found: %", currentAsString());
    tb.reset();
    int high = 0; // cached high surrogate
    while(more()) {
      final long p = pos;
      int cp = consume();

      // string is closed..
      if(cp == '"') {
        // unpaired surrogate?
        if(high != 0) add(high, p - 6, p);
        skipWs();
        return tb.toArray();
      }

      // escape sequence
      if(cp == '\\') {
        cp = consume();
        switch(cp) {
          case '\\':
          case '/':
          case '"':
            break;
          case 'b':
            cp = '\b';
            break;
          case 'f':
            cp = '\f';
            break;
          case 'n':
            cp = '\n';
            break;
          case 'r':
            cp = '\r';
            break;
          case 't':
            cp = '\t';
            break;
          case 'u':
            cp = 0;
            for(int i = 0; i < 4; i++) {
              if(!more()) throw eof(", expected four-digit hex value");
              final int cp2 = consume();
              if(cp2 >= '0' && cp2 <= '9')      cp = 16 * cp + cp2      - '0';
              else if(cp2 >= 'a' && cp2 <= 'f') cp = 16 * cp + cp2 + 10 - 'a';
              else if(cp2 >= 'A' && cp2 <= 'F') cp = 16 * cp + cp2 + 10 - 'A';
              else throw error("Illegal hexadecimal digit: %", currentAsString());
            }
            break;

          default:
            throw error("Unknown character escape: %", currentAsString());
        }
      } else if(!liberal && cp <= 0x1F) {
        throw error("Non-escaped control character: %", CTRL[cp]);
      }

      if(high != 0) {
        if(cp >= 0xDC00 && cp <= 0xDFFF) {
          // compute resulting codepoint
          cp = (high - 0xD800 << 10) + cp - 0xDC00 + 0x10000;
        } else {
          // add invalid high surrogate, treat expected low surrogate as new character
          add(high, p - 6, p);
        }
        high = 0;
      }

      if(cp >= 0xD800 && cp <= 0xDBFF) {
        // remember high surrogate
        high = cp;
      } else {
        add(cp, p, pos);
      }
    }
    throw eof(" in string literal");
  }

  /**
   * Adds the specified character.
   * @param cp character
   * @param s start position of invalid unicode sequence
   * @param e end position
   * @throws QueryException query exception
   */
  private void add(final int cp, final long s, final long e) throws QueryException {
    if(escape) {
      if(cp == '\\') {
        tb.add("\\\\");
      } else if(cp == '\b') {
        tb.add("\\b");
      } else if(cp == '\f') {
        tb.add("\\f");
      } else if(cp == '\n') {
        tb.add("\\n");
      } else if(cp == '\r') {
        tb.add("\\r");
      } else if(cp == '\t') {
        tb.add("\\t");
      } else if(XMLToken.valid(cp)) {
        tb.add(cp);
      } else {
        tb.add("\\u").add(hex(cp, 4));
      }
    } else if(XMLToken.valid(cp)) {
      tb.add(cp);
    } else if(conv.fallback == null) {
      tb.add(REPLACEMENT);
    } else {
      tb.add(conv.fallback.apply(substring(s, e).finish()));
    }
  }

  /**
   * Consumes all whitespace characters from the remaining query.
   * @throws IOException I/O exception
   */
  private void skipWs() throws IOException {
    while(more()) {
      switch(current) {
        case ' ':
        case '\t':
        case '\r':
        case '\n':
        case '\u00A0': // non-breaking space
          consume();
          break;
        default:
          return;
      }
    }
  }

  /**
   * Tries to consume the given character. If successful, following whitespace is skipped.
   * Otherwise, if the error flag is set, a parse error is thrown.
   * @param ch character to be consumed
   * @param err error flag
   * @return if the character was consumed
   * @throws QueryException query error
   * @throws IOException I/O exception
   */
  private boolean consumeWs(final char ch, final boolean err) throws QueryException, IOException {
    if(consume(ch)) {
      skipWs();
      return true;
    }
    if(err) throw error("Expected: '%', found: %", ch, currentAsString());
    return false;
  }

  /**
   * Throws an end-of-input error.
   * @param desc description
   * @return never
   * @throws QueryException query exception
   */
  private QueryException eof(final String desc) throws QueryException {
    throw error("Unexpected end of input%", desc);
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @return query exception
   */
  private QueryException error(final String msg, final Object... ext) {
    return error(PARSE_JSON_X_X_X, msg, ext);
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @param err error code
   * @return query exception
   */
  private QueryException error(final QueryError err, final String msg, final Object... ext) {
    return err.get(info(), line, col, Util.inf(msg, ext));
  }

  /**
   * Creates input information.
   * @return input info
   */
  public InputInfo info() {
    return new InputInfo(input.io().path(), (int) line, (int) col);
  }

  /**
   * Checks if more code points are found.
   * @return true, if more code points are found
   */
  private boolean more() {
    return current >= 0;
  }

  /**
   * Consumes the current code point.
   * @return current code point, or {@code 0} if string is exhausted
   * @throws IOException I/O exception
   */
  private int consume() throws IOException {
    final int cp = current;
    buf[(int) (pos++ % buf.length)] = cp;
    if(cp == '\n') {
      line++;
      col = 1;
    } else if(more()) {
      col++;
    }
    current = input.read();
    return cp;
  }

  /**
   * Peeks forward and consumes the code point if it equals the specified one.
   * @param cp code point to consume
   * @return true if code point was found
   * @throws IOException I/O exception
   */
  private boolean consume(final int cp) throws IOException {
    if(!(cp == current)) return false;
    consume();
    return true;
  }

  /**
   * Consumes input matching the given string, and skips any trailing white space.
   * @param string string to consume
   * @throws QueryException query exception, in case of mismatch
   * @throws IOException I/O exception
   */
  private void consume(final String string) throws QueryException, IOException {
    final long p = pos, l = line, c = col, len = string.length();
    for(int i = 0; i < len; ++i) {
      if(!consume(string.charAt(i))) {
        final String s = substring(p, pos) + remaining();
        line = l;
        col = c;
        throw error("Unexpected JSON value: '%'", s);
      }
    }
    skipWs();
  }

  /**
   * Returns an input substring.
   * @param s start index
   * @param e end index
   * @return substring
   */
  private TokenBuilder substring(final long s, final long e) {
    final TokenBuilder t = new TokenBuilder();
    for(long i = s; i < e; i++) t.add(buf[(int) (i % buf.length)]);
    return t;
  }

  /**
   * Returns a maximum of 15 remaining code points that have not yet been parsed.
   * @return query substring
   * @throws IOException I/O exception
   */
  private String remaining() throws IOException {
    tb.reset();
    for(int i = 0; i < 15 && more(); ++i) {
      final int cp = consume();
      if(cp == '\n') break;
      tb.add(cp);
    }
    return tb + (more() ? DOTS : "");
  }

  /**
   * Returns the current code point as string.
   * @return current code point
   */
  private String currentAsString() {
    return !more() ? "END OF INPUT" : !XMLToken.valid(current) || Character.isSpaceChar(current) ?
      Character.getName(current) :
      Character.toString(current);
  }
}
