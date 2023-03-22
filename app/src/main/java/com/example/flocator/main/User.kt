package com.example.flocator.main

data class User(val id: Long, var longitude: Double, var latitude: Double) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + latitude.hashCode()
        return result
    }
}
