package org.eclipse.featuremodel.diagrameditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.graphiti.features.IDirectEditingInfo;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.algorithms.AbstractText;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;

/**
 * Feature enables direct editing of a Feature name on mouse double click.
 * 
 */
public class DirectEditDoubleClickFeature extends AbstractCustomFeature {

    /**
     * Creates an instance of {@link DirectEditDoubleClickFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public DirectEditDoubleClickFeature(final IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Gets the name of this function feature.
     * 
     * @return The name.
     */
    @Override
    public String getName() {
        return "Rename";
    }

    /**
     * Gets the description of this function feature.
     * 
     * @return The description.
     */
    @Override
    public String getDescription() {
        return "Renames the Feature";
    }

    /**
     * Decides if the direct editing feature is available with the given context. This
     * implementation returns <code>true</code> if the pictogram element represents a Feature.
     * 
     * @param context
     *            The context.
     * @return true if the pictogram element represents a Feature.
     */
    @Override
    public boolean isAvailable(IContext context) {
        boolean result = false;
        if (context instanceof ICustomContext) {
            PictogramElement[] pes = ((ICustomContext) context).getPictogramElements();
            if (pes != null && pes.length == 1) {
                Object bo = getBusinessObjectForPictogramElement(pes[0]);
                if (bo instanceof Feature) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Checks whether the current pictogram element of the given context can be modified. This
     * implementation returns <code>true</code> if the pictogram element represents a Feature.
     * 
     * @param context
     *            the context.
     * @return true if the pictogram element represents a Feature.
     */
    @Override
    public boolean canExecute(ICustomContext context) {
        boolean result = false;
        PictogramElement[] pes = context.getPictogramElements();
        if (pes != null && pes.length == 1) {
            Object bo = getBusinessObjectForPictogramElement(pes[0]);
            if (bo instanceof Feature) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Execute the direct editing.
     * 
     * @param context
     *            The context.
     */
    @Override
    public void execute(ICustomContext context) {
        // get main pictogram
        PictogramElement pe = context.getPictogramElements()[0];

        Shape textPE = null;
        AbstractText textGA = null;
        // get text field pictogram and graphic algoritm objects
        if (pe instanceof ContainerShape) {
            ContainerShape cs = (ContainerShape) pe;
            for (Shape shape : cs.getChildren()) {
                if (shape.getGraphicsAlgorithm() instanceof AbstractText) {
                    textPE = shape;
                    textGA = (AbstractText) shape.getGraphicsAlgorithm();

                }
            }
        }

        // execute direct editing
        if (textPE != null && textGA != null) {
            IDirectEditingInfo directEditingInfo = getFeatureProvider().getDirectEditingInfo();

            directEditingInfo.setMainPictogramElement(pe);
            directEditingInfo.setPictogramElement(textPE);
            directEditingInfo.setGraphicsAlgorithm(textGA);

            directEditingInfo.setActive(true);
            getDiagramEditor().refresh();
        }
    }
}
