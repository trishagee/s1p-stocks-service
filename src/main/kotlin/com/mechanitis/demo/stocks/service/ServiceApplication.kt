package com.mechanitis.demo.stocks.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom

@SpringBootApplication
open class ServiceApplication

fun main() {
    runApplication<ServiceApplication>()
}

@RestController
class StockPricesRestController() {

    @GetMapping(value = ["/stocks/{symbol}"],
                produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun prices(@PathVariable symbol: String): Flux<Double> {
        return Flux.interval(Duration.ofSeconds(1L))
                .map { ThreadLocalRandom.current().nextDouble(100.0) }
    }

}