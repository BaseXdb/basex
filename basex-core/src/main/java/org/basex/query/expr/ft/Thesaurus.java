package org.basex.query.expr.ft;

import static org.basex.util.Token.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.ft.*;
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
  /** Relationship between equivalent terms. */
  public static final byte[] EQ = token("EQ");

  /** Map with thesaurus entries. */
  private final TokenObjectMap<ThesEntry> entries = new TokenObjectMap<>();
  /** Groups of equivalent terms. */
  private final ArrayList<ThesEntry[]> groups = new ArrayList<>();
  /** Relationships. */
  private static final TokenObjectMap<byte[]> RSHIPS = new TokenObjectMap<>();

  static {
    RSHIPS.put(token("NT"), token("BT"));
    RSHIPS.put(token("BT"), token("NT"));
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
   * @param opt full-text options for normalizing terms
   * @param roots thesaurus root nodes
   */
  public Thesaurus(final FTOpt opt, final XNode... roots) {
    final FTLexer lexer = new FTLexer(opt);
    for(final XNode root : roots) {
      for(final GNode entry : elements(root, ENTRY, true)) build(entry, lexer);
    }
  }

  /**
   * Normalizes a term.
   * @param term term
   * @param lexer lexer
   * @return normalized term
   */
  public static byte[] normalize(final byte[] term, final FTLexer lexer) {
    final TokenBuilder tb = new TokenBuilder();
    lexer.init(term);
    while(lexer.hasNext()) {
      if(!tb.isEmpty()) tb.add(' ');
      tb.add(lexer.nextToken());
    }
    return tb.finish();
  }

  /**
   * Returns all normalized terms.
   * @return normalized terms
   */
  public byte[][] keys() {
    return entries.keys();
  }

  /**
   * Returns the term for the specified normalized term.
   * @param key normalized term
   * @return term
   */
  public byte[] term(final byte[] key) {
    return entries.get(key).term;
  }

  /**
   * Passes the synonyms of a term and their relationships to the specified action.
   * @param key normalized term
   * @param action action, receiving normalized synonym and relationship
   */
  public void synonyms(final byte[] key, final BiConsumer<byte[], byte[]> action) {
    final ThesEntry entry = entries.get(key);
    for(int n = 0; n < entry.size; n++) action.accept(entry.synonyms[n].key, entry.relations[n]);
  }

  /**
   * Returns the groups of equivalent terms.
   * @return normalized terms of each group
   */
  public byte[][][] groups() {
    final int gs = groups.size();
    final byte[][][] keys = new byte[gs][][];
    for(int g = 0; g < gs; g++) {
      final ThesEntry[] group = groups.get(g);
      final int gl = group.length;
      keys[g] = new byte[gl][];
      for(int m = 0; m < gl; m++) keys[g][m] = group[m].key;
    }
    return keys;
  }

  /**
   * Returns a thesaurus entry for the specified normalized term.
   * @param key normalized term
   * @return node or {@code null}
   */
  ThesEntry get(final byte[] key) {
    return entries.get(key);
  }

  /**
   * Populates the thesaurus.
   * @param entry thesaurus entry
   * @param lexer lexer for normalizing terms
   */
  private void build(final GNode entry, final FTLexer lexer) {
    // terms without tokens are skipped
    final Function<GNode, ThesEntry> find = node -> {
      final byte[] term = value(node, TERM), key = normalize(term, lexer);
      return key.length == 0 ? null : entries.computeIfAbsent(key, () -> new ThesEntry(term, key));
    };

    final ThesEntry term = find.apply(entry);
    final ArrayList<ThesEntry> group = new ArrayList<>();
    for(final GNode synonym : elements(entry, SYNONYM, false)) {
      final ThesEntry syn = find.apply(synonym);
      if(term != null && syn != null) {
        final byte[] value = value(synonym, RELATIONSHIP);
        if(eq(value, EQ)) {
          // equivalent terms: all terms of the group are related to each other
          group.add(syn);
        } else {
          term.add(syn, value);
          final byte[] rship = RSHIPS.get(value);
          if(rship != null) syn.add(term, rship);
        }
      }
      build(synonym, lexer);
    }
    if(!group.isEmpty()) {
      group.add(term);
      final ThesEntry[] members = group.toArray(ThesEntry[]::new);
      for(final ThesEntry member : members) member.add(members);
      groups.add(members);
    }
  }

  /**
   * Returns child/descendant elements with the specified name.
   * @param node node to start from
   * @param name name of elements to find
   * @param desc return children or descendants
   * @return resulting elements
   */
  private static GNodeList elements(final GNode node, final byte[] name, final boolean desc) {
    final GNodeList list = new GNodeList();
    for(final GNode element : desc ? node.descendantIter(false) : node.childIter()) {
      if(element.kind() == Kind.ELEMENT && element.qname().eqLocal(name))
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
  private static byte[] value(final GNode node, final byte[] name) {
    final GNodeList elements = elements(node, name, false);
    return elements.isEmpty() ? EMPTY : elements.peek().string();
  }
}
