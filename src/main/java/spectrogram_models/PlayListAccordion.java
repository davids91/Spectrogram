package spectrogram_models;

import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;

public class PlayListAccordion extends Accordion {

    private int selectedIndex = -1;

    /* TODO: Drag&&Drop rearranging the songs */
    public PlayListAccordion(){
        this.expandedPaneProperty().addListener((observable, oldValue, newValue) -> {
            if(null != newValue){
                int idx = 0;
                for(TitledPane whytho: this.getPanes()){
                    if(!whytho.equals(newValue))idx++;
                        else break;
                }

                selectedIndex = idx;
            }else selectedIndex = -1;
        });
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public int getNextSelectableIndex() {
        if(getSelectedIndex() < getPanes().size()-1)
            return selectedIndex+1;
        else return 0;
    }

    public int getPrevSelectableIndex() {
        if(0 < getSelectedIndex())
            return selectedIndex-1;
        else return getPanes().size()-1;
    }
}
