package org.cryptomator.ui.preferences;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.cryptomator.ui.common.FXMLLoaderFactory;
import org.cryptomator.ui.common.FxController;
import org.cryptomator.ui.common.FxControllerKey;
import org.cryptomator.ui.common.FxmlFile;
import org.cryptomator.ui.common.FxmlScene;

import javax.inject.Named;
import javax.inject.Provider;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@Module
abstract class PreferencesModule {

	@Provides
	@PreferencesWindow
	@PreferencesScoped
	static FXMLLoaderFactory provideFxmlLoaderFactory(Map<Class<? extends FxController>, Provider<FxController>> factories, ResourceBundle resourceBundle) {
		return new FXMLLoaderFactory(factories, resourceBundle);
	}

	@Provides
	@PreferencesWindow
	@PreferencesScoped
	static Stage provideStage(ResourceBundle resourceBundle, @Named("windowIcon") Optional<Image> windowIcon) {
		Stage stage = new Stage();
		stage.setTitle(resourceBundle.getString("preferences.title"));
		stage.setResizable(false);
		windowIcon.ifPresent(stage.getIcons()::add);
		return stage;
	}

	@Provides
	@FxmlScene(FxmlFile.PREFERENCES)
	@PreferencesScoped
	static Scene providePreferencesScene(@PreferencesWindow FXMLLoaderFactory fxmlLoaders, @PreferencesWindow Stage window) {
		Scene scene = fxmlLoaders.createScene("/fxml/preferences.fxml");

		KeyCombination cmdW = new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
		scene.getAccelerators().put(cmdW, window::close);

		return scene;
	}

	// ------------------

	@Binds
	@IntoMap
	@FxControllerKey(PreferencesController.class)
	abstract FxController bindPreferencesController(PreferencesController controller);

	@Binds
	@IntoMap
	@FxControllerKey(GeneralPreferencesController.class)
	abstract FxController bindGeneralPreferencesController(GeneralPreferencesController controller);

	@Binds
	@IntoMap
	@FxControllerKey(UpdatesPreferencesController.class)
	abstract FxController bindUpdatesPreferencesController(UpdatesPreferencesController controller);

	@Binds
	@IntoMap
	@FxControllerKey(VolumePreferencesController.class)
	abstract FxController bindVolumePreferencesController(VolumePreferencesController controller);

}
