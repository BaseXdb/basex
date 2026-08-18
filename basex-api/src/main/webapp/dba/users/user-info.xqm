(:~
 : User information.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace user-info = 'dba/lib/user-info';

(:~
 : Converts a user info string to XML.
 : @param  $info  user info
 : @return info element
 :)
declare function user-info:parse(
  $info  as xs:string
) as element(info) {
  if ($info) {
    let $xml := try {
      parse-xml($info)/*
    } catch * {
      error((), 'User information is not well-formed XML.')
    }
    return if ($xml/self::info) {
      $xml update {
        delete node .//text()[not(normalize-space())]
      }
    } else {
      error((), 'User information has no "info" root element.')
    }
  } else {
    element info { }
  }
};
