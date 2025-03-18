package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.text.Normalizer.*;
import java.util.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class serializes items to an output stream.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class StandardSerializer extends OutputSerializer {
  /** Normalization form. */
  protected final Form form;
  /** Character map. */
  protected final IntObjectMap<byte[]> map;

  /** Include separator. */
  protected boolean sep;
  /** Atomic flag. */
  protected boolean atomic;

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  protected StandardSerializer(final OutputStream os, final SerializerOptions sopts)
      throws IOException {

    super(os, sopts);

    final String norm = sopts.get(NORMALIZATION_FORM);
    if(norm.equals(NORMALIZATION_FORM.value())) {
      form = null;
    } else {
      try {
        form = Form.valueOf(norm);
      } catch(final IllegalArgumentException ex) {
        Util.debug(ex);
        throw SERNORM_X.getIO(norm);
      }
    }
    if(sopts.get(USE_CHARACTER_MAPS).isEmpty()) {
      map = null;
    } else {
      map = new IntObjectMap<>();
      for(final Map.Entry<String, String> entry : sopts.toMap(USE_CHARACTER_MAPS).entrySet()) {
        final String key = entry.getKey();
        if(key.codePoints().count() != 1) throw SERPARAM_X.getIO(
            Util.info("Key in character map is not a single character: %.", key));
        map.put(key.codePointAt(0), token(entry.getValue()));
      }
    }
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(separate()) sep = false;
    super.serialize(item);
  }

  @Override
  public void reset() {
    sep = false;
    atomic = false;
    super.reset();
  }

  @Override
  protected void node(final ANode node) throws IOException {
    final Type type = node.type;
    if(type == NodeType.ATTRIBUTE) throw SERATTR_X.getIO(node);
    if(type == NodeType.NAMESPACE_NODE) throw SERNS_X.getIO(node);
    super.node(node);
  }

  @Override
  protected void function(final FItem item) throws IOException {
    if(!(item instanceof XQArray)) throw SERFUNC_X.getIO(item.seqType());
    for(final Value value : ((XQArray) item).iterable()) {
      for(final Item it : value) serialize(it);
    }
  }

  @Override
  protected void atomic(final Item item) throws IOException {
    if(sep && atomic) out.print(' ');
    try {
      if(item instanceof StrLazy && form == null) {
        try(TextInput ti = item.stringInput(null)) {
          for(int cp; (cp = ti.read()) != -1;) printChar(cp);
        }
      } else {
        printChars(normalize(item.string(null), form));
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    sep = true;
    atomic = true;
  }

  @Override
  protected final void printChar(final int cp) throws IOException {
    final byte[] value = map != null ? map.get(cp) : null;
    if(value != null) out.print(value);
    else print(cp);
  }

  /**
   * Prints a single character.
   * @param cp codepoint
   * @throws IOException I/O exception
   */
  protected void print(final int cp) throws IOException {
    out.print(cp);
  }

  /**
   * Flattens an array.
   * @param array array
   * @return contained items
   */
  static ItemList flatten(final XQArray array) {
    final ItemList list = new ItemList();
    for(final Value value : array.iterable()) {
      for(final Item item : value) {
        if(item instanceof XQArray) {
          list.add(flatten((XQArray) item));
        } else {
          list.add(item);
        }
      }
    }
    return list;
  }
}
