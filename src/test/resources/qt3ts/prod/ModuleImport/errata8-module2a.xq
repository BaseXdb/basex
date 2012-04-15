(:*******************************************************:)
(: Test: errata8-module2a.xq                             :)
(: Written By: John Snelson                              :)
(: Date: 2009/10/01                                      :)
(: Purpose: Module that imports another module and uses a variable from it, testing circular dependencies pass case :)
(:*******************************************************:)

module namespace errata8_2a="http://www.w3.org/TestModules/errata8_2a";
import module namespace errata8_2b="http://www.w3.org/TestModules/errata8_2b";

declare function errata8_2a:fun()
{
  $errata8_2b:var
};

declare function errata8_2a:fun2()
{
  10
};

