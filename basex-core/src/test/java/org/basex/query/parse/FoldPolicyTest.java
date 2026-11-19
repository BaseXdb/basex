package org.basex.query.parse;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

import org.junit.jupiter.api.Test;

/**
 * Verifies {@link FoldPolicy} against the XML form of the grammar:
 * <ul>
 * <li>The EBNF must be the one that was reviewed last, and the symbols of the parser must be
 *   productions in the XML.</li>
 * <li>The nonterminals computed by {@link GrammarXml#foldable()} must be the listed ones.</li>
 * <li>The productions of the listed nonterminals must match the signatures recorded at the last
 *   review. If one changes, review the treatment of the nonterminal, then update the signature.
 *   </li>
 * </ul>
 * Signatures are SHA-256 digests of the serialized productions, recorded for all nonterminals so
 * that {@link #changes()} can tell what a new grammar has changed. The test relies on the JDK
 * only, as the parsers may be out of date. After a change of the EBNF, run {@link GrammarUpdate}.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class FoldPolicyTest {
  /** EBNF of the grammar, from which the parser and the XML form are generated. */
  private static final Path EBNF = Paths.get(
      "src/test/resources/org/basex/query/parse/XQuery-Full-Text-Update-BaseX-40.ebnf");
  /** Nonterminals whose productions are covered by the recorded signatures. */
  private static final Set<String> REVIEWED = new TreeSet<>();

  static {
    REVIEWED.addAll(FoldPolicy.NAMES);
    REVIEWED.addAll(FoldPolicy.LISTS);
    REVIEWED.addAll(FoldPolicy.VANISH);
  }

  /** Key of the digest of the EBNF in the recorded signatures. */
  private static final String EBNF_KEY = "grammar.sha256";
  /** Key of the format of the recorded signatures, which depends on {@link GrammarXml}. */
  private static final String FORMAT_KEY = "signature.format";
  /** Current format of the recorded signatures. */
  private static final String FORMAT = "2";
  /** XML form of the grammar. */
  private static final Path GRAMMAR = Paths.get(
      "src/test/resources/org/basex/query/parse/XQuerySyntaxParser.xml");
  /** Recorded signatures. */
  private static final Path SIGNATURES = Paths.get(
      "src/test/resources/org/basex/query/parse/fold-signatures.properties");
  /**
   * Checks that the EBNF is the one that was reviewed, and that parser and XML belong together.
   * @throws Exception exception
   */
  @Test public void grammar() throws Exception {
    final Properties recorded = recorded();
    if(!grammarDigest().equals(recorded.getProperty(EBNF_KEY))) {
      fail("The grammar has changed since GrammarUpdate was run last. Run it again.");
    }

    // the symbols of the parser must be productions of the XML form
    final Set<String> productions = new GrammarXml(GRAMMAR).names();
    final Class<?> parser = Class.forName("org.basex.query.parse.gen.XQuerySyntaxParser");
    for(final String field : new String[] { "TOKEN", "NONTERMINAL" }) {
      final Field f = parser.getDeclaredField(field);
      f.setAccessible(true);
      for(final String symbol : (String[]) f.get(null)) {
        // literal tokens and internal symbols are not named in the grammar
        if(Character.isLetter(symbol.charAt(0)) && !SymGenerator.INTERNAL.contains(symbol)) {
          if(!productions.contains(symbol)) {
            fail("XQuerySyntaxParser.xml does not match XQuerySyntaxParser.java: symbol " +
                symbol + " is not a production.");
          }
        }
      }
    }
  }

  /**
   * Compares the foldable nonterminals with the ones that are computed from the grammar.
   * @throws Exception exception
   */
  @Test public void names() throws Exception {
    same("FoldPolicy.NAMES", "the foldable nonterminals of the grammar", FoldPolicy.NAMES,
        new GrammarXml(GRAMMAR).foldable());
  }

  /**
   * Checks that all left-recursive productions are listed in {@link FoldPolicy#LISTS}.
   * @throws Exception exception
   */
  @Test public void lists() throws Exception {
    same("FoldPolicy.LISTS", "the left-recursive productions of the grammar", FoldPolicy.LISTS,
        new GrammarXml(GRAMMAR).leftRecursive());
  }

  /**
   * Compares the signatures of the nonterminals of the policy.
   * @throws Exception exception
   */
  @Test public void signatures() throws Exception {
    final Map<String, String> actual = digests();
    final TreeSet<String> unknown = new TreeSet<>(REVIEWED);
    unknown.removeAll(actual.keySet());
    if(!unknown.isEmpty()) fail("Reviewed nonterminals not in the grammar: " + unknown + '.');

    if(Boolean.getBoolean("fold.update")) {
      final String nl = System.lineSeparator();
      final StringBuilder sb = new StringBuilder("# see FoldPolicyTest" + nl);
      sb.append(EBNF_KEY).append('=').append(grammarDigest()).append(nl);
      sb.append(FORMAT_KEY).append('=').append(FORMAT).append(nl);
      actual.forEach((k, v) -> sb.append(k).append('=').append(v).append(nl));
      Files.writeString(SIGNATURES, sb.toString(), StandardCharsets.UTF_8);
      return;
    }
    final Properties recorded = recorded();
    if(!FORMAT.equals(recorded.getProperty(FORMAT_KEY))) {
      fail("The format of the recorded signatures is outdated. Run GrammarUpdate.");
    }
    final TreeSet<String> changed = new TreeSet<>();
    for(final String name : REVIEWED) {
      if(!actual.get(name).equals(recorded.getProperty(name))) changed.add(name);
    }
    if(!changed.isEmpty()) {
      fail("Productions changed since GrammarUpdate was run last: " + changed +
          ". Run it again, and review the changes.");
    }
  }

  /**
   * Describes what the grammar has changed since the signatures were recorded.
   * @return description, one line per kind of change
   * @throws Exception exception
   */
  static List<String> changes() throws Exception {
    final Properties recorded = recorded();
    if(!FORMAT.equals(recorded.getProperty(FORMAT_KEY))) {
      return List.of("Not available: the recorded signatures are in an outdated format.");
    }
    final Map<String, String> actual = digests();
    final TreeSet<String> added = new TreeSet<>(actual.keySet()), removed = new TreeSet<>(),
        changed = new TreeSet<>();
    for(final String name : recorded.stringPropertyNames()) {
      if(name.equals(EBNF_KEY) || name.equals(FORMAT_KEY)) continue;
      if(!added.remove(name)) removed.add(name);
      else if(!actual.get(name).equals(recorded.getProperty(name))) changed.add(name);
    }
    final TreeSet<String> policy = new TreeSet<>(REVIEWED);
    policy.retainAll(changed);
    final TreeSet<String> gone = new TreeSet<>(REVIEWED);
    gone.retainAll(removed);
    final List<String> lines = new ArrayList<>(List.of("added nonterminals:   " + added,
        "removed nonterminals: " + removed, "changed nonterminals: " + changed));
    lines.add(policy.isEmpty() && gone.isEmpty() ?
      "none of the nonterminals of FoldPolicy has changed" :
      "REVIEW  nonterminals of FoldPolicy: changed " + policy + ", removed " + gone);
    return lines;
  }

  /**
   * Computes the digests of the signatures of all nonterminals.
   * @return digests by name
   * @throws Exception exception
   */
  private static Map<String, String> digests() throws Exception {
    final GrammarXml xml = new GrammarXml(GRAMMAR);
    final Map<String, String> signatures = new TreeMap<>();
    for(final Map.Entry<String, String> entry : xml.signatures(xml.names()).entrySet()) {
      signatures.put(entry.getKey(), digest(entry.getValue()));
    }
    return signatures;
  }

  /**
   * Reads the recorded signatures.
   * @return properties
   * @throws IOException I/O exception
   */
  private static Properties recorded() throws IOException {
    final Properties recorded = new Properties();
    try(Reader reader = Files.newBufferedReader(SIGNATURES, StandardCharsets.UTF_8)) {
      recorded.load(reader);
    }
    return recorded;
  }

  /**
   * Checks that a policy contains the nonterminals that were computed from the grammar.
   * @param name name of the policy
   * @param source description of the computation
   * @param listed nonterminals of the policy
   * @param computed computed nonterminals
   */
  private static void same(final String name, final String source, final Collection<String> listed,
      final Collection<String> computed) {
    final TreeSet<String> missing = new TreeSet<>(computed), obsolete = new TreeSet<>(listed);
    missing.removeAll(listed);
    obsolete.removeAll(computed);
    if(!missing.isEmpty() || !obsolete.isEmpty()) {
      fail(name + " differs from " + source + ". To be added: " + missing + ". To be removed: " +
          obsolete + '.');
    }
  }

  /**
   * Computes the digest of the EBNF, independent of line endings.
   * @return hexadecimal digest
   * @throws Exception exception
   */
  private static String grammarDigest() throws Exception {
    return digest(Files.readString(EBNF, StandardCharsets.UTF_8).replace("\r", ""));
  }

  /**
   * Computes a short digest.
   * @param string string
   * @return hexadecimal digest
   * @throws GeneralSecurityException security exception
   */
  private static String digest(final String string) throws GeneralSecurityException {
    final byte[] bytes = MessageDigest.getInstance("SHA-256").digest(
        string.getBytes(StandardCharsets.UTF_8));
    return HexFormat.of().formatHex(bytes).substring(0, 16);
  }
}
