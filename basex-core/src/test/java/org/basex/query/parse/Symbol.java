package org.basex.query.parse;

import java.lang.reflect.*;

import org.basex.query.parse.gen.*;

/**
 * A symbol of the generated parser, as an object that is convenient for the generators. There is
 * exactly one instance for each symbol, so that instances can be compared by identity. They are
 * ordered like the integer constants of {@link Sym}.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
final class Symbol implements Comparable<Symbol> {
  /** All symbols, indexed by their constant. */
  private static final Symbol[] ALL = all();

  /** Whitespace. */
  static final Symbol S = of(Sym.S);
  /** End of input. */
  static final Symbol EOF = of(Sym.EOF);
  /** EQName. */
  static final Symbol EQNAME = of(Sym.EQName);
  /** Technical variant of EQName. */
  static final Symbol UNRESERVED_FUNCTION_EQNAME = of(Sym.UnreservedFunctionEQName);
  /** Technical variant of QName. */
  static final Symbol UNRESERVED_FUNCTION_QNAME = of(Sym.UnreservedFunctionQName);

  /** Constant. */
  private final int id;
  /** Name of the constant. */
  private final String name;

  /**
   * Constructor.
   * @param id constant
   * @param name name of the constant
   */
  private Symbol(final int id, final String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Creates all symbols, using the names of the constants.
   * @return symbols
   */
  private static Symbol[] all() {
    final String[] names = new String[Sym.SYMBOL_COUNT];
    for(final Field field : Sym.class.getFields()) {
      final String name = field.getName();
      if(field.getType() != int.class || !Modifier.isStatic(field.getModifiers()) ||
          name.equals("SYMBOL_COUNT") || name.equals("FIRST_TERMINAL")) continue;
      try {
        names[field.getInt(null)] = name;
      } catch(final IllegalAccessException ex) {
        throw new IllegalStateException(ex);
      }
    }
    final Symbol[] symbols = new Symbol[names.length];
    for(int s = 0; s < symbols.length; s++) symbols[s] = new Symbol(s, names[s]);
    return symbols;
  }

  /**
   * Returns all symbols: first the nonterminals, then the terminals, each ordered by grammar name.
   * @return symbols
   */
  static Symbol[] values() {
    return ALL.clone();
  }

  /**
   * Returns a symbol.
   * @param id constant
   * @return symbol
   */
  static Symbol of(final int id) {
    return ALL[id];
  }

  /**
   * Returns the terminal with the given grammar name.
   * @param symbol grammar name
   * @return terminal
   * @throws IllegalArgumentException if there is no such terminal
   */
  static Symbol terminal(final String symbol) {
    return of(Sym.terminal(symbol));
  }

  /**
   * Returns the nonterminal with the given grammar name.
   * @param symbol grammar name
   * @return nonterminal
   * @throws IllegalArgumentException if there is no such nonterminal
   */
  static Symbol nonterminal(final String symbol) {
    return of(Sym.nonterminal(symbol));
  }

  /**
   * Returns the name of the constant.
   * @return name
   */
  String name() {
    return name;
  }

  /**
   * Returns the grammar name.
   * @return name, with quotes for keywords and punctuation
   */
  String symbol() {
    return Sym.symbol(id);
  }

  /**
   * Checks if this is a terminal.
   * @return result of check
   */
  boolean isTerminal() {
    return Sym.isTerminal(id);
  }

  /**
   * Checks if this is a symbol that the parser uses internally, which is not part of the grammar.
   * @return result of check
   */
  boolean internal() {
    return SymGenerator.INTERNAL.contains(name);
  }

  /**
   * Checks if this is a nonterminal that is subject to folding.
   * @return result of check
   */
  boolean folds() {
    return Sym.folds(id);
  }

  @Override
  public int compareTo(final Symbol other) {
    return Integer.compare(id, other.id);
  }

  @Override
  public String toString() {
    return name;
  }
}
