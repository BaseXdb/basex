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
 * @author BaseX Team 2005-22, BSD License
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
  private final TokenObjMap<ThesEntry> entries = new TokenObjMap<>();
  /** Relationships. */
  private static final TokenMap RSHIPS = new TokenMap();

  static {
    RSHIPS.put("NT", "BT");
    RSHIPS.put("BT", "BT");
    RSHIPS.put("BTG", "NTG");
    RSHIPS.put("NTG", "BTG");
    RSHIPS.put("BTP", "NTP");
    RSHIPS.put("NTP", "BTP");
    RSHIPS.put("USE", "UF");
    RSHIPS.put("UF", "USE");
    RSHIPS.put("RT", "RT");
  }

  /**
   * Constructor.
   * @param root thesaurus root node
   */
  public Thesaurus(final ANode root) {
    for(final ANode entry : elements(root, ENTRY, true)) build(entry);
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
  private void build(final ANode entry) {
    final Function<ANode, ThesEntry> find = node -> {
      final byte[] term = value(node, TERM);
      return entries.computeIfAbsent(term, () -> new ThesEntry(term));
    };

    final ThesEntry term = find.apply(entry);
    for(final ANode synonym : elements(entry, SYNONYM, false)) {
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
  private static ANodeList elements(final ANode node, final byte[] name, final boolean desc) {
    final ANodeList list = new ANodeList();
    for(final ANode element : desc ? node.descendantIter() : node.childIter()) {
      if(element.type == NodeType.ELEMENT && eq(element.qname().local(), name))
        list.add(element.finish());
    }
    return list;
  }

  /**
   * Returns the string value of a child element with the specified name.
   * @param node node to start from
   * @param name name of child element
   * @return value or empty string
   */
  private static byte[] value(final ANode node, final byte[] name) {
    final ANodeList elements = elements(node, name, false);
    return elements.isEmpty() ? EMPTY : elements.peek().string();
  }
}
