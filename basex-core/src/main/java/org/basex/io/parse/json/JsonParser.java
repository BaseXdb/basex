package org.basex.io.parse.json;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.build.json.JsonParserOptions.JsonDuplicates;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A JSON parser generating parse events similar to a SAX XML parser.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class JsonParser extends InputParser {
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

  /**
   * Constructor taking the input string and the spec according to which it is parsed.
   * @param input input string
   * @param opts options
   * @param conv converter
   */
  public JsonParser(final String input, final JsonParserOptions opts, final JsonConverter conv) {
    super(input);
    liberal = opts.get(JsonParserOptions.LIBERAL);
    escape = opts.get(JsonParserOptions.ESCAPE);
    final JsonDuplicates dupl = opts.get(JsonParserOptions.DUPLICATES);
    duplicates = dupl != null ? dupl : opts.get(JsonOptions.FORMAT) == JsonFormat.BASIC ?
      JsonDuplicates.RETAIN : JsonDuplicates.USE_FIRST;
    this.conv = conv;
  }

  /**
   * Parses a JSON expression.
   * @throws QueryException query exception
   */
  public void parse() throws QueryException {
    consume('\uFEFF');
    skipWs();
    try {
      value();
    } catch(final StackOverflowError er) {
      Util.debug(er);
      throw error("Input is too deeply nested");
    }
    if(more()) throw error("Unexpected trailing content: %", remaining());
  }

  /**
   * Parses a JSON value.
   * @throws QueryException query exception
   * @throws QueryException query exception
   */
  private void value() throws QueryException {
    if(pos >= length) throw eof(", expected JSON value");
    switch(current()) {
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
      default:
        // boolean, null or constructor
        if(consume("true")) conv.booleanLit(TRUE);
        else if(consume("false")) conv.booleanLit(FALSE);
        else if(consume("null")) conv.nullLit();
        else throw error("Unexpected JSON value: '%'", remaining());
        skipWs();
    }
  }

  /**
   * Parses a JSON object.
   * @throws QueryException query exception
   */
  private void object() throws QueryException {
    consumeWs('{', true);
    conv.openObject();
    if(!consumeWs('}', false)) {
      final TokenSet set = new TokenSet();
      do {
        final byte[] key = !liberal || current() == '"' ? string() : unquoted();
        final boolean dupl = set.contains(key);
        if(dupl && duplicates == JsonDuplicates.REJECT)
          throw error(JSON_DUPL_X_X_X, "Key \"%\" occurs more than once", key);

        final boolean add = !(dupl && duplicates == JsonDuplicates.USE_FIRST);
        conv.openPair(key, add);
        consumeWs(':', true);
        value();
        conv.closePair(add);
        set.put(key);
      } while(consumeWs(',', false) && !(liberal && current() == '}'));
      consumeWs('}', true);
    }
    conv.closeObject();
  }

  /**
   * Parses a JSON array.
   * @throws QueryException query exception
   */
  private void array() throws QueryException {
    consumeWs('[', true);
    conv.openArray();
    if(!consumeWs(']', false)) {
      do {
        conv.openItem();
        value();
        conv.closeItem();
      } while(consumeWs(',', false) && !(liberal && current() == ']'));
      consumeWs(']', true);
    }
    conv.closeArray();
  }

  /**
   * Reads an unquoted string literal.
   * @return the string
   * @throws QueryException query exception
   */
  private byte[] unquoted() throws QueryException {
    if(!Character.isJavaIdentifierStart(current()))
      throw error("Expected unquoted string, found %", remaining());
    tb.reset();
    do {
      tb.add(consume());
    } while(Character.isJavaIdentifierPart(current()));
    skipWs();
    return tb.toArray();
  }

  /**
   * Parses a number literal.
   * @return string representation
   * @throws QueryException query exception
   */
  private byte[] number() throws QueryException {
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
    cp = current();
    if(zero && cp >= '0' && cp <= '9') throw error("No digit allowed after '0'");

    LOOP:
    while(true) {
      switch(cp) {
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
          tb.add(cp);
          pos++;
          cp = current();
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
      cp = current();
      if(cp < '0' || cp > '9') throw error("Number expected after '.'");
      do {
        tb.add(cp);
        pos++;
        cp = current();
      } while(cp >= '0' && cp <= '9');
      if(cp != 'e' && cp != 'E') {
        skipWs();
        return tb.toArray();
      }
    }

    // 'e' or 'E'
    tb.add(consume());
    cp = current();
    if(cp == '-' || cp == '+') {
      tb.add(consume());
      cp = current();
    }

    if(cp < '0' || cp > '9') throw error("Exponent expected");
    do tb.add(consume());
    while((cp = current()) >= '0' && cp <= '9');
    skipWs();
    return tb.toArray();
  }

  /**
   * Parses a string literal.
   * @return the string
   * @throws QueryException query exception
   */
  private byte[] string() throws QueryException {
    if(!consume('"')) throw error("Expected: string, found: %", currentAsString());
    tb.reset();
    int high = 0; // cached high surrogate
    while(pos < length) {
      final int p = pos;
      int cp = consume();

      // string is closed..
      if(cp == '"') {
        // unpaired surrogate?
        if(high != 0) add(high, pos - 7, p);
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
            if(pos + 4 >= length) throw eof(", expected four-digit hex value");
            cp = 0;
            for(int i = 0; i < 4; i++) {
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
          add(high, p, pos);
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
  private void add(final int cp, final int s, final int e) throws QueryException {
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

  /** Consumes all whitespace characters from the remaining query. */
  private void skipWs() {
    while(more()) {
      switch(current()) {
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
   */
  private boolean consumeWs(final char ch, final boolean err) throws QueryException {
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
    return error(JSON_PARSE_X_X_X, msg, ext);
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @param err error code
   * @return query exception
   */
  private QueryException error(final QueryError err, final String msg, final Object... ext) {
    final InputInfo ii = info();
    return err.get(ii, ii.line(), ii.column(), Util.inf(msg, ext));
  }
}
