/*
 * StringValuePatternBuilder.java, 27 Mar 2018
 * Created by Joao Viegas (joao.viegas@mindprogeny.com)
 *
 * Copyright (c) 2018 Mind Progeny.
 */
package com.mindprogeny.wiremock.extension.scenario;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.EqualToXmlPattern;
import com.github.tomakehurst.wiremock.matching.MatchesJsonPathPattern;
import com.github.tomakehurst.wiremock.matching.MatchesXPathPattern;
import com.github.tomakehurst.wiremock.matching.NegativeRegexPattern;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.StringValuePatternJsonDeserializer;

// Reusing wiremock's guava dependency
import wiremock.com.google.common.collect.ImmutableMap;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

/**
 * Utility class that converst Wiremock's {@link StringValuePatternJsonDeserializer} to be applied to Extension Parameters instead of JSON
 * 
 * @author Jo&atilde;o Viegas (joao.viegas@mindprogeny.com)
 * @since 27 Mar 2018
 *
 */
public class StringValuePatternBuilder {

    /**
     * Possible matching pattern parameters and their corresponding Pattern Match classes
     */
    private static final Map<String, Class<? extends StringValuePattern>> MATCHERS =
        new ImmutableMap.Builder<String, Class<? extends StringValuePattern>>()
            .put("equalTo", EqualToPattern.class)
            .put("equalToJson", EqualToJsonPattern.class)
            .put("matchesJsonPath", MatchesJsonPathPattern.class)
            .put("equalToXml", EqualToXmlPattern.class)
            .put("matchesXPath", MatchesXPathPattern.class)
            .put("contains", ContainsPattern.class)
            .put("matches", RegexPattern.class)
            .put("doesNotMatch", NegativeRegexPattern.class)
            .put("anything", AnythingPattern.class)
            .build();

    /**
     * Build a string value pattern through a map of supported pattern matching parameters.
     * 
     * @param matchParameters map with pattern matching parameters
     * @return a StringValuePattern applying the provided pattern matching rules
     */
    @SuppressWarnings("unchecked")
    public static StringValuePattern build(Map<String,Object> matchParameters) {

        Class<? extends StringValuePattern> patternClass = null;
        Object matchPattern = null;
        for (String matchMethod : matchParameters.keySet()) {
            if ((patternClass = MATCHERS.get(matchMethod)) != null) {
                matchPattern =  matchParameters.get(matchMethod);
                break;
            }
        }
        
        if (patternClass == null) {
            return StringValuePattern.ABSENT;
        }
        
        if (patternClass.equals(EqualToJsonPattern.class)) {
            
            Boolean ignoreArrayOrder = (Boolean) matchParameters.get("ignoreArrayOrder");
            Boolean ignoreExtraElements = (Boolean) matchParameters.get("ignoreExtraElements");

            return new EqualToJsonPattern(matchPattern instanceof String?(String)matchPattern:Json.write(matchPattern), ignoreArrayOrder==null?false:ignoreArrayOrder, ignoreExtraElements==null?false:ignoreExtraElements);
        } 
 
        if (patternClass.equals(MatchesXPathPattern.class)) {
            Map<String,String> namespaces = (Map<String, String>) matchParameters.get("xPathNamespaces");

            return new MatchesXPathPattern(matchPattern.toString(), namespaces==null?Collections.<String, String>emptyMap():namespaces );
        }

        Constructor<? extends StringValuePattern> constructor;
        try {
            constructor = patternClass.getConstructor(String.class);
            return constructor.newInstance(matchPattern);
        } catch (Exception e) {
            return throwUnchecked(e, StringValuePattern.class);
        }
    }
}
