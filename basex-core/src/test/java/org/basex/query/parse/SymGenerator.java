package org.basex.query.parse;

import java.lang.reflect.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

/**
 * Generates the {@code Sym} class, which has the symbols as integer constants, from the symbol
 * tables of the generated parser. Run its main method in the {@code basex-core} directory after
 * the parser was regenerated, or call {@link #generate()} to compare the result with the checked-in
 * file (see {@code SymTest}).
 * <p>Nonterminals keep their grammar names. Terminals get upper-case names: lexical tokens are
 * converted from camel case, keywords from their text, and punctuation by the table below.</p>
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class SymGenerator {
  /** Generated class. */
  static final Path TARGET = Paths.get("src/main/java/org/basex/query/parse/gen/Sym.java");

  /** Names that are used by the generated class for other purposes than symbols. */
  private static final List<String> RESERVED = List.of(
      "SYMBOL_COUNT", "FIRST_TERMINAL", "SYMBOLS", "Names", "Folds");

  /** Symbols that the parser uses internally, to report the alternatives of an ambiguity. */
  static final Set<String> INTERNAL = Set.of("ALTERNATIVE", "AMBIGUOUS", "UNAMBIGUOUS");

  /** Names of punctuation tokens. */
  private static final Map<String, String> PUNCTUATION = Map.ofEntries(
    Map.entry("\"\"", "ESCAPE_QUOT"), Map.entry("\"", "QUOT"), Map.entry("#)", "PRAGMA_CLOSE"),
    Map.entry("%", "PERCENT_SIGN"), Map.entry("%fixed", "PERCENT_FIXED"),
    Map.entry("''", "APOS"), Map.entry("(#", "PRAGMA_OPEN"),
    Map.entry("(:", "COMMENT_OPEN"), Map.entry("-->", "XML_COMMENT_CLOSE"),
    Map.entry(".", "DOT"), Map.entry("..", "DOT2"), Map.entry(":)", "COMMENT_CLOSE"),
    Map.entry("<", "LESS"), Map.entry("<!--", "XML_COMMENT_OPEN"),
    Map.entry("<![CDATA[", "CDATA_OPEN"), Map.entry("</", "END_TAG_OPEN"),
    Map.entry("<?", "PI_OPEN"), Map.entry("?>", "PI_CLOSE"), Map.entry("@", "AT_SIGN"),
    Map.entry("]]>", "CDATA_CLOSE"), Map.entry("`", "BACKTICK"),
    Map.entry("``[", "STRING_CONSTRUCTOR_OPEN"), Map.entry("]``", "STRING_CONSTRUCTOR_CLOSE"),
    Map.entry("{{", "LBRACE2"), Map.entry("}}", "RBRACE2"), Map.entry("!", "BANG"),
    Map.entry("!=", "NOT_EQUALS"), Map.entry("#", "HASH"), Map.entry("$", "DOLLAR"),
    Map.entry("(", "LPAREN"), Map.entry(")", "RPAREN"), Map.entry("*", "STAR"),
    Map.entry("+", "PLUS"), Map.entry(",", "COMMA"), Map.entry("-", "MINUS"),
    Map.entry("->", "THIN_ARROW"), Map.entry("/", "SLASH"), Map.entry("//", "SLASH2"),
    Map.entry("/>", "EMPTY_TAG_CLOSE"), Map.entry(":", "COLON"), Map.entry("::", "COLON2"),
    Map.entry(":=", "ASSIGN"), Map.entry(";", "SEMICOLON"), Map.entry("<<", "LESS2"),
    Map.entry("<=", "LESS_EQUAL"), Map.entry("=", "EQUALS"), Map.entry("=!>", "MAPPING_ARROW"),
    Map.entry("=>", "FAT_ARROW"), Map.entry("=?>", "LOOKUP_ARROW"), Map.entry(">", "GREATER"),
    Map.entry(">=", "GREATER_EQUAL"), Map.entry(">>", "GREATER2"), Map.entry("?", "QUESTION"),
    Map.entry("[", "LBRACKET"), Map.entry("]", "RBRACKET"), Map.entry("{", "LBRACE"),
    Map.entry("|", "BAR"), Map.entry("||", "BAR2"), Map.entry("}", "RBRACE"),
    Map.entry("×", "MULTIPLY_SIGN"), Map.entry("÷", "DIVIDE_SIGN"));

  /** Terminals that are no tokens of the parser, but created by {@code TreeBuilder}. */
  private static final List<String> SYNTHETIC = List.of("NCName", "QName");

  /** Private constructor. */
  private SymGenerator() { }

  /**
   * Main method.
   * @param args not used
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    Files.writeString(TARGET, generate().replace("\n", System.lineSeparator()),
        StandardCharsets.UTF_8);
  }

  /**
   * Collects the symbols of the parser.
   * @param nonterms nonterminals: grammar names, which are also the constant names
   * @param terminals terminals: grammar names, mapped to constant names
   * @throws Exception exception
   */
  private static void collect(final LinkedHashMap<String, String> nonterms,
      final LinkedHashMap<String, String> terminals) throws Exception {

    final Class<?> parser = Class.forName("org.basex.query.parse.gen.XQuerySyntaxParser");
    final String[] nonterminals = table(parser, "NONTERMINAL");
    final String[] tokens = table(parser, "TOKEN");

    // a symbol may occur more than once in the token table, as for '<': one constant is enough
    final Set<String> javaNames = new HashSet<>(RESERVED);
    for(final String symbol : nonterminals) {
      if(nonterms.putIfAbsent(symbol, symbol) == null && !javaNames.add(symbol))
        throw new IllegalStateException("Duplicate name " + symbol);
    }
    // names are collapsed to terminals by the builder: they are no tokens of the parser
    final ArrayList<String> symbols = new ArrayList<>(Arrays.asList(tokens));
    symbols.addAll(SYNTHETIC);
    for(final String symbol : symbols) {
      if(terminals.containsKey(symbol)) continue;
      final String name = terminalName(symbol);
      if(!javaNames.add(name))
        throw new IllegalStateException("Duplicate name " + name + " for " + symbol);
      terminals.put(symbol, name);
    }
  }

  /**
   * Generates the source of the class with the integer constants. Within the nonterminals and
   * within the terminals, the constants are ordered by grammar name.
   * @return source, with Unix line endings
   * @throws Exception exception
   */
  static String generate() throws Exception {
    final LinkedHashMap<String, String> nonterms = new LinkedHashMap<>();
    final LinkedHashMap<String, String> terminals = new LinkedHashMap<>();
    collect(nonterms, terminals);
    final ArrayList<String> nonterminals = new ArrayList<>(nonterms.keySet());
    final ArrayList<String> tokens = new ArrayList<>(terminals.keySet());
    Collections.sort(nonterminals);
    Collections.sort(tokens);

    final StringBuilder sb = new StringBuilder();
    sb.append("""
      package org.basex.query.parse.gen;

      import java.util.*;

      import org.basex.query.parse.*;

      /**
       * Symbols of the generated parser as integer constants, which are cheap to initialize. This
       * file is generated by {@code SymGenerator}, do not edit. <p>Nonterminals are named as in the
       * grammar. Terminals are named in upper case: lexical tokens by their name, keywords by their
       * text, punctuation by what it stands for. A terminal and a nonterminal may share a grammar
       * name, such as {@code QName}. Nonterminals come first, terminals follow, each group ordered
       * by grammar name.</p>
       */
      @SuppressWarnings("all")
      public final class Sym {
        /** Private constructor. */
        private Sym() { }

      """);
    int sym = 0;
    sb.append("  // nonterminals\n");
    for(final String name : nonterminals) {
      sb.append("  public static final int ").append(name).append(" = ").append(sym++).
        append(";\n");
    }
    sb.append("\n  // terminals\n");
    for(final String symbol : tokens) {
      sb.append("  public static final int ").append(terminals.get(symbol)).append(" = ").
        append(sym++).append(";\n");
    }
    sb.append("\n  /** Number of symbols. */\n");
    sb.append("  public static final int SYMBOL_COUNT = ").append(sym).append(";\n");
    sb.append("  /** First terminal. */\n");
    sb.append("  public static final int FIRST_TERMINAL = ").append(nonterminals.size()).
      append(";\n\n");
    sb.append("  /** Grammar names, indexed by symbol; keywords and punctuation are quoted. */\n");
    sb.append("  private static final String[] SYMBOLS = {\n");
    for(final String name : nonterminals) sb.append("    \"").append(name).append("\",\n");
    for(final String symbol : tokens) {
      sb.append("    \"").append(symbol.replace("\\", "\\\\").replace("\"", "\\\"")).
        append("\",\n");
    }
    sb.append("  };\n\n");
    // the parser reports the ids of its own tables: they are mapped to symbols
    final Class<?> parser = Class.forName("org.basex.query.parse.gen.XQuerySyntaxParser");
    sb.append("  /** Symbols of the nonterminals, indexed by the ids that the parser reports. */").
        append('\n');
    sb.append("  private static final int[] NONTERMINAL_SYMS = ");
    symbols(sb, table(parser, "NONTERMINAL"), nonterminals, 0);
    sb.append("  /** Symbols of the terminals, indexed by the ids that the parser reports. */\n");
    sb.append("  private static final int[] TERMINAL_SYMS = ");
    symbols(sb, table(parser, "TOKEN"), tokens, nonterminals.size());
    sb.append("""

        /** Fold flags, which are computed on first use. */
        private static final class Folds {
          /** Fold flags, indexed by symbol (see {@link FoldPolicy}). */
          static final boolean[] VALUES = new boolean[SYMBOL_COUNT];

          static {
            for(int s = 0; s < FIRST_TERMINAL; s++) {
              VALUES[s] = FoldPolicy.NAMES.contains(SYMBOLS[s]);
            }
          }
        }

        /** Symbols by grammar name, which are built on first lookup. */
        private static final class Names {
          /** Nonterminals. */
          static final HashMap<String, Integer> NONTERMINALS = new HashMap<>(2048);
          /** Terminals. */
          static final HashMap<String, Integer> TERMINALS = new HashMap<>(2048);

          static {
            for(int s = 0; s < SYMBOL_COUNT; s++) {
              (s < FIRST_TERMINAL ? NONTERMINALS : TERMINALS).put(SYMBOLS[s], s);
            }
          }
        }

        /**
         * Returns the nonterminal with the given grammar name, as reported by the parser.
         * @param symbol grammar name
         * @return nonterminal
         * @throws IllegalArgumentException if there is no such nonterminal
         */
        public static int nonterminal(final String symbol) {
          final Integer sym = Names.NONTERMINALS.get(symbol);
          if(sym == null) throw new IllegalArgumentException("Unknown nonterminal " + symbol);
          return sym;
        }

        /**
         * Returns the terminal with the given grammar name, as reported by the parser.
         * @param symbol grammar name
         * @return terminal
         * @throws IllegalArgumentException if there is no such terminal
         */
        public static int terminal(final String symbol) {
          final Integer sym = Names.TERMINALS.get(symbol);
          if(sym == null) throw new IllegalArgumentException("Unknown terminal " + symbol);
          return sym;
        }

        /**
         * Returns the nonterminal for an id that the parser reports.
         * @param id id of the nonterminal
         * @return nonterminal
         */
        public static int ofNonterminal(final int id) {
          return NONTERMINAL_SYMS[id];
        }

        /**
         * Returns the terminal for an id that the parser reports.
         * @param id id of the terminal
         * @return terminal
         */
        public static int ofTerminal(final int id) {
          return TERMINAL_SYMS[id];
        }

        /**
         * Returns the grammar name.
         * @param sym symbol
         * @return name, with quotes for keywords and punctuation
         */
        public static String symbol(final int sym) {
          return SYMBOLS[sym];
        }

        /**
         * Checks if a symbol is a terminal.
         * @param sym symbol
         * @return result of check
         */
        public static boolean isTerminal(final int sym) {
          return sym >= FIRST_TERMINAL;
        }

        /**
         * Checks if a nonterminal is subject to folding.
         * @param sym symbol
         * @return result of check
         */
        public static boolean folds(final int sym) {
          return Folds.VALUES[sym];
        }

        /**
         * Returns a set of symbols.
         * @param syms symbols
         * @return set, indexed by symbol
         */
        public static BitSet set(final int... syms) {
          final BitSet set = new BitSet(SYMBOL_COUNT);
          for(final int sym : syms) set.set(sym);
          return set;
        }
      }
      """);
    return sb.toString();
  }

  /**
   * Writes the symbols that belong to the entries of a table of the parser, as an array
   * initializer.
   * @param sb string builder
   * @param table table of the parser
   * @param sorted sorted names of the symbols
   * @param offset constant of the first symbol in the sorted names
   */
  private static void symbols(final StringBuilder sb, final String[] table,
      final List<String> sorted, final int offset) {
    sb.append("{\n");
    final StringBuilder line = new StringBuilder("    ");
    for(int i = 0; i < table.length; i++) {
      final String number = offset + Collections.binarySearch(sorted, table[i]) +
          (i < table.length - 1 ? "," : "");
      if(line.length() + number.length() + 1 > 100) {
        sb.append(line).append('\n');
        line.setLength(0);
        line.append("    ");
      }
      if(line.length() > 4) line.append(' ');
      line.append(number);
    }
    sb.append(line).append("\n  };\n\n");
  }

  /**
   * Reads a symbol table of the parser.
   * @param parser parser class
   * @param field field name
   * @return table
   * @throws Exception exception
   */
  private static String[] table(final Class<?> parser, final String field) throws Exception {
    final Field f = parser.getDeclaredField(field);
    f.setAccessible(true);
    return (String[]) f.get(null);
  }

  /**
   * Returns the constant name of a token.
   * @param symbol token as reported by the parser
   * @return upper-case name
   */
  private static String terminalName(final String symbol) {
    if(symbol.equals("%ERROR")) return "ERROR";
    if(symbol.startsWith("'")) {
      final String text = symbol.substring(1, symbol.length() - 1);
      final String punctuation = PUNCTUATION.get(text);
      if(punctuation != null) return punctuation;
      if(text.matches("[A-Za-z][A-Za-z-]*")) return text.toUpperCase(Locale.ROOT).replace('-', '_');
      throw new IllegalStateException("No name for token " + symbol +
          ". Add it to PUNCTUATION in SymGenerator.");
    }
    return symbol.
        replaceAll("([a-z0-9])([A-Z])", "$1_$2").
        replaceAll("([A-Z])([A-Z][a-z])", "$1_$2").
        toUpperCase(Locale.ROOT);
  }
}
