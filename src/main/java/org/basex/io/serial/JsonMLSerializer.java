package org.basex.io.serial;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.query.item.*;
import org.basex.util.*;

/**
 * This class serializes data as described in the
 * <a href="http://jsonml.org">JsonML</a> specification.
 * JsonML can be used to transform any XML document to JSON and back.
 * Note, however, that namespaces, comments and processing instructions will be
 * discarded in the transformation process. More details are found in the
 * <a href="http://jsonml.org/XML/">JsonML documentation</a>.
 *
 * @author BaseX Team 2005-12, BSD License
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
    print('[');
    print('"');
    for(final byte ch : local(name)) code(ch);
    print('"');
    att = false;
  }

  @Override
  public void attribute(final byte[] name, final byte[] value) throws IOException {
    print(", ");
    if(!att) {
      print("{");
      att = true;
    }
    print('"');
    for(final byte ch : name) code(ch);
    print("\":\"");
    for(final byte ch : value) code(ch);
    print("\"");
  }

  @Override
  public void namespace(final byte[] n, final byte[] v) throws IOException {
  }

  @Override
  protected void finishOpen() throws IOException {
    if(att) print("}");
  }

  @Override
  public void finishText(final byte[] text) throws IOException {
    print(',');
    indent();
    print('"');
    for(final byte ch : text) code(ch);
    print('"');
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    print(']');
  }

  @Override
  protected void finishClose() throws IOException {
    print(']');
  }

  @Override
  protected void code(final int ch) throws IOException {
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
  public void finishComment(final byte[] value) throws IOException { }

  @Override
  public void finishPi(final byte[] name, final byte[] value) throws IOException { }

  @Override
  public void atomic(final Item value) throws IOException {
    error("Atomic values cannot be serialized");
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @throws IOException I/O exception
   */
  private static void error(final String msg, final Object... ext) throws IOException {
    throw BXJS_SER.thrwSerial(Util.inf(msg, ext));
  }
}
