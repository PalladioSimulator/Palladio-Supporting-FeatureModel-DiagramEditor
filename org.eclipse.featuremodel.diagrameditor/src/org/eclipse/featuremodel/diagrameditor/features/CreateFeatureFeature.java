package org.eclipse.featuremodel.diagrameditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.FeatureModel;
import org.eclipse.featuremodel.FeatureModelFactory;
import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.diagrameditor.FMEDiagramEditorUtil;
import org.eclipse.featuremodel.diagrameditor.utilities.BOUtil;
import org.eclipse.featuremodel.diagrameditor.utilities.BOUtil.RelationType;
import org.eclipse.featuremodel.diagrameditor.utilities.IdGen;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;

/**
 * Feature handle creating a new Feature model object.
 * 
 * @author Alexander Moor
 */
public class CreateFeatureFeature extends AbstractCreateFeature {

    /**
     * Creates an instance of {@link CreateFeatureFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public CreateFeatureFeature(IFeatureProvider fp) {
        super(fp, "Feature", "Create a new feature");
    }

    /**
     * Checks if a Feature Model object can be created for the given context.
     * 
     * @param context
     *            The context.
     * @return true if create is possible
     */
    @Override
    public boolean canCreate(ICreateContext context) {
        Shape targetContainer = context.getTargetContainer();
        Connection targetConnection = context.getTargetConnection();
        Object bo;
        // check whether the new Feature is wanted to be added to a connection.
        if (targetConnection != null) {
            bo = this.getFeatureProvider().getBusinessObjectForPictogramElement(targetConnection);
        } else {
            bo = this.getFeatureProvider().getBusinessObjectForPictogramElement(targetContainer);
        }

        // if the diagram is empty is the new Feature a root
        // else the new Feature can only be added to an existing Feature or Group
        if (this.getDiagram().getChildren().isEmpty()) {
            return true;
        } else if (bo instanceof Feature) {
            return true;
        } else if (bo instanceof Group) {
            return true;
        }

        return false;
    }

    /**
     * Creates a new Feature model object and initiate the adding a graphical representation to the
     * diagram.
     * 
     * @param context
     *            The context.
     * @return The new Feature model object.
     */
    @Override
    public Object[] create(ICreateContext context) {
        // Create new Feature model object
        Feature newFeature = FeatureModelFactory.eINSTANCE.createFeature();
        newFeature.setId(IdGen.generate());
        FMEDiagramEditorUtil.saveToModelFile(newFeature, getDiagram());
        // root Feature
        if (this.getDiagram().getChildren().isEmpty()) {
            FeatureModel fm = (FeatureModel) getFeatureProvider().getBusinessObjectForPictogramElement(getDiagram());
            fm.setRoot(newFeature);
        }

        // Add a graphical representation of new Feature model object.
        PictogramElement newFeaturePE = addGraphicalRepresentation(context, newFeature);

        Shape targetContainer = context.getTargetContainer();
        Connection targetConnection = context.getTargetConnection();
        Object parent;

        // check whether the new Feature is wanted to be added to a connection.
        if (targetConnection != null) {
            parent = this.getFeatureProvider().getBusinessObjectForPictogramElement(targetConnection);
        } else {
            parent = this.getFeatureProvider().getBusinessObjectForPictogramElement(targetContainer);
        }

        // if the new Feature is wanted to be added as child of an existing Feature create Group
        if (parent instanceof Feature || parent instanceof Group) {
            createGroup(parent, newFeature, context);
        }

        // call the layout feature
        layoutPictogramElement(newFeaturePE);
        // activate Feature name editing
        getFeatureProvider().getDirectEditingInfo().setActive(true);

        // Update the graphical representation of the Group relation type (e.g. mandatory,
        // optional)
        if (newFeature.getParentGroup() != null) {
            Connection connection = BOUtil.getPictogramElementForBusinessObject(newFeature.getParentGroup(),
                    Connection.class, getFeatureProvider());
            updatePictogramElement(connection);
        }

        return new Object[] { newFeature };
    }

    /**
     * Creates the new Group or add new Feature to an existing Group.
     * 
     * @param parent
     *            The parent the new Feature is added to.
     * @param newFeature
     *            The new Feature.
     * @param context
     *            The context;
     */
    private void createGroup(Object parent, Feature newFeature, ICreateContext context) {
        // if the new Feature a child of an existing Feature create Group
        if (parent instanceof Feature) {
            Feature parentFeature = (Feature) parent;

            // Create a new Group model object.
            Group group = FeatureModelFactory.eINSTANCE.createGroup();
            group.setId(IdGen.generate());
            // context.getTargetContainer().eResource().getContents().add(group);
            FMEDiagramEditorUtil.saveToModelFile(group, getDiagram());
            // Add the new Group to the children of parent Feature
            parentFeature.getChildren().add(group);
            // Add the new Feature to the new Group
            group.getFeatures().add(newFeature);

            // Add a graphical representation of Group model object.
            addGroup(group, newFeature, parentFeature);
        } else if (parent instanceof Group) { // Add to the the existing Group.
            Group group = (Group) parent;

            // before adding update set relation notation
            RelationType relType = BOUtil.getRelationType(group);
            if (RelationType.Mandatory.equals(relType)) {
                BOUtil.setRelationType(group, RelationType.XOR);
            } else {
                BOUtil.setRelationType(group, RelationType.OR);
            }

            // Add the new Feature to the existing Group
            group.getFeatures().add(newFeature);

            // Get the parent Feature
            Feature parentFeature = group.getFeatures().get(0).getParent();
            // Add a graphical representation of Group model object.
            addGroup(group, newFeature, parentFeature);
        }
    }

    /**
     * Initiates an adding a graphical representation of the Group.
     * 
     * @param group
     *            The Group to add.
     * @param newFeature
     *            The new child Feature.
     * @param parentFeature
     *            The parent Feature;
     */
    private void addGroup(Group group, Feature newFeature, Feature parentFeature) {
        ContainerShape parentFeatureCS = BOUtil.getPictogramElementForBusinessObject(parentFeature,
                ContainerShape.class, getFeatureProvider());
        ContainerShape newFeatureCS = BOUtil.getPictogramElementForBusinessObject(newFeature, ContainerShape.class,
                getFeatureProvider());

        // determine source and target anchors
        Anchor sourceAnchor = BOUtil.getOutputAnchor(parentFeatureCS);
        Anchor targetAnchor = BOUtil.getInputAnchor(newFeatureCS);

        AddConnectionContext addGroupContext = new AddConnectionContext(sourceAnchor, targetAnchor);
        addGroupContext.setNewObject(group);
        getFeatureProvider().addIfPossible(addGroupContext);
    }

}
