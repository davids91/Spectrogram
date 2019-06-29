    package spectrogram;

    import javafx.fxml.FXML;
    import javafx.scene.control.*;
    import javafx.scene.image.ImageView;
    import javafx.stage.FileChooser;
    import spectrogram_models.Global;
    import spectrogram_services.PlaylistHandler;
    import spectrogram_services.VariantTabHandler;

    import java.io.*;
    import java.util.ArrayList;
    import java.util.Random;
    import java.util.prefs.Preferences;

    public class Controller {

        PlaylistHandler plHandler = new PlaylistHandler();

        public ImageView imgDisplay;
        public Button openDefaultBtn;
        public Label playlistNameLabel;
        public Button setPlayListBtn;
        public Button makeDefBtn;
        public Button addVariantBtn;
        public TabPane variantTabPane;
        public TextField newVariantText;

        /* TODO: Loading Screen */
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
                    plHandler.openPlaylist(defPlayList);
                    playlistValidUpdateUI();
                } catch (IllegalStateException e) {
                   userPref.put("defaultPlayList","");/* Default Playlist is invalid! Let's delete it */
                    e.printStackTrace();
                }
            } else playlistInvalidUpdateUI();
        }

        @FXML
        private void addNewVariant()
        {
            if(
                (null != plHandler)
                &&(PlaylistHandler.Validity.emptyList.ordinal() <= plHandler.isPlaylistValid().ordinal())
            ){
                if(plHandler.addVariant(newVariantText.getText()))reloadVariants();
                else playlistInvalidUpdateUI();
            } /* else playlistHandler is in an invalid state */
        }

        private void openPlaylist(File playlist) throws IllegalStateException {
            if(null != playlist)
            {
                if(plHandler.openPlaylist(playlist))
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
            File resultFile = fileChooser.showOpenDialog(Global.getStage());
            if((null != resultFile)&&(resultFile.exists())){
                plHandler.closePlaylist();
                try {
                    openPlaylist(resultFile);
                } catch (IllegalStateException e) {
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
            } catch (IllegalStateException e) {
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
            File resultFile = fileChooser.showSaveDialog(Global.getStage());

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
                    plHandler.openPlaylist(resultFile);
                    playlistValidUpdateUI();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }else throw new IOException("Unable to create new playlist file!");
        }

        private void reloadVariants(){
            if(PlaylistHandler.Validity.unknownFormat.ordinal() < plHandler.isPlaylistValid().ordinal())
            {
                /* Fill up the tabPane with the variants from the playlist */
                addVariantBtn.setVisible(true);
                newVariantText.setVisible(true);
                variantTabPane.getTabs().clear();
                variantTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
                ArrayList<String> variants = null;
                try {
                    variants = plHandler.getPlaylistVariants();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

                for(String variant : variants)
                {
                    System.out.println("Adding Variant: " + variant);
                    VariantTabHandler tabHandler = new VariantTabHandler(plHandler,variant);
                    variantTabPane.getTabs().add(tabHandler.getTab());
                    variantTabPane.getSelectionModel().selectLast();
                }
            }
        }

        private void playlistValidUpdateUI()
        {
            if(PlaylistHandler.Validity.unknownFormat.ordinal() < plHandler.isPlaylistValid().ordinal())
            {
                try {
                    playlistNameLabel.setText(plHandler.getPlayListName());
                    if(
                        (PlaylistHandler.Validity.emptyList.ordinal() <= plHandler.isPlaylistValid().ordinal())
                            &&(plHandler.getPlayListPath().equalsIgnoreCase(defaultPlaylistPath))
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

                } catch (IllegalStateException e) {
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
            if(PlaylistHandler.Validity.notAFile.ordinal() < plHandler.isPlaylistValid().ordinal())
            {
                try {
                    String path = plHandler.getPlayListPath();
                    userPref.put("defaultPlayList", path);
                    defaultPlaylistPath = path;
                    playlistValidUpdateUI();
                } catch (IllegalStateException e) {
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
