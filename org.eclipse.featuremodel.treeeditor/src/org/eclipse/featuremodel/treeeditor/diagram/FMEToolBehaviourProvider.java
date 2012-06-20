package org.eclipse.featuremodel.treeeditor.diagram;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.tb.ContextMenuEntry;
import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
import org.eclipse.graphiti.tb.IContextMenuEntry;

/**
 * {@link FMEToolBehaviourProvider} is needed to integrate into the standard workbench tools. It
 * influence e.g., what will be displayed in the palette of the editor, how selection and double
 * clicking is handled, that some special rendering is necessary for certain objects, how zooming is
 * handled and so on.
 * 
 * @author Alexander Moor
 */
public class FMEToolBehaviourProvider extends DefaultToolBehaviorProvider {

    /**
     * Creates an instance of {@link FMEToolBehaviourProvider}.
     * 
     * @param diagramTypeProvider
     *            The diagram type provider
     */
    public FMEToolBehaviourProvider(IDiagramTypeProvider diagramTypeProvider) {
        super(diagramTypeProvider);
    }

    /**
     * Returns the context menu for the current mouse location.
     * 
     * @param context
     *            The custom context which contains the info about the location where the context
     *            menu appears.
     * @return the context menu.
     */
    @Override
    public IContextMenuEntry[] getContextMenu(ICustomContext context) {

        // create a sub-menu
        ContextMenuEntry subMenu = new ContextMenuEntry(null, context);
        subMenu.setSubmenu(false);

        // gets all custom features for given context
        ICustomFeature[] customFeatures = getFeatureProvider().getCustomFeatures(context);

        // create a menu-entry in the sub-menu for each available custom feature
        for (int i = 0; i < customFeatures.length; i++) {
            ICustomFeature customFeature = customFeatures[i];
            if (customFeature.isAvailable(context)) {
                ContextMenuEntry menuEntry = new ContextMenuEntry(customFeature, context);
                subMenu.add(menuEntry);
            }
        }

        return new IContextMenuEntry[] { subMenu };
    }

    /**
     * Indicates if the selection of connections is enabled. This implementation returns
     * <code>false</code> and tells the framework that the selection of connections is disabled.
     * 
     * @return <code>false</code>
     */
    @Override
    public boolean isConnectionSelectionEnabled() {
        return true;
    }

    /**
     * Changes the default selection on mouse click. The selection of Group pictogram elements (e.g
     * relationship figure) is disabled.
     * 
     * @param originalPe
     *            The currently selected pictogram element.
     * @param oldSelection
     *            The pictogram elements selected befor.
     * 
     * @return return null if there should not be a special selection behavior; return the diagram
     *         if there is a Group pictogram element is selected.
     */
    @Override
    public PictogramElement getSelection(PictogramElement originalPe, PictogramElement[] oldSelection) {
        // Object bo = this.getFeatureProvider().getBusinessObjectForPictogramElement(originalPe);
        // if (bo instanceof Group) {
        // return this.getDiagramTypeProvider().getDiagram();
        // }
        return super.getSelection(originalPe, oldSelection);
    }

    /**
     * Indicates if the selection tool entry is shown in the palette. This implementation returns
     * <code>false</code> and tells the framework to do not show the selection tool entry in the
     * palette.
     * 
     * @return <code>false</code>
     */
    // @Override
    // public boolean isShowSelectionTool() {
    // return false;
    // }

    /**
     * Indicates if the marquee tool entry is shown in the palette. This implementation returns
     * <code>false</code> and tells the framework to do not show the marquee tool entry in the
     * palette.
     * 
     * @return <code>false</code>
     */
    // @Override
    // public boolean isShowMarqueeTool() {
    // return false;
    // }

}
