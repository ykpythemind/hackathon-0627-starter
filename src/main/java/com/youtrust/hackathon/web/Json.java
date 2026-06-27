package com.youtrust.hackathon.web;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ごく小さな JSON ヘルパ。リクエストは「フラットなオブジェクト（値は文字列か null）」
 * のみを想定してパースし、レスポンスは手組みで生成する。
 * 数値・真偽値・ネストは扱わない（登録APIに必要な範囲に割り切る）。
 */
final class Json {

    private Json() {
    }

    static Map<String, String> parseFlatObject(String input) {
        return new Parser(input).parseObject();
    }

    /** 文字列を JSON のダブルクオート付きリテラルにする。null は素の null を返す。 */
    static String quote(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }

    private static final class Parser {
        private final String s;
        private int i;

        Parser(String s) {
            this.s = s;
        }

        Map<String, String> parseObject() {
            Map<String, String> map = new LinkedHashMap<>();
            ws();
            expect('{');
            ws();
            if (peek() == '}') {
                i++;
                return map;
            }
            while (true) {
                ws();
                String key = readString();
                ws();
                expect(':');
                ws();
                map.put(key, readValue());
                ws();
                char c = next();
                if (c == ',') {
                    continue;
                }
                if (c == '}') {
                    break;
                }
                throw err("',' か '}' が必要");
            }
            return map;
        }

        private String readValue() {
            if (peek() == '"') {
                return readString();
            }
            if (s.startsWith("null", i)) {
                i += 4;
                return null;
            }
            throw err("値は文字列か null のみ対応");
        }

        private String readString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (i >= s.length()) {
                    throw err("文字列が閉じていない");
                }
                char c = s.charAt(i++);
                if (c == '"') {
                    break;
                }
                if (c == '\\') {
                    char e = s.charAt(i++);
                    switch (e) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'u':
                            sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                            i += 4;
                            break;
                        default: throw err("不正なエスケープ: \\" + e);
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private void ws() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        private char peek() {
            if (i >= s.length()) {
                throw err("予期しない終端");
            }
            return s.charAt(i);
        }

        private char next() {
            if (i >= s.length()) {
                throw err("予期しない終端");
            }
            return s.charAt(i++);
        }

        private void expect(char c) {
            if (next() != c) {
                throw err("'" + c + "' が必要");
            }
        }

        private IllegalArgumentException err(String m) {
            return new IllegalArgumentException("JSON解析エラー: " + m + " (pos " + i + ")");
        }
    }
}
