module namespace hello = "world";

declare %basex:lazy variable $hello:lazy := xs:QName(string(<a>hello:foo</a>));
declare variable $hello:eager := xs:QName(string(<a>hello:foo</a>));
declare %basex:lazy variable $hello:func := function() {
  xs:QName(string(<a>hello:foo</a>))
};

declare function hello:inlined() {
  xs:QName(string(<a>hello:foo</a>))
};

declare function hello:world() {
  "hello world"
};
