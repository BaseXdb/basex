package org.basex.query.util.ft.thesaurus;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Thesaurus structure: concepts with labels and relationships.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Thesaurus implements ThesSource {
  /** Alternative names of relationships. */
  private static final TokenObjectMap<byte[]> ALIASES = new TokenObjectMap<>();

  static {
    ALIASES.put(token("broader"), token("BT"));
    ALIASES.put(token("narrower"), token("NT"));
    ALIASES.put(token("related"), token("RT"));
  }

  /** Normalized labels. */
  private final TokenSet keys;
  /** Original labels. */
  private final TokenList terms;
  /** Names of relationships. */
  private final TokenSet relations;
  /** Concepts of the labels: offsets and ids. */
  private final int[][] labelConcepts;
  /** Labels of the concepts: offsets and ids. */
  private final int[][] conceptLabels;
  /** Relationships of the concepts: offsets and pairs of ids. */
  private final int[][] conceptRelations;

  /**
   * Constructor.
   * @param opt full-text options for normalizing terms
   * @param roots nodes containing thesauri
   */
  public Thesaurus(final FTOpt opt, final Value roots) {
    final ThesBuilder builder = new ThesBuilder(new FTLexer(opt));
    final EntryReader entries = new EntryReader(builder);
    final SkosReader skos = new SkosReader(builder, opt.ln);
    final RdfXml rdf = new RdfXml(skos);
    for(final Item root : roots) read((GNode) root, entries, rdf);
    skos.finish();

    keys = builder.keys;
    terms = builder.terms;
    relations = builder.relations;
    conceptLabels = csr(builder.labels);
    conceptRelations = csr(builder.related);
    final ArrayList<IntList> lc = new ArrayList<>();
    for(int l = terms.size(); l > 0; l--) lc.add(new IntList(1));
    final int cl = builder.labels.size();
    for(int c = 0; c < cl; c++) {
      final IntList list = builder.labels.get(c);
      for(int l = 0; l < list.size(); l++) lc.get(list.get(l)).add(c);
    }
    labelConcepts = csr(lc);
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
   * Returns the name of a relationship, resolving alternative names.
   * @param name name
   * @return resolved name
   */
  public static byte[] relationship(final byte[] name) {
    final byte[] alias = ALIASES.get(name);
    return alias != null ? alias : name;
  }

  /**
   * Returns the number of concepts.
   * @return number of concepts
   */
  public int conceptCount() {
    return conceptLabels[0].length - 1;
  }

  /**
   * Returns the normalized labels, ordered by their ids.
   * @return normalized labels
   */
  public byte[][] keys() {
    return keys.keys();
  }

  /**
   * Returns the names of the relationships.
   * @return names
   */
  public TokenSet relations() {
    return relations;
  }

  @Override
  public int label(final byte[] key) {
    return keys.index(key) - 1;
  }

  @Override
  public byte[] term(final int label) {
    return terms.get(label);
  }

  @Override
  public int[] concepts(final int label) {
    return slice(labelConcepts, label);
  }

  @Override
  public int[] labels(final int concept) {
    return slice(conceptLabels, concept);
  }

  @Override
  public int[] relations(final int concept) {
    return slice(conceptRelations, concept);
  }

  @Override
  public int relation(final byte[] name) {
    return relations.index(relationship(name));
  }

  /**
   * Converts lists to compressed rows.
   * @param lists lists
   * @return offsets and values
   */
  private static int[][] csr(final ArrayList<IntList> lists) {
    final int ls = lists.size();
    final int[] offsets = new int[ls + 1];
    final IntList values = new IntList();
    for(int l = 0; l < ls; l++) {
      values.add(lists.get(l).toArray());
      offsets[l + 1] = values.size();
    }
    return new int[][] { offsets, values.finish() };
  }

  /**
   * Returns the values of a compressed row.
   * @param csr offsets and values
   * @param row row
   * @return values
   */
  private static int[] slice(final int[][] csr, final int row) {
    return Arrays.copyOfRange(csr[1], csr[0][row], csr[0][row + 1]);
  }

  /**
   * Reads the entries and RDF documents of a node and its descendants.
   * @param node node
   * @param entries reader for entries
   * @param rdf reader for RDF documents
   */
  private static void read(final GNode node, final EntryReader entries, final RdfXml rdf) {
    if(node.kind() == Kind.ELEMENT) {
      if(EntryReader.root(node)) {
        entries.entry(node);
        return;
      }
      if(RdfXml.root(node)) {
        rdf.parse((XNode) node);
        return;
      }
    }
    for(final GNode child : node.childIter()) {
      if(child.kind() == Kind.ELEMENT) read(child, entries, rdf);
    }
  }
}
