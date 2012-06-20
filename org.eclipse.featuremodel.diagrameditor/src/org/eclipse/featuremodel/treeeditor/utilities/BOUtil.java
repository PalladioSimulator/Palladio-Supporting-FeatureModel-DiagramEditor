package org.eclipse.featuremodel.treeeditor.utilities;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.featuremodel.Group;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.mm.Property;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

/**
 * This class contains help methods to perform work with Feature Model objects.
 * 
 * @author Alexander Moor
 * 
 */
public class BOUtil {

    /**
     * Types of Group relationship.
     */
    public enum RelationType {
        /**
         * Mandatory Feature.
         */
        Mandatory,
        /**
         * Optional Feature.
         */
        Optional,
        /**
         * At least one of the sub-Features must be selected.
         */
        OR,
        /**
         * One of the sub-Features must be selected.
         */
        XOR
    }

    /**
     * Gets the type of relation for given Group.
     * 
     * @param group
     *            The Group.
     * @return The type.
     */
    public static RelationType getRelationType(Group group) {
        RelationType relationType = null;
        if (group.getFeatures().size() <= 1) {
            if (group.getLower() == 1) {
                relationType = RelationType.Mandatory;
            } else {
                relationType = RelationType.Optional;
            }
        } else {
            if (group.getLower() == 1 && group.getUpper() == 1) { // XOR
                relationType = RelationType.XOR;
            } else {
                relationType = RelationType.OR;
            }
        }
        return relationType;
    }

    /**
     * Sets the type of relation for given Group.
     * 
     * @param group
     *            The Group.
     * @param relationType
     *            The relation type.
     */
    public static void setRelationType(Group group, RelationType relationType) {
        switch (relationType) {
        case Mandatory:
            group.setLower(1);
            break;
        case Optional:
            group.setLower(0);
            break;
        case XOR:
            group.setLower(1);
            group.setUpper(1);
            break;
        case OR:
            group.setLower(0);
            break;
        default:
            break;
        }
    }

    /**
     * Get the input anchor of a Feature figure.
     * 
     * @param cs
     *            The container of anchor.
     * @return The input anchor.
     */
    public static Anchor getInputAnchor(ContainerShape cs) {
        return getBoxRelativeAnchor(cs, "input");
    }

    /**
     * Gets the output anchor of a Feature figure.
     * 
     * @param cs
     *            The container of anchor.
     * @return The output anchor.
     */
    public static Anchor getOutputAnchor(ContainerShape cs) {
        return getBoxRelativeAnchor(cs, "output");
    }

    /**
     * Gets a box relative anchor associated with the container shape.
     * 
     * @param cs
     *            The container of anchor.
     * @param type
     *            The type of anchor ("input", "output")
     * 
     * @return The input anchor.
     */
    private static Anchor getBoxRelativeAnchor(ContainerShape cs, String type) {
        Anchor anchor = null;
        for (Anchor a : cs.getAnchors()) {
            if (a instanceof BoxRelativeAnchor) {
                for (Property p : ((BoxRelativeAnchor) a).getProperties()) {
                    if ("type".equals(p.getKey()) && type.equals(p.getValue())) {
                        anchor = a;
                        break;
                    }
                }
            }
        }
        return anchor;
    }

    /**
     * Get pictogram elemens of class <code>clazz</code> associated with the given object.
     * 
     * @param businessObject
     *            The model object.
     * @param clazz
     *            The class object of pictogram element.
     * @param <T>
     *            The object of class clazz.
     * @param fp
     *            The feature provider.
     * @return The associated pictogram element.
     */
    public static <T> T getPictogramElementForBusinessObject(Object businessObject, Class<T> clazz, //
            IFeatureProvider fp) {
        T result = null;
        for (PictogramElement pe : fp.getAllPictogramElementsForBusinessObject(businessObject)) {
            if (clazz.isAssignableFrom(pe.getClass())) {
                result = clazz.cast(pe);
                break;
            }
        }
        return result;
    }

    /**
     * Get all pictogram elements of class <code>clazz</code> associated with the given object.
     * 
     * @param businessObject
     *            The model object.
     * @param clazz
     *            The class object of pictogram element.
     * @param <T>
     *            The class type.
     * @param fp
     *            The feature provider.
     * @return The list of the associated pictogram elements.
     */
    public static <T> List<T> getAllPictogramElementsForBusinessObject(Object businessObject, Class<T> clazz, //
            IFeatureProvider fp) {
        List<T> result = new ArrayList<T>();
        for (PictogramElement pe : fp.getAllPictogramElementsForBusinessObject(businessObject)) {
            if (clazz.isAssignableFrom(pe.getClass())) {
                result.add(clazz.cast(pe));
            }
        }
        return result;
    }
}
