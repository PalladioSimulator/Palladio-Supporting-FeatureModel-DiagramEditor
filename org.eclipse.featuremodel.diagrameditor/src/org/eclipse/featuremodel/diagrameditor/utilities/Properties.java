package org.eclipse.featuremodel.diagrameditor.utilities;

/**
 * {@link Properties} contains property keys and values for property container (e.g.
 * PictogramElement or GraphicsAlgorithm). Every property container may include custom properties
 * {@link org.eclipse.graphiti.mm.Property} to describe given property container.
 * 
 * @author Alexander Moor
 * 
 */
public class Properties {
    /** Property key for an anchor type. */
    public static final String PROP_KEY_ANCHOR_TYPE = "type";
    /** Property value for an anchor of type input. */
    public static final String PROP_VAL_ANCHOR_TYPE_INPUT = "input";
    /** Property value for an anchor of type output. */
    public static final String PROP_VAL_ANCHOR_TYPE_OUTPUT = "output";
    /** Property key for a Group relation type. */
    public static final String PROP_KEY_RELATION_TYPE = "type";
    /** Property value for a Group single relation. */
    public static final String PROP_VAL_RELATION_TYPE = "relation";
    /** Property key for a container shape type. */
    public static final String PROP_KEY_CONTAINER_TYPE = "type";
    /** Property value for a container shape of type expand sign. */
    public static final String PROP_VAL_CONTAINER_TYPE_EXPANDSIGN = "expandsign";
    /** Property value for a Feature container shape of type collapsed. */
    public static final String PROP_VAL_CONTAINER_TYPE_COLLAPSED = "collapsed";
    /** Property value for a Feature container shape of type expanded. */
    public static final String PROP_VAL_CONTAINER_TYPE_EXPANDED = "expanded";
    /** Property key for a container shape initial x coordinate. */
    public static final String PROP_KEY_CONTAINER_INIT_X = "x";
    /** Property key for a container shape initial y coordinate. */
    public static final String PROP_KEY_CONTAINER_INIT_Y = "y";
}
