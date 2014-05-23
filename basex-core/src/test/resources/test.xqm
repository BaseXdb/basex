module namespace test = 'http://basex.org/modules/xqunit-tests';

(:~ Initializing function, which is called once before all tests. :)
declare %unit:before-module function test:before-all-tests() {
  ()
};
  
(:~ Initializing function, which is called once after all tests. :)
declare %unit:after-module function test:after-all-tests() {
  ()
};
  
(:~ Initializing function, which is called before each test. :)
declare %unit:before function test:before() {
  ()
};
  
(:~ Initializing function, which is called after each test. :)
declare %unit:after function test:after() {
  ()
};
  
(:~ Function demonstrating a successful test. :)
declare %unit:test function test:assert-success() {
  unit:assert(<a/>)
};
  
(:~ Function demonstrating a failure using unit:assert. :)
declare %unit:test function test:assert-failure() {
  unit:assert((), 'Empty sequence.')
};
  
(:~ Function demonstrating a failure using unit:assert-equals. :)
declare %unit:test function test:assert-equals-failure() {
  unit:assert-equals(4 + 5, 6)
};
  
(:~ Function demonstrating an unexpected success. :)
declare %unit:test("expected", "FORG0001") function test:unexpected-success() {
  ()
};
  
(:~ Function demonstrating an expected failure. :)
declare %unit:test("expected", "FORG0001") function test:expected-failure() {
  1 + <a/>
};
  
(:~ Function demonstrating the creation of a failure. :)
declare %unit:test function test:failure() {
  unit:fail("Failure!")
};
  
(:~ Function demonstrating an error. :)
declare %unit:test function test:error() {
  1 + <a/>
};
  
(:~ Skipping a test. :)
declare %unit:test %unit:ignore("Skipped!") function test:skipped() {
  ()
};
