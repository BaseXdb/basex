(:*******************************************************:)
(: Test: errata8-module3a.xq                             :)
(: Written By: John Snelson                              :)
(: Date: 2009/10/01                                      :)
(: Purpose: Module that imports another module and uses a variable from it, testing circular dependencies pass case :)
(:*******************************************************:)

module namespace errata8_3a="http://www.w3.org/TestModules/errata8_3a";
import module namespace errata8_3b="http://www.w3.org/TestModules/errata8_3b";

declare function errata8_3a:fun()
{
  $errata8_3b:var
};

declare function errata8_3a:fun2()
{
  10
};

