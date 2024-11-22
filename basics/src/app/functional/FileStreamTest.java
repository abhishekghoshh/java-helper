package app.functional;

import app.util.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static app.util.Utils.print;
import static app.util.Utils.run;

public class FileStreamTest {
    public static void main(String[] args) {
        Path path = Paths.get("user.txt");

        run(() -> {
            try (Stream<String> lines = Files.lines(path)) {
                lines.forEach(Utils::print);
            }
        }, "print all the line from the stream");


        print(() -> {
            try (Stream<String> lines = Files.lines(path)) {
                return lines.map(name -> name.toLowerCase().split(""))
                        .flatMap(Arrays::stream)
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining());
            }
        }, "first file stream then using flat map make a stream then collect");


        run(() -> {
            try (Stream<Path> paths = Files.list(Paths.get("."))) {
                paths.filter(Files::isDirectory)
                        .forEach(Utils::print);
            }
        }, "These are directories in the current folder");
    }
}
