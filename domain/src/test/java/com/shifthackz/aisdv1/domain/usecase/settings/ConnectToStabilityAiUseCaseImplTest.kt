package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.mocks.mockConfiguration
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestStabilityAiApiKeyUseCase
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class ConnectToStabilityAiUseCaseImplTest {

    private val stubThrowable = Throwable("Something went wrong.")
    private val stubGetConfigurationUseCase = mockk<GetConfigurationUseCase>()
    private val stubSetServerConfigurationUseCase = mockk<SetServerConfigurationUseCase>()
    private val stubTestStabilityAiApiKeyUseCase = mockk<TestStabilityAiApiKeyUseCase>()

    private val useCase = ConnectToStabilityAiUseCaseImpl(
        getConfigurationUseCase = stubGetConfigurationUseCase,
        setServerConfigurationUseCase = stubSetServerConfigurationUseCase,
        testStabilityAiApiKeyUseCase = stubTestStabilityAiApiKeyUseCase,
    )

    @Test
    fun `given connection process successful, API key is valid, expected success result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        every {
            stubTestStabilityAiApiKeyUseCase()
        } returns Single.just(true)

        useCase("5598")
            .test()
            .assertNoErrors()
            .await()
            .assertValue(Result.success(Unit))
            .assertComplete()
    }

    @Test
    fun `given connection process successful, API key is NOT valid, expected success result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.just(mockConfiguration)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.complete()

        every {
            stubTestStabilityAiApiKeyUseCase()
        } returns Single.just(false)

        useCase("5598")
            .test()
            .assertNoErrors()
            .await()
            .assertValue { actual ->
                actual.isFailure
                        && actual.exceptionOrNull() is IllegalStateException
                        && actual.exceptionOrNull()?.message == "Bad key"
            }
            .assertComplete()
    }

    @Test
    fun `given connection process failed, expected error result value`() {
        every {
            stubGetConfigurationUseCase()
        } returns Single.error(stubThrowable)

        every {
            stubSetServerConfigurationUseCase(any())
        } returns Completable.error(stubThrowable)

        every {
            stubTestStabilityAiApiKeyUseCase()
        } returns Single.error(stubThrowable)

        useCase("5598")
            .test()
            .assertNoErrors()
            .await()
            .assertValue(Result.failure(stubThrowable))
            .assertComplete()
    }
}