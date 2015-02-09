package org.basex.io.serial.json;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;

/**
 * This class serializes items as described in the
 * <a href="http://jsonml.org">JsonML</a> specification.
 * JsonML can be used to transform any XML document to JSON and back.
 * Note, however, that namespaces, comments and processing instructions will be
 * discarded in the transformation process. More details are found in the
 * <a href="http://jsonml.org/XML/">JsonML documentation</a>.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class JsonMLSerializer extends JsonSerializer {
  /** Indicates serialized attributes. */
  private boolean att;

  /**
   * Constructor.
   * @param out print output
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public JsonMLSerializer(final PrintOutput out, final SerializerOptions opts) throws IOException {
    super(out, opts);
  }

  @Override
  protected void startOpen(final byte[] name) throws IOException {
    if(level != 0) {
      out.print(',');
      indent();
    }
    out.print("[\"");
    for(final byte ch : local(name)) encode(ch);
    out.print('"');
    att = false;
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value) throws IOException {
    out.print(",");
    out.print(' ');
    if(!att) {
      out.print("{");
      att = true;
    }
    out.print('"');
    for(final byte ch : name) encode(ch);
    out.print("\":\"");
    for(final byte ch : norm(value)) encode(ch);
    out.print("\"");
  }

  @Override
  protected void finishOpen() throws IOException {
    if(att) out.print("}");
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    out.print(',');
    indent();
    out.print('"');
    for(final byte ch : value) encode(ch);
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
    throw BXJS_SERIAL_X.getIO("Atomic values cannot be serialized");
  }
}
