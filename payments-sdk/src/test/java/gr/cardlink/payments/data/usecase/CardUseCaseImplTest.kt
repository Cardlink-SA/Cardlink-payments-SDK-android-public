package gr.cardlink.payments.data.usecase

import gr.cardlink.payments.data.validator.Validator
import gr.cardlink.payments.domain.model.Installment
import gr.cardlink.payments.domain.model.InstalmentsConfig
import gr.cardlink.payments.domain.model.Settings
import gr.cardlink.payments.domain.repository.CardRepository
import gr.cardlink.payments.domain.repository.FileRepository
import gr.cardlink.payments.domain.repository.SettingsRepository
import gr.cardlink.payments.domain.usecase.CardUseCase
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@RunWith(JUnit4::class)
internal class CardUseCaseImplTest {

    private lateinit var systemUnderTest: CardUseCase

    @Mock
    private lateinit var cardRepository: CardRepository

    @Mock
    private lateinit var fileRepository: FileRepository

    @Mock
    private lateinit var validator: Validator

    @Mock
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
        `when`(fileRepository.getRemoteValidator()).thenReturn(Single.just(""))
        systemUnderTest = CardUseCaseImpl(cardRepository, fileRepository, validator, settingsRepository)
    }

    @Test
    fun `on null currency, return 0 installments`() {
        // arrange
        val config = InstalmentsConfig(maxInstallments = 5, variations = emptyList())

        val settings = Settings(currencyCode = null, instalmentsConfig = config)
        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        val expected = emptyList<Installment>()

        // act - assert
        systemUnderTest
            .getInstalments(1)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it == expected }
    }

    @Test
    fun `on null config, return 0 installments`() {
        // arrange
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = null)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(1)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `on max installments = 1 and empty variations, return 0 installments`() {
        // arrange
        val config = InstalmentsConfig(maxInstallments = 1, variations = emptyList())
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(1)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `on max installments = 2 and empty variations, return installments`() {
        // arrange
        val config = InstalmentsConfig(maxInstallments = 2, variations = emptyList())
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(1)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isNotEmpty() }
    }

    @Test
    fun `on any max installments, amount equal with first variation only, return 0 installments`() {
        // arrange
        val amountInCents: Long = 100 // cents

        // variation1 amount = 1 euro -> 100 cents
        val variation1 = InstalmentsConfig.Variation(1, 1)

        // variation2 amount = 2 euro -> 200 cents
        val variation2 = InstalmentsConfig.Variation(2, 2)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `on any max installments, amount less than first variation only, return 0 installments`() {
        // arrange
        val amountInCents: Long = 99 // cents

        // variation1 amount = 1 euro -> 100 cents
        val variation1 = InstalmentsConfig.Variation(1, 1)

        // variation2 amount = 2 euro -> 200 cents
        val variation2 = InstalmentsConfig.Variation(2, 2)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `on any max installments, amount greater than first variation only, return 0 installments`() {
        // arrange
        val amountInCents: Long = 101 // cents

        // variation1 amount = 1 euro -> 100 cents
        val variation1 = InstalmentsConfig.Variation(1, 1)

        // variation2 amount = 2 euro -> 200 cents
        val variation2 = InstalmentsConfig.Variation(2, 2)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `on any max installments, amount greater than second variation, return installments`() {
        // arrange
        val amountInCents: Long = 201 // cents

        // variation1 amount = 1 euro -> 100 cents
        val variation1 = InstalmentsConfig.Variation(1, 1)

        // variation2 amount = 2 euro -> 200 cents
        val variation2 = InstalmentsConfig.Variation(2, 2)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isNotEmpty() }
    }

    @Test
    fun `on amount less than first variation, return 0 installments`() {
        // arrange
        val amountInCents: Long = 9999 // 99.99 euro

        // variation1 amount = 100 euro
        val variation1 = InstalmentsConfig.Variation(100, 1)

        // variation2 amount = 150 euro
        val variation2 = InstalmentsConfig.Variation(150, 2)

        // variation3 amount = 500 euro
        val variation3 = InstalmentsConfig.Variation(500, 3)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2, variation3))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `on amount equal with first variation, return 0 installments`() {
        // arrange
        val amountInCents: Long = 10000 // 100 euro

        // variation1 amount = 100 euro
        val variation1 = InstalmentsConfig.Variation(100, 1)

        // variation2 amount = 150 euro
        val variation2 = InstalmentsConfig.Variation(150, 2)

        // variation3 amount = 500 euro
        val variation3 = InstalmentsConfig.Variation(500, 3)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2, variation3))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `on amount greater than first variation, return 0 installments`() {
        // arrange
        val amountInCents: Long = 10001 // 100.01 euro

        // variation1 amount = 100 euro
        val variation1 = InstalmentsConfig.Variation(100, 1)

        // variation2 amount = 150 euro
        val variation2 = InstalmentsConfig.Variation(150, 2)

        // variation3 amount = 500 euro
        val variation3 = InstalmentsConfig.Variation(500, 3)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2, variation3))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `on amount greater than first variation and less than second, return 0 installments`() {
        // arrange
        val amountInCents: Long = 14999 // 149.99 euro

        // variation1 amount = 100 euro
        val variation1 = InstalmentsConfig.Variation(100, 1)

        // variation2 amount = 150 euro
        val variation2 = InstalmentsConfig.Variation(150, 2)

        // variation3 amount = 500 euro
        val variation3 = InstalmentsConfig.Variation(500, 3)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2, variation3))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.isEmpty() }
    }

    @Test
    fun `on amount equal with second variation, return 2 installments`() {
        // arrange
        val expectedInstallments = 2
        val amountInCents: Long = 15000 // 150.00 euro

        // variation1 amount = 100 euro
        val variation1 = InstalmentsConfig.Variation(100, 1)

        // variation2 amount = 150 euro
        val variation2 = InstalmentsConfig.Variation(150, 2)

        // variation3 amount = 500 euro
        val variation3 = InstalmentsConfig.Variation(500, 3)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2, variation3))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.size == expectedInstallments }
    }

    @Test
    fun `on amount greater than second variation, return 2 installments`() {
        // arrange
        val expectedInstallments = 2
        val amountInCents: Long = 15001 // 150.01 euro

        // variation1 amount = 100 euro
        val variation1 = InstalmentsConfig.Variation(100, 1)

        // variation2 amount = 150 euro
        val variation2 = InstalmentsConfig.Variation(150, 2)

        // variation3 amount = 500 euro
        val variation3 = InstalmentsConfig.Variation(500, 3)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2, variation3))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.size == expectedInstallments }
    }

    @Test
    fun `on amount greater than second variation and less than third, return 2 installments`() {
        // arrange
        val expectedInstallments = 2
        val amountInCents: Long = 49999 // 499.99 euro

        // variation1 amount = 100 euro
        val variation1 = InstalmentsConfig.Variation(100, 1)

        // variation2 amount = 150 euro
        val variation2 = InstalmentsConfig.Variation(150, 2)

        // variation3 amount = 500 euro
        val variation3 = InstalmentsConfig.Variation(500, 3)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2, variation3))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.size == expectedInstallments }
    }

    @Test
    fun `on amount equal with third variation, return 6 installments`() {
        // arrange
        val expectedInstallments = 6
        val amountInCents: Long = 50000 // 500 euro

        // variation1 amount = 100 euro
        val variation1 = InstalmentsConfig.Variation(100, 1)

        // variation2 amount = 150 euro
        val variation2 = InstalmentsConfig.Variation(150, 2)

        // variation3 amount = 500 euro
        val variation3 = InstalmentsConfig.Variation(500, 6)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2, variation3))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.size == expectedInstallments }
    }

    @Test
    fun `on amount greater than third variation, return 6 installments`() {
        // arrange
        val expectedInstallments = 6
        val amountInCents: Long = 50001 // 501 euro

        // variation1 amount = 100 euro
        val variation1 = InstalmentsConfig.Variation(100, 1)

        // variation2 amount = 150 euro
        val variation2 = InstalmentsConfig.Variation(150, 2)

        // variation3 amount = 500 euro
        val variation3 = InstalmentsConfig.Variation(500, 6)

        val config = InstalmentsConfig(maxInstallments = 2, variations = listOf(variation1, variation2, variation3))
        val settings = Settings(currencyCode = "EUR", instalmentsConfig = config)

        `when`(settingsRepository.getSettings()).thenReturn(Single.just(settings))

        // act - assert
        systemUnderTest
            .getInstalments(amountInCents)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue { it.size == expectedInstallments }
    }

}