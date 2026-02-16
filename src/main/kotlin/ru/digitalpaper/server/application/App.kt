package ru.digitalpaper.server.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["ru.digitalpaper.server"])
@EnableJpaRepositories(basePackages = ["ru.digitalpaper.server"])
@EntityScan(basePackages = ["ru.digitalpaper.server"])
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
