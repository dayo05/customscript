package me.ddayo.customscript.client.event

import net.minecraftforge.eventbus.api.Event


class OnHudStateChangedEvent(public val hudName: String, public val state: Boolean): Event()