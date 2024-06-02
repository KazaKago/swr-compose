import kotlin.reflect.KClass

actual abstract class Runner
actual class TestRunner : Runner()
actual annotation class RunWith(actual val value: KClass<out Runner>)
