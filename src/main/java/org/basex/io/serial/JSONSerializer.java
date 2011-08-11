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

  /** Current name. */
  private byte[] name;

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
  protected void startOpen(final byte[] t) throws IOException {
    name = null;
    types.set(level(), null);
  }

  @Override
  protected void finishOpen() throws IOException {
    if(!tag(PAIR) && !tag(ITEM) && !tag(JSON)) error("Invalid tag: \"%\"", tag);

    final int level = level();
    if(comma.get(level)) out.print(",");

    if(!tag(JSON)) {
      comma.set(level, true);
      indent(true);
    }
    if(tag(PAIR)) {
      if(name == null) error("No name specified");
      out.print("\"");
      out.print(name);
      out.print("\": ");
    }

    final byte[] type = types.get(level);
    if(type == null) error("No type specified");
    if(!eq(type, ARR) && !eq(type, OBJ)) return;

    out.print(eq(type, ARR) ? "[" : "{");
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
        out.print(NULL);
      } else if(eq(type, STR)) {
        out.print("\"\"");
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
    out.print(eq(type, ARR) ? "]" : "}");
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) throws IOException {
    if(eq(n, TYPE)) {
      types.set(level(), v);
    } else if(eq(n, NAME)) {
      if(tag(PAIR)) name = v;
    } else {
      error("Invalid attribute: \"%\"", n);
    }
  }

  @Override
  public void finishText(final byte[] b) throws IOException {
    if(trim(b).length == 0) return;
    if(tag(PAIR) || tag(ITEM)) {
      final byte[] type = types.get(level() - 1);
      if(eq(type, STR)) {
        out.print("\"");
        out.print(b);
        out.print("\"");
      } else if(eq(type, BOOL)) {
        if(!eq(b, TRUE) && !eq(b, FALSE)) error("Invalid boolean value", b);
        out.print(b);
      } else if(eq(type, NUM)) {
        out.print(b);
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
  public void finishComment(final byte[] b) throws IOException {
    error("Comment cannot be serialized.");
  }

  @Override
  public void finishPi(final byte[] n, final byte[] v) throws IOException {
    error("Processing instruction cannot be serialized.");
  }

  @Override
  public void finishItem(final byte[] b) throws IOException {
    error("Item cannot be serialized.");
  }

  /**
   * Returns if the current tag equals the specified tag.
   * @param t tag to be compared
   * @return result of check
   */
  private boolean tag(final byte[] t) {
    return eq(tag, t);
  }

  /**
   * Prints the text declaration to the output stream.
   * @param nl newline
   * @throws IOException I/O exception
   */
  private void indent(final boolean nl) throws IOException {
    if(nl) out.print(Prop.NL);
    final int ls = level() * indents;
    for(int l = 0; l < ls; ++l) out.print(tab);
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
}
