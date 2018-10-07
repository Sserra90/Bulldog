package com.criations.bulldog_annotations

/**
 * Marks an interface as a target for Bulldog annotation processor.
 * <pre>`
 * @Bulldog
 * interface Settings {
 * val id: Int
 * val email: String
 * }
 * `</pre>
 */
@Target(AnnotationTarget.CLASS)
@Retention(value = AnnotationRetention.RUNTIME)
annotation class Bulldog(val name: String = "")
