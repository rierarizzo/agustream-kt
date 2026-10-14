package com.misgus.agustream.core.player

/**
 * Platform handle for the player's video output.
 *
 * It is an empty marker: the concrete type (a Compose frame source on desktop)
 * cannot live in the domain module, but the contract should still say "this is
 * a video output" instead of `Any`.
 */
interface VideoOutput
