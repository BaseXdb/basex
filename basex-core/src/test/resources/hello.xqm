(:~
 : This XQuery module is evaluated by some JUnit tests.
 : @author BaseX Team 2005-21, BSD License
 : @version 1.0
 : @unknown tag
 :)
module namespace hello = "world";

(:~ Variable marked as lazy. :)
declare %basex:lazy variable $hello:lazy := xs:QName(string(<a>hello:foo</a>));

(:~ Variable. :)
declare variable $hello:eager := xs:QName(string(<a>hello:foo</a>));

(:~ External variable. :)
declare %basex:lazy variable $hello:ext external;

(:~ Function marked as lazy. :)
declare %basex:lazy variable $hello:func := function() {
  xs:QName(string(<a>hello:foo</a>))
};

(:~ External function. :)
declare function hello:ext() external;

(:~ Function returning a QName. :)
declare function hello:inlined() {
  xs:QName(string(<a>hello:foo</a>))
};

(:~ Function returning a simple string. :)
declare %public function hello:world() as xs:string {
  hello:internal()
};

(:~ Private function returning a simple string. :)
declare %private %Q{ns}ignored function hello:internal() as xs:string {
  "hello world"
};

(:~ Closure. :)
declare function hello:closure() {
  count#1(1)
};
