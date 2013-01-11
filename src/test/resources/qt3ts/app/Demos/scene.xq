module namespace scene="http://www.xqsharp.com/raytracer/scene";

import module namespace vector="http://www.xqsharp.com/raytracer/vector";

declare function scene:calculate-basis($camera as element())
{
  let $position := vector:unpack($camera/position),
      $look-at := vector:unpack($camera/look-at)
  let $forward := vector:normalize(vector:sub($look-at, $position))
  let $right := vector:normalize(vector:cross($forward, (0, -1, 0)))
  let $up := vector:cross($forward, $right)
  return
    <camera>{
      vector:pack("position", $position),
      vector:pack("forward", $forward),
      vector:pack("up", $up),
      vector:pack("right", $right)
    }</camera>
};

declare function scene:prepare-scene($scene as element())
{
  <scene>{
    for $node in $scene/node()
    return typeswitch ($node)
           case $camera as element(camera, xs:anyType) return scene:calculate-basis($camera)
           default return $node
  }</scene>
};
