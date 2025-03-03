package mariia.budiak.practices.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import mariia.budiak.practices.model.ForDampReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping(value = "damp-register-info/", name = "считывание реестра непостредсвенно на компьютере")
@Api(tags = "считывание данных из дампа реестра")
@RestController
public class DampReaderController {

    @PostMapping("/uploadDampAndRead")
    @ApiOperation(value = "Считывание из дампа файла реестра винды, пример пути - HKEY_USERS\\S-1-5-18\\Software\\Classes (можно вводить несколько), в итоге смотрим начинается ли запись из дампа введенной, если да, то сохраняем в структуру. Стуктура содержит: остаток пути, имя (параметра или настройки в данном ключе реестра), значение (значение, связанное с именем)")
    public ResponseEntity<Map<String, List<ForDampReader>>> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "searchKeys") @ApiParam(required = true) String[] searchKeys) {
        Map<String, List<ForDampReader>> registryEntries = new HashMap<>();

        if (searchKeys == null || searchKeys.length == 0 || file == null ||
                file.isEmpty() ||
                (file.getOriginalFilename() != null && !file.getOriginalFilename().contains("reg"))) {
            return ResponseEntity.badRequest().build();
        }
        for (var searchKey : searchKeys) {
            registryEntries.put(searchKey, new ArrayList<ForDampReader>());
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_16))) {
            String line;
            String currentKey = null;
            for (String searchKey : searchKeys) {
                while ((line = br.readLine()) != null) {
                    line = line.trim();

                    if (line.startsWith("[") && line.endsWith("]")) {
                        // Новый ключ реестра
                        currentKey = line.substring(1, line.length() - 1).replace(" ", "");
                    } else if (currentKey != null && line.contains("=")) {
                        // Значение реестра
                        String[] parts = line.split("=", 2);
                        String name = parts[0].trim().replace("\"", "");
                        String value = parts[1].trim();

                        // Проверяем, начинается ли строка input с prefix
                        if (currentKey.startsWith(searchKey)) {
                            registryEntries.get(searchKey).add(new ForDampReader(currentKey.replace(searchKey, ""), name, value));
                        }
                    }
                }
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // Возвращаем результат
        return ResponseEntity.ok(registryEntries);
    }
}

