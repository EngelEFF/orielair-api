package com.orielair.api.user.enum

import com.fasterxml.jackson.annotation.JsonCreator

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM;

    // This called when an invalid or empty enum is provided to default Theme it to SYSTEM
    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String?): Theme =
            entries.find { it.name.equals(value ?: "", ignoreCase = true) }
                ?: SYSTEM // fallback default
    }
}