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

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.scm.BlameCommand;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.StringStreamConsumer;

public class HgBlameCommand extends BlameCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(HgBlameCommand.class);
    private final Configuration settings;

    public HgBlameCommand(final Configuration settings) {
        this.settings = settings;
    }

    @Override
    public void blame(final BlameInput input, final BlameOutput output) {
        final var executorService = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        for (final InputFile inputFile : input.filesToBlame()) {
            executorService.execute(() -> blame(inputFile, output));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void blame(final InputFile inputFile, final BlameOutput output) {
        final var fileName = inputFile.filename();
        final var cl = createCommandLine(inputFile);
        final var consumer = new HgConsumer(fileName);
        final var stderr = new StringStreamConsumer();
        LOGGER.debug("Executing: {}", cl);
        final var exitCode = HgPluginExecutor.execute(cl, consumer, stderr);
        if (exitCode != 0) {
            // Ignore the error since it may be caused by uncommited file
            LOGGER.warn("The mercurial blame command [{}] failed: {}", cl, stderr.getOutput());
        }
        final var lines = consumer.getLines();
        if (!lines.isEmpty() && lines.size() < inputFile.lines()) {
            final var last = lines.get(lines.size() - 1);
            final var length = inputFile.lines();
            for (var i = lines.size(); i < length; ++i) {
                lines.add(last);
            }
        }
        LOGGER.debug("Blame lines {} on file {}", lines.size(), fileName);
        output.blameResult(inputFile, lines);
    }

    private Command createCommandLine(final InputFile inputFile) {
        final var filename = inputFile.filename();
        final var p = Path.of(inputFile.uri());
        final var cl = Command.create("hg");
        cl.setDirectory(p.getParent().toAbsolutePath().toFile());
        cl.addArgument("blame");
        final var whiteSpace = settings.getBoolean("sonar.hg.considerWhitespaces");
        if (whiteSpace.isEmpty() || Boolean.FALSE.equals(whiteSpace.get())) {
            // Ignore whitespaces
            cl.addArgument("-w");
        }
        cl.addArgument("-v");
        cl.addArgument("--user");
        cl.addArgument("--date");
        cl.addArgument("--changeset");
        cl.addArgument("--");
        cl.addArgument(filename);
        return cl;
    }
}
