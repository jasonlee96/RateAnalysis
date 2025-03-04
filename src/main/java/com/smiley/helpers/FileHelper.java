package com.smiley.helpers;

import com.smiley.models.CsvColumnModel;
import com.smiley.models.CsvRowModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class FileHelper {
    public static List<CsvRowModel> ReadContent(Path path){
        List<CsvRowModel> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            AtomicInteger index = new AtomicInteger(0);
            while ((line = br.readLine()) != null) {
                List<String> values = Arrays.asList(line.split(",")); // Split by comma
                var columns = IntStream.range(0, values.size()) // Generate index
                        .mapToObj(i -> new CsvColumnModel(i, values.get(i))) // Combine index & value
                        .toList(); // Collect to List (Java 16+)

                result.add(new CsvRowModel(index.getAndIncrement(), columns));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
