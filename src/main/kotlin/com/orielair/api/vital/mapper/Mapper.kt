package com.orielair.api.vital.mapper

interface Mapper<E, D> {
    fun mapTo(e: E): D
    fun mapFrom(d: D): E
}