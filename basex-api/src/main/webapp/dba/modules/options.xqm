(:~
 : Global options.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace options = 'dba/options';

(:~ DBA directory. :)
declare variable $options:DBA-DIRECTORY := (
  for $dir in db:option('dbpath') || '/.dba'
  return (
    if(file:exists($dir)) then () else file:create-dir($dir),
    file:path-to-native($dir)
  )
);

(:~ Permissions. :)
declare variable $options:PERMISSIONS := ('none', 'read', 'write', 'create', 'admin');

(:~ Maximum length of XML characters. :)
declare variable $options:MAXCHARS := 'maxchars';
(:~ Maximum number of table entries. :)
declare variable $options:MAXROWS := 'maxrows';
(:~ Query timeout. :)
declare variable $options:TIMEOUT := 'timeout';
(:~ Maximal memory consumption. :)
declare variable $options:MEMORY := 'memory';
(:~ Permission when running queries. :)
declare variable $options:PERMISSION := 'permission';

(:~ Options file. :)
declare %private variable $options:FILE := $options:DBA-DIRECTORY || '.dba.xml';

(:~ Options. :)
declare %basex:lazy %private variable $options:OPTIONS := (
  let $defaults := map {
    $options:MAXCHARS  : 200000,
    $options:MAXROWS   : 200,
    $options:TIMEOUT   : 30,
    $options:MEMORY    : 500,
    $options:PERMISSION: 'admin'
  }
  return if(file:exists($options:FILE)) then (
    try {
      (: merge defaults with saved options :)
      let $options := fetch:xml($options:FILE)/options
      return map:merge(
        map:for-each($defaults, function($key, $value) {
          map:entry($key,
            let $option := $options/*[name() = $key]
            return if($option) then (
              if($value instance of xs:numeric) then xs:integer($option) else xs:string($option)
            ) else (
              $value
            )
          )
        })
      )
    } catch * {
      (: use defaults if an error occurs while parsing the options :)
      $defaults
    }
  ) else (
    $defaults
  )
);

(:~
 : Returns the value of an option.
 : @param  $name  name of option
 : @return value
 :)
declare function options:get(
  $name  as xs:string
) as xs:anyAtomicType {
  $options:OPTIONS($name)
};

(:~
 : Saves options.
 : @param  $options  keys/values that have been changed
 :)
declare function options:save(
  $options  as map(*)
) as empty-sequence() {
  file:write($options:FILE, element options {
    map:for-each($options:OPTIONS, function($key, $value) {
      element { $key } { ($options($key), $value)[1] }
    })
  })
};
