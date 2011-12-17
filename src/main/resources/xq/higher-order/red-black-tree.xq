import module namespace tree = "http://basex.org/red-black-tree" at "red-black-tree.xqm";
tree:serialize(
	fold-right(
		function($x, $tree) {
			tree:insert(
				trace(
					$x,
					"insert: "
				),
				$tree
			)
		},
		$tree:empty,
		1 to 1000
	)
)