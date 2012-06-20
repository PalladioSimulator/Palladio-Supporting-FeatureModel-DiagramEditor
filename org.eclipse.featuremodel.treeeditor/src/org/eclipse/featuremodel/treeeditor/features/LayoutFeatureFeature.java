package org.eclipse.featuremodel.treeeditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.treeeditor.utilities.BOUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;


/**
 * Layouts the new Feature pictogram element added to the diagram.
 * 
 * @author Alexander Moor
 */
public class LayoutFeatureFeature extends AbstractLayoutFeature {

    /**
     * Minimal distance between parent node and new node to layout.
     */
    private static final int PADDING_PARENT = 60;

    /**
     * Minimal distance between child node and new node to layout.
     */
    private static final int PADDING_CHILD = 30;

    /**
     * Creates an instance of {@link LayoutFeatureFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public LayoutFeatureFeature(IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Checks whether the current pictogram element of the given context can be layouted.
     * 
     * @param context
     *            The context.
     * @return true if update is possible
     */
    @Override
    public boolean canLayout(ILayoutContext context) {
        PictogramElement pe = context.getPictogramElement();
        Object obj = getBusinessObjectForPictogramElement(pe);
        if (obj instanceof Feature) {
            return true;
        }

        return false;
    }

    /**
     * Layouts only the given Feature pictogram element. The Feture pictogram element to layout is
     * positioned under the parent Feature or rightmost of all child Features.
     * 
     * @param context
     *            The context.
     * @return true, if layout process was successfully
     */
    @Override
    public boolean layout(ILayoutContext context) {
        PictogramElement pe = context.getPictogramElement();
        Feature featureToLayout = (Feature) getBusinessObjectForPictogramElement(pe);
        Feature parent = featureToLayout.getParent();
        int x, y; // new coordinates of the Feature to layout

        // if it is not the root of Feature Model tree
        if (parent != null) {

            // get the parent Feature pictogram element.
            ContainerShape peParent = BOUtil.getPictogramElementForBusinessObject(parent, ContainerShape.class,
                    getFeatureProvider());
            GraphicsAlgorithm gaParent = peParent.getGraphicsAlgorithm();

            // determine X
            // If parent Feature has more then one child Feature determine the x coordinate of the
            // rightmost child Feature pictogram element.
            x = determineRightmostChildFeature(parent, featureToLayout);
            if (x > 0) {
                x += gaParent.getWidth() + PADDING_CHILD;
            } else {
                x = gaParent.getX();
            }

            // determine Y
            y = gaParent.getY() + gaParent.getHeight() + PADDING_PARENT;

            // set the new coordinates of the Feature pictogram element to layout
            pe.getGraphicsAlgorithm().setX(x);
            pe.getGraphicsAlgorithm().setY(y);
        }

        return true;
    }

    /**
     * Determines the x coordinate of the rightmost child Feature pictogram element of the given
     * parent Feature.
     * 
     * @param parent
     *            The parent Feature
     * @param featureToLayout
     *            The Feature to layout
     * @return The x coordinate. x < 0 if children do not exist.
     */
    private int determineRightmostChildFeature(Feature parent, Feature featureToLayout) {
        int xMax = Integer.MIN_VALUE;
        for (Group g : parent.getChildren()) {
            for (Feature f : g.getFeatures()) {
                if (!f.equals(featureToLayout)) {
                    ContainerShape cs = BOUtil.getPictogramElementForBusinessObject(f, ContainerShape.class,
                            getFeatureProvider());
                    GraphicsAlgorithm ga = cs.getGraphicsAlgorithm();
                    if (ga.getX() > xMax) {
                        xMax = ga.getX();
                    }
                }
            }
        }
        return xMax;
    }

}
