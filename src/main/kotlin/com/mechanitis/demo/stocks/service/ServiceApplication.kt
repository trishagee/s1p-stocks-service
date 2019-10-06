package com.mechanitis.demo.stocks.service

import org.apache.commons.logging.LogFactory
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
import java.time.LocalDateTime
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
class StockPricesRSocketController(private val stockService: StockService) {

    @MessageMapping("stockPrices")
    fun prices(symbol: String) = stockService.streamOfPrices(symbol)
}

@Service
class StockService {
    private val pricesForStock = ConcurrentHashMap<String, Flux<StockPrice>>()
    private val log = LogFactory.getLog(javaClass)

    fun streamOfPrices(symbol: String): Flux<StockPrice> {
        return pricesForStock.computeIfAbsent(symbol) {
            Flux
                .interval(Duration.ofSeconds(1L))
                .map { StockPrice(symbol, randomStockPrice(), LocalDateTime.now()) }
                .doOnSubscribe { _ -> log.info("new subscription for symbol " + symbol + '.'.toString()) }
                .share()
        }
    }

    private fun randomStockPrice() = ThreadLocalRandom.current().nextDouble(100.0)

}

class StockPrice(val symbol: String,
                 val price: Double,
                 val time: LocalDateTime)