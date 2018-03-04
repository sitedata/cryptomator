module org.cryptomator.keychain {
	exports org.cryptomator.keychain;

	requires dagger;
	requires org.cryptomator.jni;
	requires javax.inject;
	requires org.apache.commons.lang3;
	requires com.google.common;
	requires gson;
	requires org.slf4j;
}