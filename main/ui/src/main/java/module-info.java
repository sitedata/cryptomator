module org.cryptomator.ui {
	exports org.cryptomator.ui;
	exports org.cryptomator.ui.controllers;
	exports org.cryptomator.ui.l10n;
	exports org.cryptomator.ui.model;
	exports org.cryptomator.ui.util;

	opens org.cryptomator.ui.controllers to javafx.fxml;
	opens org.cryptomator.ui.controls to javafx.fxml;

	requires org.cryptomator.commons;
	requires org.cryptomator.cryptofs;
	requires org.cryptomator.webdav.server;

	requires javax.inject;
	requires dagger;
	requires com.google.common;
	requires org.slf4j;
	requires javafx.graphics;
	requires javafx.fxml;
	requires javafx.controls;
	requires easybind;
	requires org.apache.commons.lang3;
	requires java.desktop;
	requires java.scripting;
	requires org.cryptomator.jni;
	requires org.cryptomator.cryptolib;
	requires commons.codec;
	requires org.cryptomator.keychain;
	requires zxcvbn;
	requires jdk.incubator.httpclient;
	requires gson;
}