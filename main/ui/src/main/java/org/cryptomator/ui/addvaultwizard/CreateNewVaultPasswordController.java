package org.cryptomator.ui.addvaultwizard;

import dagger.Lazy;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.cryptomator.common.vaults.Vault;
import org.cryptomator.common.vaults.VaultListManager;
import org.cryptomator.cryptofs.CryptoFileSystemProperties;
import org.cryptomator.cryptofs.CryptoFileSystemProvider;
import org.cryptomator.ui.common.FxController;
import org.cryptomator.ui.common.FxmlFile;
import org.cryptomator.ui.common.FxmlScene;
import org.cryptomator.ui.common.Tasks;
import org.cryptomator.ui.controls.FontAwesome5IconView;
import org.cryptomator.ui.controls.NiceSecurePasswordField;
import org.cryptomator.ui.common.PasswordStrengthUtil;
import org.fxmisc.easybind.EasyBind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

import static java.nio.charset.StandardCharsets.US_ASCII;

@AddVaultWizardScoped
public class CreateNewVaultPasswordController implements FxController {

	private static final Logger LOG = LoggerFactory.getLogger(CreateNewVaultPasswordController.class);
	private static final String MASTERKEY_FILENAME = "masterkey.cryptomator"; // TODO: deduplicate constant declared in multiple classes

	private final Stage window;
	private final Lazy<Scene> chooseLocationScene;
	private final Lazy<Scene> successScene;
	private final ExecutorService executor;
	private final StringProperty vaultName;
	private final ObjectProperty<Path> vaultPath;
	private final ObjectProperty<Vault> vault;
	private final VaultListManager vaultListManager;
	private final ResourceBundle resourceBundle;
	private final PasswordStrengthUtil strengthRater;
	private final ReadmeGenerator readmeGenerator;
	private final IntegerProperty passwordStrength;
	private final BooleanProperty processing;
	private final BooleanProperty readyToCreateVault;
	private final ObjectBinding<ContentDisplay> createVaultButtonState;

	public NiceSecurePasswordField passwordField;
	public NiceSecurePasswordField reenterField;
	public Label passwordStrengthLabel;
	public HBox passwordMatchBox;
	public FontAwesome5IconView checkmark;
	public FontAwesome5IconView cross;
	public Label passwordMatchLabel;
	public CheckBox finalConfirmationCheckbox;

	@Inject
	CreateNewVaultPasswordController(@AddVaultWizardWindow Stage window, @FxmlScene(FxmlFile.ADDVAULT_NEW_LOCATION) Lazy<Scene> chooseLocationScene, @FxmlScene(FxmlFile.ADDVAULT_SUCCESS) Lazy<Scene> successScene, ExecutorService executor, StringProperty vaultName, ObjectProperty<Path> vaultPath, @AddVaultWizardWindow ObjectProperty<Vault> vault, VaultListManager vaultListManager, ResourceBundle resourceBundle, PasswordStrengthUtil strengthRater, ReadmeGenerator readmeGenerator) {
		this.window = window;
		this.chooseLocationScene = chooseLocationScene;
		this.successScene = successScene;
		this.executor = executor;
		this.vaultName = vaultName;
		this.vaultPath = vaultPath;
		this.vault = vault;
		this.vaultListManager = vaultListManager;
		this.resourceBundle = resourceBundle;
		this.strengthRater = strengthRater;
		this.readmeGenerator = readmeGenerator;
		this.passwordStrength = new SimpleIntegerProperty(-1);
		this.processing = new SimpleBooleanProperty();
		this.readyToCreateVault = new SimpleBooleanProperty();
		this.createVaultButtonState = Bindings.createObjectBinding(this::getCreateVaultButtonState, processing);
	}

	@FXML
	public void initialize() {
		//binds the actual strength value to the rating of the password util
		passwordStrength.bind(Bindings.createIntegerBinding(() -> strengthRater.computeRate(passwordField.getCharacters().toString()), passwordField.textProperty()));
		//binding indicating if the passwords not match
		BooleanBinding passwordsMatch = Bindings.createBooleanBinding(() -> CharSequence.compare(passwordField.getCharacters(), reenterField.getCharacters()) == 0, passwordField.textProperty(), reenterField.textProperty());
		BooleanBinding reenterFieldNotEmpty = reenterField.textProperty().isNotEmpty();
		readyToCreateVault.bind(reenterFieldNotEmpty.and(passwordsMatch).and(finalConfirmationCheckbox.selectedProperty()).and(processing.not()));
		//make match indicator invisible when passwords do not match or one is empty
		passwordMatchBox.visibleProperty().bind(reenterFieldNotEmpty);
		checkmark.visibleProperty().bind(passwordsMatch.and(reenterFieldNotEmpty));
		checkmark.managedProperty().bind(checkmark.visibleProperty());
		cross.visibleProperty().bind(passwordsMatch.not().and(reenterFieldNotEmpty));
		cross.managedProperty().bind(cross.visibleProperty());
		passwordMatchLabel.textProperty().bind(Bindings.when(passwordsMatch.and(reenterFieldNotEmpty)).then(resourceBundle.getString("addvaultwizard.new.passwordsMatch")).otherwise(resourceBundle.getString("addvaultwizard.new.passwordsDoNotMatch")));

		//bindsings for the password strength indicator
		passwordStrengthLabel.textProperty().bind(EasyBind.map(passwordStrength, strengthRater::getStrengthDescription));
	}

	@FXML
	public void back() {
		window.setScene(chooseLocationScene.get());
	}

	@FXML
	public void next() {
		Path pathToVault = vaultPath.get();
		
		try {
			Files.createDirectory(pathToVault);
		} catch (FileAlreadyExistsException e) {
			LOG.error("Vault dir already exists.", e);
			window.setScene(chooseLocationScene.get());
		} catch (IOException e) {
			// TODO show generic error screen
			LOG.error("", e);
		}

		processing.set(true);
		Tasks.create(() -> {
			initializeVault(pathToVault, passwordField.getCharacters());
		}).onSuccess(() -> {
			initializationSucceeded(pathToVault);
		}).onError(IOException.class, e -> {
			// TODO show generic error screen
			LOG.error("", e);
		}).andFinally(() -> {
			processing.set(false);
		}).runOnce(executor);
	}

	private void initializeVault(Path path, CharSequence passphrase) throws IOException {
		CryptoFileSystemProvider.initialize(path, MASTERKEY_FILENAME, passphrase);
		CryptoFileSystemProperties fsProps = CryptoFileSystemProperties.cryptoFileSystemProperties() //
				.withPassphrase(passphrase) //
				.withFlags(Collections.emptySet()) //
				.withMasterkeyFilename(MASTERKEY_FILENAME) //
				.build();

		String vaultReadmeFileName = resourceBundle.getString("addvault.new.readme.accessLocation.fileName");
		try (FileSystem fs = CryptoFileSystemProvider.newFileSystem(path, fsProps); // 
			 WritableByteChannel ch = Files.newByteChannel(fs.getPath("/", vaultReadmeFileName), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
			ch.write(US_ASCII.encode(readmeGenerator.createVaultAccessLocationReadmeRtf()));
		}

		String storagePathReadmeFileName = resourceBundle.getString("addvault.new.readme.storageLocation.fileName");
		try (WritableByteChannel ch = Files.newByteChannel(path.resolve(storagePathReadmeFileName), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
			ch.write(US_ASCII.encode(readmeGenerator.createVaultStorageLocationReadmeRtf()));
		}
		LOG.info("Created vault at {}", path);
	}
	
	private void initializationSucceeded(Path pathToVault) {
		try {
			Vault newVault = vaultListManager.add(pathToVault);
			vault.set(newVault);
			window.setScene(successScene.get());
		} catch (NoSuchFileException e) {
			throw new UncheckedIOException(e);
		}
	}

	/* Getter/Setter */

	public String getVaultName() {
		return vaultName.get();
	}

	public StringProperty vaultNameProperty() {
		return vaultName;
	}

	public IntegerProperty passwordStrengthProperty() {
		return passwordStrength;
	}

	public int getPasswordStrength() {
		return passwordStrength.get();
	}

	public BooleanProperty readyToCreateVaultProperty() {
		return readyToCreateVault;
	}

	public boolean isReadyToCreateVault() {
		return readyToCreateVault.get();
	}

	public ObjectBinding<ContentDisplay> createVaultButtonStateProperty() {
		return createVaultButtonState;
	}

	public ContentDisplay getCreateVaultButtonState() {
		return processing.get() ? ContentDisplay.LEFT : ContentDisplay.TEXT_ONLY;
	}
}
