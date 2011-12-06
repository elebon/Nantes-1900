/**
 * 
 */
package fr.nantes1900.control.isletprocess;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import fr.nantes1900.constants.ActionTypes;
import fr.nantes1900.constants.Characteristics;
import fr.nantes1900.constants.TextsKeys;
import fr.nantes1900.models.extended.Surface;
import fr.nantes1900.models.islets.buildings.exceptions.InvalidCaseException;
import fr.nantes1900.utils.FileTools;
import fr.nantes1900.view.isletprocess.CharacteristicsStep3ElementsView;

/**
 * Characteristics panel for the second step of process of an islet. User can
 * select one or more triangles and modifies the type they belong to : building
 * or ground.
 * @author Camille
 * @author Luc
 */
public class CharacteristicsStep3ElementsController extends
        AbstractCharacteristicsSurfacesController {

    /**
     * Creates a new step 3 characteristics controller for surfaces selection
     * which will create the
     * panel and sets the action to perform when validate button is clicked.
     * @param parentController
     *            the parent controller
     * @param surfaceSelected
     *            the selected surface
     */
    public CharacteristicsStep3ElementsController(
            IsletProcessController parentController, Surface surfaceSelected) {
        super(parentController, surfaceSelected);

        this.cView = new CharacteristicsStep3ElementsView();
        modifyViewCharacteristics();

        this.cView.getValidateButton().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String typeChosen = ((CharacteristicsStep3ElementsView) CharacteristicsStep3ElementsController.this.cView)
                        .getTypeSelected();

                // gets the selected type
                int actionType = -1;
                switch (typeChosen)
                {
                    case Characteristics.TYPE_NOISE:
                        actionType = ActionTypes.TURN_TO_NOISE;
                    break;

                    case Characteristics.TYPE_BUILDING:
                        actionType = ActionTypes.TURN_TO_BUILDING;
                    break;
                }

                // launches actions
                for (Surface surface : surfacesList)
                {
                    try
                    {
                        CharacteristicsStep3ElementsController.this.parentController
                                .getBiController().action3(surface, actionType);
                    } catch (InvalidCaseException e)
                    {
                        JOptionPane
                                .showMessageDialog(
                                        CharacteristicsStep3ElementsController.this.cView,
                                        FileTools
                                                .readInformationMessage(
                                                        TextsKeys.KEY_ERROR_INCORRECTTYPE,
                                                        TextsKeys.MESSAGETYPE_MESSAGE),
                                        FileTools
                                                .readInformationMessage(
                                                        TextsKeys.KEY_ERROR_INCORRECTTYPE,
                                                        TextsKeys.MESSAGETYPE_TITLE),
                                        JOptionPane.ERROR_MESSAGE);
                    }
                }
                CharacteristicsStep3ElementsController.this.parentController
                        .refreshViews();
            }
        });
    }

    @Override
    public void modifyViewCharacteristics() {
        if (surfacesList.size() == 1)
        {
            try
            {
                ((CharacteristicsStep3ElementsView) this.cView)
                        .setType(parentController.getBiController()
                                .getCharacteristics3(surfacesList.get(0)));
            } catch (InvalidCaseException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else
        {
            ((CharacteristicsStep3ElementsView) this.cView).setType("");
        }
    }
}
