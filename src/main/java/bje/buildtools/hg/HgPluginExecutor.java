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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.StreamConsumer;

public class HgPluginExecutor {
    public static int execute(final Command cl, final StreamConsumer consumer, final StreamConsumer stderr) {
        try {
            final var args = cl.getArguments();
            final var cmds = new String[args.size() + 1];
            final var mvn = cl.getExecutable();
            cmds[0] = mvn;
            final var size = args.size();
            for (var n = 0; n < size; ++n) {
                cmds[n + 1] = args.get(n);
            }
            final var pb = new ProcessBuilder().directory(cl.getDirectory());
            pb.redirectOutput(Redirect.PIPE);
            final var p = pb.command(cmds).start();

            try (final var reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    final var reader2 = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                while (p.isAlive()) {
                    var line = reader.readLine();
                    while (line != null) {
                        if (!line.isBlank()) {
                            consumer.consumeLine(line);
                        }
                        line = reader.readLine();
                    }

                    var line2 = reader2.readLine();
                    while (line2 != null) {
                        stderr.consumeLine(line2);
                        line2 = reader2.readLine();
                    }
                    Thread.sleep(100L);
                }
                var line = reader.readLine();
                while (line != null) {
                    if (!line.isBlank()) {
                        consumer.consumeLine(line);
                    }
                    line = reader.readLine();
                }

                var line2 = reader2.readLine();
                while (line2 != null) {
                    stderr.consumeLine(line2);
                    line2 = reader2.readLine();
                }
            }

            return p.exitValue();
        } catch (final IOException e1) {
            return -1;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    private HgPluginExecutor() {
        // block default constructor
    }

}
