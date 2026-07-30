package com.tanner.langsearch;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SearchActionTest {

    @Test
    public void searchesUtf16Utf8AndGb18030LanguageFiles() {
        assertMatch("name=中文\n", StandardCharsets.UTF_16BE);
        assertMatch("name=中文\n", StandardCharsets.UTF_8);
        assertMatch("name=中文\n", Charset.forName("GB18030"));
    }

    private void assertMatch(String text, Charset charset) {
        List<SearchAction.LineMatch> matches =
                SearchAction.findMatchingLines(text.getBytes(charset), "中文");
        assertEquals(1, matches.size());
        assertEquals(1, matches.get(0).lineNumber());
        assertEquals("name=中文", matches.get(0).text());
    }
}
