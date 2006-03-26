/*
 * Created on 05.05.2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package freemind.modes.mindmapmode.actions;

import java.awt.event.ActionEvent;
import java.util.ListIterator;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.ImageIcon;

import freemind.controller.actions.generated.instance.CompoundAction;
import freemind.controller.actions.generated.instance.XmlAction;
import freemind.modes.MindMapNode;
import freemind.modes.NodeAdapter;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapMapModel;
import freemind.modes.mindmapmode.MindMapNodeModel;
import freemind.modes.mindmapmode.actions.xml.AbstractXmlAction;
import freemind.modes.mindmapmode.actions.xml.ActionPair;

public class NodeGeneralAction extends AbstractXmlAction {
    protected final MindMapController modeController;

    private freemind.modes.mindmapmode.actions.NodeActorXml actor;

    SingleNodeOperation singleNodeOperation;

    protected static Logger logger;

    /**
     * @param modeController
     * @param textID
     *           null if you cannot provide a title that is present in the
     *           resources. Use the setName method to set your not translateble
     *           title after that.
     * @param iconPath
     *           give a resource name for the icon.
     */
    protected NodeGeneralAction(MindMapController modeController,
            final String textID, String iconPath) {
        super(null, iconPath != null ? new ImageIcon(modeController
                .getResource(iconPath)) : null, modeController);
        this.modeController = modeController;
        if (textID != null) {
            setName(modeController.getText(textID));
        }

        this.singleNodeOperation = null;
        this.actor = null;
        if (logger == null) {
            logger = modeController.getFrame().getLogger(
                    this.getClass().getName());
        }
    }

    protected void setName(String name) {
        if (name != null) {
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, name);
        }

    }

    public NodeGeneralAction(MindMapController modeController, String textID,
            String iconPath, SingleNodeOperation singleNodeOperation) {
        this(modeController, textID, iconPath);
        this.singleNodeOperation = singleNodeOperation;
    }

    public NodeGeneralAction(MindMapController modeController, String textID,
            String iconPath, freemind.modes.mindmapmode.actions.NodeActorXml actor) {
        this(modeController, textID, iconPath);
        addActor(actor);
    }

    public void addActor(NodeActorXml actor) {
        this.actor = actor;
        if (actor != null) {
            super.addActor(actor);
        }
    }

    /**
     * @param singleNodeOperation
     *           The singleNodeOperation to set.
     */
    public void setSingleNodeOperation(SingleNodeOperation singleNodeOperation) {
        this.singleNodeOperation = singleNodeOperation;
    }

    public void xmlActionPerformed(ActionEvent e) {
        if (singleNodeOperation != null) {
            for (ListIterator it = modeController.getSelecteds().listIterator(); it
                    .hasNext();) {
                MindMapNodeModel selected = (MindMapNodeModel) it.next();
                singleNodeOperation.apply((MindMapMapModel) this.modeController
                        .getMap(), selected);
            }
        } else {
            // xml action:
            // Do-action
            CompoundAction doAction = new CompoundAction();
            // Undo-action
            CompoundAction undo = new CompoundAction();
            // sort selectedNodes list by depth, in order to guarantee that
            // sons are deleted first:
            for (ListIterator it = modeController.getSelecteds()
                    .listIterator(); it.hasNext();) {
                MindMapNodeModel selected = (MindMapNodeModel) it.next();
                ActionPair pair = actor.apply(this.modeController.getMap(),
                        selected);
                if (pair != null) {
                    doAction
                            
                            .addChoice(pair.getDoAction());
                    undo
                            .addAtChoice(0, pair.getUndoAction());
                }
            }
            modeController.getActionFactory().startTransaction(
                    (String) getValue(NAME));
            modeController.getActionFactory().executeAction(
                    new ActionPair(doAction, undo));
            modeController.getActionFactory().endTransaction(
                    (String) getValue(NAME));
        }

    }

    protected void execute(ActionPair pair) {
        modeController.getActionFactory().executeAction(pair);
    }

    /*
     * (non-Javadoc)
     * 
     * @see freemind.controller.actions.FreeMindAction#act(freemind.controller.actions.generated.instance.XmlAction)
     */
    public void act(XmlAction action) {
    }

    /**
     * @param string
     * @return
     */
    protected NodeAdapter getNodeFromID(String string) {
        return modeController.getNodeFromID(string);
    }

    /**
     * @param selected
     * @return
     */
    protected String getNodeID(MindMapNode selected) {
        // TODO Auto-generated method stub
        return modeController.getNodeID(selected);
    }


}