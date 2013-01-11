module namespace math="http://www.xqsharp.com/raytracer/math";

declare function math:sqrt($x as xs:double?) as xs:double?
{
  math:sqrt2($x, 0.5 + $x div 2, 5)
};

declare function math:sqrt2($x as xs:double?, $guess as xs:double?, $n as xs:integer) as xs:double?
{
  if ($n le 0)
  then $guess
  else let $next := ($x + $guess * $guess) div (2 * $guess)
       return math:sqrt2($x, $next, $n - 1)
};

declare function math:pow($a as xs:double, $b as xs:integer) as xs:double
{
  if ($b = 0) then $a
  else if ($b mod 2 = 0)
       then let $x := math:pow($a, $b idiv 2)
            return $x * $x
       else $a * math:pow($a, $b - 1)
};
