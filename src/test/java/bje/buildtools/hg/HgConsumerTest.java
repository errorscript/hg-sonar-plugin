/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025-2025 errorscript@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package bje.buildtools.hg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.SimpleDateFormat;

import org.junit.jupiter.api.Test;

class HgConsumerTest {
    private static final String[] LIST = {
            "    joshua 63555a56216a Sat Dec 18 12:25:54 2021 +0100: <project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:     <modelVersion>4.0.0</modelVersion>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:     <parent>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:             <groupId>bje</groupId>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:             <artifactId>bje-bom</artifactId>\n",
            "joshua <errorscript@gmail.com> 87c0f9d05a6b Thu Apr 03 16:57:12 2025 +0200:             <version>1.10-SNAPSHOT</version>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:     </parent>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:     <groupId>bje.toolbox</groupId>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:     <artifactId>toolbox</artifactId>\n",
            "jenkins@c8fe6d89dfc9 98b44f3e1c7d Thu Apr 03 16:08:33 2025 +0200:     <version>0.1.22-SNAPSHOT</version>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:     <packaging>pom</packaging>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:     <name>Toolbox</name>\n",
            "    joshua 80e947c30f7f Mon May 22 10:20:57 2023 +0200:     <licenses>\n",
            "    joshua 80e947c30f7f Mon May 22 10:20:57 2023 +0200:         <license>\n",
            "    joshua 80e947c30f7f Mon May 22 10:20:57 2023 +0200:             <name>MIT</name>\n",
            "    joshua 80e947c30f7f Mon May 22 10:20:57 2023 +0200:         </license>\n",
            "    joshua 80e947c30f7f Mon May 22 10:20:57 2023 +0200:     </licenses>\n",
            "    joshua 80e947c30f7f Mon May 22 10:20:57 2023 +0200:     <inceptionYear>2023</inceptionYear>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:     <modules>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:             <module>toolbox-api</module>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:             <module>toolbox-core</module>\n",
            "    joshua d962e139caa1 Sat Feb 11 17:16:35 2023 +0100:     </modules>\n",
            "    joshua 1728ce4cfad2 Sun Jan 08 12:46:16 2023 +0100: </project>\n" };

    @Test
    void test() {
        final var format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        final var c = new HgConsumer("pom.xml");
        for (final String line : LIST) {
            c.consumeLine(line);
        }
        final var blames = c.getLines();
        assertEquals(23, blames.size());
        var line = blames.get(0);
        assertEquals("joshua", line.author());
        assertEquals("20211218 12:25:54", format.format(line.date()));
        assertEquals("63555a56216a", line.revision());

        line = blames.get(5);
        assertEquals("<errorscript@gmail.com>", line.author());
        assertEquals("20250403 16:57:12", format.format(line.date()));
        assertEquals("87c0f9d05a6b", line.revision());
    }
}
