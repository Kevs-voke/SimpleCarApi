package com.rtx.Car.Repository;

import com.rtx.Car.DTo.CarDTO;
import com.rtx.Car.Entity.Car;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CarRepository extends ReactiveCrudRepository<Car,Long> {
    Mono<Boolean> existsByVin(String vin);

    Mono<Car> findByRegistrationNumber(String registrationNumber);

    Flux<Car> findBymodel(String model);

    @Query("""
        SELECT * FROM car c
        WHERE (:model IS NULL OR c.model = :model)
        AND (:color IS NULL OR c.color = :color)
        AND (:registrationNumber IS NULL OR c.registration_number = :registrationNumber)
    """)
    Flux<Car> searchCars(String model, String color, String registrationNumber);
}
