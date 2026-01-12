package com.tablegen.controller;

import com.tablegen.dto.GenerateRequest;
import com.tablegen.dto.GenerateResponse;
import com.tablegen.service.TableGenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class TableGenController {

    private final TableGenService tableGenService;

    public TableGenController(TableGenService tableGenService) {
        this.tableGenService = tableGenService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@RequestBody GenerateRequest request) {
        try {
            GenerateResponse response = tableGenService.generate(request);
            byte[] content = response.getGeneratedCode().getBytes(StandardCharsets.UTF_8);

            String filename = response.getTableName() + ".html";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.TEXT_HTML)
                    .contentLength(content.length)
                    .body(content);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(("Error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }
}
