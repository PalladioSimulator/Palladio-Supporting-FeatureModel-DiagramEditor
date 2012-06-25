package org.eclipse.featuremodel.diagrameditor.diagram;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.diagrameditor.features.DirectEditDoubleClickFeature;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IDoubleClickContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
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
     * Returns a feature which will be executed at at double click. For that purpose a custom
     * feature is used, because custom features appear in the context menu and the double click
     * feature should also appear in the context menu (usual UI guideline).
     * 
     * @param context
     *            contains information where the double click gesture has happened
     * @return the feature to execute
     */
    @Override
    public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
        Object bo = getFeatureProvider().getBusinessObjectForPictogramElement(context.getInnerPictogramElement());
        if (bo instanceof Feature) {
            return new DirectEditDoubleClickFeature(getFeatureProvider());
        }

        return super.getDoubleClickFeature(context);
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

}
