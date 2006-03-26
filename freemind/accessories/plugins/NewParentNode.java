/*
 * Created on 19.04.2004
 *
 */
package accessories.plugins;

import java.awt.datatransfer.Transferable;
import java.util.Iterator;
import java.util.List;

import freemind.modes.MindMapNode;
import freemind.modes.mindmapmode.hooks.MindMapNodeHookAdapter;

/**
 * @author foltin
 * The original version was sent by Stephen Viles (sviles)
 * https://sourceforge.net/tracker/?func=detail&atid=307118&aid=881217&group_id=7118
 *
 *Initial Comment:
 *The "New Parent Node" action creates a node as a parent of one or more selected nodes.  
 *If more than one node is selected, the selected nodes must all have the same parent -- 
 *this restriction is imposed to make the action easier to understand and to undo manually,
 * and could potentially be removed when we get automated undo.  
 * 
 * The root node must not be one of the selected nodes.
 *I find this action useful when I need to add an extra level of grouping 
 *in the middle of an existing hierarchy.  It is quicker than adding a new
 * node at the same level and then cutting-and-pasting the child nodes. 
 *  The code simply performs these actions in sequence, after validating 
 * the selected nodes.
 */
public class NewParentNode extends MindMapNodeHookAdapter {

	/**
	 * 
	 */
	public NewParentNode() {
		super();
	}

	/* (non-Javadoc)
	 * @see freemind.extensions.NodeHook#invoke(freemind.modes.MindMapNode, java.util.List)
	 */
	public void invoke(MindMapNode rootNode) {
		// we dont need node. 
		MindMapNode focussed = getMindMapController().getSelected();
		List selecteds = getMindMapController().getSelecteds();
		MindMapNode selectedNode = focussed;
		List selectedNodes = selecteds;
		
		// bug fix: sort to make independent by user's selection:
		getMindMapController().sortNodesByDepth(selectedNodes);

		if(focussed.isRoot()) {
			getMindMapController().getController().errorMessage(
					getResourceString("cannot_add_parent_to_root"));
			return;
		}

		// Make sure the selected nodes all have the same parent
		// (this restriction is to simplify the action, and could
		//    possibly be removed in the future, when we have undo)
		// Also make sure that none of the selected nodes are the root node
		MindMapNode selectedParent = selectedNode.getParentNode();
		for (Iterator it = selectedNodes.iterator(); it.hasNext();) {
			MindMapNode node = (MindMapNode) it.next();
			if (node.getParentNode() != selectedParent) {
				getMindMapController().getController().errorMessage(
					getResourceString("cannot_add_parent_diff_parents"));
				return;
			}
			if (node == rootNode) {
				getMindMapController().getController().errorMessage(
					getResourceString("cannot_add_parent_to_root"));
				return;
			}
		}

		// Create new node in the position of the selectedNode
		int childPosition = selectedParent.getChildPosition(selectedNode);
		MindMapNode newNode = getMindMapController().addNewNode(selectedParent, childPosition, selectedNode.isLeft());
		//MindMapNode newNode = getMindMapController().newNode();
		//getMap().insertNodeInto(newNode, selectedParent, childPosition);

		// Move selected nodes to become children of new node
		Transferable copy = getMindMapController().cut(selectedNodes);
		getMindMapController().paste(copy, newNode);
		nodeChanged(selectedParent);

		// Start editing new node
		getMindMapController().getView().selectAsTheOnlyOneSelected(
			newNode.getViewer());
		getMindMapController().getFrame().repaint();
		// edit is not necessary, as the new text can directly entered and editing is starting automatically.
		//getMindMapController().edit(newNode.getViewer(), selectedParent.getViewer(), null, false, false, false);
	}

}
