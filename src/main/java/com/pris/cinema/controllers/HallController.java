package com.pris.cinema.controllers;

import com.pris.cinema.entities.Hall;
import com.pris.cinema.entities.Projection;
import com.pris.cinema.entities.Seat;
import com.pris.cinema.entities.Section;
import com.pris.cinema.entities.dto.HallDisplayDto;
import com.pris.cinema.entities.dto.HallRegisterDto;
import com.pris.cinema.repository.HallRepository;
import com.pris.cinema.repository.ProjectionRepository;
import com.pris.cinema.repository.SeatRepository;
import com.pris.cinema.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/halls")
public class HallController {


    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private ProjectionRepository projectionRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SectionRepository sectionRepository;


    @GetMapping("")
    public ResponseEntity<?> getAll() {
        return new ResponseEntity<>(hallRepository.findAll(), HttpStatus.OK);
    }


    @GetMapping("/display")
    public ResponseEntity<?> getAllAsDisplayDtos() {
        return new ResponseEntity<>(
                ((List<Hall>) hallRepository.findAll()).stream()
                        .map(h -> h.getDisplayDto())
                        .collect(Collectors.toList()),
                HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHall(@PathVariable Long id) {

        // razmisliti kako ovo da se uradi

        Optional<Hall> hallOpt = hallRepository.findById(id);

        if (!hallOpt.isPresent())
            return new ResponseEntity<>("Hall not found.", HttpStatus.BAD_REQUEST);

        Hall hall = hallOpt.get();

        for (Seat s : hall.getSeats())
            seatRepository.delete(s);

        for (Projection p : hall.getProjections())
            projectionRepository.delete(p);

        hallRepository.delete(hall);

        return new ResponseEntity<>("Hall with ID " + id + " not found.", HttpStatus.BAD_REQUEST);
    }


    @PostMapping("")
    public ResponseEntity<?> registerHall(@Valid @RequestBody HallRegisterDto hallRegisterDto, BindingResult result) {

        Hall newHall = new Hall();
        newHall.setName(hallRegisterDto.getName());

        Hall persistedHall = hallRepository.save(newHall);

        generateSeats(hallRegisterDto.getSeatsGroundFloor(), persistedHall,
                sectionRepository.findBySection("GROUND_FLOOR").get());
        generateSeats(hallRegisterDto.getSeatsGalleryLeft(), persistedHall,
                sectionRepository.findBySection("GALLERY_LEFT").get());
        generateSeats(hallRegisterDto.getSeatsGalleryRight(), persistedHall,
                sectionRepository.findBySection("GALLERY_RIGHT").get());

        persistedHall = hallRepository.save(persistedHall);

        return new ResponseEntity<>(persistedHall, HttpStatus.OK);
    }


    private void generateSeats(int numSeats, Hall newHall, Section section) {
        IntStream.range(0, numSeats).forEach(i -> {
            Seat newSeat = new Seat();

            newSeat.setHall(newHall);
            newSeat.setSection(section);

            newHall.getSeats().add(seatRepository.save(newSeat));
        });
    }
}
