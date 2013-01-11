module namespace bar="http://www.xqsharp.com/test/variablereference";
declare namespace foo="http://www.xqsharp.com/test/variabledeclaration";

declare function bar:test()
{
  $foo:test
};
