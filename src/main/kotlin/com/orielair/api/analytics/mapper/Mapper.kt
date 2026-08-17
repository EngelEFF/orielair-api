package com.orielair.api.analytics.mapper

interface Mapper<E, D> {
    fun mapTo(e: E): D
}