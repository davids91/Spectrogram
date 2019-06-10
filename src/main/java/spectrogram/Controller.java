    package spectrogram;

    import javafx.fxml.FXML;
    import javafx.scene.control.*;
    import javafx.scene.image.ImageView;
    import javafx.stage.FileChooser;
    import spectrogram_models.InvalidPlaylistException;
    import spectrogram_models.PlaylistOverrideException;
    import spectrogram_services.PlaylistHandler;
    import spectrogram_services.VariantTabHandler;

    import java.io.*;
    import java.util.ArrayList;
    import java.util.Random;
    import java.util.prefs.Preferences;

    public class Controller {
        public ImageView imgDisplay;
        public Button openDefaultBtn;
        public Label playlistNameLabel;
        public Button setPlayListBtn;
        public Button makeDefBtn;
        public Button addVariantBtn;
        public TabPane variantTabPane;
        public TextField newVariantText;

        private final Preferences userPref = Preferences.userNodeForPackage(Controller.class);

        private String playlistPath = userPref.get("defaultPlayList", "");
        private String defaultPlaylistPath = userPref.get("defaultPlayList", "");
        private final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Salsa Assistant Playlist(*.sap)", "*.sap");

        public void initialize()
        {
            /* Try to load in default playlist */
            File defPlayList = new File(playlistPath);
            if(
                (!playlistPath.isEmpty())
                    &&(defPlayList.exists())
            ) { /* Default playlist exists */
                try {
                    Global.plHandler.openPlaylist(defPlayList);
                    playlistValidUpdateUI();
                } catch (InvalidPlaylistException e) {
                   userPref.put("defaultPlayList","");/* Default Playlist is invalid! Let's delete it */
                    e.printStackTrace();
                }
            } else playlistInvalidUpdateUI();
        }

        @FXML
        private void addNewVariant()
        {
            if(
                (null != Global.plHandler)
                &&(PlaylistHandler.Validity.emptyList.ordinal() <= Global.plHandler.isPlaylistValid().ordinal())
            ){
                if(Global.plHandler.addVariant(newVariantText.getText()))reloadVariants();
                else playlistInvalidUpdateUI();
            } /* else playlistHandler is in an invalid state */
        }

        private void openPlaylist(File playlist) throws InvalidPlaylistException, PlaylistOverrideException {
            if(null != playlist)
            {
                if(Global.plHandler.openPlaylist(playlist))
                    playlistValidUpdateUI();
                else playlistInvalidUpdateUI();
            }
        }

        @FXML
        void openExistingPlayList()
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Playlist");
            fileChooser.getExtensionFilters().add(extFilter);
            File resultFile = fileChooser.showOpenDialog(Global.primaryStage);
            if((null != resultFile)&&(resultFile.exists())){
                Global.plHandler.closePlaylist();
                try {
                    openPlaylist(resultFile);
                } catch (InvalidPlaylistException | PlaylistOverrideException e) {
                    playlistInvalidUpdateUI();
                    e.printStackTrace();
                }
            }
        }

        @FXML
        void openDefaultPlaylist()
        {
            File resultFile = new File(defaultPlaylistPath);
            try {
                openPlaylist(resultFile);
            } catch (InvalidPlaylistException | PlaylistOverrideException e) {
                playlistInvalidUpdateUI();
                e.printStackTrace();
            }

        }

        @FXML
        void createNewPlayList() throws IOException
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Create Playlist File");
            fileChooser.getExtensionFilters().add(extFilter);
            File resultFile = fileChooser.showSaveDialog(Global.primaryStage);

            System.out.println("Playlist file is: " + resultFile.getPath());
            if( (null != resultFile) /* Cancel is pressed */
                &&(
                    (resultFile.exists()) /* File exists */
                    ||(
                        (!resultFile.exists()) /* Or can be created */
                        &&(resultFile.createNewFile())
                    )
                )
            ){
                try {
                    Global.plHandler.openPlaylist(resultFile);
                    playlistValidUpdateUI();
                } catch (InvalidPlaylistException e) {
                    e.printStackTrace();
                }
            }else throw new IOException("Unable to create new playlist file!");
        }

        private void reloadVariants(){
            if(PlaylistHandler.Validity.unknownFormat.ordinal() < Global.plHandler.isPlaylistValid().ordinal())
            {
                /* Fill up the tabPane with the variants from the playlist */
                addVariantBtn.setVisible(true);
                newVariantText.setVisible(true);
                variantTabPane.getTabs().clear();
                variantTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
                ArrayList<String> variants = null;
                try {
                    variants = Global.plHandler.getPlaylistVariants();
                } catch (InvalidPlaylistException e) {
                    e.printStackTrace();
                }

                variantTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
                    /* TODO: Load in Controller for the playlist */
                    if(null != newTab) /* If there the new Selected tab exists */
                        System.out.println("Selected" + newTab.getText());
                });

                for(String variant : variants)
                {
                    System.out.println("Adding Variant: " + variant);
                    VariantTabHandler tab = new VariantTabHandler(Global.plHandler,variant);
                    tab.setId("variant" + new Random().nextInt());
                    variantTabPane.getTabs().add(tab);
                    variantTabPane.getSelectionModel().selectLast();
                }
            }
        }

        private void playlistValidUpdateUI()
        {
            if(PlaylistHandler.Validity.unknownFormat.ordinal() < Global.plHandler.isPlaylistValid().ordinal())
            {
                try {
                    playlistNameLabel.setText(Global.plHandler.getPlayListName());
                    if(
                        (PlaylistHandler.Validity.emptyList.ordinal() <= Global.plHandler.isPlaylistValid().ordinal())
                            &&(Global.plHandler.getPlayListPath().equalsIgnoreCase(defaultPlaylistPath))
                    ) { /* Default Playlist equals with the opened one */
                        makeDefBtn.setDisable(true);
                        openDefaultBtn.setDisable(true);
                    }
                    else
                    {
                        makeDefBtn.setDisable(false);
                        openDefaultBtn.setDisable(false);
                    }

                    reloadVariants();

                } catch (InvalidPlaylistException e) {
                    e.printStackTrace();
                    openDefaultBtn.setDisable(true);
                    playlistInvalidUpdateUI();
                }
            }
            else
            {
                playlistInvalidUpdateUI();
            }
        }

        private void playlistInvalidUpdateUI()
        {
            playlistNameLabel.setText("<< Invalid Playlist >>");
            openDefaultBtn.setDisable(true);
            makeDefBtn.setDisable(true);
            addVariantBtn.setVisible(false);
            newVariantText.setVisible(false);
            variantTabPane.getTabs().removeAll();
        }

        @FXML
        void setDefaultPlaylist()
        {
            if(PlaylistHandler.Validity.notAFile.ordinal() < Global.plHandler.isPlaylistValid().ordinal())
            {
                try {
                    String path = Global.plHandler.getPlayListPath();
                    userPref.put("defaultPlayList", path);
                    defaultPlaylistPath = path;
                    playlistValidUpdateUI();
                } catch (InvalidPlaylistException e) {
                    playlistInvalidUpdateUI();
                    e.printStackTrace();
                }
            }
            else
            {
                Alert alert = new Alert( Alert.AlertType.ERROR);
                alert.setContentText("Invalid playlist!");
                alert.showAndWait();//.filter((response) -> response == ButtonType.OK);
            }
        }
    }
