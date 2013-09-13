(:*******************************************************:)
(: Test: cnc-module.xq                                   :)
(: Written By: Till Westmann                             :)
(: Date: 2011/11/28                                      :)
(: Purpose: Library module with functions returning      :)
(:          computed namespace nodes                     :)
(:*******************************************************:)

module namespace mod1 = "http://www.w3.org/TestModules/cnc-module";

declare function mod1:one() as namespace-node() {
  namespace z { "http://www.zorba-xquery.com/" } 
};

declare function mod1:nested() as element() {
  element outer { 
    namespace out { "http://out.zorba-xquery.com/" },
    element inner {
      namespace in { "http://in.zorba-xquery.com/" }
    } 
  }
};
