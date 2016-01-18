package org.basex.index;

import static org.basex.util.Token.*;

import java.util.*;
import java.util.regex.*;

import org.basex.data.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Names and namespace uris of elements/attribute to index.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class IndexNames {
  /** Local names and namespace uris. All names are accepted if the list is empty. */
  private final Atts qnames = new Atts();

  /**
   * Constructor.
   * @param type index type
   * @param data data reference
   */
  public IndexNames(final IndexType type, final Data data) {
    final String names;
    switch(type) {
      case TEXT: names = data.meta.textinclude; break;
      case ATTRIBUTE: names = data.meta.attrinclude; break;
      case TOKEN: names = data.meta.tokeninclude; break;
      case FULLTEXT: names = data.meta.ftinclude; break;
      default: throw Util.notExpected();
    }

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
      } else if(XMLToken.isNCName(token(entry))) { // name
        uri = "";
        ln = entry;
      } else { // invalid
        Util.debug("Included name is invalid: %", entry);
        continue;
      }
      qnames.add(ln == null ? null : token(ln), uri == null ? null : token(uri));
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
    final byte[][] qname = text ? data.qname(data.parent(pre, Data.TEXT), Data.ELEM) :
      data.qname(pre, Data.ATTR);
    qname[0] = local(qname[0]);
    return contains(qname);
  }

  /**
   * Checks if the specified name is an index candidate.
   * @param qname local name and namespace uri (reference or array entries can be {@code null})
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

  /**
   * Checks if the name index contains id or idref names, or if it is empty.
   * @param idref idref flag
   * @return result of check
   */
  public boolean containsIds(final boolean idref) {
    // flawed... the correct solution would be to store all id/idref attributes as DB meta data
    final int ns = qnames.size();
    for(int n = 0; n < ns; n++) {
      final byte[] name = qnames.name(n);
      if(name == null || (idref ? Token.contains(name, IDREF) :
        Token.contains(name, ID) && !Token.contains(name, IDREF))) return true;
    }
    return isEmpty();
  }
}
