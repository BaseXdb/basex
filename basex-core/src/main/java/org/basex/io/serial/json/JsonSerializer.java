package org.basex.io.serial.json;

import java.io.*;

import org.basex.build.*;
import org.basex.io.serial.*;

/**
 * Abstract JSON serializer class.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class JsonSerializer extends OutputSerializer {
  /** JSON options. */
  protected final JsonSerialOptions jopts;
  /** Escape special characters. */
  protected final boolean escape;

  /**
   * Constructor.
   * @param os output stream reference
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  protected JsonSerializer(final OutputStream os, final SerializerOptions opts) throws IOException {
    super(os, opts);
    jopts = opts.get(SerializerOptions.JSON);
    escape = jopts.get(JsonSerialOptions.ESCAPE);
    if(jopts.contains(JsonSerialOptions.INDENT)) indent = jopts.get(JsonSerialOptions.INDENT);
  }

  @Override
  protected final void encode(final int ch) throws IOException {
    if(!escape) {
      print(ch);
    } else switch(ch) {
      case '\b': print("\\b");  break;
      case '\f': print("\\f");  break;
      case '\n': print("\\n");  break;
      case '\r': print("\\r");  break;
      case '\t': print("\\t");  break;
      case '"':  print("\\\""); break;
      case '\\': print("\\\\"); break;
      default:   print(ch);     break;
    }
  }
}
