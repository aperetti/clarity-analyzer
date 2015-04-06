package skadistats.clarity.analyzer.main;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.FileChooser;
import javafx.util.converter.NumberStringConverter;
import org.controlsfx.dialog.Dialogs;
import skadistats.clarity.analyzer.PrimaryStage;
import skadistats.clarity.analyzer.replay.ReplayController;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class MainWindowController implements Initializable {

    @FXML
    public Button buttonPlay;

    @FXML
    public Button buttonPause;

    @FXML
    public Slider slider;

    @FXML
    public Label labelTick;

    @FXML
    public Label labelLastTick;

    @Inject
    private PrimaryStage primaryStage;

    @Inject
    private Preferences preferences;

    @Inject
    private ReplayController replayController;

    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        BooleanBinding runnerIsNull = Bindings.createBooleanBinding(() -> replayController.getRunner() == null, replayController.runnerProperty());
        buttonPlay.disableProperty().bind(runnerIsNull.or(replayController.playingProperty()));
        buttonPause.disableProperty().bind(runnerIsNull.or(replayController.playingProperty().not()));
        slider.disableProperty().bind(runnerIsNull);

        labelTick.textProperty().bindBidirectional(replayController.tickProperty(), new NumberStringConverter());
        labelLastTick.textProperty().bindBidirectional(replayController.lastTickProperty(), new NumberStringConverter());

        slider.maxProperty().bindBidirectional(replayController.lastTickProperty());
        slider.valueProperty().addListener((observable, oldValue, newValue) -> replayController.getRunner().setDemandedTick(newValue.intValue()));
    }
    public void actionQuit(ActionEvent actionEvent) {
        primaryStage.getStage().close();
    }

    public void actionOpen(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load a replay");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Dota 2 replay files", "*.dem"),
            new FileChooser.ExtensionFilter("All files", "*")
        );
        File dir = new File(preferences.get("fileChooserPath", "."));
        if (!dir.isDirectory()) {
            dir = new File(".");
        }
        fileChooser.setInitialDirectory(dir);
        File f = fileChooser.showOpenDialog(primaryStage.getStage());
        if (f == null) {
            return;
        }
        preferences.put("fileChooserPath", f.getParent());
        try {
            replayController.load(f);
        } catch (Exception e) {
            Dialogs.create().title("Error loading replay").showException(e);
        }
    }


    public void clickPlay(ActionEvent actionEvent) {
        replayController.setPlaying(true);
    }

    public void clickPause(ActionEvent actionEvent) {
        replayController.setPlaying(false);

    }
}
