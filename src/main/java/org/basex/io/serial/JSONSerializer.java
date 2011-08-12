package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.io.serial.SerializerProp.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.core.Prop;
import org.basex.io.out.ArrayOutput;
import org.basex.io.out.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Util;
import org.basex.util.list.BoolList;
import org.basex.util.list.TokenList;

/**
 * This class serializes trees as JSON.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class JSONSerializer extends Serializer {
  /** Output stream. */
  private final PrintOutput out;
  /** Number of spaces to indent. */
  private final int indents;
  /** Tabular character. */
  private final String tab;

  /** Comma flag. */
  private final BoolList comma = new BoolList();
  /** Types. */
  private final TokenList types = new TokenList();
  /** Current key. */
  private byte[] key;

  /**
   * Constructor.
   * @param ao array output
   * @param props serialization properties
   * @throws IOException I/O exception
   */
  public JSONSerializer(final ArrayOutput ao, final SerializerProp props)
      throws IOException {

    out = PrintOutput.get(ao);
    final SerializerProp p = props == null ? XMLSerializer.PROPS : props;
    indents = Math.max(0, toInt(p.get(S_INDENTS)));
    tab = String.valueOf(p.check(S_TABULATOR, YES, NO).equals(YES) ?
        '\t' : ' ');
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
    key = null;
    types.set(level(), null);
  }

  @Override
  protected void finishOpen() throws IOException {
    if(!tag(PAIR) && !tag(ITEM) && !tag(JSON)) error("Invalid tag: \"%\"", tag);

    final int level = level();
    if(comma.get(level)) print(',');

    if(!tag(JSON)) {
      comma.set(level, true);
      indent(true);
    }
    if(tag(PAIR)) {
      if(key == null) error("No name specified");
      print('"');
      print(key);
      print("\": ");
    }

    final byte[] type = types.get(level);
    if(type == null) error("No type specified");
    if(!eq(type, ARR) && !eq(type, OBJ)) return;

    print(eq(type, ARR) ? '[' : '{');
    comma.set(level + 1, false);
  }

  @Override
  protected void finishClose(final boolean empty) throws IOException {
    if(empty) finishOpen();

    final byte[] type = types.get(level());
    if(type == null) return;
    final boolean struct = eq(type, ARR) || eq(type, OBJ);

    if(empty) {
      if(eq(type, NULL)) {
        print(NULL);
      } else if(eq(type, STR)) {
        print("\"\"");
      } else if(!eq(type, OBJ) && !eq(type, ARR)) {
        if(eq(type, NUM) || eq(type, BOOL)) {
          error("Value needed for % type", type);
        } else {
          error("Invalid type: \"%\"", type);
        }
      }
    } else {
      if(struct) indent(true);
    }
    if(!struct) return;
    print(eq(type, ARR) ? ']' : '}');
  }

  @Override
  public void attribute(final byte[] name, final byte[] value)
      throws IOException {

    if(eq(name, TYPE)) {
      types.set(level(), value);
    } else if(eq(name, NAME)) {
      if(tag(PAIR)) key = value;
    } else {
      error("Invalid attribute: \"%\"", name);
    }
  }

  @Override
  public void finishText(final byte[] text) throws IOException {
    if(trim(text).length == 0) return;
    if(tag(PAIR) || tag(ITEM)) {
      final byte[] type = types.get(level() - 1);
      if(eq(type, STR)) {
        print('"');
        for(final byte ch : text) {
          if(ch == '\b') {
            print("\\b");
          } else if(ch == '\f') {
            print("\\f");
          } else if(ch == '\n') {
            print("\\n");
          } else if(ch == '\r') {
            print("\\r");
          } else if(ch == '\t') {
            print("\\t");
          } else {
            print(ch);
          }
        }
        print('"');
      } else if(eq(type, BOOL)) {
        if(!eq(text, TRUE) && !eq(text, FALSE))
          error("Invalid boolean value", text);
        print(text);
      } else if(eq(type, NUM)) {
        print(text);
      } else if(eq(type, NULL)) {
        error("No value expected after \"null\"");
      } else {
        error("Invalid type: \"%\"", type);
      }
    } else {
      error("No text allowed in \"%\" tag", tag);
    }
  }

  @Override
  public void finishComment(final byte[] value) throws IOException {
    error("Comment cannot be serialized.");
  }

  @Override
  public void finishPi(final byte[] name, final byte[] value)
      throws IOException {
    error("Processing instruction cannot be serialized.");
  }

  @Override
  public void finishItem(final byte[] value) throws IOException {
    error("Item cannot be serialized.");
  }

  /**
   * Returns if the current tag equals the specified tag.
   * @param name tag to be compared
   * @return result of check
   */
  private boolean tag(final byte[] name) {
    return eq(tag, name);
  }

  /**
   * Prints the text declaration to the output stream.
   * @param nl newline
   * @throws IOException I/O exception
   */
  private void indent(final boolean nl) throws IOException {
    if(nl) print(Prop.NL);
    final int ls = level() * indents;
    for(int l = 0; l < ls; ++l) print(tab);
  }

  /**
   * Skips whitespaces, raises an error if the specified string cannot be
   * consumed.
   * @param msg error message
   * @param ext error details
   * @return build exception
   * @throws SerializerException serializer exception
   */
  private QueryException error(final String msg, final Object... ext)
      throws SerializerException {
    throw JSONSER.thrwSerial(Util.inf(msg, ext));
  }

  /**
   * Writes a string in the current encoding.
   * @param s string to be printed
   * @throws IOException I/O exception
   */
  private void print(final String s) throws IOException {
    print(token(s));
  }

  /**
   * Writes a token in the current encoding.
   * @param token token to be printed
   * @throws IOException I/O exception
   */
  private void print(final byte[] token) throws IOException {
    for(final byte b : token) out.write(b);
  }

  /**
   * Writes a character in the current encoding.
   * @param ch character to be printed
   * @throws IOException I/O exception
   */
  private void print(final int ch) throws IOException {
    if(ch <= 0x7F) {
      out.write(ch);
    } else if(ch <= 0x7FF) {
      out.write(ch >>  6 & 0x1F | 0xC0);
      out.write(ch >>  0 & 0x3F | 0x80);
    } else if(ch <= 0xFFFF) {
      out.write(ch >> 12 & 0x0F | 0xE0);
      out.write(ch >>  6 & 0x3F | 0x80);
      out.write(ch >>  0 & 0x3F | 0x80);
    } else {
      out.write(ch >> 18 & 0x07 | 0xF0);
      out.write(ch >> 12 & 0x3F | 0x80);
      out.write(ch >>  6 & 0x3F | 0x80);
      out.write(ch >>  0 & 0x3F | 0x80);
    }
  }
}
