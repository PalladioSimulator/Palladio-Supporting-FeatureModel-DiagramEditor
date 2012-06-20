package de.fzi.se.eclipse.featuremodel.treeeditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.FeatureModel;
import org.eclipse.featuremodel.Group;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.AreaContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import de.fzi.se.eclipse.featuremodel.treeeditor.utilities.BOUtil;
import de.fzi.se.eclipse.featuremodel.treeeditor.utilities.IdGen;

/**
 * Feature handle adding to a diagram a graphical representation of an existing Feature Model
 * object.
 * 
 * @author Alexander Moor
 */
public class AddFeatureModelFeature extends AbstractAddFeature {

    /**
     * Creates an instance of {@link AddFeatureModelFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public AddFeatureModelFeature(IFeatureProvider fp) {
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
        // This feature supports to add a 'Feature Model' object directly into the diagram
        if (context.getNewObject() instanceof FeatureModel && context.getTargetContainer() instanceof Diagram) {
            return true;
        }
        return false;
    }

    /**
     * Adds a graphical representation of an existing Feature Model to the diagram.
     * 
     * @param context
     *            The context.
     * @return The root element of the Feature Model.
     */
    @Override
    public PictogramElement add(IAddContext context) {
        // Get information from context
        FeatureModel featureModel = (FeatureModel) context.getNewObject();
        Diagram diagram = (Diagram) context.getTargetContainer();
        // set name of the diagram
        // diagram.setName(featureModel.getId());

        link(diagram, featureModel);

        // The root Feature of the Feature Model diagram
        PictogramElement root = null;

        if (featureModel.getRoot() != null) {
            if (featureModel.getRoot().getId() == null || featureModel.getRoot().getId().isEmpty()) {
                featureModel.getRoot().setId(IdGen.generate());
            }
            AddContext addFeatureContext = new AddContext(new AreaContext(), featureModel.getRoot());
            addFeatureContext.setTargetContainer(diagram);
            root = addGraphicalRepresentation(addFeatureContext, featureModel.getRoot());

            createFeatureModelTree(featureModel.getRoot());
        }

        // call the layout feature
        layoutPictogramElement(diagram);

        for (Connection c : getDiagram().getConnections()) {
            this.updatePictogramElement(c);
        }

        // Return the root pictogram element
        return root;
    }

    /**
     * Help method to build recursive the Feature Model tree.
     * 
     * @param parentFeature
     *            The root Feature object.
     */
    private void createFeatureModelTree(Feature parentFeature) {
        ContainerShape parentContainer = (ContainerShape) this.getFeatureProvider()
                .getPictogramElementForBusinessObject(parentFeature);

        for (Group group : parentFeature.getChildren()) {
            if (group.getId() == null || group.getId().isEmpty()) {
                group.setId(IdGen.generate());
            }

            for (Feature feature : group.getFeatures()) {

                if (feature.getId() == null || feature.getId().isEmpty()) {
                    feature.setId(IdGen.generate());
                }

                // Create the visualization of the Feature.
                AddContext addFeatureContext = new AddContext(new AreaContext(), feature);
                addFeatureContext.setTargetContainer(parentContainer);
                addGraphicalRepresentation(addFeatureContext, feature);

                // Create the visualization of the Group
                Anchor sourceAnchor = BOUtil.getOutputAnchor(parentContainer);
                ContainerShape cs = BOUtil.getPictogramElementForBusinessObject(feature, ContainerShape.class,
                        getFeatureProvider());
                Anchor targetAnchor = BOUtil.getInputAnchor(cs);

                AddConnectionContext addGroupContext = new AddConnectionContext(sourceAnchor, targetAnchor);
                addGroupContext.setNewObject(group);
                getFeatureProvider().addIfPossible(addGroupContext);

                createFeatureModelTree(feature);
            }
        }
    }
}
