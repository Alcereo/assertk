package assertk

import assertk.assertions.support.show

/**
 * Marks the assertion DSL.
 */
@DslMarker
annotation class AssertkDsl

/**
 * An assertion. Holds an actual value to assertion on and an optional name.
 * @see [assert]
 */
@AssertkDsl
sealed class Assert<out T>(val name: String?, internal val context: Any?) {
    /**
     * Transforms an assertion from one type to another. If the assertion is failing the resulting assertion will still
     * be failing, otherwise the mapping function is called. An optional name can be provided, otherwise this
     * assertion's name will be used.
     */
    fun <R> transform(name: String? = this.name, transform: (T) -> R): Assert<R> {
        return when (this) {
            is ValueAssert -> {
                try {
                    assert(transform(value), name)
                } catch (e: Throwable) {
                    notifyFailure(e)
                    FailingAssert<R>(e, name, context)
                }
            }
            is FailingAssert -> FailingAssert(error, name, context)
        }
    }

    /**
     * Allows checking the actual value of an assert. This can be used to build your own custom assertions.
     * ```
     * fun Assert<Int>.isTen() = given { actual ->
     *     if (actual == 10) return
     *     expected("to be 10 but was:${show(actual)}")
     * }
     * ```
     */
    inline fun given(assertion: (T) -> Unit) {
        if (this is ValueAssert) {
            try {
                assertion(value)
            } catch (e: Throwable) {
                notifyFailure(e)
            }
        }
    }

    /**
     * Asserts on the given value with an optional name.
     *
     * ```
     * assert(true, name = "true").isTrue()
     * ```
     */
    abstract fun <R> assert(actual: R, name: String? = this.name): Assert<R>

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(message = "Use `given` or `transform` to access the actual value instead")
    val actual: T
        get() = when (this) {
            is ValueAssert -> value
            is FailingAssert -> throw error
        }
}

@AssertkDsl
class ValueAssert<out T> internal constructor(val value: T, name: String?, context: Any?) :
    Assert<T>(name, context) {

    override fun <R> assert(actual: R, name: String?): Assert<R> =
        ValueAssert(actual, name, if (context != null || this.value === actual) context else this.value)
}

class FailingAssert<out T> internal constructor(val error: Throwable, name: String?, context: Any?) :
    Assert<T>(name, context) {
    override fun <R> assert(actual: R, name: String?): Assert<R> = FailingAssert(error, name, context)
}

/**
 * An assertion on a block of code. Can assert that it either throws and error or returns a value.
 */
sealed class AssertBlock<out T> {
    /**
     * Runs the given lambda if the block throws an error, otherwise fails.
     */
    abstract fun thrownError(f: Assert<Throwable>.() -> Unit)

    /**
     * Runs the given lambda if the block returns a value, otherwise fails.
     */
    abstract fun returnedValue(f: Assert<T>.() -> Unit)

    abstract fun doesNotThrowAnyException()

    internal class Value<out T> internal constructor(private val value: T) : AssertBlock<T>() {
        override fun thrownError(f: Assert<Throwable>.() -> Unit) {
            fail("expected exception but was:${show(value)}")
        }

        override fun returnedValue(f: Assert<T>.() -> Unit) {
            f(assert(value))
        }

        override fun doesNotThrowAnyException() {
            assert(value)
        }
    }

    internal class Error<out T> internal constructor(private val error: Throwable) : AssertBlock<T>() {
        override fun thrownError(f: Assert<Throwable>.() -> Unit) {
            f(assert(error))
        }

        override fun returnedValue(f: Assert<T>.() -> Unit) {
            fail("expected value but threw:${showError(error)}")
        }

        override fun doesNotThrowAnyException() {
            fail("expected to not throw an exception but threw:${showError(error)}")
        }
    }
}

/**
 * Calls platform specific function so that it is possible to show stacktrace if able
 *
 * TODO: use @OptionalExpectation (https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-optional-expectation/index.html) here once available and call default implementation of [show] for JS
 */
internal expect fun showError(e: Throwable): String

/**
 * Asserts on the given value with an optional name.
 *
 * ```
 * assert(true, name = "true").isTrue()
 * ```
 */
fun <T> assert(actual: T, name: String? = null): Assert<T> = ValueAssert(actual, name, null)

/**
 * All assertions in the given lambda are run.
 *
 * ```
 * assert("test", name = "test").all {
 *   startsWith("t")
 *   endsWith("t")
 * }
 * ```
 */
fun <T> Assert<T>.all(f: Assert<T>.() -> Unit) {
    FailureContext.run(SoftFailure()) {
        f()
    }
}

/**
 * Asserts on the given block. You can test that it returns a value or throws an exception.
 *
 * ```
 * assert { 1 + 1 }.returnedValue {
 *   isPositive()
 * }
 *
 * assert {
 *   throw Exception("error")
 * }.thrownError {
 *   hasMessage("error")
 * }
 * ```
 */
fun <T> assert(f: () -> T): AssertBlock<T> {
    return FailureContext.run(SoftFailure()) {
        @Suppress("TooGenericExceptionCaught")
        try {
            AssertBlock.Value(f())
        } catch (e: Throwable) {
            AssertBlock.Error(e)
        }
    }
}

/**
 * Runs all assertions in the given lambda and reports any failures.
 */
fun assertAll(f: () -> Unit) {
    FailureContext.run(SoftFailure(), f)
}

/**
 * Catches any exceptions thrown in the given lambda and returns it. This is an easy way to assert on expected thrown
 * exceptions.
 *
 * ```
 * val exception = catch { throw Exception("error") }
 * assert(exception).isNotNull {
 *   hasMessage("error")
 * }
 * ```
 */
fun catch(f: () -> Unit): Throwable? {
    @Suppress("TooGenericExceptionCaught")
    try {
        f()
        return null
    } catch (e: Throwable) {
        return e
    }
}
