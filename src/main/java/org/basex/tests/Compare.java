package org.basex.tests;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.ByteList;
import org.basex.util.list.StringList;

/**
 * This class compares query results with other query processors.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Compare {
  /** XSLT flag; not set to 'final' to circumvent dead-code warnings. */
  private static boolean xsltMode;
  /** Verbose mode. */
  private static final boolean VERBOSE = true;

  /** Database context. */
  private static final Context CONTEXT = new Context();

  /** XSLT skeleton. */
  private static final String XSLT =
    "<xsl:stylesheet version='2.0' " +
    "xmlns:xsl='http://www.w3.org/1999/XSL/Transform' " +
    "xmlns:xs ='http://www.w3.org/2001/XMLSchema'>" +
    "<xsl:template match='/'><xsl:value-of select=\"%\" />" +
    "</xsl:template></xsl:stylesheet>";
  /** Query file . */
  private static final String QUERIES = "etc/queries.txt";
  /** Temporary XSLT context. */
  private static final String TMPCTX = Prop.TMP + "queries.xml";
  /** Temporary query file . */
  private static final String TMP = Prop.TMP + "queries.tmp";
  /** Query processors. */
  private static final String[][] XQUERYPROCS = {
    { "Saxon", "java", "-cp",
      "c:/Program Files/Saxon;c:/Program Files/Saxon/saxon9ee.jar",
      "net.sf.saxon.Query", "-qversion:1.1", "-sa", TMP,
      "!omit-xml-declaration=yes" },
    /*{ "Saxon", "java", "-cp", "c:/Program Files/Saxon/saxon9he.jar",
      "net.sf.saxon.Query", TMP, "!omit-xml-declaration=yes" }, */
    /*{ "XQSharp", "c:/Program Files (x86)/XQSharp/xquery.exe",
      TMP, "!omit-xml-declaration=yes" } */
  };
  /** XSLT processors. */
  private static final String[][] XSLTPROCS = {
    { "Saxon", "java", "-jar", "c:/Program Files/Saxon/saxon9he.jar",
      TMPCTX, TMP, "!omit-xml-declaration=yes" },
    { "XQShp", "c:/Program Files (x86)/XQSharp/xslt.exe",
      "-s", TMPCTX, TMP, "!omit-xml-declaration=yes" }
  };
  /** Selected processors. */
  private static final String[][] PROCS = xsltMode ? XSLTPROCS : XQUERYPROCS;

  /**
   * Main method of the test class.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public static void main(final String[] args) throws Exception {
    // write temporary XSLT query context to disk
    if(xsltMode) new IOFile(TMPCTX).write(Token.token("<x/>"));

    // loop through all queries
    final BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(QUERIES), Token.UTF8));
    while(true) {
      final String line = br.readLine();
      if(line == null) break;
      if(!line.isEmpty() && line.charAt(0) != '#') query(line);
    }
    br.close();

    // delete temporary files
    new IOFile(TMP).delete();
    new IOFile(TMPCTX).delete();
  }

  /**
   * Runs a single query on all processors.
   * @param query query input
   * @throws Exception exception
   */
  public static void query(final String query) throws Exception {
    final StringList sl = new StringList();
    String result = "";
    try {
      result = new XQuery(query).execute(CONTEXT);
    } catch(final BaseXException ex) {
      result = ex.getMessage().replaceAll("[\\r\\n]+", " ");
    }

    // write XQuery or XSLT to temporary file
    new IOFile(TMP).write(xsltMode ? Util.inf(XSLT, query) :
      Token.token(query));

    boolean same = true;
    for(final String[] proc : PROCS) {
      final String res = execute(proc);
      same &= result.equals(res);
      sl.add(res);
    }

    if(!same) {
      Util.outln("Query: %", query);
      Util.outln("BaseX: %", result);
      for(int p = 0; p < PROCS.length; p++) {
        Util.outln("%: %", PROCS[p][0], sl.get(p));
      }
      Util.outln();
    } else if(VERBOSE) {
      Util.outln("Query: %", query);
      Util.outln("Result: %", result);
      Util.outln();
    }
  }

  /**
   * Runs a single processor.
   * @param proc query processor
   * @return string result
   */
  public static String execute(final String[] proc) {
    try {
      final ArrayList<String> al = new ArrayList<String>();
      al.addAll(Arrays.asList(proc).subList(1, proc.length));

      final ProcessBuilder pb = new ProcessBuilder(al);
      pb.redirectErrorStream(true);
      final Process pr = pb.start();
      final InputStream is = pr.getInputStream();
      final ByteList  bl = new ByteList();
      while(true) {
        final int t = is.read();
        if(t == -1) break;
        bl.add(t);
      }
      return bl.toString().replaceAll("[\\r\\n ]+", " ");
    } catch(final IOException ex) {
      return ex.getMessage();
    }
  }
}
