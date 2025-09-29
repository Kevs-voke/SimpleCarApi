package com.rtx.Car.Service.Implementation;


import com.rtx.Car.DTo.CarDTO;
import com.rtx.Car.Entity.Car;

import com.rtx.Car.Exceptions.NotFoundException;
import com.rtx.Car.Mapper.CarDTOMapper;
import com.rtx.Car.Repository.CarRepository;
import com.rtx.Car.Service.Interface.CarService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class CarServiceImp implements CarService {

    private final CarRepository carRepository;
    private final CarDTOMapper carDTOMapper;
    private final CircuitBreaker circuitBreaker;

    public CarServiceImp(CarRepository carRepository,
                         CarDTOMapper carDTOMapper,
                         CircuitBreakerRegistry registry) {
        this.carRepository = carRepository;
        this.carDTOMapper = carDTOMapper;

        this.circuitBreaker = registry.circuitBreaker("carServiceCircuitBreaker");
    }
    @Override
    public Mono<CarDTO> saveCar(CarDTO carDTO) {
        return carRepository.existsByVin(carDTO.vin()).flatMap(exists ->
        {
            if (exists) {
                return Mono.error(new DuplicateKeyException("The car already exist"));
            }
            Car car = carDTOMapper.mapToEntity(carDTO);
            return carRepository.save(car).map(carDTOMapper::mapToDTO);
        });
    }

    @Override
    public Mono<Car> findCarByregistrationNumber(String registrationNumber) {
        return carRepository.findByRegistrationNumber(registrationNumber)
                .switchIfEmpty(
                        Mono.error(new NotFoundException(
                                "Car with registration number " + registrationNumber + " not found"
                        ))
                );
    }


    public Flux<Car> findCarsByModel(String model) {
        if ("notfound".equalsIgnoreCase(model)) {
            return Flux.error(new NotFoundException("Cars are not found of that model: " + model));
        }
        if ("invalid".equalsIgnoreCase(model)) {
            return Flux.error(new IllegalArgumentException("The provided model is invalid."));
        }
        return carRepository.findBymodel(model);
    }

    @Override
    public Flux<CarDTO> searchCars(String model, String color, String registrationNumber) {
        return carRepository.searchCars(model, color, registrationNumber)
                .map(carDTOMapper::mapToDTO)
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(1))
                                .filter(ex -> ex instanceof DataAccessException)
                )
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
    }

}
