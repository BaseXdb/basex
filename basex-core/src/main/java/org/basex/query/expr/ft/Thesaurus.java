package org.basex.query.expr.ft;

import static org.basex.util.Token.*;

import java.util.function.*;

import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Thesaurus structure.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Thesaurus {
  /** Element name: entry. */
  private static final byte[] ENTRY = token("entry");
  /** Element name: relationship. */
  private static final byte[] RELATIONSHIP = token("relationship");
  /** Element name: synonym. */
  private static final byte[] SYNONYM = token("synonym");
  /** Element name: term. */
  private static final byte[] TERM = token("term");

  /** Map with thesaurus entries. */
  private final TokenObjectMap<ThesEntry> entries = new TokenObjectMap<>();
  /** Relationships. */
  private static final TokenObjectMap<byte[]> RSHIPS = new TokenObjectMap<>();

  static {
    RSHIPS.put(token("NT"), token("BT"));
    RSHIPS.put(token("BT"), token("BT"));
    RSHIPS.put(token("BTG"), token("NTG"));
    RSHIPS.put(token("NTG"), token("BTG"));
    RSHIPS.put(token("BTP"), token("NTP"));
    RSHIPS.put(token("NTP"), token("BTP"));
    RSHIPS.put(token("USE"), token("UF"));
    RSHIPS.put(token("UF"), token("USE"));
    RSHIPS.put(token("RT"), token("RT"));
  }

  /**
   * Constructor.
   * @param root thesaurus root node
   */
  public Thesaurus(final XNode root) {
    for(final XNode entry : elements(root, ENTRY, true)) build(entry);
  }

  /**
   * Returns a thesaurus entry for the specified term.
   * @param term term
   * @return node or {@code null}
   */
  ThesEntry get(final byte[] term) {
    return entries.get(term);
  }

  /**
   * Populates the thesaurus.
   * @param entry thesaurus entry
   */
  private void build(final XNode entry) {
    final Function<XNode, ThesEntry> find = node -> {
      final byte[] term = value(node, TERM);
      return entries.computeIfAbsent(term, () -> new ThesEntry(term));
    };

    final ThesEntry term = find.apply(entry);
    for(final XNode synonym : elements(entry, SYNONYM, false)) {
      final ThesEntry syn = find.apply(synonym);
      final byte[] value = value(synonym, RELATIONSHIP);
      term.add(syn, value);

      final byte[] rship = RSHIPS.get(value);
      if(rship != null) syn.add(term, rship);
      build(synonym);
    }
  }

  /**
   * Returns child/descendant elements with the specified name.
   * @param node node to start from
   * @param name name of elements to find
   * @param desc return children or descendants
   * @return resulting elements
   */
  private static ANodeList elements(final XNode node, final byte[] name, final boolean desc) {
    final ANodeList list = new ANodeList();
    for(final XNode element : desc ? node.descendantIter(false) : node.childIter()) {
      if(element.type == NodeType.ELEMENT && eq(element.qname().local(), name))
        list.add(element);
    }
    return list;
  }

  /**
   * Returns the string value of a child element with the specified name.
   * @param node node to start from
   * @param name name of child element
   * @return value or empty string
   */
  private static byte[] value(final XNode node, final byte[] name) {
    final ANodeList elements = elements(node, name, false);
    return elements.isEmpty() ? EMPTY : elements.peek().string();
  }
}
