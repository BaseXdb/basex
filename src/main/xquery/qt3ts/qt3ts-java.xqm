(:~
 : Serialization module.
 : @author Leo Woerteler
 : @version 0.1
 :)
module namespace qt3ts-java='http://www.basex.org/modules/qt3ts/java';

declare variable $qt3ts-java:not-supported :=
  QName('http://www.basex.org/modules/qt3ts/java', 'NSUP0001');

declare function qt3ts-java:test-suites(
  $package,
  $test-sets
) as map(*) {
  map:new((
    $test-sets,
    for $path in map:keys($test-sets)
    let $parts   := tokenize($path, '/'),
        $classes := $parts[2]
    group by $suite := $parts[1]
    order by $suite
    return
      let $suite-name := qt3ts-java:class-name($suite || 'Tests')
      return map:entry(
        $suite-name,
        string-join((
  'package ', $package, ';

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.basex.test.qt3ts.', $suite, '.*;

/**
 * Test suite for the "', $suite, '" test group.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@RunWith(Suite.class)
@SuiteClasses({',
  string-join(
    for $class in $classes
    order by $class
    return '&#xa;  ' || $class || '.class',
    ','
  ),
'
})
public class ', $suite-name, ' { }
'
      ))
    )
  ))
};

declare function qt3ts-java:test-set(
  $package as xs:string,
  $dir as xs:string,
  $name as xs:string,
  $comment as xs:string,
  $test-cases as map(*)
) as xs:string+ {
  let $class-name := qt3ts-java:class-name($name)
  return (
    $class-name,
    string-join((
'package ', $package, '.', $dir, ';

import org.basex.tests.bxapi.XQuery;
import ', $package, '.QT3TestSet;

/**
',
  string-join(
    for $line in tokenize($comment, '\r?\n|\r')
    return concat(' * ', replace($line, '\*/', '* /')),
    '&#xa;'
  ),
  '.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ',  $class-name, ' extends QT3TestSet {
',
  for $name in map:keys($test-cases)
  let $test-case := $test-cases($name)
  order by $name
  return try {'
  /**
',
  string-join(
    for $line in tokenize($test-case('description'), '\r?\n|\r')
    return concat('   * ', replace($line, '\*/', '* /')),
    '&#xa;'
  ),
  '.
   */
  @org.junit.Test
  public void ', qt3ts-java:method-name($name), '() {
', '    xquery10();
'[$test-case('xq10')],
'    final XQuery query = new XQuery(
',  if($test-case('is_file')) then (
'      queryFile(
        file(
',        qt3ts-java:string-literal($test-case('test'), '            '), '
        )
      )'
    ) else qt3ts-java:string-literal($test-case('test'), '      '), ',
      ctx);
    try {
',
  for $line in qt3ts-java:environment($test-case('environment'))
  return concat('      ', $line, '&#xa;'),

  let $mods := $test-case('modules')
  for $mod in map:keys($mods)
  return concat('      query.addModule("', qt3ts-java:escape-string($mod),
    '", file("', qt3ts-java:escape-string($mods($mod)), '"));&#xa;'),
'      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
',
  for $line in qt3ts-java:result($test-case('result'), '')
  return concat('      ', $line, '&#xa;'), 
'    );
  }
'
  } catch qt3ts-java:NSUP0001 {
    () (: trace($err:description, 'Skipped test "' || $name || '" because of environment: ')[2] :)
  },
'}
'), '')
  )
};

declare function qt3ts-java:environment(
  $envs as map(*)*
) as xs:string* {
  for $env in $envs
  for $name in map:keys($env)
  let $vals := $env($name)
  return switch($name)
    case 'source'
      return if(exists($vals('validation'))) then error($qt3ts-java:not-supported, 'validation')
        else if($vals('role') = '.')
          then 'query.context(node(file("' || qt3ts-java:escape-string($vals('file')) || '")));'
        else if(exists($vals('role')))
          then 'query.bind("' || qt3ts-java:escape-string($vals('role')) || '", ' ||
          'node(file("' || qt3ts-java:escape-string($vals('file')) || '")));'
        else 'query.addDocument("' || qt3ts-java:escape-string(($vals('uri'), $vals('file'))[1]) ||
          '", ' || 'file("' || qt3ts-java:escape-string($vals('file')) || '"));'

    case 'namespace'
      return 'query.namespace("' || qt3ts-java:escape-string($vals('prefix')) ||
        '", "' || qt3ts-java:escape-string($vals('uri')) || '");'
    case 'collation'
      return if($vals('uri') != 'http://www.w3.org/2005/xpath-functions/collation/codepoint')
        then error($qt3ts-java:not-supported, 'uri: ' || $vals('uri'))
        else ()
    case 'collection'
      return 'query.addCollection("' || qt3ts-java:escape-string($vals('uri')) || '", ' ||
        'new String[] { file("' || string-join(
          for $doc in map:keys($vals('docs'))
          return qt3ts-java:escape-string($vals('docs')($doc)('file'))
        , '"), file("') || '") });'
    case 'decimal-format'
      return '// decimal format'
    case 'resource'
      return '// resource: ' || $vals('uri')
    case 'schema'
      return error($qt3ts-java:not-supported, 'schema')
    case 'static-base-uri'
      return 'query.baseURI("' || qt3ts-java:escape-string($vals) || '");'
    case 'param'
      return 'query.bind("' || qt3ts-java:escape-string($vals('name')) || '", ' ||
        'new XQuery("' || qt3ts-java:escape-string($vals('select')) || '", ctx).value());'
      (: map:entry($name, map{
        'name' := $env/@name/string(),
        'select' := $env/@select/string(),
        'as' := $env/@as/string(),
        'declared' := $env/@declared != 'false'
      }) :)
    case 'context-item'
      return 'query.context(new XQuery("' || qt3ts-java:escape-string($vals) || '", ctx).value());'
    default
      return trace('', 'Unknown Java-serialization for: ')[2]
};

declare function qt3ts-java:result(
  $result as map(*),
  $indent as xs:string
) as xs:string* {
  let $name := map:keys($result),
      $vals := $result($name)
  return
    switch($name)
      case 'assert-true' return $indent || 'assertBoolean(true)'
      case 'assert-false' return $indent || 'assertBoolean(false)'
      case 'assert-empty' return $indent || 'assertEmpty()'
      case 'assert'
        return $indent || 'assertQuery("' || qt3ts-java:escape-string($vals) || '")'
      case 'assert-eq'
        return $indent || 'assertEq("' || qt3ts-java:escape-string($vals) || '")'
      case 'assert-string-value'
        return $indent || 'assertStringValue(' || $vals('normalize-space') || ', "' ||
          qt3ts-java:escape-string($vals('result')) || '")'
      case 'assert-permutation'
        return $indent || 'assertPermutation("' || qt3ts-java:escape-string($vals) || '")'
      case 'assert-type'
        return $indent || 'assertType("' || qt3ts-java:escape-string($vals) || '")'
      case 'assert-deep-eq'
        return $indent || 'assertDeepEq("' || qt3ts-java:escape-string($vals) || '")'
      case 'assert-count' return $indent || 'assertCount(' || $vals || ')'
      case 'error'
        return $indent || 'error("' || qt3ts-java:escape-string($vals) || '")'
      case 'assert-serialization-error'
        return $indent || 'assertSerialError("' || qt3ts-java:escape-string($vals) || '")'
      case 'assert-xml'
        return $indent || 'assertSerialization("' ||
          qt3ts-java:escape-string($vals('result')) || '", ' || $vals('ignore-prefixes') || ')'
      case 'serialization-matches'
        return $indent || 'serializationMatches("' ||
          qt3ts-java:escape-string($vals('regex')) || '", "' ||
          qt3ts-java:escape-string($vals('flags')) || '")'
      case 'all-of' return (
          $indent || '(',
          tail(
            for $sub in $vals
            return (
              $indent || '&amp;&amp;',
              qt3ts-java:result($sub, '  ' || $indent)
            )
          ),
          $indent || ')'
        )
      case 'any-of' return (
          $indent || '(',
          tail(
            for $sub in $vals
            return (
              $indent || '||',
              qt3ts-java:result($sub, '  ' || $indent)
            )
          ),
          $indent || ')'
        )
      default return 'true'
};

declare function qt3ts-java:string-literal(
  $str as xs:string,
  $indent as xs:string
) as xs:string {
  if(empty($str) or string-length($str) eq 0)
  then $indent || '""'
  else concat(
    string-join(
      for $line in qt3ts-java:lines($str)
      return concat($indent, '"', qt3ts-java:escape-string($line)),
      '\n" +&#xa;'
    ),
    '"'
  )
};

declare function qt3ts-java:method-name(
  $name as xs:string
) as xs:string {
  let $camel :=  qt3ts-java:class-name($name)
  return concat(lower-case(substring($camel, 1, 1)), substring($camel, 2))
};

declare function qt3ts-java:class-name(
  $name as xs:string
) as xs:string {
  string-join(
    for $part in tokenize($name, '\W')
    where string-length($part) gt 0
    return concat(upper-case(substring($part, 1, 1)), substring($part, 2))
  )
};

declare function qt3ts-java:escape-string(
  $str as xs:string
) as xs:string {
  string-join((
    for $cp in string-to-codepoints($str)
    return switch($cp)
      case  9 return '\t'
      case 10 return '\n'
      case 11 return '\v'
      case 13 return '\r'
      case 34 return '\"'
      case 92 return '\\'
      default return
        if($cp ge 0 and $cp le 31 or $cp ge 127 and $cp le 159)
        then out:format('\u%04x', $cp)
        else
          let $ch := codepoints-to-string($cp) return
          if($cp le 255 or matches($ch, '\w')) then $ch
          else if($cp lt 65536) then out:format('\u%04x', $cp)
          else
            let $offset := $cp - 65536,
                $high   := xs:int(($offset idiv 1024) + 55296),
                $low    := xs:int(($offset mod 1024) + 56320)
            return out:format('\u%04x\u%04x', $high, $low)
  ))
};

declare function qt3ts-java:lines(
  $str as xs:string
) as xs:string* {
  tokenize($str, '&#xa;')
};
