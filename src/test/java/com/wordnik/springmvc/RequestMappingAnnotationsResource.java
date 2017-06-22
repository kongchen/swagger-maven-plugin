package com.wordnik.springmvc;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/requestMappings")
@Api
public class RequestMappingAnnotationsResource {
    @ApiOperation(value = "getMapping")
    @GetMapping(value="/getMapping")
    public ResponseEntity<?> testingGetMapping() {
        return ResponseEntity.ok("getMapping");
    }
    @ApiOperation(value = "postMapping")
    @PostMapping(value="/postMapping")
    public ResponseEntity<?> testingPostMapping() {
        return ResponseEntity.created(URI.create("/getMapping")).build();
    }
    @ApiOperation(value = "putMapping")
    @PutMapping(value="/putMapping")
    public ResponseEntity<?> testingPutMapping() {
        return ResponseEntity.ok("putMapping");
    }
    @ApiOperation(value = "patchMapping")
    @PatchMapping(value="/patchMapping")
    public ResponseEntity<?> testingPatchMapping() {
        return ResponseEntity.ok("patchMapping");
    }
    @ApiOperation(value = "deleteMapping")
    @DeleteMapping(value="/deleteMapping")
    public ResponseEntity<?> testingDeleteMapping() {
        return ResponseEntity.noContent().build();
    }
}
