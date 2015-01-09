package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the SwitchExpr production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdSwitchExpr extends QT3TestSet {

  /**
   *  basic switch example .
   */
  @org.junit.Test
  public void switch001() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $animal as xs:string := \"Cat\"; \n" +
      "        <out>{ switch ($animal) \n" +
      "            case \"Cow\" return \"Moo\" \n" +
      "            case \"Cat\" return \"Meow\" \n" +
      "            case \"Duck\" return \"Quack\" \n" +
      "            default return \"What's that odd noise?\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Meow</out>", false)
    );
  }

  /**
   *  basic switch example, matches default clause .
   */
  @org.junit.Test
  public void switch002() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $animal as xs:string := \"Dog\"; \n" +
      "        <out>{ switch ($animal) \n" +
      "            case \"Cow\" return \"Moo\"\n" +
      "            case \"Cat\" return \"Meow\" \n" +
      "            case \"Duck\" return \"Quack\" \n" +
      "            default return \"What's that odd noise?\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>What's that odd noise?</out>", false)
    );
  }

  /**
   *  switch test, non-constant case clause .
   */
  @org.junit.Test
  public void switch003() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $animal as xs:string := \"!?!?\"; \n" +
      "        <out>{ switch (upper-case($animal)) \n" +
      "            case \"COW\" return \"Moo\"\n" +
      "            case \"CAT\" return \"Meow\" \n" +
      "            case \"DUCK\" return \"Quack\" \n" +
      "            case lower-case($animal) return \"Oink\" \n" +
      "            default return \"What's that odd noise?\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Oink</out>", false)
    );
  }

  /**
   *  switch test, multiple case clauses .
   */
  @org.junit.Test
  public void switch004() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $animal as xs:string := \"goose\"; \n" +
      "        <out>{ switch (upper-case($animal)) \n" +
      "            case \"COW\" return \"Moo\" \n" +
      "            case \"CAT\" return \"Meow\" \n" +
      "            case \"DUCK\" case \"GOOSE\" return \"Quack\" \n" +
      "            case \"PIG\" case \"SWINE\" return \"Oink\" \n" +
      "            default return \"What's that odd noise?\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Quack</out>", false)
    );
  }

  /**
   *  switch test, multiple case clauses .
   */
  @org.junit.Test
  public void switch005() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $animal as xs:string := \"duck\"; \n" +
      "        <out>{ switch (upper-case($animal)) \n" +
      "            case \"COW\" return \"Moo\" \n" +
      "            case \"CAT\" return \"Meow\" \n" +
      "            case \"DUCK\" case \"GOOSE\" return \"Quack\" \n" +
      "            case \"PIG\" case \"SWINE\" return \"Oink\" \n" +
      "            default return \"What's that odd noise?\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Quack</out>", false)
    );
  }

  /**
   *  switch test, numeric case clauses, no type error .
   */
  @org.junit.Test
  public void switch006() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $number as xs:decimal := 42; \n" +
      "        <out>{ switch ($number) case 21 return \"Moo\" \n" +
      "            case current-time() return \"Meow\" \n" +
      "            case 42 return \"Quack\" \n" +
      "            default return 3.14159 }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Quack</out>", false)
    );
  }

  /**
   *  switch test, untypedAtomic is converted to string .
   */
  @org.junit.Test
  public void switch007() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := <a>42</a>; \n" +
      "        <out>{ switch ($in) \n" +
      "            case 42 return \"Moo\" \n" +
      "            case \"42\" return \"Meow\" \n" +
      "            case 42e0 return \"Quack\" \n" +
      "            case \"42e0\" return \"Oink\" \n" +
      "            default return \"Expletive deleted\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Meow</out>", false)
    );
  }

  /**
   *  switch test, untypedAtomic is converted to string .
   */
  @org.junit.Test
  public void switch008() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := \"42\"; \n" +
      "        <out>{ switch ($in) \n" +
      "            case 42 return \"Moo\" \n" +
      "            case <a>42</a> return \"Meow\" \n" +
      "            case 42e0 return \"Quack\" \n" +
      "            case \"42e0\" return \"Oink\" \n" +
      "            default return \"Expletive deleted\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Meow</out>", false)
    );
  }

  /**
   *  switch test, empty matches empty .
   */
  @org.junit.Test
  public void switch009() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := (); \n" +
      "        <out>{ switch ($in) \n" +
      "            case 42 return \"Moo\" \n" +
      "            case <a>42</a> return \"Meow\" \n" +
      "            case 42e0 return \"Quack\" \n" +
      "            case \"42e0\" return \"Oink\" \n" +
      "            case () return \"Woof\" \n" +
      "            default return \"Expletive deleted\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Woof</out>", false)
    );
  }

  /**
   *  switch test, non-matching empty .
   */
  @org.junit.Test
  public void switch010() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 21; \n" +
      "        <out>{ switch ($in) \n" +
      "            case 42 return \"Moo\" \n" +
      "            case <a>42</a> return \"Meow\" \n" +
      "            case 42e0 return \"Quack\" \n" +
      "            case \"42e0\" return \"Oink\" \n" +
      "            case () return \"Woof\" \n" +
      "            default return \"Expletive deleted\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Expletive deleted</out>", false)
    );
  }

  /**
   *  switch test, NaN matches NaN .
   */
  @org.junit.Test
  public void switch011() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := xs:double('NaN'); \n" +
      "        <out>{ switch ($in) \n" +
      "            case 42 return \"Moo\" \n" +
      "            case <a>42</a> return \"Meow\" \n" +
      "            case 42e0 return \"Quack\" \n" +
      "            case \"42e0\" return \"Oink\" \n" +
      "            case xs:float('NaN') return \"Woof\" \n" +
      "            default return \"Expletive deleted\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Woof</out>", false)
    );
  }

  /**
   *  switch test, no dynamic errors .
   */
  @org.junit.Test
  public void switch012() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 25; \n" +
      "        declare variable $zero := 0; \n" +
      "        <out>{ switch ($in) \n" +
      "            case 42 return $in div $zero \n" +
      "            case 25 return \"Baa\" \n" +
      "            case 39 return $in div $zero \n" +
      "            default return \"Woof\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Baa</out>", false)
    );
  }

  /**
   *  switch test, no dynamic errors .
   */
  @org.junit.Test
  public void switch013() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 25; \n" +
      "        declare variable $zero := 0; \n" +
      "        <out>{ switch ($in) \n" +
      "            case 42 return \"Quack\" \n" +
      "            case 25 return \"Baa\" \n" +
      "            case $in div $zero return \"Neigh\" \n" +
      "            default return \"Woof\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<out>Baa</out>", false)
    );
  }

  /**
   *  switch, type error, switch operand >1 item .
   */
  @org.junit.Test
  public void switch901() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 2; \n" +
      "        <out>{ switch (1 to $in) \n" +
      "            case 1 return \"Moo\" \n" +
      "            case 2 return \"Meow\" \n" +
      "            case 3 return \"Quack\" \n" +
      "            case 4 return \"Oink\" \n" +
      "            default return \"Baa\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  switch, type error, case operand >1 item .
   */
  @org.junit.Test
  public void switch902() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 2; \n" +
      "        <out>{ switch ($in) \n" +
      "            case 1 return \"Moo\" \n" +
      "            case 5 return \"Meow\" \n" +
      "            case 3 return \"Quack\" \n" +
      "            case ($in to 4) return \"Oink\" \n" +
      "            default return \"Baa\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  switch, static error, no cases .
   */
  @org.junit.Test
  public void switch903() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 2; \n" +
      "        <out>{ switch ($in) default return \"Baa\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  switch, static error, no default .
   */
  @org.junit.Test
  public void switch904() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 2; \n" +
      "        <out>{ switch ($in) \n" +
      "            case 1 return \"Moo\" \n" +
      "            case 5 return \"Meow\" \n" +
      "            case 3 return \"Quack\" \n" +
      "            case ($in to 4) return \"Oink\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  switch, static error, empty case list .
   */
  @org.junit.Test
  public void switch905() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 2; \n" +
      "        <out>{ switch ($in) \n" +
      "            case 1 return \"Moo\" \n" +
      "            case 5 return \"Meow\" return \"Quack\" \n" +
      "            case ($in to 4) return \"Oink\" \n" +
      "            default return \"Baa\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  switch, static error, not an ExprSingle .
   */
  @org.junit.Test
  public void switch906() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 2; \n" +
      "        <out>{ switch ($in) \n" +
      "            case 1 return \"Moo\", \"Boo\" \n" +
      "            case 5 return \"Meow\" \n" +
      "            case 7 return \"Quack\" \n" +
      "            case 4 return \"Oink\" \n" +
      "            default return \"Baa\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  switch, static error, params required .
   */
  @org.junit.Test
  public void switch907() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 2; \n" +
      "        <out>{ switch $in \n" +
      "            case 1 return \"Moo\", \"Boo\" \n" +
      "            case 5 return \"Meow\" \n" +
      "            case 7 return \"Quack\" \n" +
      "            case 4 return \"Oink\" \n" +
      "            default return \"Baa\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XPST0003")
      )
    );
  }

  /**
   *  switch, static error, curlies not allowed .
   */
  @org.junit.Test
  public void switch908() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 2; \n" +
      "        <out>{ switch ($in) { \n" +
      "            case 1 return \"Moo\", \"Boo\" \n" +
      "            case 5 return \"Meow\" \n" +
      "            case 7 return \"Quack\" \n" +
      "            case 4 return \"Oink\" \n" +
      "            default return \"Baa\" } }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  switch, static error, colons not allowed .
   */
  @org.junit.Test
  public void switch909() {
    final XQuery query = new XQuery(
      "xquery version \"3.0\"; \n" +
      "        declare variable $in := 2; \n" +
      "        <out>{ switch ($in) \n" +
      "            case 1: return \"Moo\", \"Boo\" \n" +
      "            case 5: return \"Meow\" \n" +
      "            case 7: return \"Quack\" \n" +
      "            case 4: return \"Oink\" \n" +
      "            default: return \"Baa\" }</out>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }
}
