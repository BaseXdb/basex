package org.basex.query.util.ft.thesaurus;

import static org.basex.util.Token.*;

import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Reader for SKOS thesauri: collects concepts, labels and relationships from RDF triples.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SkosReader implements RdfXml.Handler {
  /** SKOS namespace URI. */
  private static final String SKOS_URI = "http://www.w3.org/2004/02/skos/core#";
  /** SKOS: concept. */
  private static final byte[] CONCEPT = token(SKOS_URI + "Concept");
  /** SKOS: labels. */
  private static final TokenSet LABELS = new TokenSet(SKOS_URI + "prefLabel",
    SKOS_URI + "altLabel", SKOS_URI + "hiddenLabel");
  /** SKOS: direct semantic relationships and their names (transitive ones are derived). */
  private static final TokenObjectMap<byte[]> RELATIONS = new TokenObjectMap<>();

  static {
    final String[] names = { "broader", "BT", "broadMatch", "BT", "narrower", "NT",
      "narrowMatch", "NT", "related", "RT", "relatedMatch", "RT" };
    for(int n = 0; n < names.length; n += 2) {
      RELATIONS.put(token(SKOS_URI + names[n]), token(names[n + 1]));
    }
  }

  /** Builder for the thesaurus structure. */
  private final ThesBuilder builder;
  /** Language of labels (can be {@code null}). */
  private final Language language;
  /** Concepts. */
  private final TokenSet concepts = new TokenSet();
  /** Labels: subjects and labels. */
  private final TokenList labels = new TokenList();
  /** Relationships: subjects, names and objects. */
  private final TokenList relations = new TokenList();

  /**
   * Constructor.
   * @param builder builder for the thesaurus structure
   * @param language language of labels (can be {@code null})
   */
  SkosReader(final ThesBuilder builder, final Language language) {
    this.builder = builder;
    this.language = language;
  }

  @Override
  public void resource(final byte[] subject, final byte[] predicate, final byte[] object) {
    if(eq(predicate, RdfXml.TYPE)) {
      if(eq(object, CONCEPT)) concepts.add(subject);
    } else {
      // subjects and objects of semantic relationships are concepts
      final byte[] name = RELATIONS.get(predicate);
      if(name == null) return;
      concepts.add(subject);
      concepts.add(object);
      relations.add(subject, name, object);
    }
  }

  @Override
  public void literal(final byte[] subject, final byte[] predicate, final byte[] value,
      final byte[] lang) {
    // labels without language or in the requested language
    if(LABELS.contains(predicate) && (language == null || lang.length == 0 ||
        language.equals(Language.get(string(lang))))) labels.add(subject, value);
  }

  /**
   * Adds the collected concepts to the thesaurus structure.
   */
  void finish() {
    // ids of the concept set start with 1
    final int first = builder.concepts(concepts.size()) - 1;
    for(int l = 0; l < labels.size(); l += 2) {
      final int c = concepts.index(labels.get(l));
      if(c != 0) builder.label(first + c, labels.get(l + 1));
    }
    for(int r = 0; r < relations.size(); r += 3) {
      final int s = concepts.index(relations.get(r)), o = concepts.index(relations.get(r + 2));
      if(s != 0 && o != 0) builder.relate(first + s, relations.get(r + 1), first + o);
    }
  }
}
