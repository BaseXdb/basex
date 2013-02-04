import module namespace qt3ts='http://www.basex.org/modules/qt3ts' at 'qt3ts/qt3ts.xqm';

declare option db:chop "off";
declare option db:intparse "off";

declare variable $QT3TS external;

qt3ts:to-junit(
  $QT3TS,
  '*',
  'org.basex.test.qt3ts',
  'src/test/java/org/basex/test/qt3ts',
  function($type, $vals) {
    switch($type)
      case 'spec'
        return $vals = ('XQ10+', 'XQ30', 'XQ30+', 'XT30', 'XT30+')
      case 'xsd-version'
        return $vals = '1.0'
      case 'feature'
        return switch($vals)
          case 'schemaValidation' return false()
          case 'schemaImport' return false()
          case 'collection-stability' return true()
          case 'directory-as-collection-uri' return true()
          case 'staticTyping' return false()
          case 'namespace-axis' return false()
          case 'schema-location-hint' return false()
          case 'higherOrderFunctions' return true()
          default return (qt3ts:debug('Unknown feature', $vals), true())
      case 'xml-version'
        return some $v in $vals satisfies tokenize($v, ':')[1] = '1.0'
      case 'default-language'
      case 'language'
        return $vals = 'en'
      case 'unicode-normalization-form'
        return $vals = ('NFD', 'NFC', 'NFKD', 'NFKC')
      default
        return (qt3ts:debug('Unknown dependency', $type), true())
  }
)