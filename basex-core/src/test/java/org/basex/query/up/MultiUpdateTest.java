package org.basex.query.up;

import static org.basex.query.QueryError.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * Test for multiple targets in update expressions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class MultiUpdateTest extends SandboxTest {
  /** Rename elements. */
  @Test public void renameElements() {
    final String input = "<xml><old/><old/></xml>";
    query(input + " update { rename nodes ./unknown as 'new' }",
        input);
    query(input + " update { rename nodes head(./*) as 'new' }",
        "<xml><new/><old/></xml>");
    query(input + " update { rename nodes tail(./*) as 'new' }",
        "<xml><old/><new/></xml>");
    query(input + " update { rename nodes ./* as 'new' }",
        "<xml><new/><new/></xml>");
    query(input + " update { rename nodes descendant-or-self::* as 'new' }",
        "<new><new/><new/></new>");

    query(input + " update { rename nodes descendant-or-self::* as QName('NEW', 'new') }",
        "<new xmlns=\"NEW\"><new/><new/></new>");
  }

  /** Rename elements with namespaces. */
  @Test public void renameElementsNS() {
    final String input = "<xml xmlns=\"x\"><old xmlns=\"y\"/><old xmlns=\"y\"/></xml>";
    query(input + " update { rename nodes ./*:unknown as QName('y', 'new') }",
        input);
    query(input + " update { rename nodes head(./*:old) as QName('y', 'new') }",
        "<xml xmlns=\"x\"><new xmlns=\"y\"/><old xmlns=\"y\"/></xml>");
    query(input + " update { rename nodes tail(./*:old) as QName('y', 'new') }",
        "<xml xmlns=\"x\"><old xmlns=\"y\"/><new xmlns=\"y\"/></xml>");
    query(input + " update { rename nodes ./*:old as QName('y', 'new') }",
        "<xml xmlns=\"x\"><new xmlns=\"y\"/><new xmlns=\"y\"/></xml>");

    query(input + " update { rename nodes descendant-or-self::Q{y}* as QName('y', 'new') }",
        "<xml xmlns=\"x\"><new xmlns=\"y\"/><new xmlns=\"y\"/></xml>");
  }

  /** Rename attributes. */
  @Test public void renameAttributes() {
    final String input = "<xml old=\"\"><sub old=\"\"/></xml>";
    query(input + " update { rename nodes head(.//@unknown) as 'new' }",
        input);
    query(input + " update { rename nodes head(.//@old) as 'new' }",
        "<xml new=\"\"><sub old=\"\"/></xml>");
    query(input + " update { rename nodes tail(.//@old) as 'new' }",
        "<xml old=\"\"><sub new=\"\"/></xml>");
    query(input + " update { rename nodes .//@old as 'new' }",
        "<xml new=\"\"><sub new=\"\"/></xml>");

    query("declare namespace new = 'NEW';" +
        input + " update { rename nodes .//@* as xs:QName('new:new') }",
        "<xml xmlns:new=\"NEW\" new:new=\"\"><sub new:new=\"\"/></xml>");
  }

  /** Rename processing instructions. */
  @Test public void renamePIs() {
    final String input = "<xml><?old ?><?old ?></xml>";
    query(input + " update { rename nodes ./processing-instruction(unknown) as 'new' }",
        input);
    query(input + " update { rename nodes head(./processing-instruction()) as 'new' }",
        "<xml><?new ?><?old ?></xml>");
    query(input + " update { rename nodes tail(./processing-instruction()) as 'new' }",
        "<xml><?old ?><?new ?></xml>");
    query(input + " update { rename nodes ./processing-instruction() as 'new' }",
        "<xml><?new ?><?new ?></xml>");
  }

  /** Rename mixed nodes. */
  @Test public void renameMixed() {
    final String input = "<element attribute=''><?pi ?></element>";
    query(input + " update { rename nodes (., @*, node()) as 'new' }",
        "<new new=\"\"><?new ?></new>");
  }

  /** Rename: errors. */
  @Test public void renameErrors() {
    final String input = "<xml xmlns=\"OLD\"><!--X-->bla</xml>";
    query(input + " update { rename nodes ./*/comment() as 'new' }", input);
    query(input + " update { rename nodes ./*/text() as 'new' }", input);
    query(input + " update { rename nodes ./* as QName('NEW', 'new') }", input);

    error(input + " update { rename nodes 'x' as 'new' }", UPWRTRGTYP_X);
    error(input + " update { rename nodes ./comment() as 'new' }", UPWRTRGTYP_X);
    error(input + " update { rename nodes ./text() as 'new' }", UPWRTRGTYP_X);
    error(input + " update { rename nodes . as QName('NEW', 'new') }", UPNSCONFL_X_X);
  }

  /** Replace elements. */
  @Test public void replaceElements() {
    final String input = "<xml><old/><old/></xml>";
    query(input + " update { replace nodes ./unknown with <new/> }",
        input);
    query(input + " update { replace nodes head(./*) with <new/> }",
        "<xml><new/><old/></xml>");
    query(input + " update { replace nodes tail(./*) with <new/> }",
        "<xml><old/><new/></xml>");
    query(input + " update { replace nodes ./* with <new/> }",
        "<xml><new/><new/></xml>");

    query(input + " update { replace nodes ./* with (<new1/>, <new2/>) }",
        "<xml><new1/><new2/><new1/><new2/></xml>");
    query(input + " update { replace nodes ./* with () }",
        "<xml/>");

    query(input + " update { replace nodes ./* with element Q{NEW}new {} }",
        "<xml><new xmlns=\"NEW\"/><new xmlns=\"NEW\"/></xml>");
  }

  /** Replace elements with namespaces. */
  @Test public void replaceElementsNS() {
    final String decl = "declare namespace new = 'NEW'; ";
    final String input = "<xml xmlns=\"x\"><old xmlns=\"y\"/><old xmlns=\"y\"/></xml>";
    query(decl + input + " update { replace nodes ./*:unknown with <new:new/> }",
        input);
    query(decl + input + " update { replace nodes head(./*:old) with <new:new/> }",
        "<xml xmlns=\"x\"><new:new xmlns:new=\"NEW\"/><old xmlns=\"y\"/></xml>");
    query(decl + input + " update { replace nodes tail(./*:old) with <new:new/> }",
        "<xml xmlns=\"x\"><old xmlns=\"y\"/><new:new xmlns:new=\"NEW\"/></xml>");
    query(decl + input + " update { replace nodes ./*:old with <new:new/> }",
        "<xml xmlns=\"x\"><new:new xmlns:new=\"NEW\"/><new:new xmlns:new=\"NEW\"/></xml>");
  }

  /** Replace attributes. */
  @Test public void replaceAttributes() {
    final String input = "<xml old=\"\"><sub old=\"\"/></xml>";
    query(input + " update { replace nodes head(.//@unknown) with attribute new {} }",
        input);
    query(input + " update { replace nodes head(.//@old) with attribute new {} }",
        "<xml new=\"\"><sub old=\"\"/></xml>");
    query(input + " update { replace nodes tail(.//@old) with attribute new {} }",
        "<xml old=\"\"><sub new=\"\"/></xml>");
    query(input + " update { replace nodes .//@old with attribute new {} }",
        "<xml new=\"\"><sub new=\"\"/></xml>");

    query(input + " update { replace nodes .//@old with (attribute new1 {}, attribute new2 {}) }",
        "<xml new1=\"\" new2=\"\"><sub new1=\"\" new2=\"\"/></xml>");
    query(input + " update { replace nodes .//@old with () }",
        "<xml><sub/></xml>");

    query(input + " update { replace nodes .//@* with attribute Q{NEW}new {} }",
        "<xml xmlns:ns0=\"NEW\" ns0:new=\"\"><sub ns0:new=\"\"/></xml>");
  }

  /** Replace mixed nodes. */
  @Test public void replaceMixed() {
    final String input = "<xml><element attribute=''/><?pi ?></xml>";
    query(input + " update { replace nodes ./node() with <new/> }",
        "<xml><new/><new/></xml>");
    query(input + " update { replace nodes ./node() with 'new' }",
        "<xml>newnew</xml>");
  }

  /** Replace: errors. */
  @Test public void replaceErrors() {
    final String input = "<xml xmlns=\"OLD\" old=\"\"><!--X-->bla</xml>";
    query(input + " update { replace nodes * with <x/> }", input);
    query(input + " update { replace nodes ./*/text() with <x/> }", input);

    error("document {} update { replace nodes . with <x/> }", UPTRGNODE_X);
    error(input + " update { replace nodes 'x' with <x/> }", UPTRGNODE_X);
    error(input + " update { replace nodes node() with attribute invalid {} }", UPWRELM_X);
    error(input + " update { replace nodes @* with <invalid/> }", UPWRATTR_X);
    error(input + " update { replace nodes . with <invalid/> }", UPNOPAR_X);
  }

  /** Replace value of elements. */
  @Test public void replaceValueOfElements() {
    final String input = "<xml><old/><old/></xml>";
    query(input + " update { replace value of nodes ./unknown with 'NEW' }",
        input);
    query(input + " update { replace value of nodes head(./*) with 'NEW' }",
        "<xml><old>NEW</old><old/></xml>");
    query(input + " update { replace value of nodes tail(./*) with 'NEW' }",
        "<xml><old/><old>NEW</old></xml>");
    query(input + " update { replace value of nodes ./* with 'NEW' }",
        "<xml><old>NEW</old><old>NEW</old></xml>");

    query(input + " update { replace value of nodes ./* with <NEW/> }",
        input);
    query(input + " update { replace value of nodes ./* with attribute new {} }",
        input);

    query(input + " update { replace value of nodes ./* with ('NEW1', 'NEW2') }",
        "<xml><old>NEW1 NEW2</old><old>NEW1 NEW2</old></xml>");
    query(input + " update { replace value of nodes ./* with () }",
        input);
  }

  /** Replace value of other nodes. */
  @Test public void replaceValueOfOthers() {
    final String input = "<xml><!--X--><?pi ?></xml>";
    query(input + " update { replace value of nodes comment() with 'NEW' }",
        "<xml><!--NEW--><?pi ?></xml>");
    query(input + " update { replace value of nodes processing-instruction() with 'NEW' }",
        "<xml><!--X--><?pi NEW?></xml>");
    query(input + " update { replace value of nodes node() with 'NEW' }",
        "<xml><!--NEW--><?pi NEW?></xml>");
  }

  /** Insert into elements. */
  @Test public void insertIntoElements() {
    final String input = "<xml><old/><old/></xml>";
    query(input + " update { insert nodes <new/> into unknown }",
        input);
    query(input + " update { insert nodes <new/> into head(./*) }",
        "<xml><old><new/></old><old/></xml>");
    query(input + " update { insert nodes <new/> into tail(./*) }",
        "<xml><old/><old><new/></old></xml>");
    query(input + " update { insert nodes <new/> into ./* }",
        "<xml><old><new/></old><old><new/></old></xml>");

    query(input + " update { insert nodes (<new1/>, <new2/>) into ./* }",
        "<xml><old><new1/><new2/></old><old><new1/><new2/></old></xml>");
    query(input + " update { insert nodes () into ./* }",
        input);

    query(input + " update { insert nodes element Q{NEW}new {} into ./* }",
        "<xml><old><new xmlns=\"NEW\"/></old><old><new xmlns=\"NEW\"/></old></xml>");
  }

  /** Insert into attributes. */
  @Test public void insertIntoAttributes() {
    final String input = "<xml><old/><old/></xml>";
    query(input + " update { insert nodes attribute new {} into unknown }",
        input);
    query(input + " update { insert nodes attribute new {} into head(./*) }",
        "<xml><old new=\"\"/><old/></xml>");
    query(input + " update { insert nodes attribute new {} into tail(./*) }",
        "<xml><old/><old new=\"\"/></xml>");
    query(input + " update { insert nodes attribute new {} into ./* }",
        "<xml><old new=\"\"/><old new=\"\"/></xml>");

    query(input + " update { insert nodes (attribute new1 {}, attribute new2 {}) into ./* }",
        "<xml><old new1=\"\" new2=\"\"/><old new1=\"\" new2=\"\"/></xml>");
    query(input + " update { insert nodes () into @* }",
        input);
  }

  /** Insert into: errors. */
  @Test public void insertIntoErrors() {
    final String input = "<xml xmlns=\"OLD\" old=\"\"><!--X-->bla</xml>";
    query(input + " update { insert nodes <x/> into * }", input);
    query(input + " update { insert nodes <x/> into ./*/text() }", input);

    error(input + " update { insert nodes <x/> into @* }", UPTRGTYP_X);
    error(input + " update { insert nodes <x/> into 'x' }", UPTRGTYP_X);

    error("document {} update { insert nodes attribute new {} into . }", UPATTELM2_X);
  }
}
