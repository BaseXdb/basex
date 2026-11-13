package org.basex.query.parse;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

import org.basex.query.parse.gen.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the generated {@link Sym} class.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class SymTest {
  /**
   * Checks that the class was generated from the current parser.
   * @throws Exception exception
   */
  @Test public void upToDate() throws Exception {
    final String checkedIn = Files.readString(SymGenerator.TARGET, StandardCharsets.UTF_8).
        replace("\r", "");
    assertEquals(SymGenerator.generate(), checkedIn, "Sym.java is outdated. After regenerating " +
        "XQuerySyntaxParser.java, follow the steps of GrammarUpdate.");
  }

  /** Lookup of symbols. */
  @Test public void lookup() {
    assertEquals(Sym.SYMBOL_COUNT, Symbol.values().length);
    final BitSet seen = new BitSet();
    for(final Symbol symbol : Symbol.values()) {
      final int sym = symbol.isTerminal() ? Sym.terminal(symbol.symbol()) :
          Sym.nonterminal(symbol.symbol());
      assertSame(symbol, Symbol.of(sym), symbol.name());
      assertFalse(seen.get(sym), symbol.name());
      seen.set(sym);
      assertEquals(symbol.symbol(), Sym.symbol(sym));
      assertEquals(symbol.isTerminal(), Sym.isTerminal(sym));
    }
    assertEquals(Sym.SYMBOL_COUNT, seen.cardinality());
    assertEquals(Sym.QName, Sym.nonterminal("QName"));
    assertEquals(Sym.Q_NAME, Sym.terminal("QName"));
    assertEquals(Sym.LPAREN, Sym.terminal("'('"));
    assertEquals(Sym.FOR, Sym.terminal("'for'"));
    assertTrue(Sym.folds(Sym.AdditiveExpr));
    assertFalse(Sym.folds(Sym.FLWORExpr));
    assertFalse(Sym.folds(Sym.Q_NAME));
    assertThrows(IllegalArgumentException.class, () -> Sym.terminal("NoSuchToken"));
    assertThrows(IllegalArgumentException.class, () -> Sym.nonterminal("'for'"));
  }

  /** Mapping of the ids that the parser reports to symbols. */
  @Test public void ids() {
    for(int id = 0; id < XQuerySyntaxParser.TOKEN.length; id++) {
      final int sym = Sym.ofTerminal(id);
      assertTrue(Sym.isTerminal(sym), XQuerySyntaxParser.TOKEN[id]);
      assertEquals(XQuerySyntaxParser.TOKEN[id], Sym.symbol(sym));
    }
    for(int id = 0; id < XQuerySyntaxParser.NONTERMINAL.length; id++) {
      final int sym = Sym.ofNonterminal(id);
      assertFalse(Sym.isTerminal(sym), XQuerySyntaxParser.NONTERMINAL[id]);
      assertEquals(XQuerySyntaxParser.NONTERMINAL[id], Sym.symbol(sym));
    }
  }

  /** Sets of symbols. */
  @Test public void set() {
    assertTrue(Sym.set(Sym.FOR, Sym.LET).get(Sym.LET));
    assertFalse(Sym.set(Sym.FOR, Sym.LET).get(Sym.IF));
  }
}
