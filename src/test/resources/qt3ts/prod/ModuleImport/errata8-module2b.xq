(:*******************************************************:)
(: Test: errata8-module2b.xq                             :)
(: Written By: John Snelson                              :)
(: Date: 2009/10/01                                      :)
(: Purpose: Module that imports another module and uses a function from it, testing circular dependencies :)
(:*******************************************************:)

module namespace errata8_2b="http://www.w3.org/TestModules/errata8_2b";
import module namespace errata8_2a="http://www.w3.org/TestModules/errata8_2a";

declare variable $errata8_2b:var := errata8_2a:fun2();
