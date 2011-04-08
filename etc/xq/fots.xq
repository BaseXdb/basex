(: global namespace of the test suite. :)
declare namespace fots = "http://www.w3.org/2010/09/qt-fots-catalog";

(: prefix of catalog to test. :)
declare variable $doc := doc('catalog.xml');
(: prefix of catalog to test. :)
declare variable $catalog := '';
(: prefix of tests to test. :)
declare variable $prefix := '';

(: loops throgh the test set and evaluates all test cases :)
declare function fots:main() {
  let $env := $doc//*:environment
  for $set in $doc//*:test-set[starts-with(@name, $catalog)]
  let $href := $set/@href
  let $base-dir := replace($href, '/.*','/')
  let $doc := doc($href)
  for $case in $doc//*:test-case[starts-with(@name, $prefix)]
  let $env := $env | $doc//*:environment
  return fots:test($case, $env, $base-dir)
};

(: runs a single test :)
declare function fots:test($case, $envir, $base) {
  let $query := $case/*:test/text()
  let $env := $envir[@name = $case/*:environment/@ref]/*:source/@file
  let $result := $case/*:result
  let $prolog :=
    if($env)
    then concat("declare context item := doc('",
     if(file:exists(concat($base, $env))) then $base else (), $env, "');")
    else ()
  let $query := concat($prolog, $query)
  return
  try {
    let $val := util:eval($query)
    return if
     (fots:allof($result, $val) or
      fots:anyof($result, $val) or
      fots:true($result, $val) or
      fots:false($result, $val) or
      fots:eq($result, $val) or
      fots:string($result, $val))
      then fots:wrong($case, $val, $result/*)
      else ()
  } catch *($code, $msg) {
    if(xs:string($code) = $result//*:error/@code)
    then ()
    else fots:wrong($case, concat($code,': ',$msg), $result/*)
  }
};

(: gives feedback on an erroneous query :)
declare function fots:wrong($test, $found, $exp) {
  <test name="{ $test/@name }">
    <query>{ $test/*:test/text() }</query>
    <found>{ $found }</found>
    <expected>{
      if($exp/self::*:error)
      then data($exp/@code)
      else $exp
    }</expected>
  </test>
};

(: converts a value to the specified type :)
declare function fots:convert($type, $val) {
  if($type)
  then
    try {
      util:eval(concat($type, '("', $val, '")'))
    } catch *($c,$m) {
      trace('', concat($c, ': ', $m))
    }
  else $val
};

(: checks if any of the given tests is wrong :)
declare function fots:allof($result, $val) {
  let $type := $result//*:assert-type
  let $val := fots:convert($type, $val)
  for $c in $result/*:all-of/*
  let $eq := replace($c/self::*:assert-eq, "^('|"")|('|"")$", "")
  return 
    if(not($eq) or fots:convert($type, $eq) eq $val)
    then ()
    else false()
};

(: checks if none of the given tests is true :)
declare function fots:anyof($result, $val) {
  let $type := $result//*:assert-type
  let $val := fots:convert($type, $val)
  let $ok :=
    for $c in $result/*:any-of/*
    return if(
      not(fots:true($c, $val)) and
      not(fots:false($c, $val)) and
      not(fots:string($c, $val)))
      then true()
      else ()
  return exists($result/*:any-of) and empty($ok)
};

(: checks if the given value is not true :)
declare function fots:true($result, $val) {
  $result/*:assert-true and not($val)
};

(: checks if the given value is not false :)
declare function fots:false($result, $val) {
  $result/*:assert-false and $val
};

(: checks if the given string is wrong :)
declare function fots:string($result, $val) {
  $result/*:assert-string-value ne string($val)
};

(: checks if the equality yields false :)
declare function fots:eq($result, $val) {
  for $i in $result/*:assert-eq
  return
    try {
      let $r := util:eval($i)
      return (typeswitch($val)
        case xs:string  return xs:string($r)
        case xs:double  return xs:double($r)
        case xs:float   return xs:float($r)
        case xs:integer return xs:integer($r)
        default return $val) ne $val
    } catch *($c,$m) {
      trace('', concat($c, ': ', $m))
    }
};

(: runs the main function :)
fots:main()

