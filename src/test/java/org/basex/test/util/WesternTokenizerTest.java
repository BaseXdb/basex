package org.basex.test.util;

import static org.junit.Assert.*;

import static org.basex.util.Token.*;

import org.basex.util.ft.FTFlag;
import org.basex.util.ft.FTOpt;
import org.basex.util.ft.WesternTokenizer;
import org.junit.Test;

/**
 * Tests for {@link WesternTokenizer}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public final class WesternTokenizerTest {
  /** Case sensitive. */
  private static final int FTCS = 1;
  /** Diacritics. */
  private static final int FTDC = 2;
  /** Lower case. */
  private static final int FTLC = 4;
  /** Upper case. */
  private static final int FTUC = 8;
  /** Wild cards. */
  private static final int FTWC = 16;

  /** Full-text options to use. */
  private final FTOpt opt = new FTOpt();

  /** Test text to tokenize. */
  private static final byte[] TEXT = token("\\Tést.*\\tÄSte\\\\Toast\\.");

  /** Test case insensitive. */
  @Test
  public void cI() {
    run(TEXT, "test", "taste", "toast");
  }

  /** Test case sensitive. */
  @Test
  public void cS() {
    setFTFlags(FTCS);
    run(TEXT, "Test", "tASte", "Toast");
  }

  /** Test lower case. */
  @Test
  public void lC() {
    setFTFlags(FTCS | FTLC);
    run(TEXT, "test", "taste", "toast");
  }

  /** Test upper case. */
  @Test
  public void uC() {
    setFTFlags(FTCS | FTUC);
    run(TEXT, "TEST", "TASTE", "TOAST");
  }

  /** Test diactrics + case insensitive. */
  @Test
  public void diaCI() {
    setFTFlags(FTDC);
    run(TEXT, "tést", "täste", "toast");
  }

  /** Test diactrics + case sensitive. */
  @Test
  public void diaCS() {
    setFTFlags(FTDC | FTCS);
    run(TEXT, "Tést", "tÄSte", "Toast");
  }

  /** Test diactrics + lower case. */
  @Test
  public void diaLC() {
    setFTFlags(FTDC | FTCS | FTLC);
    run(TEXT, "tést", "täste", "toast");
  }

  /** Test diactrics + upper case. */
  @Test
  public void diaUC() {
    setFTFlags(FTDC | FTCS | FTUC);
    run(TEXT, "TÉST", "TÄSTE", "TOAST");
  }

  /** Test wild cards + case insensitive. */
  @Test
  public void wildCardsCI() {
    setFTFlags(FTWC);
    run(TEXT, "\\test.*\\taste", "toast");
  }

  /** Test wild cards + case sensitive. */
  @Test
  public void wildCardsCS() {
    setFTFlags(FTWC | FTCS);
    run(TEXT, "\\Test.*\\tASte", "Toast");
  }

  /** Test wild cards + lower case. */
  @Test
  public void wildCardsLC() {
    setFTFlags(FTWC | FTCS | FTLC);
    run(TEXT, "\\test.*\\taste", "toast");
  }

  /** Test wild cards + upper case. */
  @Test
  public void wildCardsUC() {
    setFTFlags(FTWC | FTCS | FTUC);
    run(TEXT, "\\TEST.*\\TASTE", "TOAST");
  }

  /** Test wild cards + diactrics + case insensitive. */
  @Test
  public void wildCardsDiaCI() {
    setFTFlags(FTWC | FTDC);
    run(TEXT, "\\tést.*\\täste", "toast");
  }

  /** Test wild cards + diactrics + case sensitive. */
  @Test
  public void wildCardsDiaCS() {
    setFTFlags(FTWC | FTDC | FTCS);
    run(TEXT, "\\Tést.*\\tÄSte", "Toast");
  }

  /** Test wild cards + diactrics + lower case. */
  @Test
  public void wildCardsDiaLC() {
    setFTFlags(FTWC | FTDC | FTCS | FTLC);
    run(TEXT, "\\tést.*\\täste", "toast");
  }

  /** Test wild cards + diactrics + upper case. */
  @Test
  public void wildCardsDiaUC() {
    setFTFlags(FTWC | FTDC | FTCS | FTUC);
    run(TEXT, "\\TÉST.*\\TÄSTE", "TOAST");
  }

  /**
   * Perform tokenization test.
   * @param input input text to tokenize
   * @param tokens expected tokens
   */
  private void run(final byte[] input, final String... tokens) {
    final WesternTokenizer tok = new WesternTokenizer(opt);
    tok.init(input);
    int i = -1;
    while(tok.hasNext()) {
      assertTrue(eq(tok.nextToken(), token(tokens[++i])));
    }
  }

  /**
   * Set the full-text option flags.
   * @param flags bit mask with full-text flags
   */
  private void setFTFlags(final int flags) {
    if((flags & FTCS) != 0) opt.set(FTFlag.CS, true);
    if((flags & FTDC) != 0) opt.set(FTFlag.DC, true);
    if((flags & FTLC) != 0) opt.set(FTFlag.LC, true);
    if((flags & FTUC) != 0) opt.set(FTFlag.UC, true);
    if((flags & FTWC) != 0) opt.set(FTFlag.WC, true);
  }
}
