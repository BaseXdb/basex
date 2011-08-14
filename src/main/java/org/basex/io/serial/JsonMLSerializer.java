package org.basex.io.serial;

import static org.basex.query.util.Err.*;

import java.io.IOException;
import java.io.OutputStream;

import org.basex.query.QueryException;
import org.basex.util.Util;

/**
 * This class serializes data according to the JsonML specification.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class JsonMLSerializer extends OutputSerializer {
  /** Indicates serialized attributes. */
  private boolean att;

  /**
   * Constructor.
   * @param os output stream reference
   * @param props serialization properties
   * @throws IOException I/O exception
   */
  public JsonMLSerializer(final OutputStream os, final SerializerProp props)
      throws IOException {
    super(os, props);
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
    if(level != 0) {
      print(',');
      indent();
    }
    print("[ \"");
    for(final byte ch : name) ch(ch);
    print('"');
    att = false;
  }

  @Override
  public void attribute(final byte[] name, final byte[] value)
      throws IOException {

    print(',');
    if(!att) {
      print(" {");
      att = true;
    }
    indent(level + 1);
    print('"');
    for(final byte ch : name) ch(ch);
    print("\": \"");
    for(final byte ch : value) ch(ch);
    print("\"");
  }

  @Override
  protected void finishOpen() throws IOException {
    if(att) {
      indent(level + 1);
      print("}");
    }
  }

  @Override
  public void finishText(final byte[] text) throws IOException {
    print(',');
    indent();
    print('"');
    for(final byte ch : text) ch(ch);
    print('"');
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    indent();
    print(']');
  }

  @Override
  protected void ch(final int ch) throws IOException {
    switch(ch) {
      case '\b': print("\\b");  break;
      case '\f': print("\\f");  break;
      case '\n': print("\\n");  break;
      case '\r': print("\\r");  break;
      case '\t': print("\\t");  break;
      case '"':  print("\\\""); break;
      case '/':  print("\\/");  break;
      case '\\': print("\\\\"); break;
      default:   print(ch);     break;
    }
  }

  @Override
  public void finishComment(final byte[] value) throws IOException {
    error("Comments cannot be serialized");
  }

  @Override
  public void finishPi(final byte[] name, final byte[] value)
      throws IOException {
    error("Processing instructions cannot be serialized");
  }

  @Override
  public void finishItem(final byte[] value) throws IOException {
    error("Items cannot be serialized");
  }

  /**
   * Prints some indentation.
   * @param lvl level
   * @throws IOException I/O exception
   */
  protected void indent(final int lvl) throws IOException {
    print(NL);
    final int ls = lvl * indents;
    for(int l = 0; l < ls; ++l) print(tab);
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @return build exception
   * @throws IOException I/O exception
   */
  private QueryException error(final String msg, final Object... ext)
      throws IOException {
    throw JSONSER.thrwSerial(Util.inf(msg, ext));
  }
}
