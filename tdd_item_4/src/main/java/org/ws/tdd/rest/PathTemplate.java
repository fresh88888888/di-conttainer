package org.ws.tdd.rest;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathTemplate implements UriTemplate {
    private Pattern pattern;
    private int VariableGroupStartFrom;
    private PathVariables pathVariables = new PathVariables();
    public PathTemplate(String template) {
        this.pattern = Pattern.compile(group(pathVariables.template(template)) + "(/.*)?");
        VariableGroupStartFrom = 2;
    }
    @Override
    public Optional<MatchResult> match(String path) {
        Matcher matcher = pattern.matcher(path);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new PathMatchResult(matcher, pathVariables));
    }

    class PathVariables implements Comparable<PathVariables> {
        public static final String DefaultVariablePattern = "([^/]+?)";
        private static final String LeftBracket = "\\{";
        private static final String RightBracket = "}";
        private static final String VariableName = "\\w[\\w\\.-]*";
        private static final String NonBracket = "[^\\{}]+";
        private static final Pattern variable = Pattern.compile(LeftBracket + group(VariableName) + group(":" + group(NonBracket)) + "?" + RightBracket);
        private static final int VariableNameGroup = 1;
        private static final int VariablePatternGroup = 3;
        private List<String> variables = new ArrayList<>();
        private int specificPatternCount = 0;

        private String template(String template) {
            return variable.matcher(template).replaceAll(this::replace);
        }

        private String replace(java.util.regex.MatchResult result) {
            String name = result.group(VariableNameGroup);
            String pattern = result.group(VariablePatternGroup);
            if (variables.contains(name)) {
                throw new IllegalArgumentException("duplicate variable " + name);
            }
            variables.add(name);
            if (pattern != null) {
                specificPatternCount++;
                return group(pattern);
            }
            return DefaultVariablePattern;
        }

        public Map<String, String> extract(Matcher matcher) {
            Map<String, String> parameters = new HashMap<>();
            for (int i = 0; i < pathVariables.variables.size(); i++) {
                parameters.put(pathVariables.variables.get(i), matcher.group(VariableGroupStartFrom + i));
            }
            return parameters;
        }

        @Override
        public int compareTo(PathVariables o) {
            if (variables.size() > o.variables.size()) {
                return -1;
            }
            if (variables.size() < o.variables.size()) {
                return 1;
            }
            return Integer.compare(o.specificPatternCount, specificPatternCount);
        }
    }

    class PathMatchResult implements MatchResult {
        private int matchLiteralCount;
        private PathVariables pathVariables;
        private Matcher matcher;
        private Map<String, String> parameters;

        public PathMatchResult(Matcher matcher, PathVariables variables) {
            this.matcher = matcher;
            this.pathVariables = variables;
            this.parameters = variables.extract(matcher);
            this.matchLiteralCount = matcher.group(1).length() - parameters.values().stream().map(String::length).reduce(0, Integer::sum);
        }

        @Override
        public String getMatched() {
            return matcher.group(1);
        }

        @Override
        public String getRemaining() {
            return matcher.group(matcher.groupCount());
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
            if (matchLiteralCount < result.matchLiteralCount) {
                return 1;
            }
            return pathVariables.compareTo(result.pathVariables);
        }
    }

    private static String group(String pattern) {
        return "(" + pattern + ")";
    }
}
