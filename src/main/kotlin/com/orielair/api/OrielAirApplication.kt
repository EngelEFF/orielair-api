package com.orielair.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableScheduling
class OrielAirApplication

fun main(args: Array<String>) {
	runApplication<OrielAirApplication>(*args)
}
