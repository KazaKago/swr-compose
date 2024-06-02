import kotlin.reflect.KClass

expect abstract class Runner
expect class TestRunner : Runner
expect annotation class RunWith(val value: KClass<out Runner>)
