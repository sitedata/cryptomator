package org.cryptomator.ui.controls;

/**
 * Inspired by de.jensd:fontawesomefx-fontawesome
 */
public enum FontAwesome5Icon {
	ANCHOR("\uF13D"), //
	ARROW_ALT_UP("\uF357"), //
	CHECK("\uF00C"), //
	COG("\uF013"), //
	COGS("\uF085"), //
	EXCLAMATION_TRIANGLE("\uF071"), //
	EYE("\uF06E"), //
	EYE_SLASH("\uF070"), //
	FILE_IMPORT("\uF56F"), //
	FOLDER_OPEN("\uF07C"), //
	HDD("\uF0A0"), //
	KEY("\uF084"), //
	LOCK_ALT("\uF30D"), //
	LOCK_OPEN_ALT("\uF3C2"), //
	MINUS("\uF068"), //
	PLUS("\uF067"), //
	QUESTION("\uF128"), //
	SPARKLES("\uF890"), //
	SPINNER("\uF110"), //
	SYNC("\uF021"), //
	TIMES("\uF00D"), //
	WRENCH("\uF0AD"), //
	;

	private final String unicode;

	FontAwesome5Icon(String unicode) {
		this.unicode = unicode;
	}

	public String unicode() {
		return unicode;
	}
}
