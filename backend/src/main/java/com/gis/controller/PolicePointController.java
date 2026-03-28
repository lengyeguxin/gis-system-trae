package com.gis.controller;

import com.gis.entity.PolicePoint;
import com.gis.repository.PolicePointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/police")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:4173", "http://localhost:3000", "http://localhost:3002", "http://localhost:5176"})
public class PolicePointController {

    @Autowired
    private PolicePointRepository policePointRepository;

    @GetMapping
    public ResponseEntity<List<PolicePoint>> getAllPolicePoints() {
        List<PolicePoint> policePoints = policePointRepository.findAll();
        return new ResponseEntity<>(policePoints, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicePoint> getPolicePointById(@PathVariable @NonNull Long id) {
        return policePointRepository.findById(id)
                .map(policePoint -> new ResponseEntity<>(policePoint, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<PolicePoint> createPolicePoint(@RequestBody @NonNull PolicePoint policePoint) {
        PolicePoint savedPolicePoint = policePointRepository.save(policePoint);
        return new ResponseEntity<>(savedPolicePoint, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolicePoint> updatePolicePoint(@PathVariable @NonNull Long id, @RequestBody @NonNull PolicePoint policePoint) {
        return policePointRepository.findById(id)
                .map(existingPolicePoint -> {
                    policePoint.setId(id);
                    PolicePoint updatedPolicePoint = policePointRepository.save(policePoint);
                    return new ResponseEntity<>(updatedPolicePoint, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicePoint(@PathVariable @NonNull Long id) {
        if (policePointRepository.findById(id).isPresent()) {
            policePointRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
