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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.scm.BlameLine;
import org.sonar.api.utils.command.StreamConsumer;

public class HgConsumer implements StreamConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HgConsumer.class);
    private final String filename;
    private final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
    private final List<BlameLine> lines = new ArrayList<>();

    public HgConsumer(final String filename) {
        this.filename = filename;
    }

    @Override
    public void consumeLine(final String line) {
        final var trimmedLine = line.trim();
        final var tokenizer = new StringTokenizer(trimmedLine, " ", false);
        LOGGER.debug("{} tokens on line on file {} at line {}: {}", tokenizer.countTokens(), filename,
                getLines().size() + 1, line);
        if (tokenizer.countTokens() > 3) {
            var author = tokenizer.nextToken();
            var shift = false;
            final var first = tokenizer.nextToken();
            if (first.startsWith("<")) {
                shift = true;
                author = first;
            }
            final var revision = shift ? tokenizer.nextToken() : first;
            var n = trimmedLine.indexOf(":", 0);
            n = trimmedLine.indexOf(":", n + 1);
            n = trimmedLine.indexOf(":", n + 1);
            Date dateTime = null;
            if (n >= 30) {
                final var dateStr = trimmedLine.substring(n - 30, n);
                try {
                    dateTime = format.parse(dateStr);
                    final var bl = new BlameLine().date(dateTime).revision(revision).author(author);
                    LOGGER.debug("Blame {} on file {} at line {}: {}", bl, filename, getLines().size() + 1, line);
                    lines.add(bl);
                } catch (final ParseException e) {
                    LOGGER.warn("skip ParseException on file " + filename + " at line " + (getLines().size() + 1) + ": "
                            + e.getMessage() + " during parsing date " + dateStr, e);
                }
            } else {
                LOGGER.warn("skip unparseable date on file {} at line {}: {}", filename, getLines().size() + 1, line);
            }
        } else {
            LOGGER.warn("skip unparseable line on file {} at line {}: {}", filename, getLines().size() + 1, line);
        }
    }

    public List<BlameLine> getLines() {
        return lines;
    }
}
