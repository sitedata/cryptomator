module org.cryptomator.launcher {
	opens org.cryptomator.launcher to java.rmi, javafx.graphics;

	requires org.cryptomator.commons;
	requires org.cryptomator.ui;

	requires org.cryptomator.webdav.server;
	requires org.cryptomator.keychain;

	requires org.slf4j;
	requires javafx.graphics;
	requires org.apache.commons.lang3;
	requires java.desktop;
	requires java.rmi;
	requires com.google.common;
	requires javax.inject;
	requires dagger;
	requires javafx.fxml;
	requires ch.qos.logback.classic;
}