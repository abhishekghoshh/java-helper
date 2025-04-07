package app.functional;

import app.runner.Run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Run(active = false)
public class FileStreamTest {

    @Run(id = "print all the line from the stream")
    private static void printLines() throws IOException {
        Path path = Paths.get("user.txt");

        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(p -> System.out.print(p + " "));
        }
        System.out.println();
    }

    @Run(id = "first file stream then using flat map make a stream then collect")
    private static void fileLineStream() throws IOException {
        Path path = Paths.get("user.txt");

        try (Stream<String> lines = Files.lines(path)) {
            String content = lines
                    .map(name -> name.toLowerCase().split(""))
                    .flatMap(Arrays::stream)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining());

            System.out.println(content);
        }
    }

    @Run(id = "These are directories in the current folder")
    private static void printDirectories() throws IOException {
        try (Stream<Path> paths = Files.list(Paths.get("."))) {
            paths.filter(Files::isDirectory)
                    .forEach(p -> System.out.print(p + " "));
        }
        System.out.println();
    }

}
