module namespace shapes="http://www.xqsharp.com/raytracer/shapes";

import module namespace vector="http://www.xqsharp.com/raytracer/vector";
import module namespace math="http://www.xqsharp.com/raytracer/math";

declare function shapes:intersect($start as xs:double*, 
                                  $dir as xs:double*,
                                  $shape as element()) as xs:double?
{
  typeswitch ($shape)
  case element(plane, xs:anyType) return
    shapes:intersect-plane($start, $dir, $shape)
  case element(sphere, xs:anyType) return
    shapes:intersect-sphere($start, $dir, $shape)
  default return
    ()
};

declare function shapes:normal($position as xs:double*,
                               $shape as element()) as xs:double*
{
  typeswitch ($shape)
  case element(plane, xs:anyType) return
    vector:unpack($shape/normal)
  case element(sphere, xs:anyType) return
    shapes:sphere-normal($position, $shape)
  default return 
    ()
};


declare function shapes:intersect-plane($start as xs:double*, 
                                        $direction as xs:double*,
                                        $plane as element()) as xs:double?
{
  let $normal := vector:unpack($plane/normal),
      $offset := xs:double($plane/@offset)
  let $denom := vector:dot($direction, $normal) 
  where ($denom ne 0)
  return
    ($offset - vector:dot($start, $normal) div ($denom))[. gt 0]
};

declare function shapes:intersect-sphere($start as xs:double*, 
                                         $direction as xs:double*,
                                         $sphere as element()) as xs:double?
{
  let $center := vector:unpack($sphere/center),
      $radius := xs:double($sphere/@radius)
      
  let $y := vector:sub($start, $center)
  
  let $beta := vector:dot($direction, $y),
      $gamma := vector:dot($y, $y) - $radius * $radius
      
  let $descriminant := $beta * $beta - $gamma
  where ($descriminant > 0)
  
  return
    let $sqrt := math:sqrt($descriminant)
    return
      (-$beta - $sqrt, -$beta + $sqrt)[. > 0][1]
};

declare function shapes:sphere-normal($position as xs:double*,
                                      $sphere as element()) as xs:double*
{
  let $center := vector:unpack($sphere/center),
      $radius := xs:double($sphere/@radius)
  return vector:scale(vector:sub($position, $center), 1 div $radius)
};
