(: Name: XQST0048_module :)
(: Description: Test generating XQST0048 :)
(: Author: Tim Mills :)
(: Date: 2008-05-16 :)
module namespace foo = "http://www.example.org/foo";
declare namespace bar = "http://www.example.org/bar";

declare function bar:foo()
{
  1
};

