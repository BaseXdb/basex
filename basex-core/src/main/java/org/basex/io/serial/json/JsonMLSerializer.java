package org.basex.io.serial.json;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.io.serial.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class serializes items as described in the
 * <a href="http://jsonml.org">JsonML</a> specification.
 * JsonML can be used to transform any XML document to JSON and back.
 * Note, however, that namespaces, comments and processing instructions will be
 * discarded in the transformation process. More details are found in the
 * <a href="http://jsonml.org/XML/">JsonML documentation</a>.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonMLSerializer extends JsonSerializer {
  /** Indicates serialized attributes. */
  private boolean att;

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  public JsonMLSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts);
  }

  @Override
  protected void startOpen(final QNm name) throws IOException {
    if(level != 0) {
      out.print(',');
      indent();
    }
    out.print('[');
    out.print('"');
    for(final byte ch : name.local()) printChar(ch);
    out.print('"');
    att = false;
  }

  @Override
  protected void namespace(final byte[] prefix, final byte[] uri, final boolean standalone) { }

  @Override
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone)
      throws IOException {

    out.print(',');
    out.print(' ');
    if(!att) {
      out.print('{');
      att = true;
    }
    out.print('"');
    for(final byte ch : Token.local(name)) printChar(ch);
    out.print('"');
    out.print(':');
    out.print('"');
    for(final byte ch : Token.normalize(value, form)) printChar(ch);
    out.print('"');
  }

  @Override
  protected void finishOpen() throws IOException {
    if(att) out.print('}');
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    out.print(',');
    indent();
    out.print('"');
    for(final byte ch : value) printChar(ch);
    out.print('"');
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    out.print(']');
  }

  @Override
  protected void finishClose() throws IOException {
    out.print(']');
  }

  @Override
  protected void atomic(final Item value) throws IOException {
    throw JSON_SERIALIZE_X.getIO("Atomic items cannot be serialized");
  }
}
