package de.fzi.se.eclipse.featuremodel.treeeditor;

import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.part.EditorActionBarContributor;

/**
 * This class is responsible for managing the installation and removal of global menus, menu items,
 * and toolbar buttons for a editor.
 * 
 * @author Alexander Moor
 */
public class FMEEditorActionBarContributor extends EditorActionBarContributor {

    /**
     * Contributes to the given tool bar. This implementation adds a combo box tool for zooming
     * functionality.
     * 
     * @param toolBarManager
     *            the manager that controls the workbench tool bar
     */
    @Override
    public void contributeToToolBar(final IToolBarManager toolBarManager) {
        super.contributeToToolBar(toolBarManager);

        String[] zoomStrings = new String[] { ZoomManager.FIT_ALL, ZoomManager.FIT_HEIGHT, ZoomManager.FIT_WIDTH };
        toolBarManager.add(new ZoomComboContributionItem(getPage(), zoomStrings));

    }
}
