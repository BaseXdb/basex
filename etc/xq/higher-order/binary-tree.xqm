module namespace bin = "http://basex.org/bin-tree";

declare function bin:empty() {
  function($k) { () }
};

declare function bin:left($tree)  { $tree(1) };
declare function bin:key($tree)   { $tree(2) };
declare function bin:right($tree) { $tree(3) };

declare function bin:is-empty($tree) {
  fn:empty(bin:key($tree))
};

declare function bin:tree($left, $key, $right) {
  function($k) { ($left, $key, $right)[$k] }
};

declare function bin:insert($x, $tree) {
  let $l := bin:left($tree),
      $y := bin:key($tree),
      $r := bin:right($tree)
  return if(fn:empty($y)) then (
    bin:tree(bin:empty(), $x, bin:empty())
  ) else if($x lt $y) then (
    bin:tree(bin:insert($x, $l), $y, $r)
  ) else if($x gt $y) then (
    bin:tree($l, $y, bin:insert($x, $r))
  ) else $tree
};

declare function bin:to-seq($tree) {
  if(bin:is-empty($tree)) then ()
  else (bin:to-seq(bin:left($tree)), bin:key($tree), bin:to-seq(bin:right($tree)))
};

declare function bin:serialize($tree) {
  if(bin:is-empty($tree)) then <empty/>
  else <tree key='{bin:key($tree)}'>{
    bin:serialize(bin:left($tree)),
    bin:serialize(bin:right($tree))
  }</tree>
};
