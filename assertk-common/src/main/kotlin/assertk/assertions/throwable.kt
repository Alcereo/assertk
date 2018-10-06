package assertk.assertions

import assertk.Assert
import assertk.all
import kotlin.reflect.KClass

/**
 * Returns an assert on the throwable's message.
 */
fun <T : Throwable> Assert<T>.message() = prop("message", Throwable::message)

/**
 * Returns an assert on the throwable's cause.
 */
fun <T : Throwable> Assert<T>.cause() = prop("cause", Throwable::cause)

/**
 * Returns an assert on the throwable's root cause.
 */
fun <T : Throwable> Assert<T>.rootCause() = prop("rootCause", Throwable::rootCause)

/**
 * Asserts the throwable has the expected message.
 */
fun <T : Throwable> Assert<T>.hasMessage(message: String?) {
    message().isEqualTo(message)
}

/**
 * Asserts the throwable is similar to the expected cause, checking the type and message.
 * @see [hasNoCause]
 */
fun <T : Throwable> Assert<T>.hasCause(cause: Throwable) {
    cause().isNotNull {
        kClass().isEqualTo(cause::class)
        hasMessage(cause.message)
    }
}

/**
 * Asserts the throwable has no cause.
 * @see [hasCause]
 */
fun <T : Throwable> Assert<T>.hasNoCause() {
    cause().isNull()
}

/**
 * Asserts the throwable is similar to the expected root cause, checking the type and message.
 */
fun <T : Throwable> Assert<T>.hasRootCause(cause: Throwable) {
    rootCause().all {
        kClass().isEqualTo(cause::class)
        hasMessage(cause.message)
    }
}

/**
 * Asserts the throwable has a message starting with the expected string.
 */
@Deprecated(
    message = "Use message().isNotNull { startsWith(prefix) } instead.",
    replaceWith = ReplaceWith("message().isNotNull { startsWith(prefix) }"),
    level = DeprecationLevel.ERROR
)
fun <T : Throwable> Assert<T>.hasMessageStartingWith(prefix: String) {
    assert(actual.message, "message").isNotNull { startsWith(prefix) }
}

/**
 * Asserts the throwable has a message containing the expected string.
 */
@Deprecated(
    message = "Use message().isNotNull { contains(string) } instead.",
    replaceWith = ReplaceWith("message().isNotNull { contains(string) }"),
    level = DeprecationLevel.ERROR
)
fun <T : Throwable> Assert<T>.hasMessageContaining(string: String) {
    assert(actual.message, "message").isNotNull { contains(string) }
}

/**
 * Asserts the throwable has a messaging matching the expected regular expression.
 */
@Deprecated(
    message = "Use message().isNotNull { matches(regex) } instead.",
    replaceWith = ReplaceWith("message().isNotNull { matches(regex) }"),
    level = DeprecationLevel.ERROR
)
fun <T : Throwable> Assert<T>.hasMessageMatching(regex: Regex) {
    assert(actual.message, "message").isNotNull { matches(regex) }
}

/**
 * Asserts the throwable has a message ending with the expected string.
 */
@Deprecated(
    message = "Use message().isNotNull { endsWith(suffix) } instead.",
    replaceWith = ReplaceWith("message().isNotNull { endsWith(suffix) }"),
    level = DeprecationLevel.ERROR
)
fun <T : Throwable> Assert<T>.hasMessageEndingWith(suffix: String) {
    assert(actual.message, "message").isNotNull { endsWith(suffix) }
}

/**
 * Asserts the throwable's cause matches the expected kotlin class.
 * @see [hasCauseInstanceOf]
 * @see [hasRootCauseWithClass]
 */
@Deprecated(
    message = "Use cause().isNotNull { kClass().isEqualTo(kclass) } instead.",
    replaceWith = ReplaceWith("cause().isNotNull { kClass().isEqualTo(kclass) }"),
    level = DeprecationLevel.ERROR
)
fun <T : Throwable> Assert<T>.hasCauseWithClass(kclass: KClass<out T>) {
    assert(actual.cause, "cause").isNotNull { kClass().isEqualTo(kclass) }
}

/**
 * Asserts the throwable's root cause matches the expected kotlin class.
 * @see [hasRootCauseInstanceOf]
 * @see [hasCauseWithClass]
 */
@Deprecated(
    message = "Use rootCause().isNotNull { kClass().isEqualTo(kclass) } instead.",
    replaceWith = ReplaceWith("rootCause().isNotNull { kClass().isEqualTo(kclass) }"),
    level = DeprecationLevel.ERROR
)
fun <T : Throwable> Assert<T>.hasRootCauseWithClass(kclass: KClass<out T>) {
    assert(actual.rootCause(), "root cause").isNotNull { kClass().isEqualTo(kclass) }
}

/**
 * Asserts the throwable's cause is an instance of the expected kotlin class.
 * @see [hasCauseWithClass]
 * @see [hasRootCauseInstanceOf]
 */
@Deprecated(
    message = "Use cause().isNotNull { isInstanceOf(kclass) } instead.",
    replaceWith = ReplaceWith("cause().isNotNull { isInstanceOf(kclass) }"),
    level = DeprecationLevel.ERROR
)
fun <T : Throwable> Assert<T>.hasCauseInstanceOf(kclass: KClass<out T>) {
    assert(actual.cause, "cause").isNotNull { isInstanceOf(kclass) }
}

/**
 * Asserts the throwable's root cause is an instance of the expected kotlin class.
 * @see [hasRootCauseWithClass]
 * @see [hasCauseInstanceOf]
 */
@Deprecated(
    message = "Use rootCause().isInstanceOf(kclass) instead.",
    replaceWith = ReplaceWith("rootCause().isInstanceOf(kclass)"),
    level = DeprecationLevel.ERROR
)
fun <T : Throwable> Assert<T>.hasRootCauseInstanceOf(kclass: KClass<out T>) {
    assert(actual.rootCause(), "root cause").isNotNull { isInstanceOf(kclass) }
}

private fun Throwable.rootCause(): Throwable = this.cause?.rootCause() ?: this

