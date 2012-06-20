package org.eclipse.featuremodel.diagrameditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.Group;
import org.eclipse.graphiti.features.IDirectEditingInfo;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeService;
import org.eclipse.graphiti.util.ColorConstant;

/**
 * Feature handle adding to the diagram a graphical representation of an existing Feature model
 * object.
 * 
 * @author Alexander Moor
 */
public class AddFeatureFeature extends AbstractAddFeature {

    /**
     * High of the rectangle.
     */
    private static final int RECTANGLE_HIGH = 40;
    /**
     * Width of the rectangle.
     */
    private static final int RECTANGLE_WIDTH = 120;

    /**
     * Creates an instance of {@link AddFeatureFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public AddFeatureFeature(IFeatureProvider fp) {
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
        Shape targetContainer = context.getTargetContainer();
        Connection targetConnection = context.getTargetConnection();
        Object bo;
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
     * Adds a graphical representation of an existing Feature model object to the diagram.
     * 
     * @param context
     *            The context.
     * @return The representation of the Feature model object.
     */
    @Override
    public PictogramElement add(IAddContext context) {
        Feature feature = (Feature) context.getNewObject();

        IPeService peService = Graphiti.getPeService();
        IGaService gaService = Graphiti.getGaService();

        // Create the visualization of the Feature as a rectangle
        ContainerShape featureContainerShape = peService.createContainerShape(getDiagram(), true);
        Rectangle featureRectangle = gaService.createRectangle(featureContainerShape);
        featureRectangle.setBackground(manageColor(ColorConstant.WHITE));
        featureRectangle.setFilled(true);
        gaService.setLocationAndSize(featureRectangle, context.getX(), context.getY(), RECTANGLE_WIDTH, RECTANGLE_HIGH);
        // Link the visualization with the Feature model
        link(featureContainerShape, feature);

        // Create the Feature name field
        Shape featureNameShape = peService.createShape(featureContainerShape, false);
        Text featureNameText = gaService.createText(featureNameShape);
        featureNameText.setForeground(manageColor(ColorConstant.BLACK));
        featureNameText.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
        featureNameText.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
        gaService.setLocationAndSize(featureNameText, 10, 10, RECTANGLE_WIDTH - 20, 20);
        // gaService.createFont(featureNameText, "Arial", 16);
        featureNameText.setValue(feature.getName());
        link(featureNameShape, feature);

        // add a chopbox anchor to the shape
        ChopboxAnchor chopBoxAnchor = peService.createChopboxAnchor(featureContainerShape);
        gaService.createInvisibleRectangle(chopBoxAnchor);

        // add the anchor for input connections
        BoxRelativeAnchor inputAnchor = peService.createBoxRelativeAnchor(featureContainerShape);
        inputAnchor.setRelativeHeight(0.0);
        inputAnchor.setRelativeWidth(0.5);
        gaService.createInvisibleRectangle(inputAnchor);
        peService.setPropertyValue(inputAnchor, "type", "input");

        // add the anchor for output connections
        BoxRelativeAnchor outputAnchor = peService.createBoxRelativeAnchor(featureContainerShape);
        outputAnchor.setRelativeHeight(1.0);
        outputAnchor.setRelativeWidth(0.5);
        gaService.createInvisibleRectangle(outputAnchor);
        peService.setPropertyValue(outputAnchor, "type", "output");

        // activate direct editing after Feature object creation
        IDirectEditingInfo directEditingInfo = getFeatureProvider().getDirectEditingInfo();
        directEditingInfo.setMainPictogramElement(featureContainerShape);
        directEditingInfo.setPictogramElement(featureNameShape);
        directEditingInfo.setGraphicsAlgorithm(featureNameText);

        // Return the root pictogram element
        return featureContainerShape;
    }
}
