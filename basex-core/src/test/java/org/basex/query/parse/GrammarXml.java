package org.basex.query.parse;

import java.nio.file.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

/**
 * Answers the questions about the XML form of the grammar that are needed for the review of
 * {@link FoldPolicy}. The XML form was created by REx; recreate it by
 * {@code rex -remake XQuerySyntaxParser.xml}. This class relies on the JDK only, so that it works
 * even if the parsers that are generated from the grammar are out of date.
 *
 * <p>A production is <i>transparent</i> if each of its alternatives has exactly one required
 * item, which is a reference or a terminal; everything else in the alternative is optional or
 * repeated. Lexical productions are no candidates, and neither are productions with a single
 * alternative and no optional parts: these are role wrappers such as {@code QueryBody}. A
 * transparent production is <i>foldable</i> if it can be reached from the {@link #SEEDS} via
 * required references, and if it is no {@link #STOPS}.</p>
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
final class GrammarXml {
  /** Namespace of the XML form. */
  private static final String NS = "http://www.w3.org/2001/03/XPath/grammar";
  /**
   * Start of the transparent chains: expressions, full-text selections, constructor content,
   * FLWOR clauses and bindings, setters and imports.
   */
  private static final List<String> SEEDS = List.of("Expr", "FTSelection", "DirElemContent",
      "QuotAttrValueContent", "AposAttrValueContent", "InitialClause", "IntermediateClause",
      "ForBinding", "LetBinding", "Setter", "Import");
  /** Transparent productions that stay visible: consumers want to know about constructors. */
  private static final List<String> STOPS = List.of("NodeConstructor", "DirectConstructor",
      "ComputedConstructor", "FunctionItemExpr", "ArrayConstructor");

  /** Productions by name, in the order of the grammar. */
  private final Map<String, List<Element>> productions = new LinkedHashMap<>();

  /**
   * Constructor.
   * @param file XML form of the grammar
   * @throws Exception exception
   */
  GrammarXml(final Path file) throws Exception {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    final Document doc = dbf.newDocumentBuilder().parse(file.toFile());
    for(final Element production : elements(doc.getDocumentElement())) {
      if(!is(production, "production")) continue;
      productions.computeIfAbsent(production.getAttribute("name"), k -> new ArrayList<>()).
        add(production);
    }
  }

  /**
   * Returns the names of the productions.
   * @return names
   */
  Set<String> names() {
    return new TreeSet<>(productions.keySet());
  }

  /**
   * Returns the left-recursive productions: those that contain a sequence that starts with a
   * reference to the production itself.
   * @return names
   */
  Set<String> leftRecursive() {
    final Set<String> names = new TreeSet<>();
    for(final List<Element> list : productions.values()) {
      for(final Element production : list) {
        final String name = production.getAttribute("name");
        final NodeList sequences = production.getElementsByTagNameNS(NS, "sequence");
        for(int s = 0; s < sequences.getLength(); s++) {
          final List<Element> items = elements((Element) sequences.item(s));
          if(!items.isEmpty() && is(items.get(0), "ref") &&
              items.get(0).getAttribute("name").equals(name)) names.add(name);
        }
      }
    }
    return names;
  }

  /**
   * Returns the nonterminals that are folded.
   * @return names
   */
  Set<String> foldable() {
    // transparent productions, with the nonterminals that their alternatives require
    final Map<String, List<String>> units = new HashMap<>();
    for(final Map.Entry<String, List<Element>> entry : productions.entrySet()) {
      final List<List<Element>> alternatives = new ArrayList<>();
      boolean lexical = false;
      for(final Element production : entry.getValue()) {
        lexical |= production.hasAttribute("whitespace-spec");
        alternatives.addAll(alternatives(production));
      }
      // a single alternative without optional parts only names a role
      if(lexical || !alternatives.stream().allMatch(GrammarXml::unit) ||
          alternatives.size() == 1 && alternatives.get(0).stream().noneMatch(
              item -> is(item, "optional", "zeroOrMore"))) continue;
      final List<String> refs = new ArrayList<>();
      for(final List<Element> alternative : alternatives) {
        for(final Element item : alternative) {
          if(is(item, "ref")) refs.add(item.getAttribute("name"));
        }
      }
      units.put(entry.getKey(), refs);
    }

    // follow the required nonterminals until nothing is added
    final Set<String> reached = new TreeSet<>();
    for(final String seed : SEEDS) {
      if(!units.containsKey(seed)) {
        throw new IllegalStateException("Seed is not a transparent production: " + seed);
      }
      reached.add(seed);
    }
    for(boolean added = true; added;) {
      added = false;
      for(final String name : new ArrayList<>(reached)) {
        for(final String ref : units.get(name)) {
          if(units.containsKey(ref)) added |= reached.add(ref);
        }
      }
    }
    for(final String stop : STOPS) {
      if(!reached.contains(stop)) {
        throw new IllegalStateException("Stop is not reached from the seeds: " + stop);
      }
    }
    reached.removeAll(STOPS);
    return reached;
  }

  /**
   * Returns the serialized productions of nonterminals. Whitespace between elements and
   * comments are not significant.
   * @param names names of the nonterminals
   * @return serialized productions by name; names that are no productions are missing
   */
  Map<String, String> signatures(final Collection<String> names) {
    final Map<String, String> signatures = new TreeMap<>();
    for(final String name : names) {
      final List<Element> list = productions.get(name);
      if(list == null) continue;
      final StringBuilder sb = new StringBuilder();
      for(final Element production : list) serialize(production, sb);
      signatures.put(name, sb.toString());
    }
    return signatures;
  }

  /**
   * Returns the alternatives of a production.
   * @param production production
   * @return one list per alternative, containing its items
   */
  private static List<List<Element>> alternatives(final Element production) {
    final List<Element> body = elements(production);
    if(body.size() != 1 || !is(body.get(0), "choice")) return List.of(body);
    final List<List<Element>> alternatives = new ArrayList<>();
    for(final Element item : elements(body.get(0))) {
      alternatives.add(is(item, "sequence") ? elements(item) : List.of(item));
    }
    return alternatives;
  }

  /**
   * Checks if an alternative has exactly one required item, which is a reference or a terminal.
   * @param alternative alternative
   * @return result of check
   */
  private static boolean unit(final List<Element> alternative) {
    final List<Element> required = alternative.stream().
        filter(item -> !is(item, "optional", "zeroOrMore")).toList();
    return required.size() == 1 && is(required.get(0), "ref", "string");
  }

  /**
   * Serializes an element in a canonical form: sorted attributes, without insignificant text.
   * @param element element
   * @param sb string builder
   */
  private static void serialize(final Element element, final StringBuilder sb) {
    sb.append('<').append(element.getLocalName());
    final NamedNodeMap attributes = element.getAttributes();
    final TreeMap<String, String> sorted = new TreeMap<>();
    for(int a = 0; a < attributes.getLength(); a++) {
      final Node attribute = attributes.item(a);
      if(!attribute.getNodeName().startsWith("xmlns")) {
        sorted.put(attribute.getNodeName(), attribute.getNodeValue());
      }
    }
    sorted.forEach((k, v) -> sb.append(' ').append(k).append("=\"").append(escape(v)).append('"'));
    sb.append('>');
    for(Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
      if(child instanceof final Element e) {
        serialize(e, sb);
      } else if(child instanceof final Text text && !text.getData().isBlank()) {
        sb.append(escape(text.getData()));
      }
    }
    sb.append("</").append(element.getLocalName()).append('>');
  }

  /**
   * Escapes a text.
   * @param text text
   * @return escaped text
   */
  private static String escape(final String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
  }

  /**
   * Returns the child elements of an element.
   * @param parent parent
   * @return child elements
   */
  private static List<Element> elements(final Element parent) {
    final List<Element> list = new ArrayList<>();
    for(Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
      if(child instanceof final Element element) list.add(element);
    }
    return list;
  }

  /**
   * Checks if an element has one of the specified local names.
   * @param element element
   * @param names names
   * @return result of check
   */
  private static boolean is(final Element element, final String... names) {
    return Arrays.asList(names).contains(element.getLocalName());
  }
}
