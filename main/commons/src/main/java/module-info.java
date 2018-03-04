module org.cryptomator.commons {
	exports org.cryptomator.common;
	exports org.cryptomator.common.settings;

	requires java.sql;
	requires javax.inject;
	requires dagger;
	requires com.google.common;
	requires org.apache.commons.lang3;
	requires javafx.base;
	requires org.slf4j;
	requires gson;
	requires easybind;
}