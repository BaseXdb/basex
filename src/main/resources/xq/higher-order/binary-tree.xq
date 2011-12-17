import module namespace bin = "http://basex.org/bin-tree" at "binary-tree.xqm";

declare function local:randomize($seq) {
	for $x in $seq
	order by math:random()
	return $x
};

let $seq := local:randomize(1 to 1000)
return bin:serialize(
  fold-left(
    function($tree, $x) { bin:insert($x, $tree) },
    bin:empty(),
    $seq
  )
)
