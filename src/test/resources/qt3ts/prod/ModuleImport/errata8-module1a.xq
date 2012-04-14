(:*******************************************************:)
(: Test: errata8-module1a.xq                             :)
(: Written By: John Snelson                              :)
(: Date: 2009/10/01                                      :)
(: Purpose: Module that imports another module and uses a variable from it, testing circular dependencies :)
(:*******************************************************:)

module namespace errata8_1a="http://www.w3.org/TestModules/errata8_1a";
import module namespace errata8_1b="http://www.w3.org/TestModules/errata8_1b";

declare function errata8_1a:fun()
{
  $errata8_1b:var
};
