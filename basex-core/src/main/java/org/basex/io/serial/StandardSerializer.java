package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.text.*;
import java.text.Normalizer.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.XQArray;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class serializes items to an output stream.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class StandardSerializer extends OutputSerializer {
  /** Normalization form. */
  protected final Form form;
  /** Character map. */
  private final IntObjMap<byte[]> map;

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

    // normalization form
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

    final String maps = sopts.get(USE_CHARACTER_MAPS);
    if(maps.isEmpty()) {
      map = null;
    } else {
      map = new IntObjMap<>();
      for(final Map.Entry<String, String> entry : sopts.toMap(USE_CHARACTER_MAPS).entrySet()) {
        final String key = entry.getKey();
        if(key.length() != 1) throw SERMAP_X.getIO(key);
        map.put(key.charAt(0), token(entry.getValue()));
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
    for(final Value value : ((XQArray) item).members()) {
      for(final Item it : value) serialize(it);
    }
  }

  @Override
  protected void atomic(final Item item) throws IOException {
    if(sep && atomic) out.print(' ');
    try {
      if(item instanceof StrLazy && form == null) {
        try(InputStream is = item.input(null)) {
          for(int cp; (cp = is.read()) != -1;) printChar(cp);
        }
      } else {
        printChars(norm(item.string(null)));
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
    sep = true;
    atomic = true;
  }

  @Override
  protected final void printChar(final int cp) throws IOException {
    if(!characterMap(cp)) print(cp);
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
   * Normalizes the specified text.
   * @param text text to be normalized
   * @return normalized text
   */
  protected final byte[] norm(final byte[] text) {
    return form == null || ascii(text) ? text : token(Normalizer.normalize(string(text), form));
  }

  /**
   * Replaces a character with an entry from the character map.
   * @param cp codepoint
   * @return {@code true} if replacement was found
   * @throws IOException I/O exception
   */
  protected final boolean characterMap(final int cp) throws IOException {
    if(map != null) {
      final byte[] value = map.get(cp);
      if(value != null) {
        out.print(value);
        return true;
      }
    }
    return false;
  }
}
