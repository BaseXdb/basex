(:~
 : Conversion module for creating JUnit tests from the QT3TS test suite.
 : @author Leo Woerteler
 : @version 0.1
 :)
module namespace qt3ts='http://www.basex.org/modules/qt3ts';

import module namespace qt3ts-java='http://www.basex.org/modules/qt3ts/java'
  at 'qt3ts-java.xqm';

declare default element namespace 'http://www.w3.org/2010/09/qt-fots-catalog';

(:~
 : Main entry point.
 : @param $path path to the test-suite
 : @param $test-sets to be converted
 : @param $package Java package
 : @param $out-path path for the generated Java files
 : @return nothing
 :)
declare function qt3ts:to-junit(
  $path as xs:string,
  $test-sets as xs:string*,
  $package as xs:string,
  $out-path as xs:string,
  $supports as function(xs:string, xs:string*) as xs:boolean
) as empty-sequence() {
  let $catalog := doc($path || $file:directory-separator || 'catalog.xml'),
      $root    := replace(base-uri($catalog), 'catalog\.xml$', ''),
      $files   := qt3ts:get-files($root, $catalog),
      $envs    := qt3ts:environments(map:new(), $catalog/catalog/environment, $files)
  let $files   :=
        qt3ts-java:test-suites(
          $package,
          map:new((
            for $test-set in
              if($test-sets = '*') then $catalog//test-set
              else
                for $name in $test-sets
                let $set := $catalog//test-set[@name = $name]
                return if($set) then $set else qt3ts:debug('Test-set not found', $name)
            return qt3ts:test-set($test-set, $root, $package, $supports, $envs)
          ))
        )
  for $name in map:keys($files)
  let $path := $out-path || $file:directory-separator || $name || '.java'
  return (
    file:create-directory(replace($path, '[^/\\]+$', '')),
    file:write($path, $files($name), map{ 'method' := 'text' })
  )
};

(:~
 : Processes a test-set.
 : @param $test-set the test-set element
 : @param $package Java package
 : @param $out-path path for the generated Java files
 : @return full class name
 :)
declare function qt3ts:test-set(
  $test-set as element(test-set),
  $root as xs:string,
  $package as xs:string,
  $supports as function(xs:string, xs:string*) as xs:boolean,
  $envs as map(*)
) as map(*)? {
  let $name := $test-set/@name/string(),
      $dir  := replace($test-set/@file, '/[^/]+$', ''),
      $path := $root || $test-set/@file,
      $doc  := doc($path),
      $file := qt3ts:get-files($root, $doc),
      $envs := qt3ts:environments($envs, $doc/test-set/environment, $file),
      $desc := if($doc/test-set/description)
                 then $doc/test-set/description/string()
                 else 'Test-set ' || $name || '.'
  let $tests := map:new((
    for $test-case in $doc/test-set/test-case
    where every $dep in $test-case/dependency
      satisfies $supports($dep/@type, tokenize($dep/@value, ' '))
    return map{ $test-case/@name := qt3ts:test-case($test-set, $test-case, $root, $envs, $file) }
  ))
  return if(map:size($tests) gt 0)
    then
      let $ts := qt3ts-java:test-set($package, $dir, $name, $desc, $tests)
      return if(contains($ts[2], ' void '))
        then map:entry($dir || '/' || $ts[1], $ts[2])
        else qt3ts:debug('Test-set skipped after serialization', $name)
    else qt3ts:debug('Test-set skipped', $name)
};

declare function qt3ts:test-case(
  $test-set as element(test-set),
  $test-case as element(test-case),
  $root as xs:string,
  $envs as map(*),
  $file as map(*)
) as map(*) {
  let $desc := $test-case/description/string(),
      $env := $test-case/environment,
      $mods := map:new((
          for $mod in $test-case/module
          return map:entry(
            xs:anyURI($mod/@uri/string()),
            $file($mod/@file)
          )
        )),
      $map :=
        if($env) then
          if($env/@ref) then
            if(map:contains($envs, $env/@ref))
              then $envs($env/@ref)
              else (
                qt3ts:debug('Couldn''t find environment',
                  $test-set/@name || ', ' || $env/@ref),
                map{}
              )
          else qt3ts:environment($env, $file)
        else map{},
      $test := if($test-case/test/@file)
        then qt3ts:read-test($test-set, $test-case)
        else $test-case/test/string(),
      $result := qt3ts:result($test-case/result/*, $file, $root)
  return map{
    'description' := $desc,
    'modules'     := $mods,
    'environment' := $map,
    'test'        := $test,
    'result'      := $result
  }
};

declare function qt3ts:result(
  $assert as element(),
  $file as map(*),
  $root as xs:string
) as map(*) {
  let $name := $assert/local-name()
  return map:entry($name,
    switch($name)
      case 'assert-true' return ()
      case 'assert-false' return ()
      case 'assert-empty' return ()
      case 'assert' return $assert/string()
      case 'assert-eq' return $assert/string()
      case 'assert-string-value' return map{
          'normalize-space' := $assert/@normalize-space = ('true', '1'),
          'result'          := $assert/string()
        }
      case 'assert-permutation' return $assert/string()
      case 'assert-type' return $assert/string()
      case 'assert-deep-eq' return $assert/string()
      case 'assert-count' return xs:integer($assert/string())
      case 'error' return $assert/@code/string()
      case 'assert-serialization-error' return $assert/@code/string()
      case 'assert-serialization' return map{
          'ignore-prefixes' := $assert/@ignore-prefixes = ('true', '1'),
          'result' := if($assert/@file)
            then
              let $path := $root || $file($assert/@file)
              return unparsed-text($path, 'UTF-8')
            else $assert/string()
        }
      case 'all-of' return map(qt3ts:result(?, $file, $root), $assert/*)
      case 'any-of' return map(qt3ts:result(?, $file, $root), $assert/*)
      default return qt3ts:debug('Unknown assertion', $name)
  )
};

declare function qt3ts:read-test(
  $test-set as element(test-set),
  $test-case as element(test-case)
) as xs:string {
  let $path := replace(base-uri($test-case), '[^/]+$', $test-case/test/@file)
  return unparsed-text($path, 'UTF-8')
};

declare function qt3ts:environments(
  $map as map(*),
  $envs as element(environment)*,
  $file as map(*)
) as map(*) {
  map:new(
    ($map,
      for $env in $envs
      return if($env/@name)
        then map:entry($env/@name, qt3ts:environment($env, $file))
        else qt3ts:debug('Environment has no name', serialize($env))
    )
  )
};

declare function qt3ts:environment(
  $envs as element(environment),
  $file as map(*)
) as map(*)* {
  for $env in $envs/*
  let $name := xs:string($env/local-name())
  return switch($name)
    case 'source'
      return map:entry($name, map{
        'role' := $env/@role/string(),
        'file' := $file($env/@file/string()),
        'validation' := $env/@validation/string(),
        'uri' := xs:anyURI($env/@uri/string())
      })
    case 'namespace'
      return map:entry($name, map{
        'prefix' := $env/@prefix/string(),
        'uri' := xs:anyURI($env/@uri/string())
      })
    case 'collation'
      return map:entry($name, map{
        'uri' := xs:anyURI($env/@uri/string()),
        'default' := $env/@default = 'true'
      })
    case 'collection'
      return map:entry($name, map{
        'uri' := xs:anyURI($env/@uri/string()),
        'docs' := map:new((
          for $src in $env/source
          return map:entry($src/@file, map{
            'role' := $src/@role/string(),
            'file' := $file($src/@file/string()),
            'validation' := $src/@validation/string(),
            'uri' := xs:anyURI($src/@uri/string())
          })
        ))
      })
    case 'decimal-format'
      return map:entry($name, map:new((
        for $att in $env/@*
        return map:entry($att/name(), $att/string())
      )))
    case 'resource'
      return map:entry($name, map{
        'file' := $file($env/@file/string()),
        'media-type' := $env/@media-type/string(),
        'encoding' := $env/@encoding/string(),
        'uri' := xs:anyURI($env/@uri/string())
      })
    case 'schema'
      return map:entry($name, map{
        'uri' := xs:anyURI($env/@uri/string()),
        'file' := $file($env/@file/string())
      })
    case 'static-base-uri'
      return map:entry($name, xs:anyURI($env/@uri/string()))
    case 'param'
      return map:entry($name, map{
        'name' := $env/@name/string(),
        'select' := $env/@select/string(),
        'as' := $env/@as/string(),
        'declared' := $env/@declared != 'false'
      })
    default
      return qt3ts:debug('Unknown environment parameter:', $env)
};

declare function qt3ts:get-files(
  $root as xs:string,
  $node as node()
)  as map(*) {
  map:new((
    let $root-len := string-length(file:path-to-native(substring($root, 9))) + 2,
        $uri := base-uri($node)
    for $file in $node//@file
    let $uri := replace($uri, '[^/]+$', $file)
    group by $path := replace(substring(
      file:path-to-native(substring($uri, 9)), $root-len), '\\', '/')
    return
      for $file in $file
      return map:entry($file, $path)
  ))
};

declare function qt3ts:insert-with(
  $combine as function(item()*, item()*) as item()*,
  $map as map(*),
  $key as item(),
  $value as item()*
) as map(*) {
  map:new(
    ( $map,
      map:entry(
        $key,
        if(map:contains($map, $key))
          then $combine(map:get($map, $key), $value)
          else $value
      )
    )
  )
};

declare function qt3ts:debug(
  $msg as xs:string,
  $itm as item()
) as empty-sequence() {
  trace($itm, $msg)[2]
};
