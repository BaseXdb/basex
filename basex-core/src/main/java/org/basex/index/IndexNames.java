package org.basex.index;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Names and namespace URIs of elements/attribute to index.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IndexNames {
  /** Local names and namespace URIs. All names are accepted if the list is empty. */
  private final Atts qnames = new Atts();
  /** Data reference. */
  private final Data data;

  /**
   * Constructor.
   * @param type index type
   * @param data data reference
   */
  public IndexNames(final IndexType type, final Data data) {
    this.data = data;
    final String names = data.meta.names(type);
    final HashSet<String> inc = toSet(names.trim());
    for(final String entry : inc) {
      // global wildcard: ignore all assignments
      if(entry.equals("*") || entry.equals("*:*")) {
        qnames.reset();
        return;
      }

      final byte[] name, uri;
      final byte[][] parsed = QNm.parseExpanded(token(entry), true);
      if(parsed != null) { // Q{uri}name, Q{uri}*
        name = parsed[0];
        uri = parsed[1];
      } else if(entry.startsWith("*:")) { // *:name
        name = token(entry.substring(2));
        uri = null;
      } else if(XMLToken.isNCName(token(entry))) { // name
        name = token(entry);
        uri = EMPTY;
      } else { // invalid
        Util.debugln("Included name is invalid: %", entry);
        continue;
      }
      qnames.add(name == null ? null : name, uri == null ? null : uri);
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
   * @param pre PRE value
   * @param text text flag
   * @return result of check
   */
  public boolean contains(final int pre, final boolean text) {
    final byte[][] qname = text ? data.qname(data.parent(pre, Data.TEXT), Data.ELEM) :
      data.qname(pre, Data.ATTR);
    qname[0] = local(qname[0]);
    return contains(qname);
  }

  /**
   * Checks if the specified name is an index candidate.
   * @param qname local name and namespace URI (reference or array entries can be {@code null})
   * @return result of check
   */
  public boolean contains(final byte[][] qname) {
    if(isEmpty()) return true;

    if(qname != null) {
      final int ns = qnames.size();
      final byte[] ln = qname[0], uri = qname[1];
      for(int n = 0; n < ns; n++) {
        final byte[] iln = qnames.name(n);
        if(iln != null && (ln == null || !eq(ln, iln))) continue;
        final byte[] iuri = qnames.value(n);
        if(iuri != null && (uri == null || !eq(uri, iuri))) continue;
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
  private static HashSet<String> toSet(final String names) {
    final HashSet<String> set = new HashSet<>();
    final StringBuilder value = new StringBuilder();
    final int sl = names.length();
    for(int s = 0; s < sl; s++) {
      final char ch = names.charAt(s);
      if(ch == ',') {
        if(s + 1 == sl || names.charAt(s + 1) != ',') {
          if(!value.isEmpty()) {
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
    if(!value.isEmpty()) set.add(value.toString().trim());
    return set;
  }

  /**
   * Checks if the index names contain all relevant ID or IDREF attributes.
   * @param idref IDREF flag
   * @return result of check
   */
  public boolean containsIds(final boolean idref) {
    // no entries: all attributes are indexed
    if(isEmpty()) return true;
    // currently no support for documents with namespaces (cannot be resolved from name index)
    if(data.nspaces.isEmpty()) return false;
    // find ID candidates
    final TokenSet names = new TokenSet();
    final int ns = qnames.size();
    for(int n = 0; n < ns; n++) {
      final byte[] name = qnames.name(n);
      if(name != null && XMLToken.isId(name, idref)) names.add(name);
    }
    // check if database name index consists of other IDs
    for(final byte[] name : data.attrNames) {
      if(XMLToken.isId(name, idref && !names.contains(name))) return false;
    }
    return true;
  }
}
