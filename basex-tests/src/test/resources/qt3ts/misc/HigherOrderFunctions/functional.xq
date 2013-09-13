xquery version "3.0";

module namespace func = "http://snelson.org.uk/functions/functional";
declare default function namespace "http://snelson.org.uk/functions/functional";

declare function id($a)
{
  $a
};

declare function incr($a)
{
  $a + 1
};

declare function flip($f)
{
  function($a, $b) { $f($b, $a) }
};

declare function iterate($f, $a)
{
  $a, iterate($f, $f($a))
};

declare function repeat($a)
{
  $a, repeat($a)
};

declare function take($n, $l)
{
  fn:subsequence($l, 1, $n)
};

declare function until($p, $f, $a)
{
  if($p($a)) then $a
  else until($p, $f, $f($a))
};

(: Wrap a sequence as a function item  :)
declare function ref($arg as item()*) as function() as item()*
{
   function() { $arg }
};

(: Unwrap a sequence from a function item :)
declare function deref($arg as function() as item()*) as item()*
{
   $arg()
};

(: Curries a function (up to arity 10) :)
declare function curry($f as function(*)) as function(item()*) as item()*
{
  let $arity := fn:function-arity($f)
  return

  if($arity eq 1) then $f
  else if($arity eq 2) then curry2($f)
  else if($arity eq 3) then curry3($f)
  else if($arity eq 4) then curry4($f)
  else if($arity eq 5) then curry5($f)
  else if($arity eq 6) then curry6($f)
  else if($arity eq 7) then curry7($f)
  else if($arity eq 8) then curry8($f)
  else if($arity eq 9) then curry9($f)
  else if($arity eq 10) then curry10($f)
  else if($arity eq 0) then fn:error(xs:QName("func:FNDY0001"), "Can't curry a 0 arity function item")
  else fn:error(xs:QName("func:FNDY0002"), "Currying not implemented for a function item with arity greater than 10")
};

declare function curry2($f as function(*)) as function(item()*) as item()*
{
  function($a) { $f($a, ?) }
};

declare function curry3($f as function(*)) as function(item()*) as item()*
{
  function($b) { curry2($f($b, ?, ?)) }
};

declare function curry4($f as function(*)) as function(item()*) as item()*
{
  function($c) { curry3($f($c, ?, ?, ?)) }
};

declare function curry5($f as function(*)) as function(item()*) as item()*
{
  function($d) { curry4($f($d, ?, ?, ?, ?)) }
};

declare function curry6($f as function(*)) as function(item()*) as item()*
{
  function($e) { curry5($f($e, ?, ?, ?, ?, ?)) }
};

declare function curry7($f as function(*)) as function(item()*) as item()*
{
  function($g) { curry6($f($g, ?, ?, ?, ?, ?, ?)) }
};

declare function curry8($f as function(*)) as function(item()*) as item()*
{
  function($h) { curry7($f($h, ?, ?, ?, ?, ?, ?, ?)) }
};

declare function curry9($f as function(*)) as function(item()*) as item()*
{
  function($i) { curry8($f($i, ?, ?, ?, ?, ?, ?, ?, ?)) }
};

declare function curry10($f as function(*)) as function(item()*) as item()*
{
  function($j) { curry9($f($j, ?, ?, ?, ?, ?, ?, ?, ?, ?)) }
};

(: Y combinator for a function (up to arity 10) :)
declare function Y($f as function(*)) as function(*)
{
  let $arity := fn:function-arity($f)
  return

  if($arity eq 1) then Y1($f)
  else if($arity eq 2) then Y2($f)
  else if($arity eq 3) then Y3($f)
  else if($arity eq 4) then Y4($f)
  else if($arity eq 5) then Y5($f)
  else if($arity eq 6) then Y6($f)
  else if($arity eq 7) then Y7($f)
  else if($arity eq 8) then Y8($f)
  else if($arity eq 9) then Y9($f)
  else if($arity eq 10) then Y10($f)
  else fn:error(xs:QName("func:FNDY0002"), "Y combinator not implemented for a function item with arity greater than 10")
};

declare function Y1($f as function(*)) as function(*)
{
  function() { $f(Y1($f)) }
};

declare function Y2($f as function(*)) as function(*)
{
  $f(Y2($f), ?)
};

declare function Y3($f as function(*)) as function(*)
{
  $f(Y3($f), ?, ?)
};

declare function Y4($f as function(*)) as function(*)
{
  $f(Y4($f), ?, ?, ?)
};

declare function Y5($f as function(*)) as function(*)
{
  $f(Y5($f), ?, ?, ?, ?)
};

declare function Y6($f as function(*)) as function(*)
{
  $f(Y6($f), ?, ?, ?, ?, ?)
};

declare function Y7($f as function(*)) as function(*)
{
  $f(Y7($f), ?, ?, ?, ?, ?, ?)
};

declare function Y8($f as function(*)) as function(*)
{
  $f(Y8($f), ?, ?, ?, ?, ?, ?, ?)
};

declare function Y9($f as function(*)) as function(*)
{
  $f(Y9($f), ?, ?, ?, ?, ?, ?, ?, ?)
};

declare function Y10($f as function(*)) as function(*)
{
  $f(Y10($f), ?, ?, ?, ?, ?, ?, ?, ?, ?)
};

(: Compose a sequence of single argument functions into a single function :)
declare function compose($functions as function(*)+) as function(*)
{
  let $head := fn:head($functions)
  let $tail := fn:tail($functions)
  return

  if(fn:empty($tail)) then $head
  else compose-helper($tail, $head)
};

declare %private function compose-helper($functions as function(*)+, $result as function(*)) as function(*)
{
  let $head := fn:head($functions)
  let $tail := fn:tail($functions)
  return

  if(fn:empty($tail)) then (
    let $arity := fn:function-arity($head)
    return

    if($arity eq 0) then function() { $result($head()) }
    else if($arity eq 1) then function($a) { $result($head($a)) }
    else if($arity eq 2) then function($a, $b) { $result($head($a, $b)) }
    else if($arity eq 3) then function($a, $b, $c) { $result($head($a, $b, $c)) }
    else if($arity eq 4) then function($a, $b, $c, $d) { $result($head($a, $b, $c, $d)) }
    else if($arity eq 5) then function($a, $b, $c, $d, $e) { $result($head($a, $b, $c, $d, $e)) }
    else if($arity eq 6) then function($a, $b, $c, $d, $e, $f) { $result($head($a, $b, $c, $d, $e, $f)) }
    else if($arity eq 7) then function($a, $b, $c, $d, $e, $f, $g) { $result($head($a, $b, $c, $d, $e, $f, $g)) }
    else if($arity eq 8) then function($a, $b, $c, $d, $e, $f, $g, $h) { $result($head($a, $b, $c, $d, $e, $f, $g, $h)) }
    else if($arity eq 9) then function($a, $b, $c, $d, $e, $f, $g, $h, $i) { $result($head($a, $b, $c, $d, $e, $f, $g, $h, $i)) }
    else if($arity eq 10) then function($a, $b, $c, $d, $e, $f, $g, $h, $i, $j) { $result($head($a, $b, $c, $d, $e, $f, $g, $h, $i, $j)) }
    else fn:error(xs:QName("func:FNDY0002"), "compose not implemented for final function items with arity greater than 10")

  ) else (
    compose-helper($tail, function($a) { $result($head($a)) })
  )
};

declare function compose($f1 as function(*), $f2 as function(*)) as function(*)
{
  let $arity := fn:function-arity($f2)
  return

  if($arity eq 0) then function() { $f1($f2()) }
  else if($arity eq 1) then function($a) { $f1($f2($a)) }
  else if($arity eq 2) then function($a, $b) { $f1($f2($a, $b)) }
  else if($arity eq 3) then function($a, $b, $c) { $f1($f2($a, $b, $c)) }
  else if($arity eq 4) then function($a, $b, $c, $d) { $f1($f2($a, $b, $c, $d)) }
  else if($arity eq 5) then function($a, $b, $c, $d, $e) { $f1($f2($a, $b, $c, $d, $e)) }
  else if($arity eq 6) then function($a, $b, $c, $d, $e, $f) { $f1($f2($a, $b, $c, $d, $e, $f)) }
  else if($arity eq 7) then function($a, $b, $c, $d, $e, $f, $g) { $f1($f2($a, $b, $c, $d, $e, $f, $g)) }
  else if($arity eq 8) then function($a, $b, $c, $d, $e, $f, $g, $h) { $f1($f2($a, $b, $c, $d, $e, $f, $g, $h)) }
  else if($arity eq 9) then function($a, $b, $c, $d, $e, $f, $g, $h, $i) { $f1($f2($a, $b, $c, $d, $e, $f, $g, $h, $i)) }
  else if($arity eq 10) then function($a, $b, $c, $d, $e, $f, $g, $h, $i, $j) { $f1($f2($a, $b, $c, $d, $e, $f, $g, $h, $i, $j)) }
  else fn:error(xs:QName("func:FNDY0002"), "compose not implemented for final function items with arity greater than 10")
};

declare function compose($f1 as function(*), $f2 as function(*), $f3 as function(*)) as function(*)
{
  compose(function($a) { $f1($f2($a)) }, $f3)
};

declare function compose($f1 as function(*), $f2 as function(*), $f3 as function(*), $f4 as function(*)) as function(*)
{
  compose(function($a) { $f1($f2($f3($a))) }, $f4)
};

declare function compose($f1 as function(*), $f2 as function(*), $f3 as function(*), $f4 as function(*), $f5 as function(*)) as function(*)
{
  compose(function($a) { $f1($f2($f3($f4($a)))) }, $f5)
};

declare function compose($f1 as function(*), $f2 as function(*), $f3 as function(*), $f4 as function(*), $f5 as function(*),
  $f6 as function(*)) as function(*)
{
  compose(function($a) { $f1($f2($f3($f4($f5($a))))) }, $f6)
};

declare function compose($f1 as function(*), $f2 as function(*), $f3 as function(*), $f4 as function(*), $f5 as function(*),
  $f6 as function(*), $f7 as function(*)) as function(*)
{
  compose(function($a) { $f1($f2($f3($f4($f5($f6($a)))))) }, $f7)
};

declare function compose($f1 as function(*), $f2 as function(*), $f3 as function(*), $f4 as function(*), $f5 as function(*),
  $f6 as function(*), $f7 as function(*), $f8 as function(*)) as function(*)
{
  compose(function($a) { $f1($f2($f3($f4($f5($f6($f7($a))))))) }, $f8)
};

declare function compose($f1 as function(*), $f2 as function(*), $f3 as function(*), $f4 as function(*), $f5 as function(*),
  $f6 as function(*), $f7 as function(*), $f8 as function(*), $f9 as function(*)) as function(*)
{
  compose(function($a) { $f1($f2($f3($f4($f5($f6($f7($f8($a)))))))) }, $f9)
};

declare function compose($f1 as function(*), $f2 as function(*), $f3 as function(*), $f4 as function(*), $f5 as function(*),
  $f6 as function(*), $f7 as function(*), $f8 as function(*), $f9 as function(*), $f10 as function(*)) as function(*)
{
  compose(function($a) { $f1($f2($f3($f4($f5($f6($f7($f8($f9($a))))))))) }, $f10)
};

