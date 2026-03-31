package com.gis.controller;

import com.gis.entity.Camera;
import com.gis.repository.CameraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/camera")
public class CameraController {

    @Autowired
    private CameraRepository cameraRepository;

    @GetMapping
    public ResponseEntity<List<Camera>> getAllCameras() {
        List<Camera> cameras = cameraRepository.findAll();
        return new ResponseEntity<>(cameras, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Camera> getCameraById(@PathVariable @NonNull Long id) {
        return cameraRepository.findById(id)
                .map(camera -> new ResponseEntity<>(camera, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Camera> createCamera(@RequestBody @NonNull Camera camera) {
        Camera savedCamera = cameraRepository.save(camera);
        return new ResponseEntity<>(savedCamera, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Camera> updateCamera(@PathVariable @NonNull Long id, @RequestBody @NonNull Camera camera) {
        return cameraRepository.findById(id)
                .map(existingCamera -> {
                    camera.setId(id);
                    Camera updatedCamera = cameraRepository.save(camera);
                    return new ResponseEntity<>(updatedCamera, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCamera(@PathVariable @NonNull Long id) {
        if (cameraRepository.findById(id).isPresent()) {
            cameraRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
