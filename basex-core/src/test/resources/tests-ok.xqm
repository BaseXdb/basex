module namespace _ = '_';

(:~ Function demonstrating a successful test. :)
declare %unit:test function _:assert-success() {
  unit:assert(<a/>)
};
  
(:~ Function demonstrating an expected failure. :)
declare %unit:test("expected", "FORG0001") function _:expected-failure() {
  1 + <a/>
};
