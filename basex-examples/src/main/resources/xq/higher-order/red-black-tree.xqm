module namespace tree = "http://basex.org/red-black-tree";

declare variable $tree:color := 1;
declare variable $tree:left  := 2;
declare variable $tree:key   := 3;
declare variable $tree:right := 4;
declare variable $tree:red   := false();
declare variable $tree:black := true();
declare variable $tree:empty := function($i as xs:integer) as item()? { () };

declare function tree:branch(
	$c as xs:boolean,
	$l as function(xs:integer) as item()?,
	$x as item(),
	$r as function(xs:integer) as item()?
) as function(xs:integer) as item()? {
	function($i as xs:integer) as item()? {
		($c, $l, $x, $r)[$i]
	}
};

declare function tree:red(
	$l as function(xs:integer) as item()?,
	$x as item(),
	$r as function(xs:integer) as item()?
) as function(xs:integer) as item()? {
	tree:branch($tree:red, $l, $x, $r)
};

declare function tree:black(
	$l as function(xs:integer) as item()?,
	$x as item(),
	$r as function(xs:integer) as item()?
) as function(xs:integer) as item()? {
	tree:branch($tree:black, $l, $x, $r)
};

declare function tree:is-empty(
	$tree as function(xs:integer) as item()?
) as xs:boolean {
	fn:empty($tree($tree:key))
};

declare function tree:member(
	$x as item(),
	$tree as function(xs:integer) as item()?
) as xs:boolean {
	let $y := $tree($tree:key)
	return (
		if(fn:empty($y)) then false()
		else if($x lt $y) then tree:member($x, $tree($tree:left))
		else if($x eq $y) then true()
		else tree:member($x, $tree($tree:right))
	)
};

declare function tree:is-red(
	$tree as function(xs:integer) as item()?
) as xs:boolean {
	$tree($tree:color) = $tree:red
};

declare function tree:make-black(
	$tree as function(xs:integer) as item()?
) as function(xs:integer) as item()?? {
	if(tree:is-empty($tree)) then ()
	else tree:black($tree($tree:left), $tree($tree:key), $tree($tree:right))
};

declare function tree:insert(
	$x as item(),
	$tree as function(xs:integer) as item()?
) as function(xs:integer) as item()? {
	tree:make-black(tree:ins($x, $tree))
};

declare function tree:ins(
	$x as item(),
	$tree as function(xs:integer) as item()?
) as function(xs:integer) as item()? {
	let $c := $tree($tree:color),
	    $l := $tree($tree:left),
	    $y := $tree($tree:key),
	    $r := $tree($tree:right)
	return if(fn:empty($y))
	then tree:red($tree:empty, $x, $tree:empty)
	else if($x lt $y) then tree:balance($c, tree:ins($x, $l), $y, $r)
	else if($x eq $y) then tree:branch($c, $l, $y, $r)
	else (: $x gt $y :)    tree:balance($c, $l, $y, tree:ins($x, $r))
};

declare function tree:balance(
	$color as xs:boolean,
	$l as function(xs:integer) as item()?,
	$x as item(),
	$r as function(xs:integer) as item()?
) as function(xs:integer) as item()? {
	if($color eq $tree:black) then (
		let $red-l := tree:is-red($l),
		    $red-r := tree:is-red($r)
		return if($red-l and tree:is-red($l($tree:left))) then (
			let $ll := $l($tree:left),
			    $lr := $l($tree:right)
			return
				(: case 1:
				 :    [B [R [R a x b] y c] z d] 
				 : => [R [B a x b] y [B c z d]]
				 :)
				 tree:red(
				 	tree:make-black($ll),
				 	$l($tree:key),
				 	tree:black($lr, $x, $r)
				 )
		) else if($red-l and tree:is-red($l($tree:right))) then (
			let $ll := $l($tree:left),
			    $lr := $l($tree:right)
			return
				(: case 2:
				 :    [B [R a x [R b y c]] z d] 
				 : => [R [B a x b] y [B c z d]]
				 :)
				 tree:red(
				 	tree:black($ll, $l($tree:key), $lr($tree:left)),
				 	$lr($tree:key),
				 	tree:black($lr($tree:right), $x, $r)
				 )
		) else if($red-r and tree:is-red($r($tree:left))) then (
			let $rl := $r($tree:left),
			    $rr := $r($tree:right)
			return
				(: case 3:
				 :    [B a x [R [R b y c] z d]]
				 : => [R [B a x b] y [B c z d]]
				 :)
				 tree:red(
				 	tree:black($l, $x, $rl($tree:left)),
				 	$rl($tree:key),
				 	tree:black($rl($tree:right), $r($tree:key), $rr)
				 )
		) else if($red-r and tree:is-red($r($tree:right))) then (
			let $rl := $r($tree:left),
			    $rr := $r($tree:right)
			return
				(: case 4:
				 :    [B a x [R b y [R c z d]]] 
				 : => [R [B a x b] y [B c z d]]
				 :)
				 tree:red(
				 	tree:black($l, $x, $rl),
				 	$r($tree:key),
				 	tree:make-black($rr)
				 )
		) else tree:branch($color, $l, $x, $r)
	) else tree:branch($color, $l, $x, $r)
};

declare function tree:serialize(
	$tree as function(xs:integer) as item()?
) as element() {
  if(tree:is-empty($tree)) then element leaf { }
  else element tree {
  	attribute color { if($tree($tree:color)) then 'black' else 'red' },
	  attribute key   { $tree($tree:key) },
	  tree:serialize($tree($tree:left)),
	  tree:serialize($tree($tree:right))
  }
};
