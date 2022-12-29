package org.ws.tdd.rest;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

interface UriTemplate {
    interface MatchResult extends Comparable<MatchResult> {
        String getMatched();

        String getRemaining();

        Map<String, String> getMatchedPathParameters();
    }
    Optional<MatchResult> match(String path);
}

 class UriTemplateString implements UriTemplate{
     private  Pattern pattern;

     public UriTemplateString(String template) {
         this.pattern = Pattern.compile("(" + template + ")" + "(/.*)?");
     }

     @Override
     public Optional<MatchResult> match(String path) {
         Matcher matcher = pattern.matcher(path);
         if (!matcher.matches()){
             return Optional.empty();
         }
         int count = matcher.groupCount();
         return Optional.of(new MatchResult() {
             @Override
             public String getMatched() {
                 return matcher.group(1);
             }
             @Override
             public String getRemaining() {
                 return matcher.group(count);
             }
             @Override
             public Map<String, String> getMatchedPathParameters() {
                 return null;
             }
             @Override
             public int compareTo(MatchResult o) {
                 return 0;
             }
         });
     }
 }
