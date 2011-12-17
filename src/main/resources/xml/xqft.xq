declare namespace fts="http://www.w3.org/2007/xpath-full-text";

declare function fts:matchTokenInfos (
      $searchContext as item(),
      $matchOptions as element(fts:matchOptions),
      $stopWords as xs:string*,
      $queryTokens as element(fts:queryToken)* )
   as element(fts:tokenInfo)*  {()};


declare function fts:evaluate (
      $ftSelection as element(*, fts:ftSelection),
      $searchContext as item(),
      $matchOptions as element(fts:matchOptions),
      $queryTokenNum as xs:integer )
   as element(fts:allMatches)
{
   if (fn:count($ftSelection/fts:matchOptions) > 0) then
      (: First we deal with all match options that the    :)
      (: FTSelection might bear: we add the match options :)
      (: to the current match options structure, and      :)
      (: pass the new structure to the recursive call.    :)
      let $newFTSelection :=
         <fts:selection>{$ftSelection/*
                           [fn:not(self::fts:matchOptions)]}</fts:selection>
      return fts:evaluate($newFTSelection,
                          $searchContext,
                          fts:replaceMatchOptions($matchOptions,
                                              $ftSelection/fts:matchOptions),
                          $queryTokenNum)
   else if (fn:count($ftSelection/fts:weight) > 0) then
      (: Weight has no bearing on semantics -- just :)
      (: call "evaluate" on nested FTSelection     :)
      let $newFTSelection := $ftSelection/*[fn:not(self::fts:weight)]
      return fts:evaluate($newFTSelection,
                          $searchContext,
                          $matchOptions,
                          $queryTokenNum)
   else
      typeswitch ($ftSelection/*[1])
         case $nftSelection as element(fts:ftWords) return
            (: Apply the FTWords in the search context :)
            fts:ApplyFTWords($searchContext,
                             $matchOptions,
                             $nftSelection/@type,
                             $nftSelection/fts:queryItem,
                             $queryTokenNum + 1)
         case $nftSelection as element(fts:ftAnd) return
            let $left := fts:evaluate($nftSelection/fts:left,
                                     $searchContext,
                                     $matchOptions,
                                     $queryTokenNum)
            let $newQueryTokenNum := $left/@stokenNum
            let $right := fts:evaluate($nftSelection/fts:right,
                                      $searchContext,
                                      $matchOptions,
                                      $newQueryTokenNum)
            return fts:ApplyFTAnd($left, $right)
         case $nftSelection as element(fts:ftOr) return
            let $left := fts:evaluate($nftSelection/fts:left,
                                     $searchContext,
                                     $matchOptions,
                                     $queryTokenNum)
            let $newQueryTokenNum := $left/@stokenNum
            let $right := fts:evaluate($nftSelection/fts:right,
                                      $searchContext,
                                      $matchOptions,
                                      $newQueryTokenNum)
            return fts:ApplyFTOr($left, $right)
         case $nftSelection as element(fts:ftUnaryNot) return
            let $nested := fts:evaluate($nftSelection/fts:selection,
                                        $searchContext,
                                        $matchOptions,
                                        $queryTokenNum)
            return fts:ApplyFTUnaryNot($nested)
         case $nftSelection as element(fts:ftMildNot) return
            let $left := fts:evaluate($nftSelection/fts:left,
                                     $searchContext,
                                     $matchOptions,
                                     $queryTokenNum)
            let $newQueryTokenNum := $left/@stokenNum
            let $right := fts:evaluate($nftSelection/fts:right,
                                      $searchContext,
                                      $matchOptions,
                                      $newQueryTokenNum)
            return fts:ApplyFTMildNot($left, $right)
         case $nftSelection as element(fts:ftOrder) return
            let $nested := fts:evaluate($nftSelection/fts:selection,
                                        $searchContext,
                                        $matchOptions,
                                        $queryTokenNum)
            return fts:ApplyFTOrder($nested)
         case $nftSelection as element(fts:ftScope) return
            let $nested := fts:evaluate($nftSelection/fts:selection,
                                        $searchContext,
                                        $matchOptions,
                                        $queryTokenNum)
            return fts:ApplyFTScope($nftSelection/@type,
                                    $nftSelection/@scope,
                                    $nested)
         case $nftSelection as element(fts:ftContent) return
            let $nested := fts:evaluate($nftSelection/fts:selection,
                                        $searchContext,
                                        $matchOptions,
                                        $queryTokenNum)
            return fts:ApplyFTContent($searchContext,
                                      $nftSelection/@type,
                                      $nested)
         case $nftSelection as element(fts:ftDistance) return
            let $nested := fts:evaluate($nftSelection/fts:selection,
                                        $searchContext,
                                        $matchOptions,
                                        $queryTokenNum)
            return fts:ApplyFTDistance($nftSelection/@type,
                                       $nftSelection/fts:range,
                                       $nested)
         case $nftSelection as element(fts:ftWindow) return
            let $nested := fts:evaluate($nftSelection/fts:selection,
                                        $searchContext,
                                        $matchOptions,
                                        $queryTokenNum)
            return fts:ApplyFTWindow($nftSelection/@type,
                                     $nftSelection/@size,
                                     $nested)
         case $nftSelection as element(fts:ftTimes) return
            let $nested := fts:evaluate($nftSelection/fts:selection,
                                        $searchContext,
                                        $matchOptions,
                                        $queryTokenNum)
            return fts:ApplyFTTimes($nftSelection/fts:range,
                                    $nested)
         default return <fts:allMatches stokenNum="0" />
};


(: simplified version not dealing with special match options :)
declare function fts:applyQueryTokensAsPhraseSimple (
      $searchContext as item(),
      $matchOptions as element(fts:matchOptions),
      $queryTokens as element(fts:queryToken)*,
      $queryPos as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$queryPos}">
   {
      for $tokenInfo in
         fts:matchTokenInfos(
            $searchContext,
            $matchOptions,
            (),
            $queryTokens )
      return
         <fts:match>
            <fts:stringInclude queryPos="{$queryPos}" isContiguous="true">
            {$tokenInfo}
            </fts:stringInclude>
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:MakeDisjunction (
      $curRes as element(fts:allMatches),
      $rest as element(fts:allMatches)* )
   as element(fts:allMatches)
{
   if (fn:count($rest) = 0)
   then $curRes
   else
      let $firstAllMatches := $rest[1]
      let $restAllMatches := fn:subsequence($rest, 2)
      let $newCurRes := fts:ApplyFTOr($curRes,
                                      $firstAllMatches)
      return fts:MakeDisjunction($newCurRes,
                                 $restAllMatches)
};

declare function fts:ApplyFTWordsAnyWord (
      $searchContext as item(),
      $matchOptions as element(fts:matchOptions),
      $queryItems as element(fts:queryItem)*,
      $queryPos as xs:integer )
   as element(fts:allMatches)
{
   (: Tokenization of query string has already occurred. :)
   (: Get sequence of QueryTokens over all query items. :)
   let $queryTokens := $queryItems/fts:queryToken
   return
      if (fn:count($queryItems) eq 0)
      then <fts:allMatches stokenNum="0" />
      else
         let $allAllMatches :=
            for $queryToken at $pos in $queryTokens
            return fts:applyQueryTokensAsPhrase($searchContext,
                                                 $matchOptions,
                                                 $queryToken,
                                                 $queryPos + $pos - 1)
         let $firstAllMatches := $allAllMatches[1]
         let $restAllMatches := fn:subsequence($allAllMatches, 2)
         return fts:MakeDisjunction($firstAllMatches, $restAllMatches)
};


declare function fts:MakeConjunction (
      $curRes as element(fts:allMatches),
      $rest as element(fts:allMatches)* )
   as element(fts:allMatches)
{
   if (fn:count($rest) = 0)
   then $curRes
   else
      let $firstAllMatches := $rest[1]
      let $restAllMatches := fn:subsequence($rest, 2)
      let $newCurRes := fts:ApplyFTAnd($curRes,
                                       $firstAllMatches)
      return fts:MakeConjunction($newCurRes,
                                 $restAllMatches)
};

declare function fts:ApplyFTWordsAllWord (
      $searchContext as item(),
      $matchOptions as element(fts:matchOptions),
      $queryItems as element(fts:queryItem)*,
      $queryPos as xs:integer )
   as element(fts:allMatches)
{
   (: Tokenization of query strings has already occurred. :)
   (: Get sequence of QueryTokens over all query items :)
   let $queryTokens := $queryItems/fts:queryToken
   return
      if (fn:count($queryTokens) eq 0)
      then <fts:allMatches stokenNum="0" />
      else
         let $allAllMatches :=
            for $queryToken at $pos in $queryTokens
            return fts:applyQueryTokensAsPhrase($searchContext,
                                                 $matchOptions,
                                                 $queryToken,
                                                 $queryPos + $pos - 1)
            let $firstAllMatches := $allAllMatches[1]
            let $restAllMatches := fn:subsequence($allAllMatches, 2)
            return fts:MakeConjunction($firstAllMatches, $restAllMatches)
};


declare function fts:ApplyFTWordsPhrase (
      $searchContext as item(),
      $matchOptions as element(fts:matchOptions),
      $queryItems as element(fts:queryItem)*,
      $queryPos as xs:integer )
   as element(fts:allMatches)
{
   (: Get sequence of QueryTokenInfos over all query items :)
   let $queryTokens := $queryItems/fts:queryToken
   return
      if (fn:count($queryTokens) eq 0)
      then <fts:allMatches stokenNum="0" />
      else
         fts:applyQueryTokensAsPhrase($searchContext,
                                       $matchOptions,
                                       $queryTokens,
                                       $queryPos)
};


declare function fts:ApplyFTWordsAny (
      $searchContext as item(),
      $matchOptions as element(fts:matchOptions),
      $queryItems as element(fts:queryItem)*,
      $queryPos as xs:integer )
   as element(fts:allMatches)
{
   if (fn:count($queryItems) eq 0)
   then <fts:allMatches stokenNum="0" />
   else
      let $firstQueryItem := $queryItems[1]
      let $restQueryItem := fn:subsequence($queryItems, 2)
      let $firstAllMatches :=
         fts:ApplyFTWordsPhrase($searchContext,
                                $matchOptions,
                                $firstQueryItem,
                                $queryPos)
      let $newQueryPos :=
         if ($firstAllMatches//@queryPos)
         then fn:max($firstAllMatches//@queryPos) + 1
         else $queryPos
      let $restAllMatches :=
         fts:ApplyFTWordsAny($searchContext,
                             $matchOptions,
                             $restQueryItem,
                             $newQueryPos)
      return fts:ApplyFTOr($firstAllMatches, $restAllMatches)
};


declare function fts:ApplyFTWordsAll (
      $searchContext as item(),
      $matchOptions as element(fts:matchOptions),
      $queryItems as element(fts:queryItem)*,
      $queryPos as xs:integer )
   as element(fts:allMatches)
{
   if (fn:count($queryItems) = 0)
   then <fts:allMatches stokenNum="0" />
   else
      let $firstQueryItem := $queryItems[1]
      let $restQueryItem := fn:subsequence($queryItems, 2)
      let $firstAllMatches :=
         fts:ApplyFTWordsPhrase($searchContext,
                                $matchOptions,
                                $firstQueryItem,
                                $queryPos)
      return
         if ($restQueryItem) then
            let $newQueryPos :=
               if ($firstAllMatches//@queryPos)
               then fn:max($firstAllMatches//@queryPos) + 1
               else $queryPos
            let $restAllMatches :=
               fts:ApplyFTWordsAll($searchContext,
                                   $matchOptions,
                                   $restQueryItem,
                                   $newQueryPos)
            return
               fts:ApplyFTAnd($firstAllMatches, $restAllMatches)
         else $firstAllMatches
};


declare function fts:ApplyFTWords (
      $searchContext as item(),
      $matchOptions as element(fts:matchOptions),
      $type as fts:ftWordsType,
      $queryItems as element(fts:queryItem)*,
      $queryPos as xs:integer )
   as element(fts:allMatches)
{
   if ($type eq "any word")
   then fts:ApplyFTWordsAnyWord($searchContext,
                                $matchOptions,
                                $queryItems,
                                $queryPos)
   else if ($type eq "all word")
   then fts:ApplyFTWordsAllWord($searchContext,
                                $matchOptions,
                                $queryItems,
                                $queryPos)
   else if ($type eq "phrase")
   then fts:ApplyFTWordsPhrase($searchContext,
                               $matchOptions,
                               $queryItems,
                               $queryPos)
   else if ($type eq "any")
   then fts:ApplyFTWordsAny($searchContext,
                            $matchOptions,
                            $queryItems,
                            $queryPos)
   else fts:ApplyFTWordsAll($searchContext,
                            $matchOptions,
                            $queryItems,
                            $queryPos)
};


declare function fts:applyQueryTokensAsPhrase (
      $searchContext as item(),
      $matchOptions as element(fts:matchOptions),
      $queryTokens as element(fts:queryToken)*,
      $queryPos as xs:integer )
   as element(fts:allMatches)
{
   let $thesaurusOption := $matchOptions/fts:thesaurus[1]
   return
      if ($thesaurusOption and
          $thesaurusOption/@thesaurusIndicator eq "with") then
         let $noThesaurusOptions :=
            <fts:matchOptions>{
               $matchOptions/*[fn:not(self::fts:thesaurus)]
            }</fts:matchOptions>
         let $lookupRes := fts:applyThesaurusOption($thesaurusOption,
                                                    $queryTokens)
         return fts:ApplyFTWordsAny($searchContext,
                                    $noThesaurusOptions,
                                    $lookupRes,
                                    $queryPos)
      else
         (: from here on we have a single sequence of query tokens :)
         (: which is to be matched a phrase; no alternatives anymore :)
         <fts:allMatches stokenNum="{$queryPos}">
         {
            for $pos in
               fts:matchTokenInfos(
                  $searchContext,
                  $matchOptions,
                  fts:applyStopWordOption($matchOptions/fts:stopwords),
                  $queryTokens )
            return
               <fts:match>
                  <fts:stringInclude queryPos="{$queryPos}" isContiguous="true">
                  {$pos}
                  </fts:stringInclude>
               </fts:match>
         }
         </fts:allMatches>
};


declare function fts:replaceMatchOptions (
      $matchOptions as element(fts:matchOptions),
      $newMatchOptions as element(fts:matchOptions) )
   as element(fts:matchOptions)
{
   <fts:matchOptions>
   {
      (if ($newMatchOptions/fts:thesaurus) then $newMatchOptions/fts:thesaurus
       else $matchOptions/fts:thesaurus),
      (if ($newMatchOptions/fts:stopwords) then $newMatchOptions/fts:stopwords
       else $matchOptions/fts:stopwords),
      (if ($newMatchOptions/fts:case) then $newMatchOptions/fts:case
       else $matchOptions/fts:case),
      (if ($newMatchOptions/fts:diacritics) then $newMatchOptions/fts:diacritics
       else $matchOptions/fts:diacritics),
      (if ($newMatchOptions/fts:stem) then $newMatchOptions/fts:stem
       else $matchOptions/fts:stem),
      (if ($newMatchOptions/fts:wildcard) then $newMatchOptions/fts:wildcard
       else $matchOptions/fts:wildcard),
      (if ($newMatchOptions/fts:language) then $newMatchOptions/fts:language
       else $matchOptions/fts:language)
   }
   </fts:matchOptions>
};


declare function fts:resolveStopWordsUri ( $uri as xs:string? )
   as xs:string* {()};

declare function fts:lookupThesaurus (
      $tokens as element(fts:queryToken)*,
      $thesaurusName as xs:string?,
      $thesaurusLanguage as xs:string?,
      $relationship as xs:string?,
      $range as element(fts:range)? )
   as element(fts:queryItem)* {()};


declare function fts:applyThesaurusOption (
      $matchOption as element(fts:thesaurus),
      $queryTokens as element(fts:queryToken)* )
   as element(fts:queryItem)*
{
   if ($matchOption/@thesaurusIndicator = "with") then
      fts:lookupThesaurus( $queryTokens,
                           $matchOption/fts:thesaurusName,
                           $matchOption/@language,
                           $matchOption/fts:relationship,
                           $matchOption/fts:range )
   else if ($matchOption/@thesaurusIndicator = "without") then
      <fts:queryItem>
      {$queryTokens}
      </fts:queryItem>
   else ()
};


declare function fts:applyStopWordOption (
      $stopWordOption as element(fts:stopwords)? )
   as xs:string*
{
   if ($stopWordOption) then
      let $swords :=
         typeswitch ($stopWordOption/*[1])
            case $e as element(fts:stopword)
               return $e/text()
            case $e as element(fts:uri)
               return fts:resolveStopWordsUri($e/text())
            case element(fts:default-stopwords)
               return fts:resolveStopWordsUri(())
            default return ()
      return fts:calcStopWords( $swords, $stopWordOption/fts:oper )
   else ()
};
declare function fts:calcStopWords (
      $stopWords as xs:string*,
      $opers as element(fts:oper)* )
   as xs:string*
{
   if ( fn:empty($opers) ) then $stopWords
   else
      let $swords :=
         typeswitch ($opers[1]/*[1])
            case $e as element(fts:stopword)
               return $e/text()
            case $e as element(fts:uri)
               return fts:resolveStopWordsUri($e/text())
            default return ()
      return
         if ($opers[1]/@type eq "union") then
            fts:calcStopWords( ($stopWords, $swords),
                               $opers[fn:position() gt 2] )
         else (: "except" :)
            fts:calcStopWords( $stopWords[fn:not(.)=$swords],
                               $opers[fn:position() gt 2] )
};


declare function fts:ApplyFTOr (
      $allMatches1 as element(fts:allMatches),
      $allMatches2 as element(fts:allMatches) )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{fn:max(($allMatches1/@stokenNum,
                                       $allMatches2/@stokenNum))}">
   {$allMatches1/fts:match,$allMatches2/fts:match}
   </fts:allMatches>
};


declare function fts:ApplyFTAnd (
      $allMatches1 as element(fts:allMatches),
      $allMatches2 as element(fts:allMatches) )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{fn:max(($allMatches1/@stokenNum,
                                       $allMatches2/@stokenNum))}" >
   {
      for $sm1 in $allMatches1/fts:match
      for $sm2 in $allMatches2/fts:match
      return <fts:match>
             {$sm1/*, $sm2/*}
             </fts:match>
   }
   </fts:allMatches>
};


declare function fts:InvertStringMatch ( $strm as element(*,fts:stringMatch) )
   as element(*,fts:stringMatch)
{
   if ($strm instance of element(fts:stringExclude)) then
      <fts:stringInclude queryPos="{$strm/@queryPos}" isContiguous="{$strm/@isContiguous}">
      {$strm/fts:tokenInfo}
      </fts:stringInclude>
   else
      <fts:stringExclude queryPos="{$strm/@queryPos}" isContiguous="{$strm/@isContiguous}">
      {$strm/fts:tokenInfo}
      </fts:stringExclude>
};

declare function fts:UnaryNotHelper ( $matches as element(fts:match)* )
   as element(fts:match)*
{
   if (fn:empty($matches))
   then <fts:match/>
   else
      for $sm in $matches[1]/*
      for $rest in fts:UnaryNotHelper( fn:subsequence($matches, 2) )
      return
         <fts:match>
         {
            fts:InvertStringMatch($sm),
            $rest/*
         }
         </fts:match>
};

declare function fts:ApplyFTUnaryNot (
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      fts:UnaryNotHelper($allMatches/fts:match)
   }
   </fts:allMatches>
};


declare function fts:CoveredIncludePositions (
       $match as element(fts:match) )
    as xs:integer*
{
    for $strInclude in $match/fts:stringInclude
    return $strInclude/fts:tokenInfo/@startPos
           to $strInclude/fts:tokenInfo/@endPos
};

declare function fts:ApplyFTMildNot (
       $allMatches1 as element(fts:allMatches),
       $allMatches2 as element(fts:allMatches) )
    as element(fts:allMatches)
{
    if (fn:count($allMatches1//fts:stringExclude) gt 0) then
       fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'FTDY0017'),
                "Invalid expression on the left-hand side of a not-in")
    else if (fn:count($allMatches2//fts:stringExclude) gt 0) then
       fn:error(fn:QName('http://www.w3.org/2005/xqt-errors', 'FTDY0017'),
                "Invalid expression on the right-hand side of a not-in")
    else if (fn:count($allMatches2//fts:stringInclude) eq 0) then
       $allMatches1
    else
       <fts:allMatches stokenNum="{$allMatches1/@stokenNum}">
       {
          $allMatches1/fts:match[
             every $matches2 in $allMatches2/fts:match
             satisfies
                let $posSet1 := fts:CoveredIncludePositions(.)
                let $posSet2 := fts:CoveredIncludePositions($matches2)
                   return some $pos in $posSet1 satisfies fn:not($pos = $posSet2)
          ]
       }
       </fts:allMatches>
};


declare function fts:ApplyFTOrder (
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      where every $stringInclude1 in $match/fts:stringInclude,
                  $stringInclude2 in $match/fts:stringInclude
            satisfies (($stringInclude1/fts:tokenInfo/@startPos <=
                        $stringInclude2/fts:tokenInfo/@startPos)
                       and
                       ($stringInclude1/@queryPos <=
                        $stringInclude2/@queryPos))
                      or
                       (($stringInclude1/fts:tokenInfo/@startPos>=
                         $stringInclude2/fts:tokenInfo/@startPos)
                        and
                        ($stringInclude1/@queryPos >=
                         $stringInclude2/@queryPos))
      return
         <fts:match>
         {
            $match/fts:stringInclude,
            for $stringExcl in $match/fts:stringExclude
            where every $stringIncl in $match/fts:stringInclude
                  satisfies (($stringExcl/fts:tokenInfo/@startPos <=
                              $stringIncl/fts:tokenInfo/@startPos)
                             and
                              ($stringExcl/@queryPos <=
                               $stringIncl/@queryPos))
                            or
                             (($stringExcl/fts:tokenInfo/@startPos >=
                               $stringIncl/fts:tokenInfo/@startPos)
                              and
                              ($stringExcl/@queryPos >=
                               $stringIncl/@queryPos))
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTScopeSameSentence (
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      where every $stringInclude1 in $match/fts:stringInclude,
                  $stringInclude2 in $match/fts:stringInclude
            satisfies $stringInclude1/fts:tokenInfo/@startSent =
                      $stringInclude2/fts:tokenInfo/@startSent
                  and $stringInclude1/fts:tokenInfo/@startSent =
                      $stringInclude1/fts:tokenInfo/@endSent
                  and $stringInclude2/fts:tokenInfo/@startSent =
                      $stringInclude2/fts:tokenInfo/@endSent
                  and $stringInclude1/fts:tokenInfo/@startSent > 0
                  and $stringInclude2/fts:tokenInfo/@startSent > 0
      return
        <fts:match>
        {
           $match/fts:stringInclude,
           for $stringExcl in $match/fts:stringExclude
           where
              $stringExcl/fts:tokenInfo/@startSent = 0
              or
              ($stringExcl/fts:tokenInfo/@startSent =
               $stringExcl/fts:tokenInfo/@endSent
               and
                  (every $stringIncl in $match/fts:stringInclude
                   satisfies $stringIncl/fts:tokenInfo/@startSent =
                             $stringExcl/fts:tokenInfo/@startSent) )
           return $stringExcl
        }
        </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTScopeDifferentSentence (
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      where every $stringInclude1 in $match/fts:stringInclude,
                  $stringInclude2 in $match/fts:stringInclude
            satisfies $stringInclude1 is $stringInclude2
                  or $stringInclude1/fts:tokenInfo/@endSent <
                     $stringInclude2/fts:tokenInfo/@startSent
                  or $stringInclude2/fts:tokenInfo/@endSent <
                     $stringInclude1/fts:tokenInfo/@startSent
      return
         <fts:match>
         {
            $match/fts:stringInclude,
            for $stringExcl in $match/fts:stringExclude
            where every $stringIncl in $match/fts:stringInclude
                  satisfies $stringExcl/fts:tokenInfo/@endSent <
                            $stringIncl/fts:tokenInfo/@startSent
                         or $stringIncl/fts:tokenInfo/@endSent <
                            $stringExcl/fts:tokenInfo/@startSent
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTScopeSameParagraph (
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      where every $stringInclude1 in $match/fts:stringInclude,
                  $stringInclude2 in $match/fts:stringInclude
            satisfies $stringInclude1/fts:tokenInfo/@startPara =
                      $stringInclude2/fts:tokenInfo/@startPara
                  and $stringInclude1/fts:tokenInfo/@startPara =
                      $stringInclude1/fts:tokenInfo/@endPara
                  and $stringInclude2/fts:tokenInfo/@startPara =
                      $stringInclude2/fts:tokenInfo/@endPara
                  and $stringInclude1/fts:tokenInfo/@startPara > 0
                  and $stringInclude2/fts:tokenInfo/@endPara > 0
      return
         <fts:match>
         {
            $match/fts:stringInclude,
            for $stringExcl in $match/fts:stringExclude
            where
               $stringExcl/fts:tokenInfo/@startPara = 0
               or
               ($stringExcl/fts:tokenInfo/@startPara =
                $stringExcl/fts:tokenInfo/@endPara
                and
                   (every $stringIncl in $match/fts:stringInclude
                    satisfies $stringIncl/fts:tokenInfo/@startPara =
                              $stringExcl/fts:tokenInfo/@startPara) )
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTScopeDifferentParagraph (
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      where every $stringInclude1 in $match/fts:stringInclude,
                  $stringInclude2 in $match/fts:stringInclude
            satisfies $stringInclude1 is $stringInclude2
                   or $stringInclude1/fts:tokenInfo/@endPara <
                      $stringInclude2/fts:tokenInfo/@startPara
                   or $stringInclude2/fts:tokenInfo/@endPara <
                      $stringInclude1/fts:tokenInfo/@startPara
      return
         <fts:match>
         {
            $match/fts:stringInclude,
            for $stringExcl in $match/fts:stringExclude
            where every $stringIncl in $match/fts:stringInclude
                  satisfies $stringExcl/fts:tokenInfo/@endPara <
                            $stringIncl/fts:tokenInfo/@startPara
                         or $stringIncl/fts:tokenInfo/@endPara <
                            $stringExcl/fts:tokenInfo/@startPara
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTScope (
      $type as fts:scopeType,
      $selector as fts:scopeSelector,
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   if ($type eq "same" and $selector eq "sentence")
   then fts:ApplyFTScopeSameSentence($allMatches)
   else if ($type eq "different" and $selector eq "sentence")
      then fts:ApplyFTScopeDifferentSentence($allMatches)
   else if ($type eq "same" and $selector eq "paragraph")
      then fts:ApplyFTScopeSameParagraph($allMatches)
   else fts:ApplyFTScopeDifferentParagraph($allMatches)
};


declare function fts:ApplyFTContent (
      $searchContext as item(),
      $type as fts:contentMatchType,
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      $allMatches/fts:match[
         let $start_pos := fts:getLowestTokenPosition($searchContext),
             $end_pos   := fts:getHighestTokenPosition($searchContext),
             $match     := .
         return
            if ($type eq "entire content") then
               every $pos in $start_pos to $end_pos
               satisfies
                  some $si in $match/fts:stringInclude[data(@isContiguous)]
                  satisfies
                     fts:TokenInfoCoversTokenPosition($si/fts:tokenInfo, $pos)
            else
               let $pos :=
                  if ($type eq "at start") then
                     $start_pos
                  else (: $type eq "at end" :)
                     $end_pos
               return
                  some $ti in $match/fts:stringInclude/fts:tokenInfo
                  satisfies
                     fts:TokenInfoCoversTokenPosition($ti, $pos)
      ]
   }
   </fts:allMatches>
};


declare function fts:TokenInfoCoversTokenPosition(
      $tokenInfo as element(fts:tokenInfo),
      $tokenPosition as xs:integer )
   as xs:boolean
{
   ($tokenPosition >= $tokenInfo/@startPos)
   and
   ($tokenPosition <= $tokenInfo/@endPos)
};


declare function fts:getLowestTokenPosition(
      $searchContext as item() )
   as xs:integer
   {3};

declare function fts:getHighestTokenPosition(
      $searchContext as item() )
   as xs:integer
   {3};


declare function fts:joinIncludes(
      $strIncls as element(fts:stringInclude)* )
   as element(fts:stringInclude)?
{
   if (fn:empty($strIncls))
   then
      $strIncls
   else
      let $posSet := fts:CoveredIncludePositions(<fts:match>$strIncls</fts:match>),
         $minPos := fn:min($strIncls/fts:tokenInfo/@startPos),
         $maxPos := fn:max($strIncls/fts:tokenInfo/@endPos),
         $isContiguous :=
            ( every $pos in $minPos to $maxPos
              satisfies ($pos = $posSet) )
            and
            ( every $strIncl in $strIncls
              satisfies $strIncl/@isContiguous )
      return
         <fts:stringInclude
            queryPos="{$strIncls[1]/@queryPos}"
            isContiguous="{$isContiguous}">
            <fts:tokenInfo
               startPos ="{$minPos}"
               endPos   ="{$maxPos}"
               startSent="{fn:min($strIncls/fts:tokenInfo/@startSent)}"
               endSent  ="{fn:max($strIncls/fts:tokenInfo/@endSent)}"
               startPara="{fn:min($strIncls/fts:tokenInfo/@startPara)}"
               endPara  ="{fn:max($strIncls/fts:tokenInfo/@endPara)}"/>
         </fts:stringInclude>
};


declare function fts:ApplyFTWordWindow (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $minpos := fn:min($match/fts:stringInclude/fts:tokenInfo/@startPos),
          $maxpos := fn:max($match/fts:stringInclude/fts:tokenInfo/@endPos)
      for $windowStartPos in ($maxpos - $n + 1 to $minpos)
      let $windowEndPos := $windowStartPos + $n - 1
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExclude in $match/fts:stringExclude
            where $stringExclude/fts:tokenInfo/@startPos >=
                  $windowStartPos
              and $stringExclude/fts:tokenInfo/@endPos <=
                  $windowEndPos
            return $stringExclude
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTSentenceWindow (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $minpos := fn:min($match/fts:stringInclude/fts:tokenInfo/@startSent),
          $maxpos := fn:max($match/fts:stringInclude/fts:tokenInfo/@endSent)
      for $windowStartPos in ($maxpos - $n + 1 to $minpos)
      let $windowEndPos := $windowStartPos + $n - 1
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExclude in $match/fts:stringExclude
            where $stringExclude/fts:tokenInfo/@startSent >=
                  $windowStartPos
              and $stringExclude/fts:tokenInfo/@endSent <=
                  $windowEndPos
            return $stringExclude
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTParagraphWindow (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $minpos := fn:min($match/fts:stringInclude/fts:tokenInfo/@startPara),
          $maxpos := fn:max($match/fts:stringInclude/fts:tokenInfo/@endPara)
      for $windowStartPos in ($maxpos - $n + 1 to $minpos)
      let $windowEndPos := $windowStartPos + $n - 1
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExclude in $match/fts:stringExclude
            where $stringExclude/fts:tokenInfo/@startPara >=
                  $windowStartPos
              and $stringExclude/fts:tokenInfo/@endPara <=
                  $windowEndPos
            return $stringExclude
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTWindow (
      $type as fts:distanceType,
      $size as xs:integer,
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   if ($type eq "word") then
      fts:ApplyFTWordWindow($allMatches, $size)
   else if ($type eq "sentence") then
      fts:ApplyFTSentenceWindow($allMatches, $size)
   else
      fts:ApplyFTParagraphWindow($allMatches, $size)
};


declare function fts:ApplyFTDistance (
      $type as fts:distanceType,
      $range as element(fts:range),
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   if ($type eq "word") then
      if ($range/@type eq "exactly") then
         fts:ApplyFTWordDistanceExactly($allMatches, $range/@n)
      else if ($range/@type eq "at least") then
         fts:ApplyFTWordDistanceAtLeast($allMatches, $range/@n)
      else if ($range/@type eq "at most") then
         fts:ApplyFTWordDistanceAtMost( $allMatches, $range/@n)
      else
         fts:ApplyFTWordDistanceFromTo( $allMatches, $range/@m, $range/@n)
   else if ($type eq "sentence") then
      if ($range/@type eq "exactly") then
         fts:ApplyFTSentenceDistanceExactly($allMatches, $range/@n)
      else if ($range/@type eq "at least") then
         fts:ApplyFTSentenceDistanceAtLeast($allMatches, $range/@n)
      else if ($range/@type eq "at most") then
         fts:ApplyFTSentenceDistanceAtMost( $allMatches, $range/@n)
      else
         fts:ApplyFTSentenceDistanceFromTo( $allMatches, $range/@m, $range/@n)
   else
      if ($range/@type eq "exactly") then
         fts:ApplyFTParagraphDistanceExactly($allMatches, $range/@n)
      else if ($range/@type eq "at least") then
         fts:ApplyFTParagraphDistanceAtLeast($allMatches, $range/@n)
      else if ($range/@type eq "at most") then
         fts:ApplyFTParagraphDistanceAtMost( $allMatches, $range/@n)
      else
         fts:ApplyFTParagraphDistanceFromTo( $allMatches, $range/@m, $range/@n)
};


declare function fts:ApplyFTWordDistanceExactly(
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startPos ascending,
                              $si/fts:tokenInfo/@endPos ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $idx in 1 to fn:count($sorted) - 1
            satisfies fts:wordDistance(
                         $sorted[$idx]/fts:tokenInfo,
                         $sorted[$idx+1]/fts:tokenInfo
                      ) = $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:wordDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) = $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTWordDistanceAtLeast (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startPos ascending,
                              $si/fts:tokenInfo/@endPos ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:wordDistance(
                         $sorted[$index]/fts:tokenInfo,
                         $sorted[$index+1]/fts:tokenInfo
                      ) >= $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:wordDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) >= $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTWordDistanceAtMost (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startPos ascending,
                              $si/fts:tokenInfo/@endPos ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:wordDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) <= $n
      return
        <fts:match>
        {
           fts:joinIncludes($match/fts:stringInclude),
           for $stringExcl in $match/fts:stringExclude
           where some $stringIncl in $match/fts:stringInclude
                 satisfies fts:wordDistance(
                               $stringIncl/fts:tokenInfo,
                               $stringExcl/fts:tokenInfo
                           ) <= $n
           return $stringExcl
        }
        </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTWordDistanceFromTo (
      $allMatches as element(fts:allMatches),
      $m as xs:integer,
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startPos ascending,
                              $si/fts:tokenInfo/@endPos ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:wordDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) >= $m
                      and
                      fts:wordDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) <= $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:wordDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) >= $m
                            and
                            fts:wordDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) <= $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:wordDistance (
      $tokenInfo1 as element(fts:tokenInfo),
      $tokenInfo2 as element(fts:tokenInfo) )
   as xs:integer
{
   (: Ensure tokens are in order :)
   let $sorted :=
      for $ti in ($tokenInfo1, $tokenInfo2)
      order by $ti/@startPos ascending, $ti/@endPos ascending
      return $ti
   return
      (: -1 because we count starting at 0 :)
      $sorted[2]/@startPos - $sorted[1]/@endPos - 1
};


declare function fts:ApplyFTSentenceDistanceExactly (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startSent ascending,
                              $si/fts:tokenInfo/@endSent ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:sentenceDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) = $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:sentenceDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) = $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTSentenceDistanceAtLeast (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                    order by $si/fts:tokenInfo/@startSent ascending,
                             $si/fts:tokenInfo/@endSent ascending
                    return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:sentenceDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) >= $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:sentenceDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) >= $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTSentenceDistanceAtMost (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startSent ascending,
                              $si/fts:tokenInfo/@endSent ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:sentenceDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) <= $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:sentenceDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) <= $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTSentenceDistanceFromTo (
      $allMatches as element(fts:allMatches),
      $m as xs:integer,
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startSent ascending,
                              $si/fts:tokenInfo/@endSent ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:sentenceDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) >= $m
                      and
                      fts:sentenceDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) <= $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:sentenceDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) >= $m
                            and
                            fts:sentenceDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) <= $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:sentenceDistance (
      $tokenInfo1 as element(fts:tokenInfo),
      $tokenInfo2 as element(fts:tokenInfo) )
   as xs:integer
{
   (: Ensure tokens are in order :)
   let $sorted :=
      for $ti in ($tokenInfo1, $tokenInfo2)
      order by $ti/@startPos ascending, $ti/@endPos ascending
      return $ti
   return
      (: -1 because we count starting at 0 :)
      $sorted[2]/@startSent - $sorted[1]/@endSent - 1
};


declare function fts:ApplyFTParagraphDistanceExactly (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startPara ascending,
                              $si/fts:tokenInfo/@endPara ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:paraDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) = $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:paraDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) = $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTParagraphDistanceAtLeast (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startPara ascending,
                              $si/fts:tokenInfo/@endPara ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:paraDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) >= $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:paraDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) >= $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTParagraphDistanceAtMost (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startPara ascending,
                              $si/fts:tokenInfo/@endPara ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:paraDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) <= $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:paraDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) <= $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:ApplyFTParagraphDistanceFromTo (
      $allMatches as element(fts:allMatches),
      $m as xs:integer,
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {
      for $match in $allMatches/fts:match
      let $sorted := for $si in $match/fts:stringInclude
                     order by $si/fts:tokenInfo/@startPara ascending,
                              $si/fts:tokenInfo/@endPara ascending
                     return $si
      where
         if (fn:count($sorted) le 1) then fn:true() else
            every $index in (1 to fn:count($sorted) - 1)
            satisfies fts:paraDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) >= $m
                      and
                      fts:paraDistance(
                          $sorted[$index]/fts:tokenInfo,
                          $sorted[$index+1]/fts:tokenInfo
                      ) <= $n
      return
         <fts:match>
         {
            fts:joinIncludes($match/fts:stringInclude),
            for $stringExcl in $match/fts:stringExclude
            where some $stringIncl in $match/fts:stringInclude
                  satisfies fts:paraDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) >= $m
                            and
                            fts:paraDistance(
                                $stringIncl/fts:tokenInfo,
                                $stringExcl/fts:tokenInfo
                            ) <= $n
            return $stringExcl
         }
         </fts:match>
   }
   </fts:allMatches>
};


declare function fts:paraDistance (
      $tokenInfo1 as element(fts:tokenInfo),
      $tokenInfo2 as element(fts:tokenInfo) )
   as xs:integer
{
   (: Ensure tokens are in order :)
   let $sorted :=
      for $ti in ($tokenInfo1, $tokenInfo2)
      order by $ti/@startPos ascending, $ti/@endPos ascending
      return $ti
   return
      (: -1 because we count starting at 0 :)
      $sorted[2]/@startPara - $sorted[1]/@endPara - 1
};


declare function fts:FormCombinations (
      $sms as element(fts:match)*,
      $k as xs:integer )
   as element(fts:match)*
(:
   Find all combinations of exactly $k elements from $sms, and
   for each such combination, construct a match whose children are
   copies of all the children of all the elements in the combination.
   Return the sequence of all such matches.
:)
{
   if ($k eq 0) then <fts:match/>
   else if (fn:count($sms) lt $k) then ()
   else if (fn:count($sms) eq $k) then <fts:match>{$sms/*}</fts:match>
   else
      let $first := $sms[1],
          $rest  := fn:subsequence($sms, 2)
      return (
         (: all the combinations that does not involve $first :)
         fts:FormCombinations($rest, $k),

         (: and all the combinations that do involve $first :)
         for $combination in fts:FormCombinations($rest, $k - 1)
         return
            <fts:match>
            {
               $first/*,
               $combination/*
            }
            </fts:match>
      )
};

declare function fts:FormCombinationsAtLeast (
      $sms as element(fts:match)*,
      $times as xs:integer)
   as element(fts:match)*
(:
   Find all combinations of $times or more elements from $sms, and
   for each such combination, construct a match whose children are
   copies of all the children of all the elements in the combination.
   Return the sequence of all such matches.
:)
{
   for $k in $times to fn:count($sms)
   return fts:FormCombinations($sms, $k)
};

declare function fts:FormRange (
      $sms as element(fts:match)*,
      $l as xs:integer,
      $u as xs:integer,
      $stokenNum as xs:integer )
   as element(fts:allMatches)
{
   if ($l > $u) then <fts:allMatches stokenNum="0" />
   else
      let $am1 := <fts:allMatches stokenNum="{$stokenNum}">
                     {fts:FormCombinationsAtLeast($sms, $l)}
                  </fts:allMatches>
      let $am2 := <fts:allMatches stokenNum="{$stokenNum}">
                     {fts:FormCombinationsAtLeast($sms, $u+1)}
                  </fts:allMatches>
      return fts:ApplyFTAnd($am1,
                            fts:ApplyFTUnaryNot($am2))
};


declare function fts:ApplyFTTimesExactly (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   fts:FormRange($allMatches/fts:match, $n, $n, $allMatches/@stokenNum)
};


declare function fts:ApplyFTTimesAtLeast (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   <fts:allMatches stokenNum="{$allMatches/@stokenNum}">
   {fts:FormCombinationsAtLeast($allMatches/fts:match, $n)}
   </fts:allMatches>
};


declare function fts:ApplyFTTimesAtMost (
      $allMatches as element(fts:allMatches),
      $n as xs:integer )
   as element(fts:allMatches)
{
   fts:FormRange($allMatches/fts:match, 0, $n, $allMatches/@stokenNum)
};


declare function fts:ApplyFTTimesFromTo (
      $allMatches as element(fts:allMatches),
      $m as xs:integer,
      $n as xs:integer )
   as element(fts:allMatches)
{
   fts:FormRange($allMatches/fts:match, $m, $n, $allMatches/@stokenNum)
};


declare function fts:ApplyFTTimes (
      $range as element(fts:range),
      $allMatches as element(fts:allMatches) )
   as element(fts:allMatches)
{
   if (fn:count($allMatches//fts:stringExclude) gt 0) then
      fn:error(fn:QName('http://www.w3.org/2005/xqt-errors',
                        'XPST0003'))
   else if ($range/@type eq "exactly") then
      fts:ApplyFTTimesExactly($allMatches, $range/@n)
   else if ($range/@type eq "at least") then
      fts:ApplyFTTimesAtLeast($allMatches, $range/@n)
   else if ($range/@type eq "at most") then
      fts:ApplyFTTimesAtMost($allMatches, $range/@n)
   else fts:ApplyFTTimesFromTo($allMatches,
                               $range/@m,
                               $range/@n)
};


declare function fts:FTContainsExpr (
      $searchContextItems as item()*,
      $ignoreNodes as node()*,
      $ftSelection as element(*,fts:ftSelection),
      $defOptions as element(fts:matchOptions) )
   as xs:boolean
{
   some $searchContext in $searchContextItems
   satisfies
      let $newSearchContext := fts:reconstruct( $searchContext, $ignoreNodes )
      return
         if (fn:empty($newSearchContext)) then fn:false()
         else
            let $allMatches := fts:evaluate($ftSelection,
                                            $newSearchContext,
                                            $defOptions,
                                            0)
            return
               some $match in $allMatches/fts:match
               satisfies
                  fn:count($match/fts:stringExclude) eq 0
};

declare function fts:reconstruct (
      $n as item(),
      $ignore as node()* )
   as item()?
{
   typeswitch ($n)
     case node() return
        if (some $i in $ignore satisfies $n is $i) then ()
        else if ($n instance of element()) then
           let $nodeName := fn:node-name($n)
           let $nodeContent := for $nn in $n/node()
                               return fts:reconstruct($nn,$ignore)
           return element {$nodeName} {$nodeContent}
        else if ($n instance of document-node()) then
           document {
              for $nn in $n/node()
              return fts:reconstruct($nn, $ignore)
           }
        else $n
     default return $n
};

1
