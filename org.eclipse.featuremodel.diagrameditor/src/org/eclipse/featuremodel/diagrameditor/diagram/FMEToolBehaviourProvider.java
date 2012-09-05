package org.eclipse.featuremodel.diagrameditor.diagram;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.FeatureModel;
import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.diagrameditor.features.CollapseFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.DirectEditDoubleClickFeature;
import org.eclipse.featuremodel.diagrameditor.features.ExpandFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.LayoutDiagramActionFeature;
import org.eclipse.featuremodel.diagrameditor.features.SetMandatoryRelationTypeFeature;
import org.eclipse.featuremodel.diagrameditor.features.SetORRelationTypeFeature;
import org.eclipse.featuremodel.diagrameditor.features.SetOptionalRelationTypeFeature;
import org.eclipse.featuremodel.diagrameditor.features.SetXORRelationTypeFeature;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IDoubleClickContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.palette.IPaletteCompartmentEntry;
import org.eclipse.graphiti.palette.impl.ObjectCreationToolEntry;
import org.eclipse.graphiti.palette.impl.PaletteCompartmentEntry;
import org.eclipse.graphiti.tb.ContextMenuEntry;
import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
import org.eclipse.graphiti.tb.IContextMenuEntry;

/**
 * {@link FMEToolBehaviourProvider} is needed to integrate into the standard workbench tools. It
 * influence e.g., what will be displayed in the palette of the editor, how selection and double
 * clicking is handled, that some special rendering is necessary for certain objects, how zooming is
 * handled and so on.
 * 
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
        List<IContextMenuEntry> menuList = new ArrayList<IContextMenuEntry>();

        if (context.getPictogramElements() != null) {
            for (PictogramElement pictogramElement : context.getPictogramElements()) {
                Object bo = getFeatureProvider().getBusinessObjectForPictogramElement(pictogramElement);
                if (bo == null) {
                    continue;
                } else if (bo instanceof FeatureModel) {
                    // context menu to layout the diagram
                    ContextMenuEntry menuEntry = new ContextMenuEntry(new LayoutDiagramActionFeature(
                            getFeatureProvider()), context);
                    menuList.add(menuEntry);
                } else if (bo instanceof Group) {
                    // context menus to set/change Group relation
                    ContextMenuEntry subMenu = new ContextMenuEntry(null, context);
                    subMenu.setSubmenu(false);
                    menuList.add(subMenu);
                    // menu to set optional relation
                    ContextMenuEntry menuEntry = new ContextMenuEntry(new SetOptionalRelationTypeFeature(
                            getFeatureProvider()), context);
                    subMenu.add(menuEntry);
                    // menu to set mandatory relation
                    menuEntry = new ContextMenuEntry(new SetMandatoryRelationTypeFeature(getFeatureProvider()), context);
                    subMenu.add(menuEntry);
                    // menu to set OR relation
                    menuEntry = new ContextMenuEntry(new SetORRelationTypeFeature(getFeatureProvider()), context);
                    subMenu.add(menuEntry);
                    // menu to set XOR relation
                    menuEntry = new ContextMenuEntry(new SetXORRelationTypeFeature(getFeatureProvider()), context);
                    subMenu.add(menuEntry);
                } else if (bo instanceof Feature) {
                    // context menu to collapse/expand Feature children
                    ContextMenuEntry subMenu = new ContextMenuEntry(null, context);
                    subMenu.setSubmenu(false);
                    menuList.add(subMenu);
                    // menu to collapse a Feature
                    ContextMenuEntry menuEntry = new ContextMenuEntry(new CollapseFeatureFeature(getFeatureProvider()),
                            context);
                    subMenu.add(menuEntry);
                    // menu to expand a Feature
                    menuEntry = new ContextMenuEntry(new ExpandFeatureFeature(getFeatureProvider()), context);
                    subMenu.add(menuEntry);
                }

            }
        }

        return menuList.toArray(new IContextMenuEntry[menuList.size()]);
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
     * Gets the diagram palette with tools to create Feature Diagrams.
     * 
     * @return the palette entries
     */
    @Override
    public IPaletteCompartmentEntry[] getPalette() {
        List<IPaletteCompartmentEntry> compartments = new ArrayList<IPaletteCompartmentEntry>();

        PaletteCompartmentEntry compartmentEntry = new PaletteCompartmentEntry("Objects", null);
        compartments.add(compartmentEntry);

        // creates tools to create Features
        ICreateFeature[] createFeatures = getFeatureProvider().getCreateFeatures();

        for (ICreateFeature createFeature : createFeatures) {
            ObjectCreationToolEntry objectCreationToolEntry = new ObjectCreationToolEntry(
                    createFeature.getCreateName(), createFeature.getCreateDescription(),
                    createFeature.getCreateImageId(), createFeature.getCreateLargeImageId(), createFeature);

            compartmentEntry.addToolEntry(objectCreationToolEntry);

        }

        return compartments.toArray(new IPaletteCompartmentEntry[compartments.size()]);
    }
}
