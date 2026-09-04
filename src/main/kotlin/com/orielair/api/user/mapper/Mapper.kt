package com.orielair.api.user.mapper

interface Mapper<E, D> {
    fun mapTo(e: E): D
    fun mapFrom(d: D): E
}