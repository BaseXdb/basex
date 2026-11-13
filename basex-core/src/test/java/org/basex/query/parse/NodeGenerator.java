package org.basex.query.parse;

import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

/**
 * Generates the node classes of the syntax tree and the factory that creates them from the
 * XML form of the grammar. Run the main method in the {@code basex-core} directory after the
 * parser was regenerated, or call {@link #generate()} to compare the result with the checked-in
 * files (see {@code NodeGeneratorTest}).
 * <p>A record is generated for each nonterminal. Its components are derived from the production:
 * </p>
 * <ul>
 * <li>A symbol that occurs at most once (or twice) becomes a component with a type, which is
 *   the record of the symbol, {@code Terminal} for tokens that carry a value, or {@code Node}
 *   for symbols that may be replaced by one of their children (see below).</li>
 * <li>An optional keyword or operator becomes a boolean component, a mandatory one is omitted.
 *   </li>
 * <li>Symbols that occur in a repetition (and operators that vary within it) are collected in
 *   the array {@code children}, in document order.</li>
 * <li>The nodes of delimiters and whitespace are dropped, as defined by {@link FoldPolicy}.</li>
 * </ul>
 * <p>A nonterminal that always has exactly one child is replaced by the child. This is also done
 * for the nonterminals in {@link FoldPolicy#NAMES}, if they have one child only. Such symbols are
 * referred to as {@code Node}.</p>
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class NodeGenerator {
  /** Directory of the generated classes. */
  static final Path DIR = Paths.get("src/main/java/org/basex/query/parse/gen");
  /** Generated records. */
  static final Path NODES = DIR.resolve("Nodes.java");
  /** Generated factory. */
  static final Path FACTORY = DIR.resolve("NodeFactory.java");
  /** XML form of the grammar. */
  private static final Path GRAMMAR = Paths.get(
      "src/test/resources/org/basex/query/parse/XQuerySyntaxParser.xml");
  /** Infinite number of occurrences. */
  private static final int INF = 1_000_000;
  /** Nonterminals that are collapsed to terminals by the builder. */
  private static final Set<String> COLLAPSED = Set.of("NCName", "QName", "UnreservedFunctionQName");
  /** Keywords that are kept as components, as the parser refers to their positions. */
  private static final Set<String> POSITIONED = Set.of("'in'");
  /** Maximum length of the lines of the generated sources. */
  private static final int MAX_LINE = 100;
  /** Choices with more alternatives than this number get no interface. */
  private static final int MAX_ALTERNATIVES = 12;
  /** Choices with more alternatives than this number offer only the components that all have. */
  private static final int MAX_UNION = 4;
  /** Names that cannot be used for components. */
  private static final Set<String> RESERVED = Set.of("begin", "end", "children", "hashCode",
      "toString", "equals", "getClass", "notify", "notifyAll", "wait");

  /** Regular expression of a production. */
  private sealed interface Rx { }
  /**
   * Sequence.
   * @param items items
   */
  private record Seq(List<Rx> items) implements Rx { }
  /**
   * Alternatives.
   * @param alts alternatives
   */
  private record Alt(List<Rx> alts) implements Rx { }
  /**
   * Optional.
   * @param body optional expression
   */
  private record Opt(Rx body) implements Rx { }
  /**
   * Repetition.
   * @param body repeated expression
   * @param plus at least one repetition
   */
  private record Rep(Rx body, boolean plus) implements Rx { }
  /**
   * Use of a symbol.
   * @param sym symbol
   */
  private record Use(Symbol sym) implements Rx { }

  /**
   * Occurrences of a symbol.
   * @param min minimum
   * @param max maximum
   */
  private record Occ(int min, int max) { }

  /**
   * Component of a record.
   * @param name name
   * @param type type
   * @param sym symbol
   * @param token keyword or operator, which is represented by a flag
   * @param ordinal position among the components with the same symbol
   */
  private record Slot(String name, String type, Symbol sym, boolean token, int ordinal) { }

  /** Class description. */
  private static final class Shape {
    /** Symbol. */
    final Symbol sym;
    /** Production. */
    final Rx rx;
    /** Typed components. */
    final List<Slot> slots = new ArrayList<>();
    /** Symbols that are collected in the children array. */
    final Set<Symbol> members = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param sym symbol
     * @param rx production
     */
    Shape(final Symbol sym, final Rx rx) {
      this.sym = sym;
      this.rx = rx;
    }
  }

  /** Productions by name. */
  private final Map<String, Element> productions = new HashMap<>();
  /** Delimiters. */
  private final Set<Symbol> delimiters = new TreeSet<>();
  /** Nonterminals with exactly one child. */
  private final Set<Symbol> wrappers = new TreeSet<>();
  /** Nonterminals that are replaced by their only child. */
  private final Set<Symbol> folds = new TreeSet<>();
  /** Choices with an interface for their common components. */
  private final Set<Symbol> viewSyms = new TreeSet<>();
  /** Shapes of the nonterminals with a record. */
  private final Map<Symbol, Shape> shapes = new LinkedHashMap<>();

  /** Private constructor. */
  private NodeGenerator() { }

  /**
   * Main method.
   * @param args not used
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    final Map<Path, String> files = generate();
    Files.createDirectories(DIR);
    for(final Map.Entry<Path, String> entry : files.entrySet()) {
      Files.writeString(entry.getKey(), entry.getValue().replace("\n", System.lineSeparator()),
          StandardCharsets.UTF_8);
    }
  }

  /**
   * Generates the sources.
   * @return sources by path, with Unix line endings
   * @throws Exception exception
   */
  static Map<Path, String> generate() throws Exception {
    return new NodeGenerator().run();
  }

  /**
   * Generates the sources.
   * @return sources by path
   * @throws Exception exception
   */
  private Map<Path, String> run() throws Exception {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    final Document doc = dbf.newDocumentBuilder().parse(GRAMMAR.toFile());
    final NodeList list = doc.getElementsByTagNameNS("*", "production");
    for(int i = 0; i < list.getLength(); i++) {
      final Element p = (Element) list.item(i);
      productions.put(p.getAttribute("name"), p);
    }
    for(final String text : FoldPolicy.DELIMITERS) delimiters.add(terminal(quote(text)));

    // nonterminals that always have exactly one child are replaced by it
    for(final Symbol sym : Symbol.values()) {
      if(sym.isTerminal() || sym.internal() || sym == Symbol.UNRESERVED_FUNCTION_EQNAME ||
          sym == Symbol.UNRESERVED_FUNCTION_QNAME || COLLAPSED.contains(sym.symbol()) ||
          FoldPolicy.LISTS.contains(sym.symbol())) continue;
      final Rx rx = rx(sym);
      if(rx == null) continue;
      final int[] count = count(rx, sym);
      // delimiters give a node an identity: nodes that drop them cannot be replaced by the child
      if(count[0] == 1 && count[1] == 1 && delimiters(rx, sym) == 0) wrappers.add(sym);
    }
    for(final Symbol sym : Symbol.values()) {
      if(!sym.isTerminal() && (sym.folds() || wrappers.contains(sym))) folds.add(sym);
    }

    // records: the types of the components refer to the interfaces, which are determined first
    for(int pass = 0; pass < 2; pass++) {
      shapes.clear();
      for(final Symbol sym : Symbol.values()) {
        if(sym.isTerminal() || sym.internal() || sym == Symbol.UNRESERVED_FUNCTION_EQNAME ||
            COLLAPSED.contains(sym.symbol()) || wrappers.contains(sym) ||
            FoldPolicy.VANISH.contains(sym.symbol())) continue;
        final Rx rx = rx(sym);
        if(rx == null) throw new IllegalStateException("No production for " + sym);
        final Shape shape = new Shape(sym, rx);
        shapes.put(sym, shape);
        slots(shape);
      }
      if(pass == 0) viewSyms.addAll(views().keySet());
    }
    if(!viewSyms.equals(views().keySet())) throw new IllegalStateException("Unstable interfaces.");

    final Map<Path, String> files = new LinkedHashMap<>();
    files.put(NODES, wrap(nodes()));
    files.put(FACTORY, wrap(factory()));
    return files;
  }

  // ---------------------------------------------------------------------------------------------
  // grammar

  /**
   * Returns the quoted name of a literal token.
   * @param text text
   * @return name
   */
  private static String quote(final String text) {
    return "'" + text.replace("'", "''") + "'";
  }

  /**
   * Returns a terminal.
   * @param name name as reported by the parser
   * @return symbol, or {@code null} if there is none
   */
  private static Symbol terminalOrNull(final String name) {
    try {
      return Symbol.terminal(name);
    } catch(final IllegalArgumentException ex) {
      return null;
    }
  }

  /**
   * Returns a terminal.
   * @param name name as reported by the parser
   * @return symbol
   */
  private static Symbol terminal(final String name) {
    return Symbol.terminal(name);
  }

  /**
   * Returns the production of a nonterminal.
   * @param sym nonterminal
   * @return expression, or {@code null} if the nonterminal has no production
   */
  private Rx rx(final Symbol sym) {
    final String name = sym == Symbol.EQNAME ? "EQName" : sym.symbol();
    final Element production = productions.get(name);
    return production == null ? null : seq(children(production));
  }

  /**
   * Returns the child elements.
   * @param element element
   * @return children
   */
  private static List<Element> children(final Element element) {
    final List<Element> list = new ArrayList<>();
    for(Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
      if(n instanceof final Element e) list.add(e);
    }
    return list;
  }

  /**
   * Converts a list of elements to a sequence.
   * @param elements elements
   * @return sequence
   */
  private Rx seq(final List<Element> elements) {
    final List<Rx> items = new ArrayList<>();
    for(final Element e : elements) {
      final Rx rx = convert(e);
      if(rx != null) items.add(rx);
    }
    return new Seq(items);
  }

  /**
   * Converts an element.
   * @param e element
   * @return expression, or {@code null} if the element is of no interest
   */
  private Rx convert(final Element e) {
    final String name = e.getLocalName();
    switch(name) {
      case "string": {
        final Symbol sym = terminalOrNull(quote(e.getTextContent()));
        return sym == null ? null : new Use(sym);
      }
      case "ref": {
        String ref = e.getAttribute("name");
        if(ref.equals("UnreservedFunctionEQName")) ref = "EQName";
        final Symbol nt = nonterminalOrNull(ref);
        Symbol sym = nt != null ? nt : terminalOrNull(ref);
        if(sym == null) {
          // production that consists of a single string: the parser reports the string
          final Element production = productions.get(ref);
          final List<Element> list = production == null ? List.of() : children(production);
          if(list.size() == 1 && list.get(0).getLocalName().equals("string")) {
            sym = terminalOrNull(quote(list.get(0).getTextContent()));
          }
        }
        return sym == null ? null : new Use(sym);
      }
      case "choice":
      case "orderedChoice": {
        final List<Rx> alts = new ArrayList<>();
        for(final Element c : children(e)) {
          if(c.getLocalName().equals("sequence")) alts.add(seq(children(c)));
          else alts.add(seq(List.of(c)));
        }
        return new Alt(alts);
      }
      case "sequence":
        return seq(children(e));
      case "optional":
        return new Opt(seq(children(e)));
      case "zeroOrMore":
        return new Rep(seq(children(e)), false);
      case "oneOrMore":
        return new Rep(seq(children(e)), true);
      default:
        return null;
    }
  }

  /**
   * Returns a nonterminal.
   * @param name name
   * @return symbol, or {@code null} if there is none
   */
  private static Symbol nonterminalOrNull(final String name) {
    try {
      return Symbol.nonterminal(name);
    } catch(final IllegalArgumentException ex) {
      return null;
    }
  }

  /**
   * Checks if the node of a symbol is kept as a child of the given nonterminal.
   * @param sym symbol
   * @param parent parent
   * @return result of check
   */
  private boolean retained(final Symbol sym, final Symbol parent) {
    if(!sym.isTerminal()) return true;
    if(sym == Symbol.S || sym == Symbol.EOF || delimiters.contains(sym)) return false;
    final Set<String> parentDelimiters = FoldPolicy.PARENT_DELIMITERS.get(parent.symbol());
    if(parentDelimiters != null) {
      for(final String text : parentDelimiters) {
        if(sym == terminalOrNull(quote(text))) return false;
      }
    }
    return true;
  }

  /**
   * Counts the retained children of a production.
   * @param rx expression
   * @param parent parent
   * @return minimum and maximum
   */
  private int[] count(final Rx rx, final Symbol parent) {
    if(rx instanceof final Use use) {
      final int c = retained(use.sym(), parent) ? 1 : 0;
      return new int[] { c, c };
    }
    if(rx instanceof final Seq seq) {
      int min = 0, max = 0;
      for(final Rx item : seq.items()) {
        final int[] c = count(item, parent);
        min += c[0];
        max = Math.min(INF, max + c[1]);
      }
      return new int[] { min, max };
    }
    if(rx instanceof final Alt alt) {
      int min = INF, max = 0;
      for(final Rx a : alt.alts()) {
        final int[] c = count(a, parent);
        min = Math.min(min, c[0]);
        max = Math.max(max, c[1]);
      }
      return new int[] { min == INF ? 0 : min, max };
    }
    if(rx instanceof final Opt opt) return new int[] { 0, count(opt.body(), parent)[1] };
    final Rep rep = (Rep) rx;
    final int[] c = count(rep.body(), parent);
    return new int[] { rep.plus() ? c[0] : 0, c[1] == 0 ? 0 : INF };
  }

  /**
   * Returns the maximum number of delimiters (dropped tokens) of a production.
   * @param rx expression
   * @param parent parent
   * @return maximum
   */
  private int delimiters(final Rx rx, final Symbol parent) {
    if(rx instanceof final Use use) {
      final Symbol sym = use.sym();
      return sym.isTerminal() && sym != Symbol.S && sym != Symbol.EOF &&
          !retained(sym, parent) ? 1 : 0;
    }
    int max = 0;
    if(rx instanceof final Seq seq) {
      for(final Rx item : seq.items()) max = Math.min(INF, max + delimiters(item, parent));
    } else if(rx instanceof final Alt alt) {
      for(final Rx a : alt.alts()) max = Math.max(max, delimiters(a, parent));
    } else if(rx instanceof final Opt opt) {
      max = delimiters(opt.body(), parent);
    } else {
      max = delimiters(((Rep) rx).body(), parent) == 0 ? 0 : INF;
    }
    return max;
  }

  /**
   * Returns the occurrences of the symbols of an expression.
   * @param rx expression
   * @param map occurrences by symbol, in order of the first occurrence
   * @return the same map
   */
  private static Map<Symbol, Occ> occ(final Rx rx, final Map<Symbol, Occ> map) {
    if(rx instanceof final Use use) {
      map.put(use.sym(), new Occ(1, 1));
    } else if(rx instanceof final Seq seq) {
      for(final Rx item : seq.items()) {
        for(final Map.Entry<Symbol, Occ> e : occ(item, new LinkedHashMap<>()).entrySet()) {
          final Occ o = map.get(e.getKey());
          final Occ n = e.getValue();
          map.put(e.getKey(), o == null ? n :
            new Occ(o.min() + n.min(), Math.min(INF, o.max() + n.max())));
        }
      }
    } else if(rx instanceof final Alt alt) {
      final List<Map<Symbol, Occ>> alts = new ArrayList<>();
      final Set<Symbol> syms = new LinkedHashSet<>();
      for(final Rx a : alt.alts()) {
        final Map<Symbol, Occ> m = occ(a, new LinkedHashMap<>());
        alts.add(m);
        syms.addAll(m.keySet());
      }
      for(final Symbol s : syms) {
        int min = INF, max = 0;
        for(final Map<Symbol, Occ> m : alts) {
          final Occ o = m.getOrDefault(s, new Occ(0, 0));
          min = Math.min(min, o.min());
          max = Math.max(max, o.max());
        }
        final Occ o = map.get(s);
        map.put(s, o == null ? new Occ(min, max) :
          new Occ(o.min() + min, Math.min(INF, o.max() + max)));
      }
    } else if(rx instanceof final Opt opt) {
      for(final Map.Entry<Symbol, Occ> e : occ(opt.body(), new LinkedHashMap<>()).entrySet()) {
        final Occ o = map.get(e.getKey());
        final Occ n = new Occ(0, e.getValue().max());
        map.put(e.getKey(), o == null ? n : new Occ(o.min(), Math.min(INF, o.max() + n.max())));
      }
    } else {
      final Rep rep = (Rep) rx;
      for(final Map.Entry<Symbol, Occ> e : occ(rep.body(), new LinkedHashMap<>()).entrySet()) {
        final Occ o = map.get(e.getKey());
        final int min = rep.plus() ? e.getValue().min() : 0;
        map.put(e.getKey(), o == null ? new Occ(min, INF) : new Occ(o.min() + min, INF));
      }
    }
    return map;
  }

  /**
   * Collects the symbols that occur in a repetition.
   * @param rx expression
   * @param inside inside a repetition
   * @param repeated symbols in repetitions
   * @param varying tokens that vary within a repetition
   */
  private static void repeated(final Rx rx, final boolean inside, final Set<Symbol> repeated,
      final Set<Symbol> varying) {
    if(rx instanceof final Use use) {
      if(inside) repeated.add(use.sym());
    } else if(rx instanceof final Seq seq) {
      for(final Rx item : seq.items()) repeated(item, inside, repeated, varying);
    } else if(rx instanceof final Alt alt) {
      for(final Rx a : alt.alts()) repeated(a, inside, repeated, varying);
    } else if(rx instanceof final Opt opt) {
      repeated(opt.body(), inside, repeated, varying);
    } else {
      final Rep rep = (Rep) rx;
      for(final Map.Entry<Symbol, Occ> e : occ(rep.body(), new LinkedHashMap<>()).entrySet()) {
        if(e.getValue().min() == 0) varying.add(e.getKey());
      }
      repeated(rep.body(), true, repeated, varying);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // shapes

  /**
   * Determines the components of a record.
   * @param shape shape
   */
  private void slots(final Shape shape) {
    final Symbol sym = shape.sym;
    final Map<Symbol, Occ> occ = occ(shape.rx, new LinkedHashMap<>());
    if(FoldPolicy.LISTS.contains(sym.symbol())) {
      // all children except for the list itself
      for(final Symbol s : occ.keySet()) {
        if(s != sym && retained(s, sym)) shape.members.add(s);
      }
      return;
    }
    final Set<Symbol> repeated = new TreeSet<>(), varying = new TreeSet<>();
    repeated(shape.rx, false, repeated, varying);
    final Set<String> names = new HashSet<>(RESERVED);
    for(final Map.Entry<Symbol, Occ> e : occ.entrySet()) {
      final Symbol s = e.getKey();
      final Occ o = e.getValue();
      if(!retained(s, sym) || FoldPolicy.VANISH.contains(s.symbol())) continue;
      if(o.max() >= INF || repeated.contains(s)) {
        // keywords that are present in each iteration carry no information
        if(s.isTerminal() && isToken(s) && !varying.contains(s)) continue;
        shape.members.add(s);
      } else if(s.isTerminal() && isToken(s) && POSITIONED.contains(s.symbol())) {
        // keywords whose positions are needed by the parser
        for(int i = 0; i < o.max(); i++) {
          shape.slots.add(new Slot(unique(names, name(s) + (i == 0 ? "" : i + 1)), "Terminal", s,
              false, i));
        }
      } else if(s.isTerminal() && isToken(s)) {
        if(o.min() == 0) {
          shape.slots.add(new Slot(unique(names, "is" + camel(s)), "boolean", s, true, 0));
        }
      } else {
        for(int i = 0; i < o.max(); i++) {
          shape.slots.add(new Slot(unique(names, name(s) + (i == 0 ? "" : i + 1)), type(s), s,
              false, i));
        }
      }
    }
  }

  /**
   * Checks if a terminal is a keyword or an operator (as opposed to a token with a value).
   * @param sym terminal
   * @return result of check
   */
  private static boolean isToken(final Symbol sym) {
    return sym.symbol().startsWith("'");
  }

  /**
   * Returns a unique name.
   * @param names used names
   * @param name name
   * @return name
   */
  private static String unique(final Set<String> names, final String name) {
    String n = name;
    while(!names.add(n)) n += '_';
    return n;
  }

  /**
   * Returns the type of a component.
   * @param sym symbol
   * @return type
   */
  private String type(final Symbol sym) {
    if(sym.isTerminal() || COLLAPSED.contains(sym.symbol())) return "Terminal";
    return folds.contains(sym) ? viewSyms.contains(sym) ? "Nodes." + sym.name() : "Node" :
      "Nodes." + sym.name();
  }

  /**
   * Returns the type of the elements of the children array.
   * @param shape shape
   * @return type
   */
  private String memberType(final Shape shape) {
    return shape.members.size() == 1 ? type(shape.members.iterator().next()) : "Node";
  }

  /**
   * Returns the name of a symbol in camel case.
   * @param sym symbol
   * @return name
   */
  private static String name(final Symbol sym) {
    final String n = sym.isTerminal() ? sym.name() : sym.symbol();
    final StringBuilder sb = new StringBuilder();
    boolean up = false;
    for(int i = 0; i < n.length(); i++) {
      final char c = n.charAt(i);
      if(c == '_') {
        up = true;
      } else {
        sb.append(up ? Character.toUpperCase(c) : sym.isTerminal() ? Character.toLowerCase(c) : c);
        up = false;
      }
    }
    return lower(sb.toString());
  }

  /**
   * Returns the name of a terminal in camel case, with a leading upper-case letter.
   * @param sym terminal
   * @return name
   */
  private static String camel(final Symbol sym) {
    final String n = name(sym);
    return Character.toUpperCase(n.charAt(0)) + n.substring(1);
  }

  /**
   * Converts the leading upper-case letters of a name to lower case.
   * @param name name
   * @return name
   */
  private static String lower(final String name) {
    int i = 0;
    while(i < name.length() && Character.isUpperCase(name.charAt(i))) i++;
    if(i <= 1) return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    // keep the last upper-case letter if it starts a word
    if(i < name.length()) i--;
    return name.substring(0, i).toLowerCase(Locale.ROOT) + name.substring(i);
  }

  // ---------------------------------------------------------------------------------------------
  // output

  /**
   * Returns the text of a production.
   * @param rx expression
   * @return text
   */
  private static String text(final Rx rx) {
    if(rx instanceof final Use use) {
      return use.sym().symbol().startsWith("'") ? use.sym().symbol() : use.sym().symbol();
    }
    if(rx instanceof final Seq seq) {
      final StringBuilder sb = new StringBuilder();
      for(final Rx item : seq.items()) sb.append(sb.isEmpty() ? "" : " ").append(text(item));
      return sb.toString();
    }
    if(rx instanceof final Alt alt) {
      final StringJoiner sj = new StringJoiner(" | ", "(", ")");
      for(final Rx a : alt.alts()) sj.add(text(a));
      return sj.toString();
    }
    if(rx instanceof final Opt opt) return '[' + text(opt.body()) + ']';
    final Rep rep = (Rep) rx;
    return '{' + text(rep.body()) + (rep.plus() ? "}+" : "}*");
  }

  /**
   * Escapes text for a comment.
   * @param text text
   * @return escaped text
   */
  private static String escape(final String text) {
    return text.replace("*/", "*&#47;").replace("@", "&#64;").replace("<", "&lt;").
        replace(">", "&gt;").replace("&&", "&amp;&amp;");
  }

  /**
   * Generates the records.
   * @return source
   */
  private String nodes() {
    final StringBuilder sb = new StringBuilder();
    sb.append("""
      package org.basex.query.parse.gen;

      import org.basex.query.parse.syntax.*;

      /**
       * Nodes of the syntax tree. This file is generated by {@code NodeGenerator}, do not
       * edit. There is a record for each nonterminal that has more than one child or that
       * has no children at all, see the productions in the comments. Nonterminals with exactly
       * one child are replaced by it: they are referred to as {@link Node}.
       */
      @SuppressWarnings("all")
      public final class Nodes {
        /** Private constructor. */
        private Nodes() { }
      """);
    final Map<Symbol, Map<String, String>> views = views();
    final Map<Symbol, List<String>> implemented = new HashMap<>();
    for(final Symbol sym : views.keySet()) {
      for(final Shape shape : alternatives(sym)) {
        implemented.computeIfAbsent(shape.sym, k -> new ArrayList<>()).add(sym.name());
      }
    }
    for(final Shape shape : shapes.values()) {
      sb.append("\n  /** ").append(shape.sym.symbol()).append(" ::= ").
        append(escape(text(shape.rx))).append(" */\n");
      sb.append("  public record ").append(shape.sym.name()).append("(int begin, int end");
      for(final Slot slot : shape.slots) {
        sb.append(", ").append(slot.type()).append(' ').append(slot.name());
      }
      if(!shape.members.isEmpty()) sb.append(", ").append(memberType(shape)).append("[] children");
      sb.append(") implements Node");
      for(final String view : implemented.getOrDefault(shape.sym, List.of())) {
        sb.append(", ").append(view);
      }
      sb.append(" {\n    @Override\n    public int sym() {\n      return Sym.").
        append(shape.sym.name()).append(";\n    }\n  }\n");
    }
    // common views of the alternatives of a choice
    for(final Map.Entry<Symbol, Map<String, String>> view : views.entrySet()) {
      final StringJoiner permits = new StringJoiner(", ");
      for(final Shape shape : alternatives(view.getKey())) permits.add(shape.sym.name());
      sb.append("\n  /** Alternatives of ").append(view.getKey().symbol()).
        append(", with their common components. */\n  public sealed interface ").
        append(view.getKey().name()).append(" extends Node permits ").append(permits).
        append(" {\n");
      for(final Map.Entry<String, String> e : view.getValue().entrySet()) {
        final String type = e.getValue();
        final String value = type.equals("boolean") ? "false" : type.endsWith("[]") ?
          "new " + type.substring(0, type.length() - 1) + "0]" : "null";
        sb.append("    /**\n     * Returns the component, or the default value if there is none.").
          append("\n     * @return component\n     */\n    default ").append(type).append(' ').
          append(e.getKey()).append("() {\n      return ").append(value).append(";\n    }\n");
      }
      sb.append("  }\n");
    }
    sb.append("}\n");
    return sb.toString();
  }

  /**
   * Returns the records of the alternatives of a choice.
   * @param sym choice
   * @return shapes, or {@code null} if some alternatives have no records: terminals, or
   *   nonterminals that are not choices
   */
  private List<Shape> alternatives(final Symbol sym) {
    final List<Shape> list = new ArrayList<>();
    final Rx rx = rx(sym);
    if(!(rx instanceof final Seq seq && seq.items().size() == 1 &&
        seq.items().get(0) instanceof final Alt alt)) return null;
    for(final Rx a : alt.alts()) {
      if(!(a instanceof final Seq s && s.items().size() == 1 &&
          s.items().get(0) instanceof final Use use)) return null;
      final Symbol u = use.sym();
      final List<Shape> inner;
      // nonterminals that can be replaced by their only child do not always have a record
      if(shapes.containsKey(u) && !folds.contains(u)) inner = List.of(shapes.get(u));
      else if(folds.contains(u) && !shapes.containsKey(u) && u != sym) inner = alternatives(u);
      else return null;
      if(inner == null) return null;
      for(final Shape shape : inner) if(!list.contains(shape)) list.add(shape);
    }
    return list;
  }

  /**
   * Determines the choices whose alternatives have common components.
   * @return types of the components by name, by choice
   */
  private Map<Symbol, Map<String, String>> views() {
    final Map<Symbol, Map<String, String>> views = new LinkedHashMap<>();
    for(final Symbol sym : folds) {
      if(sym.isTerminal()) continue;
      final List<Shape> list = alternatives(sym);
      if(list == null || list.size() < 2 || list.size() > MAX_ALTERNATIVES) continue;
      final Map<String, String> types = new LinkedHashMap<>();
      final Map<String, Integer> counts = new HashMap<>();
      final Set<String> conflicts = new HashSet<>();
      for(final Shape shape : list) {
        final Map<String, String> own = new LinkedHashMap<>();
        for(final Slot slot : shape.slots) own.put(slot.name(), slot.type());
        if(!shape.members.isEmpty()) own.put("children", memberType(shape) + "[]");
        for(final Map.Entry<String, String> e : own.entrySet()) {
          final String old = types.put(e.getKey(), e.getValue());
          if(old != null && !old.equals(e.getValue())) conflicts.add(e.getKey());
          counts.merge(e.getKey(), 1, Integer::sum);
        }
      }
      types.keySet().removeAll(conflicts);
      // large choices only offer the components that all alternatives have
      if(list.size() > MAX_UNION) types.keySet().removeIf(name -> counts.get(name) != list.size());
      views.put(sym, types);
    }
    return views;
  }

  /**
   * Wraps the lines of a source that are too long. Comments are wrapped at spaces, and code is
   * wrapped after commas.
   * @param source source
   * @return source with lines of limited length
   */
  private static String wrap(final String source) {
    final List<String> lines = new ArrayList<>();
    for(final String line : source.split("\n", -1)) {
      if(line.length() <= MAX_LINE) {
        lines.add(line);
        continue;
      }
      final String trimmed = line.stripLeading();
      final String indent = line.substring(0, line.length() - trimmed.length());
      if(trimmed.startsWith("/** ") && trimmed.endsWith(" */")) {
        // comment
        lines.add(indent + "/**");
        final StringBuilder sb = new StringBuilder();
        for(final String word : trimmed.substring(4, trimmed.length() - 3).split(" ")) {
          if(sb.length() > 0 && indent.length() + 3 + sb.length() + 1 + word.length() > MAX_LINE) {
            lines.add(indent + " * " + sb);
            sb.setLength(0);
          }
          if(sb.length() > 0) sb.append(' ');
          sb.append(word);
        }
        lines.add(indent + " * " + sb);
        lines.add(indent + " */");
      } else {
        // code
        final String continuation = indent + "    ";
        String rest = line;
        while(rest.length() > MAX_LINE) {
          int cut = rest.lastIndexOf(", ", MAX_LINE - 1);
          if(cut == -1) cut = rest.lastIndexOf(' ', MAX_LINE);
          else cut++;
          lines.add(rest.substring(0, cut).stripTrailing());
          rest = continuation + rest.substring(cut).strip();
        }
        lines.add(rest);
      }
    }
    return String.join("\n", lines);
  }

  /**
   * Generates the factory.
   * @return source
   */
  private String factory() {
    final StringBuilder sb = new StringBuilder();
    sb.append("""
      package org.basex.query.parse.gen;

      import java.util.*;

      import org.basex.query.parse.syntax.*;

      /**
       * Creates the nodes of the syntax tree. This file is generated by
       * {@code NodeGenerator}, do not edit.
       */
      @SuppressWarnings("all")
      public final class NodeFactory {
        /** Nonterminals that are replaced by their only child. */
        public static final BitSet FOLDS = Sym.set(
      """);
    final List<Symbol> list = new ArrayList<>();
    for(final Symbol sym : Symbol.values()) if(folds.contains(sym)) list.add(sym);
    // as many symbols as fit in a line
    final StringBuilder sj = new StringBuilder("    ");
    int length = 4;
    for(int i = 0; i < list.size(); i++) {
      final String name = "Sym." + list.get(i).name() + (i < list.size() - 1 ? "," : "");
      if(length + name.length() + 1 > MAX_LINE) {
        sj.setLength(sj.length() - 1);
        sj.append("\n    ");
        length = 4;
      }
      sj.append(name).append(' ');
      length += name.length() + 1;
    }
    sj.setLength(sj.length() - 1);
    sb.append(sj).append(");\n\n");
    sb.append("""
        /** No nodes. */
        private static final Node[] NONE = { };

        /** Private constructor. */
        private NodeFactory() { }

        /**
         * Creates a node.
         * @param kind nonterminal
         * @param begin start offset
         * @param end end offset
         * @param st nodes of the children, which may include placeholders
         * @param sy symbols of the children
         * @param from index of the first child
         * @param to index after the last child
         * @return node
         */
        public static Node create(final int kind, final int begin, final int end, final Node[] st,
            final int[] sy, final int from, final int to) {
          return switch(kind) {
      """);
    for(final Shape shape : shapes.values()) {
      sb.append("      case Sym.").append(shape.sym.name()).append(" -> ").
          append(method(shape.sym)).
        append("(begin, end, st, sy, from, to);\n");
    }
    sb.append("""
            default -> throw new IllegalStateException("No node for " + Sym.symbol(kind) + ".");
          };
        }
      """);
    for(final Shape shape : shapes.values()) create(shape, sb);
    sb.append("}\n");
    return sb.toString();
  }

  /**
   * Returns the name of a creation method.
   * @param sym nonterminal
   * @return name
   */
  private static String method(final Symbol sym) {
    return "create" + sym.name();
  }

  /**
   * Generates a creation method.
   * @param shape shape
   * @param sb string builder
   */
  private void create(final Shape shape, final StringBuilder sb) {
    final String name = shape.sym.name();
    sb.append("\n  private static Nodes.").append(name).append(' ').append(method(shape.sym)).
      append("(final int begin, final int end, final Node[] st, final int[] sy, final int from, "
          + "final int to) {\n");
    final boolean list = FoldPolicy.LISTS.contains(shape.sym.symbol());
    for(final Slot slot : shape.slots) {
      sb.append("    ").append(slot.type()).append(' ').append(slot.name()).append(" = ").
        append(slot.type().equals("boolean") ? "false" : "null").append(";\n");
    }
    final boolean members = !shape.members.isEmpty();
    if(members) {
      sb.append("    int size = to - from;\n");
      if(list) {
        // the children of a preceding list are taken over
        sb.append("    for(int i = from; i < to; i++) {\n");
        sb.append("      if(sy[i] == Sym.").append(name).
          append(" && st[i] instanceof final Nodes.").append(name).
          append(" p) size += p.children().length;\n    }\n");
      }
      sb.append("    final ").append(memberType(shape)).append("[] ch = new ").
        append(memberType(shape)).append("[size];\n    int n = 0;\n");
    }
    sb.append("    for(int i = from; i < to; i++) {\n");
    sb.append("      final Node c = st[i];\n      if(c instanceof Marker) continue;\n");
    if(list) {
      sb.append("      if(sy[i] == Sym.").append(name).append(") {\n");
      sb.append("        for(final Node p : ((Nodes.").append(name).
        append(") c).children()) ch[n++] = p;\n");
      sb.append("        continue;\n      }\n");
    }
    sb.append("      switch(sy[i]) {\n");
    // slots by symbol, in the order of the first occurrence
    final Map<Symbol, List<Slot>> bySym = new LinkedHashMap<>();
    for(final Slot slot : shape.slots) {
      bySym.computeIfAbsent(slot.sym(), k -> new ArrayList<>()).add(slot);
    }
    for(final Map.Entry<Symbol, List<Slot>> e : bySym.entrySet()) {
      sb.append("        case Sym.").append(e.getKey().name()).append(" -> ");
      final List<Slot> slots = e.getValue();
      if(slots.get(0).token()) {
        sb.append(slots.get(0).name()).append(" = true;\n");
      } else {
        if(slots.size() > 1) sb.append("{ ");
        for(int s = 0; s < slots.size(); s++) {
          final Slot slot = slots.get(s);
          final String cast = slot.type().equals("Node") ? "c" : "(" + slot.type() + ") c";
          if(s < slots.size() - 1) {
            sb.append("if(").append(slot.name()).append(" == null) ").append(slot.name()).
              append(" = ").append(cast).append("; else ");
          } else {
            sb.append(slot.name()).append(" = ").append(cast).append(';');
            sb.append(slots.size() > 1 ? " }\n" : "\n");
          }
        }
      }
    }
    if(members) {
      final StringJoiner sj = new StringJoiner(", ");
      for(final Symbol s : shape.members) sj.add("Sym." + s.name());
      // members are listed in a group of labels
      final StringJoiner labels = new StringJoiner(", ");
      for(final Symbol s : shape.members) {
        if(!bySym.containsKey(s)) labels.add("Sym." + s.name());
      }
      final String mt = memberType(shape);
      if(labels.length() > 0) {
        sb.append("        case ").append(labels).append(" -> ch[n++] = ").
          append(mt.equals("Node") ? "c" : "(" + mt + ") c").append(";\n");
      }
    }
    sb.append("        default -> { }\n      }\n    }\n");
    sb.append("    return new Nodes.").append(name).append("(begin, end");
    for(final Slot slot : shape.slots) sb.append(", ").append(slot.name());
    if(members) {
      sb.append(", ").append(memberType(shape).equals("Node") ? "n == 0 ? NONE : " : "").
        append("n == ch.length ? ch : Arrays.copyOf(ch, n)");
    }
    sb.append(");\n  }\n");
  }
}
