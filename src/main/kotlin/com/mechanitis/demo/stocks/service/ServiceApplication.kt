package com.mechanitis.demo.stocks.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

@SpringBootApplication
open class ServiceApplication

fun main() {
    runApplication<ServiceApplication>()
}

@RestController
class StockPricesRestController(private val stockService: StockService) {

    @GetMapping(value = ["/stocks/{symbol}"],
                produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun prices(@PathVariable symbol: String) = stockService.streamOfPrices(symbol)

}

@Controller
class StockPricesRSocketController(val stockService: StockService) {
    @MessageMapping("stockPrices")
    fun prices(symbol: String) : Flux<String> {
        return stockService.streamOfPrices(symbol)
    }
}

@Service
class StockService {
    private val pricesForStock = ConcurrentHashMap<String, Flux<String>>()

    fun streamOfPrices(symbol: String): Flux<String> {
        return pricesForStock.computeIfAbsent(symbol) {
            Flux
                .interval(Duration.ofSeconds(1L))
                .map { randomStockPrice() }
                .share()
        }

    }

    private fun randomStockPrice() :String = ThreadLocalRandom.current().nextDouble(100.0).toString()

}