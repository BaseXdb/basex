(: Name: XQST0070_module :)
(: Description: check that invalid module declarations are reported correctly :)
(: Author: Tim Mills :)
(: Date: 2008-05-16 :)
module namespace xml = 'http://www.example.org/';

declare function xml:foo()
{
  1
}
