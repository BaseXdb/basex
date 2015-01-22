package org.basex.io.serial;

import java.io.*;

import org.basex.io.out.*;
import org.basex.io.serial.json.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class serializes items in adaptive mode.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class AdaptiveSerializer extends OutputSerializer {
  /** XML serializer. */
  private final XMLSerializer xml;
  /** JSON serializer. */
  private final JsonNodeSerializer json;

  /** Indicates if more than one item is serialized. */
  private boolean sep;

  /**
   * Constructor, specifying serialization options.
   * @param out print output
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  public AdaptiveSerializer(final PrintOutput out, final SerializerOptions sopts)
      throws IOException {

    super(out, sopts);
    xml = new XMLSerializer(out, sopts);
    json = new JsonNodeSerializer(out, sopts, this);
    itemsep(sopts, Token.token("\n"));
  }

  /**
   * Serializes a value.
   * @param value value
   * @throws IOException I/O exception
   */
  public void serialize(final Value value) throws IOException {
    final boolean s = sep;
    for(final Item it : value) serialize(it, false, false);
    sep = s;
  }

  @Override
  public void serialize(final Item item, final boolean atts, final boolean iter)
      throws IOException {

    if(sep) xml.printChars(itemsep);

    if(item instanceof FItem) {
      if(item instanceof Map || item instanceof Array) {
        json.serialize(item);
        json.reset();
      } else {
        final FItem fi = (FItem) item;
        final TokenBuilder tb = new TokenBuilder("function ");
        final QNm fn = fi.funcName();
        if(fn == null) tb.add("(anonymous)");
        else tb.add(fn.string());
        xml.out.print(tb.add('#').addInt(fi.arity()).finish());
      }
    } else {
      if(item instanceof ANode) {
        xml.serialize((ANode) item);
      } else {
        xml.serialize(item, atts, iter);
      }
      xml.reset();
    }
    sep = true;
  }

  @Override
  public void reset() {
    sep = false;
  }

  @Override
  public void close() throws IOException {
    xml.close();
    super.close();
  }
}
