abstract class UseCase<Input, Output> {
    @Throws(Exception::class)
    abstract suspend fun execute(input: Input): Output
}