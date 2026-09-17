package org.basex.util.http;

import java.util.*;
import java.util.Map.*;

import org.basex.query.util.list.*;
import org.basex.query.value.*;

/**
 * Container for a parsed HTTP response body.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ResponseBody {
  /** Header fields of a part, in the order of the message. */
  public final ArrayList<Entry<String, String>> headers = new ArrayList<>();
  /** Parts of a multipart body. */
  public final ArrayList<ResponseBody> parts = new ArrayList<>();
  /** Media type. */
  public MediaType type = MediaType.TEXT_PLAIN;
  /** Multipart boundary (can be {@code null}). */
  public byte[] boundary;
  /** Parsed contents (can be {@code null}). */
  public Value value;

  /**
   * Returns the parsed contents of the body, or of all its parts.
   * @return contents
   */
  public Value values() {
    final ItemList items = new ItemList();
    if(parts.isEmpty()) {
      if(value != null) items.add(value);
    } else {
      for(final ResponseBody part : parts) {
        if(part.value != null) items.add(part.value);
      }
    }
    return items.value();
  }
}
