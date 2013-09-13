import module namespace tree = "http://basex.org/red-black-tree" at "red-black-tree.xqm";
tree:serialize(
  fold-right(
    1 to 1000,
    $tree:empty,
    function($x, $tree) {
      tree:insert(
        trace(
          $x,
          "insert: "
        ),
        $tree
      )
    }
  )
)