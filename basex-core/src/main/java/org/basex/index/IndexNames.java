package org.basex.index;

import static org.basex.util.Token.*;

import java.util.*;
import java.util.regex.*;

import org.basex.data.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;

/**
 * Names and namespace uris of elements/attribute to index.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class IndexNames {
  /** Names and namespace uris. */
  private final TokenList qnames = new TokenList();

  /**
   * Constructor.
   * @param names names and namespace uris
   */
  public IndexNames(final String names) {
    final HashSet<String> inc = toSet(names.trim());
    for(final String entry : inc) {
      // global wildcard: ignore all assignments
      if(entry.equals("*") || entry.equals("*:*")) {
        qnames.reset();
        return;
      }

      final String uri, ln;
      final Matcher m = QNm.EQNAME.matcher(entry);
      if(m.find()) { // Q{uri}name, Q{uri}*
        uri = m.group(1);
        ln = m.group(2).equals("*") ? null : m.group(2);
      } else if(entry.startsWith("*:")) { // *:name
        uri = null;
        ln = entry.substring(2);
      } else { // name
        uri = "";
        ln = entry;
      }
      qnames.add(ln == null ? null : token(ln));
      qnames.add(uri == null ? null : token(uri));
    }
  }

  /**
   * Checks if the list of names is empty.
   * @return result of check
   */
  public boolean isEmpty() {
    return qnames.isEmpty();
  }

  /**
   * Checks if the name of the addressed database entry is to be indexed.
   * @param data data reference
   * @param pre pre value
   * @param text text flag
   * @return result of check
   */
  public boolean contains(final Data data, final int pre, final boolean text) {
    return contains(text ? data.qname(data.parent(pre, Data.TEXT), Data.ELEM) :
      data.qname(pre, Data.ATTR));
  }

  /**
   * Checks if the specified name is to be indexed.
   * @param qname local name and namespace uri
   * @return result of check
   */
  public boolean contains(final byte[][] qname) {
    if(isEmpty()) return true;

    final int ns = qnames.size();
    if(qname != null) {
      for(int n = 0; n < ns; n += 2) {
        final byte[] ln = qnames.get(n), uri = qnames.get(n + 1);
        if(ln != null && !eq(qname[0], ln)) continue;
        if(uri != null && !eq(qname[1], uri)) continue;
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a set of all entries of the requested string (separated by commas).
   * @param names names
   * @return map
   */
  private HashSet<String> toSet(final String names) {
    final HashSet<String> set = new HashSet<>();
    final StringBuilder value = new StringBuilder();
    final int sl = names.length();
    for(int s = 0; s < sl; s++) {
      final char ch = names.charAt(s);
      if(ch == ',') {
        if(s + 1 == sl || names.charAt(s + 1) != ',') {
          if(value.length() != 0) {
            set.add(value.toString().trim());
            value.setLength(0);
          }
          continue;
        }
        // literal commas are escaped by a second comma
        s++;
      }
      value.append(ch);
    }
    if(value.length() != 0) set.add(value.toString().trim());
    return set;
  }
}
