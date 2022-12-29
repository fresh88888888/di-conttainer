package org.ws.tdd.rest;

import java.util.*;
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
     private static final String LeftBracket = "\\{";
     private static final String RightBracket = "}";
     private static final String VariableName = "\\w[\\w\\.-]*";
     private static final String NonBracket = "[^\\{}]+";
     public static final String DefaultVariablePattern = "([^/]+?)";
     private static final Pattern variable = Pattern.compile(LeftBracket + group(VariableName) + group(":" + group(NonBracket)) + "?" + RightBracket);
     private static final int VariableNameGroup = 1;
     private static final int VariablePatternGroup = 3;
     private  Pattern pattern;
     private List<String> variables = new ArrayList<>();
     private int VariableGroupStartFrom;
     public UriTemplateString(String template) {
         this.pattern = Pattern.compile(group(variable(template)) + "(/.*)?");
         VariableGroupStartFrom = 2;
     }
     private static String group(String pattern){
        return  "(" + pattern + ")";
     }
     private String variable(String template) {
         return variable.matcher(template).replaceAll(result -> {
             String pattern = result.group(VariablePatternGroup);
             String variableName = result.group(VariableNameGroup);
             if(variables.contains(variableName)){
                 throw new IllegalArgumentException("duplicate variable " + variableName);
             }
             variables.add(variableName);
             return pattern == null ? DefaultVariablePattern : group(pattern);
         });
     }

     @Override
     public Optional<MatchResult> match(String path) {
         Matcher matcher = pattern.matcher(path);
         if (!matcher.matches()){
             return Optional.empty();
         }
         int count = matcher.groupCount();
         Map<String, String> parameters = new HashMap<>();
         for (int i = 0; i < variables.size(); i++){
             parameters.put(variables.get(i), matcher.group(VariableGroupStartFrom + i));
         }
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
                 return parameters;
             }
             @Override
             public int compareTo(MatchResult o) {
                 return 0;
             }
         });
     }
 }
