package com.youtrust.hackathon.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Test;

public class JsonTest {

    @Test
    public void parsesFlatObject() {
        Map<String, String> m = Json.parseFlatObject("{\"email\":\"a@b.com\",\"name\":\"Alice\"}");
        assertEquals("a@b.com", m.get("email"));
        assertEquals("Alice", m.get("name"));
    }

    @Test
    public void parsesNullValueAndEmptyObject() {
        Map<String, String> m = Json.parseFlatObject("{ \"password\" : null }");
        assertTrue(m.containsKey("password"));
        assertNull(m.get("password"));
        assertTrue(Json.parseFlatObject("{}").isEmpty());
    }

    @Test
    public void handlesEscapeSequences() {
        Map<String, String> m = Json.parseFlatObject("{\"name\":\"a\\\"b\\n\"}");
        assertEquals("a\"b\n", m.get("name"));
    }

    @Test
    public void quoteEscapesSpecialChars() {
        assertEquals("\"a\\\"b\"", Json.quote("a\"b"));
        assertEquals("null", Json.quote(null));
    }

    @Test
    public void rejectsNonObject() {
        assertThrows(IllegalArgumentException.class, () -> Json.parseFlatObject("123"));
    }
}
