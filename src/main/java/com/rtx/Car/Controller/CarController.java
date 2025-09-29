package com.rtx.Car.Controller;

import com.rtx.Car.DTo.CarDTO;
import com.rtx.Car.Exceptions.NotFoundException;
import com.rtx.Car.Mapper.CarDTOMapper;
import com.rtx.Car.Service.Interface.CarService;

import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.constraints.Pattern;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("car")
public class CarController {

    private final CarService carService;
    private final CircuitBreaker circuitBreaker;
    private final CarDTOMapper carDTOMapper;
    private static final Logger log = LoggerFactory.getLogger(CarController.class);


    public CarController(CarService carService, CircuitBreakerRegistry registry, CarDTOMapper carDTOMapper) {
        this.carService = carService;
        this.circuitBreaker = registry.circuitBreaker("carSearchBreaker");
        this.carDTOMapper = carDTOMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<CarDTO>> createCar(@RequestBody Mono<CarDTO> carDTOMono) {
        return carDTOMono
                .flatMap(carDTO ->
                        carService.saveCar(carDTO)
                                .map(savedCar -> ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(savedCar))
                                .onErrorResume(DuplicateKeyException.class, e ->
                                        Mono.just(ResponseEntity
                                                .status(HttpStatus.CONFLICT)
                                                .body(null)))
                )
                .defaultIfEmpty(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build());
    }

    @GetMapping("registrationNumber")
    public Mono<ResponseEntity<CarDTO>> findCarByRegistrationNumber(@RequestParam String registrationNumber) {
        return carService.findCarByregistrationNumber(registrationNumber)
                .map(carDTOMapper::mapToDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(NotFoundException.class, ex -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(IllegalArgumentException.class, ex -> Mono.just(ResponseEntity.badRequest().build()))
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }


    @GetMapping("/model")
    public Mono<ResponseEntity<Flux<CarDTO>>> findCarsByModel(@RequestParam String model) {
        Flux<CarDTO> cars = carService.findCarsByModel(model)
                .map(carDTOMapper::mapToDTO);

        return cars.hasElements()
                .flatMap(hasCars -> {
                    if (hasCars) {
                        return Mono.just(ResponseEntity.ok(cars));
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }


    @GetMapping("/search")
    public Mono<ResponseEntity<Flux<CarDTO>>> searchCars(
            @RequestParam(required = false) @Pattern(regexp = "^[a-zA-Z0-9 ]*$", message = "Invalid model") String model,
            @RequestParam(required = false) @Pattern(regexp = "^[a-zA-Z0-9 ]*$", message = "Invalid color") String color,
            @RequestParam(required = false) @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Invalid registration number") String registrationNumber) {

        Flux<CarDTO> cars = carService.searchCars(model, color, registrationNumber);

        return cars.hasElements()
                .flatMap(hasElements -> {
                    if (hasElements) {
                        return Mono.just(ResponseEntity.ok(cars));
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Flux.<CarDTO>empty()));
                    }
                })
                .onErrorResume(CallNotPermittedException.class, e -> Mono.just(
                        ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                .body(Flux.<CarDTO>just(new CarDTO(
                                        null, null, null, 0, null,
                                        "ERROR: Circuit breaker open", "0.0")))
                ))
                .onErrorResume(DataAccessException.class, e -> Mono.just(
                        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(Flux.<CarDTO>just(new CarDTO(
                                        null, null, null, 0, null,
                                        "ERROR: Database unavailable", "0.0")))
                ))
                .onErrorResume(Exception.class, e -> {
                    log.error("Unexpected error in searchCars", e);
                    return Mono.just(
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Flux.<CarDTO>just(new CarDTO(
                                            null, null, null, 0, null,
                                            "ERROR: Unexpected error: " + e.getMessage(), "0.0")))
                    );
                });
    }

}