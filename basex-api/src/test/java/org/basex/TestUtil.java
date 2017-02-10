package org.basex;

import static org.basex.core.Text.*;
import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HttpText.*;
import static org.junit.Assert.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.QueryError.ErrType;
import org.basex.query.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.http.HttpRequest.Part;
import org.junit.*;
import org.junit.Test;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.builder.Input;

/**
 * This is a common utility class for tests.
 *
 * @author BaseX Team 2005-17, BSD License
 */
public class TestUtil {

  /**
   * Compare that expected and returned strings have the same parts.
   *
   * E.g. "Host,Accept,Connection,User-Agent"
   * should match with: "Accept,Connection,User-Agent,Host"
   *
   * @param exp expected value as String
   * @param ret returned value as String
   * @return boolean
   */
  public static boolean stringPartsMatch(String exp, String ret) {
    String[] expParts = exp.split(",");
    String[] retParts = ret.split(",");

    if (expParts.length != retParts.length) {
      System.err.println("Lengths differ :: " + expParts.length + ", " + retParts.length);
      return false;
    }

    for(int i = 0; i < expParts.length; i++) {
      boolean found = false;
      for(int j = 0; j < retParts.length; j++) {
        if (expParts[i].trim().equals(retParts[j].trim())) {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compare that expected and returned values match.
   *
   * E.g. <http:multipart media-type="multipart/mixed" boundary="simple boundary">
   * should match with: <http:multipart boundary="simple boundary" media-type="multipart/mixed">
   *
   * @param exp expected value as String
   * @param ret returned value as String
   * @return boolean
   */
  public static boolean expectedAndReturnedLinesMatch(String exp, String ret, String xmlns) {
    String[] expLines = exp.split("\n");
    String[] retLines = ret.split("\n");

    if (expLines.length != retLines.length) {
      System.err.println("Lengths differ :: " + expLines.length + ", " + retLines.length);
      return false;
    }

    for(int i = 0; i < expLines.length; i++) {
      boolean found = false;
      for(int j = 0; j < retLines.length; j++) {
        if (expLines[i].equals(retLines[j])) {
          found = true;
          break;
        } else if (expLines[i].trim().startsWith("<") && expLines[i].trim().endsWith(">")
                    && retLines[j].trim().startsWith("<") && retLines[j].trim().endsWith(">")
                    && !expLines[i].trim().startsWith("</") && !retLines[j].trim().startsWith("</")) {

          // Find end tag for expected if not self closing element
          String endTagExp = "";
          if (!expLines[i].trim().endsWith("/>")) {
            endTagExp = (expLines[i].trim().split(" "))[0] + ">";
            endTagExp = endTagExp.trim().substring(0, 1) + "/" + endTagExp.trim().substring(1, endTagExp.trim().length());
          }

          // Find end tag for returned if not self closing element
          String endTagRet = "";
          if (!retLines[j].trim().endsWith("/>")) {
            endTagRet = (retLines[j].trim().split(" "))[0] + ">";
            endTagRet = endTagRet.trim().substring(0, 1) + "/" + endTagRet.trim().substring(1, endTagRet.trim().length());
          }

          // Bind xmlns for expected line, if not already bound
          if (!expLines[i].contains("xmlns:")) {
            expLines[i] = expLines[i].trim().substring(0, expLines[i].trim().indexOf(" ")) + 
                " " + xmlns + " " + 
                expLines[i].trim().substring(expLines[i].trim().indexOf(" "), expLines[i].trim().length());
          }

          // Bind xmlns for returned line, if not already bound
          if (!retLines[j].contains("xmlns:")) {
            retLines[j] = retLines[j].trim().substring(0, retLines[j].trim().indexOf(" ")) + 
                " " + xmlns + " " + 
                retLines[j].trim().substring(retLines[j].trim().indexOf(" "), retLines[j].trim().length());
          }
          
          Diff responseDiff = DiffBuilder.compare(Input.fromString(expLines[i] + endTagExp))
            .checkForSimilar()
            .ignoreWhitespace()
            .withTest(Input.fromString(retLines[j] + endTagRet))
            .build();

          if (!responseDiff.hasDifferences()) {
            found = true;
            break;
          }
        }
      }
      if (found == false) {
        System.err.println("NOT FOUND :: " + expLines[i]);
        return false;
      }
    }
    return true;
  }

  /**
   * Serialize Value to String.
   * @param value BaseX Value
   * @throws Exception exception
   */
  public static String serializeValue(final Value value) throws Exception {
    final long es = value.size();
    final TokenBuilder result = new TokenBuilder();
    for(int e = 0; e < es; e++) {
      final Item item = value.itemAt(e);
      result.addExt(item.serialize());
    }
    return result.toString();
  }
  
  /**
   * Compares results.
   * @param expected expected result
   * @param returned returned result
   * @throws Exception exception
   */
  public static void compare(final Value expected, final Value returned) throws Exception {
    // Compare response with expected result
    assertEquals("Different number of results", expected.size(), returned.size());

    final long es = expected.size();
    for(int e = 0; e < es; e++) {
      final Item exp = expected.itemAt(e), ret = returned.itemAt(e);
      if(!new DeepEqual().equal(exp, ret)) {
        final TokenBuilder tb = new TokenBuilder("Result ").addLong(e).add(" differs:\nReturned: ");
        tb.addExt(ret.serialize()).add("\nExpected: ").addExt(exp.serialize());
        fail(tb.toString());
      }
    }
  }
}