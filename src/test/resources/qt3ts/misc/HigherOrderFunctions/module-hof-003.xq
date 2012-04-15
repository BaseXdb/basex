xquery version "3.0";
module namespace m="http://example.com/hof-003";

declare function m:f() as xs:integer {
  42
};

declare function m:f($x as xs:integer) as xs:integer {
  $x + 1
};