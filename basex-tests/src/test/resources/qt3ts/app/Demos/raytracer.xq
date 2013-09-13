module namespace raytracer="http://www.xqsharp.com/raytracer";

import module namespace vector="http://www.xqsharp.com/raytracer/vector";
import module namespace shapes="http://www.xqsharp.com/raytracer/shapes";
import module namespace materials="http://www.xqsharp.com/raytracer/materials";
import module namespace math="http://www.xqsharp.com/raytracer/math";

(:~
 : Returns the color of a pixel in the ray-traced image.
 :
 : @param $scene The scene to draw
 : @param $x The x co-ordinate of the pixel (usually from -1 to 1, 
 :           although the range can be increased for wide aspect ratios).
 : @param $y The y co-ordinate of the pixel (usually from -1 to 1, 
 :           although the range can be increased for tall aspect ratios).
 : @return A list of three xs:double values representing thre red, green
 :         and blue channels, in the range [0, 1].
 :)
declare function raytracer:plot-pixel($scene as element(),
                                      $x as xs:double,
                                      $y as xs:double) as xs:double+
{
  let $camera-position := vector:unpack($scene/camera/position),
      $camera-forward := vector:unpack($scene/camera/forward),
      $camera-up := vector:unpack($scene/camera/up),
      $camera-right := vector:unpack($scene/camera/right)
  
  let $ray-source := $camera-position,
      $ray-direction := vector:normalize(
                          vector:add(
                            $camera-forward,
                              vector:add(
                                vector:scale($camera-right, $x),
                                vector:scale($camera-up, $y))))
                                
  for $channel in raytracer:trace-ray($scene,
                                      $ray-source,
                                      $ray-direction,
                                      (),
                                      1)
  return if ($channel lt 0) then 0
         else if ($channel gt 1) then 1
         else $channel
};

declare function raytracer:trace-ray($scene as element(),
                                     $source as xs:double*,
                                     $direction as xs:double*,
                                     $ignore as element()?,
                                     $contribution as xs:double) as xs:double*
{
  subsequence(
    (
      for $shape in $scene/shapes/*[not(. is $ignore)]
      let $distance := shapes:intersect($source, $direction, $shape)
      let $position := vector:add($source, vector:scale($direction, $distance))
      where exists($distance)
      order by $distance
      return
        raytracer:shade($position,
                        $direction,
                        $shape,
                        $scene,
                        $contribution)
    ,
      vector:unpack-color($scene/background)
    ),
    1,
    3)
};

declare function raytracer:test-ray($scene as element(),
                                    $source as xs:double*,
                                    $direction as xs:double*,
                                    $ignore as element()) as xs:double*
{
    for $shape in $scene/shapes/*[not(. is $ignore)]
    return
      shapes:intersect($source, $direction, $shape)
};

declare function raytracer:shade($position as xs:double*, 
                                 $direction as xs:double*,
                                 $shape as element(),
                                 $scene as element(),
                                 $contribution as xs:double) as xs:double*
{
  let $material := materials:material($shape/@surface, $position)
  let $normal := shapes:normal($position, $shape)
  let $normal := if (vector:dot($direction, $normal) > 0) then -$normal else $normal
  let $reflected-direction := vector:sub($direction, 
                                 vector:scale($normal, 
                                   2*vector:dot($normal, $direction)))
  let $surface-reflectiveness := $material[4]
  let $contribution := $contribution * $surface-reflectiveness
  return
    vector:sum(
      (
        raytracer:light($scene,
                        $shape,
                        $position,
                        $normal,
                        $reflected-direction,
                        $material)
     ,
        vector:scale(
          raytracer:trace-ray($scene,
                              $position,
                              $reflected-direction,
                              $shape,
                              $contribution)[$contribution gt 0.01],
          $surface-reflectiveness)
      ), 3)
};

declare function raytracer:light($scene as element(),
                                 $shape as element(),
                                 $position as xs:double*,
                                 $normal as xs:double*,
                                 $reflected-direction as xs:double*,
                                 $material as xs:double*)
{
  let $surface-color := subsequence($material, 1, 3),
      $surface-reflectiveness := $material[4],
      $surface-smoothness := xs:integer($material[5])
  return
    vector:sum(
      for $light in $scene/light
      let $light-color := vector:unpack-color($light/color),
          $light-position := vector:unpack($light/position),
          $light-direction := vector:normalize(
                                vector:sub($light-position, $position)),
          $light-distance := vector:length(
                               vector:sub($light-position, $position))
      let $illumination := vector:dot($light-direction, $normal)
      let $specular := vector:dot($light-direction, $reflected-direction)
      where not(raytracer:test-ray($scene, $position, $light-direction, $shape) < 
                $light-distance)
      return
      (
        vector:scale(vector:blend($light-color, $surface-color), 
                     $illumination)[$illumination gt 0],
                     
        vector:scale(
          $light-color,
          math:pow($specular, $surface-smoothness) * $surface-reflectiveness
        )[$specular gt 0]
      ),
      3
    )
};
