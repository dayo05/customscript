package me.ddayo.customscript.client.event

import net.minecraftforge.eventbus.api.Event


class OnDynamicValueUpdateEvent(public val name: String, public val value: String): Event()