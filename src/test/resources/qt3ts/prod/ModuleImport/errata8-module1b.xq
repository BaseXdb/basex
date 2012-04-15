(:*******************************************************:)
(: Test: errata8-module1b.xq                             :)
(: Written By: John Snelson                              :)
(: Date: 2009/10/01                                      :)
(: Purpose: Module that imports another module and uses a function from it, testing circular dependencies :)
(:*******************************************************:)

module namespace errata8_1b="http://www.w3.org/TestModules/errata8_1b";
import module namespace errata8_1a="http://www.w3.org/TestModules/errata8_1a";

declare variable $errata8_1b:var := errata8_1a:fun();
