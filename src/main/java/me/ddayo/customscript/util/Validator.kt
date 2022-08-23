package me.ddayo.customscript.util

interface Validator<T> {
    fun validate(t: T): Boolean
}