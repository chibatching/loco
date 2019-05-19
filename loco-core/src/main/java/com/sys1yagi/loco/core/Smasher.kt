package com.sys1yagi.loco.core

/**
 * Log serializer
 */
interface Smasher {
    fun smash(log: LocoLog): String
}

fun Smasher(smash: (LocoLog) -> String) = object : Smasher {
    override fun smash(log: LocoLog): String {
        return smash.invoke(log)
    }
}
