package org.ws.tdd.rest;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

interface UriTemplate {
    Optional<MatchResult> match(String path);

    interface MatchResult extends Comparable<MatchResult> {
        String getMatched();

        String getRemaining();

        Map<String, String> getMatchedPathParameters();
    }
}

class UriTemplateString implements UriTemplate {
    public static final String DefaultVariablePattern = "([^/]+?)";
    private static final String LeftBracket = "\\{";
    private static final String RightBracket = "}";
    private static final String VariableName = "\\w[\\w\\.-]*";
    private static final String NonBracket = "[^\\{}]+";
    private static final Pattern variable = Pattern.compile(LeftBracket + group(VariableName) + group(":" + group(NonBracket)) + "?" + RightBracket);
    private static final int VariableNameGroup = 1;
    private static final int VariablePatternGroup = 3;
    private Pattern pattern;
    private List<String> variables = new ArrayList<>();
    private int VariableGroupStartFrom;

    public UriTemplateString(String template) {
        this.pattern = Pattern.compile(group(variable(template)) + "(/.*)?");
        VariableGroupStartFrom = 2;
    }

    private static String group(String pattern) {
        return "(" + pattern + ")";
    }

    private String variable(String template) {
        return variable.matcher(template).replaceAll(result -> {
            String pattern = result.group(VariablePatternGroup);
            String variableName = result.group(VariableNameGroup);
            if (variables.contains(variableName)) {
                throw new IllegalArgumentException("duplicate variable " + variableName);
            }
            variables.add(variableName);
            return pattern == null ? DefaultVariablePattern : group(pattern);
        });
    }

    @Override
    public Optional<MatchResult> match(String path) {
        Matcher matcher = pattern.matcher(path);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new PathMatchResult(matcher));
    }

    class PathMatchResult implements MatchResult {
        private int matchLiteralCount;
        private int count;
        private Matcher matcher;
        private Map<String, String> parameters = new HashMap<>();

        public PathMatchResult(Matcher matcher) {
            this.matcher = matcher;
            this.count = matcher.groupCount();
            this.matchLiteralCount = matcher.group(1).length();

            for (int i = 0; i < variables.size(); i++) {
                this.parameters.put(variables.get(i), matcher.group(VariableGroupStartFrom + i));
                matchLiteralCount -= matcher.group(VariableGroupStartFrom + i).length();
            }
        }
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
            PathMatchResult result = (PathMatchResult) o;
            if (matchLiteralCount > result.matchLiteralCount) {
                return -1;
            }
            return 0;
        }
    }
}
