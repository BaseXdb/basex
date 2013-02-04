module namespace materials="http://www.xqsharp.com/raytracer/materials";

declare function materials:material($name as xs:string,
                                    $position as xs:double*) as xs:double*
{
  if ($name eq "shiny") then
    (1, 1, 1, .6, 50)
  else if ($name eq "checkerboard") then
    if (((floor($position[1]) + floor($position[3])) mod 2) eq 0)
      then (0, 0, 0, .7, 150)
      else (1, 1, 1, .1, 50)
  else ()
};