package org.eclipse.featuremodel.diagrameditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.diagrameditor.utilities.Properties;
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
     * High of the Feature figure.
     */
    private static final int FEATURE_FIGURE_HIGH = 40;
    /**
     * Width of the Feature figure.
     */
    private static final int FEATURE_FIGURE_WIDTH = 120;

    /**
     * Size of the expand sign.
     */
    private static final int EXPAND_SIGN_SIZE = 11;

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
        peService.setPropertyValue(featureContainerShape, Properties.PROP_KEY_CONTAINER_TYPE,
                Properties.PROP_VAL_CONTAINER_TYPE_EXPANDED);

        Rectangle featureRectangle = gaService.createRectangle(featureContainerShape);
        featureRectangle.setBackground(manageColor(ColorConstant.WHITE));
        featureRectangle.setFilled(true);
        gaService.setLocationAndSize(featureRectangle, context.getX(), context.getY(), FEATURE_FIGURE_WIDTH,
                FEATURE_FIGURE_HIGH);
        // Link the visualization with the Feature model
        link(featureContainerShape, feature);

        // Create the Feature name field
        Shape featureNameShape = peService.createShape(featureContainerShape, false);
        Text text = gaService.createText(featureNameShape);
        text.setForeground(manageColor(ColorConstant.BLACK));
        text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
        text.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
        gaService.setLocationAndSize(text, 10, 10, featureRectangle.getWidth() - 20, featureRectangle.getHeight() - 20);
        // gaService.createFont(featureNameText, "Arial", 16);
        text.setValue(feature.getName());
        link(featureNameShape, feature);

        // add a chopbox anchor to the shape
        ChopboxAnchor chopBoxAnchor = peService.createChopboxAnchor(featureContainerShape);
        gaService.createInvisibleRectangle(chopBoxAnchor);

        // create anchors for connections
        createConnectionAnchors(featureContainerShape);

        // draw expand sign
        ContainerShape expandSignContainer = drawExpadSign(featureContainerShape);
        link(expandSignContainer, feature);

        // activate direct editing after Feature object creation
        IDirectEditingInfo directEditingInfo = getFeatureProvider().getDirectEditingInfo();
        directEditingInfo.setMainPictogramElement(featureContainerShape);
        directEditingInfo.setPictogramElement(featureNameShape);
        directEditingInfo.setGraphicsAlgorithm(text);

        // Return the root pictogram element
        return featureContainerShape;
    }

    /**
     * Creates anchors for connections.
     * 
     * @param featureContainerShape
     *            the main container shape of the Feature
     */
    private void createConnectionAnchors(ContainerShape featureContainerShape) {
        IPeService peService = Graphiti.getPeService();
        IGaService gaService = Graphiti.getGaService();

        // add the anchor for input connections
        BoxRelativeAnchor inputAnchor = peService.createBoxRelativeAnchor(featureContainerShape);
        inputAnchor.setRelativeHeight(0.0);
        inputAnchor.setRelativeWidth(0.5);
        gaService.createInvisibleRectangle(inputAnchor);
        peService.setPropertyValue(inputAnchor, Properties.PROP_KEY_ANCHOR_TYPE, Properties.PROP_VAL_ANCHOR_TYPE_INPUT);

        // add the anchor for output connections
        BoxRelativeAnchor outputAnchor = peService.createBoxRelativeAnchor(featureContainerShape);
        outputAnchor.setRelativeHeight(1.0);
        outputAnchor.setRelativeWidth(0.5);
        gaService.createInvisibleRectangle(outputAnchor);
        peService.setPropertyValue(outputAnchor, Properties.PROP_KEY_ANCHOR_TYPE, //
                Properties.PROP_VAL_ANCHOR_TYPE_OUTPUT);
    }

    /**
     * Draws expand sign. A Features with child Features can be collapsed (see
     * {@link CollapseFeatureFeature}) to hide the child elements and expanded (see
     * {@link ExpandFeatureFeature}) to show this. The expand sign is only shown by Features with
     * child Features.
     * 
     * @param featureContainerShape
     *            the main container shape of the Feature
     * @return the container shape of drown expand sign
     * 
     */
    private ContainerShape drawExpadSign(ContainerShape featureContainerShape) {
        IPeService peService = Graphiti.getPeService();
        IGaService gaService = Graphiti.getGaService();
        // the expand sign container
        ContainerShape expandSignContainer = peService.createContainerShape(featureContainerShape, false);
        expandSignContainer.setVisible(false);
        peService.setPropertyValue(expandSignContainer, Properties.PROP_KEY_CONTAINER_TYPE,
                Properties.PROP_VAL_CONTAINER_TYPE_EXPANDSIGN);

        // the border rectangle
        Rectangle expandRectangle = gaService.createRectangle(expandSignContainer);
        expandRectangle.setBackground(manageColor(ColorConstant.WHITE));
        expandRectangle.setFilled(true);
        int x = featureContainerShape.getGraphicsAlgorithm().getWidth() - EXPAND_SIGN_SIZE - 5;
        int y = featureContainerShape.getGraphicsAlgorithm().getHeight() - EXPAND_SIGN_SIZE - 5;
        gaService.setLocationAndSize(expandRectangle, x, y, EXPAND_SIGN_SIZE, EXPAND_SIGN_SIZE);

        // draw lines for plus sign
        // vertical line
        Shape xLineShape = peService.createShape(expandSignContainer, false);
        int[] points = new int[] { EXPAND_SIGN_SIZE / 2, 1, EXPAND_SIGN_SIZE / 2, EXPAND_SIGN_SIZE - 2 };
        gaService.createPolyline(xLineShape, points);
        // horizontal line
        Shape yLineShape = peService.createShape(expandSignContainer, false);
        points = new int[] { 1, EXPAND_SIGN_SIZE / 2, EXPAND_SIGN_SIZE - 2, EXPAND_SIGN_SIZE / 2 };
        gaService.createPolyline(yLineShape, points);

        return expandSignContainer;
    }
}
