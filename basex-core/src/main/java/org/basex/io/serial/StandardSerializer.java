package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.text.*;
import java.text.Normalizer.Form;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class serializes items to an output stream.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class StandardSerializer extends OutputSerializer {
  /** Normalization form. */
  protected final Form form;
  /** WebDAV flag. */
  protected final IntObjMap<byte[]> map;

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
    itemsep(null);

    // normalization form
    final String norm = sopts.get(NORMALIZATION_FORM);
    if(norm.equals(SerializerOptions.NORMALIZATION_FORM.value())) {
      form = null;
    } else {
      try {
        form = Form.valueOf(norm);
      } catch(final IllegalArgumentException ex) {
        throw SERNORM_X.getIO(norm);
      }
    }

    final String maps = sopts.get(USE_CHARACTER_MAPS);
    if(maps.isEmpty()) {
      map = null;
    } else {
      map = new IntObjMap<>();
      for(final String s : Strings.split(maps, ',')) {
        final String[] kv = Strings.split(s, '=', 2);
        if(kv.length < 2) throw SERMAP_X.getIO(maps);
        map.put(kv[0].charAt(0), token(kv[1]));
      }
    }
  }

  @Override
  public void serialize(final Item item) throws IOException {
    if(more && itemsep != null) {
      out.print(itemsep);
      sep = false;
    }
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
    if(type == NodeType.ATT) throw SERATTR_X.getIO(node);
    if(type == NodeType.NSP) throw SERNS_X.getIO(node);
    super.node(node);
  }

  @Override
  protected void function(final FItem item) throws IOException {
    throw SERFUNC_X.getIO(item.seqType());
  }

  @Override
  protected void atomic(final Item item) throws IOException {
    if(sep && atomic) out.print(' ');
    try {
      if(item instanceof StrStream && form == null) {
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

  /**
   * Normalizes the specified text.
   * @param text text to be normalized
   * @return normalized text
   */
  protected final byte[] norm(final byte[] text) {
    return form == null || ascii(text) ? text : token(Normalizer.normalize(string(text), form));
  }
}
