package org.eclipse.featuremodel.diagrameditor.features;

import org.eclipse.featuremodel.Group;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeService;
import org.eclipse.graphiti.util.IColorConstant;

/**
 * Feature handle adding to the diagram a graphical representation of an existing Group model
 * object.
 * 
 */
public class AddGroupFeature extends AbstractAddFeature {

    /**
     * Creates an instance of {@link AddGroupFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public AddGroupFeature(IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Checks whether a pictogram element of the given Feature Model object can be added to the
     * diagram.
     * 
     * @param context
     *            The context.
     * @return true if add is possible
     */
    @Override
    public boolean canAdd(IAddContext context) {
        // This feature supports to add a 'Group' object as a connection
        if (context instanceof IAddConnectionContext && context.getNewObject() instanceof Group) {
            return true;
        }
        return false;
    }

    /**
     * Adds a graphical representation of an existing Group model object to the diagram.
     * 
     * @param context
     *            The context.
     * @return The representation of the Group model object.
     */
    @Override
    public PictogramElement add(IAddContext context) {
        IAddConnectionContext addConContext = (IAddConnectionContext) context;
        Group group = (Group) context.getNewObject();

        IPeService peService = Graphiti.getPeService();
        IGaService gaService = Graphiti.getGaService();

        // Create the visualization of the Group as a polyline
        FreeFormConnection connection = peService.createFreeFormConnection(getDiagram());
        connection.setStart(addConContext.getSourceAnchor());
        connection.setEnd(addConContext.getTargetAnchor());
        connection.setActive(false);
        // create link and wire it
        link(connection, group);

        Polyline polyline = gaService.createPolyline(connection);
        polyline.setLineWidth(2);
        polyline.setForeground(manageColor(IColorConstant.BLACK));

        return connection;
    }
}
