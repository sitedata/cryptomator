package org.cryptomator.launcher;

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Module
class CryptomatorModule {

	@Provides
	@Singleton
	@Named("shutdownTaskScheduler")
	Consumer<Runnable> provideShutdownTaskScheduler(CleanShutdownPerformer shutdownPerformer) {
		return shutdownPerformer::scheduleShutdownTask;
	}

	@Provides
	@Singleton
	@Named("shutdownLatch")
	static CountDownLatch provideShutdownLatch() {
		return new CountDownLatch(1);
	}

	@Provides
	@Singleton
	@Named("applicationVersion")
	static Optional<String> provideApplicationVersion() {
		return Optional.ofNullable(Cryptomator.class.getPackage().getImplementationVersion());
	}

}
