package org.basex.io.serial;

import java.io.*;

import org.basex.io.out.*;
import org.basex.io.serial.json.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
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
    more = false;
    for(final Item it : value) serialize(it);
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(more) xml.printChars(itemsep);
    super.serialize(item);
  }

  @Override
  protected void node(final ANode item) throws IOException {
    final Type type = item.type;
    if(type == NodeType.ATT) xml.attribute(item.name(), item.string());
    else if(type == NodeType.NSP) xml.namespace(item.name(), item.string());
    else xml.node(item);
    xml.reset();
  }

  @Override
  protected void function(final FItem item) throws IOException {
    if(item instanceof Map || item instanceof Array) {
      json.function(item);
      json.reset();
    } else {
      final TokenBuilder tb = new TokenBuilder("function ");
      final QNm fn = item.funcName();
      if(fn == null) tb.add("(anonymous)");
      else tb.add(fn.string());
      xml.out.print(tb.add('#').addInt(item.arity()).finish());
    }
  }

  @Override
  protected void atomic(final Item item) throws IOException {
    try {
      xml.out.print(item.string(null));
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  @Override
  public void close() throws IOException {
    xml.close();
    super.close();
  }
}
