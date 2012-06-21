package org.eclipse.featuremodel.diagrameditor.features;

import java.util.List;

import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.diagrameditor.utilities.BOUtil;
import org.eclipse.featuremodel.diagrameditor.utilities.BOUtil.RelationType;
import org.eclipse.graphiti.datatypes.ILocation;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
import org.eclipse.graphiti.mm.Property;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.Polygon;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.IColorConstant;

/**
 * Feature handle updating any changes made to a Group model object.
 * 
 * @author Alexander Moor
 */
public class UpdateRelationshipFeature extends AbstractUpdateFeature {

    /**
     * The size of the polygon represents set relation.
     */
    private static final int POLIGON_SIZE = 40;

    /**
     * Creates an instance of {@link UpdateRelationshipFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public UpdateRelationshipFeature(final IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Checks whether the values of the current pictogram element of the given context can be
     * updated.
     * 
     * @param context
     *            The context.
     * @return true if update is possible
     */
    @Override
    public boolean canUpdate(final IUpdateContext context) {
        Object obj = getBusinessObjectForPictogramElement(context.getPictogramElement());
        return (obj instanceof Group);
    }

    /**
     * Updates the Group relationship pictogram element. <br>
     * Updating includes: <br>
     * <ul>
     * <li>Update of the graphical representation of Group according to actual capacity of Group
     * (single or set relation).
     * <li>Update of the location and form of set relation pictogram element.
     * <li>Update look of the given pictogram element for Group according to the cardinality of the
     * group.
     * </ul>
     * 
     * @param context
     *            The context.
     * @return true, if update process was successful.
     */
    @Override
    public boolean update(final IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        Group group = (Group) getBusinessObjectForPictogramElement(pe);

        if (pe instanceof Connection) {
            // if more then one Feature in the Group it is set relation
            if (group.getFeatures().size() > 1) {
                updateSetRelation(group);
            } else {
                updateSingleRelation(group);
            }

            return true;
        }

        return false;
    }

    /**
     * Updates the set relation. Creates pictogram element and graphical representation of set
     * relation if not exist, otherwise update the location and form of the pictogram element and
     * graphical representation.
     * 
     * @param group
     *            The Group to update.
     */
    private void updateSetRelation(Group group) {
        // get pictogram element represents set relation
        Shape relationPE = BOUtil.getPictogramElementForBusinessObject(group, Shape.class, getFeatureProvider());
        // create if pictogram element not exists
        if (relationPE == null) {
            relationPE = Graphiti.getPeService().createContainerShape(getDiagram(), true);
            link(relationPE, group);

            // delete single relations if exist
            deleteSingleRelations(group);
        }
        // create or update graphical representation of set relation
        createSetRelationGraphic(group, relationPE);
    }

    /**
     * Updates the single relation. Creates pictogram element and graphical representation of single
     * relation if not exist.
     * 
     * @param group
     *            The Group to update.
     */
    private void updateSingleRelation(Group group) {
        // get pictogram element represents the single relation
        Connection connection = BOUtil.getPictogramElementForBusinessObject(group, Connection.class,
                getFeatureProvider());
        ConnectionDecorator cd = getSingleRelationDecorator(connection);

        // create single relation if not exists
        if (cd == null) {
            // create pictogram element
            cd = Graphiti.getPeService().createConnectionDecorator(connection, false, 1.0, true);
            Graphiti.getPeService().setPropertyValue(cd, "type", "relation");

            // delete set relation if exist
            deleteSetRelation(group);
        }

        // create graphical representation of single relation
        createSingleRelationGraphic(group, cd);
    }

    /**
     * Creates graphical representation of set relation.
     * 
     * @param group
     *            The Group the set relation associated to.
     * @param pe
     *            The container for the new graphical element.
     */
    private void createSetRelationGraphic(Group group, PictogramElement pe) {
        // Create the graphic representation of relation.
        // get all connections belong to group
        List<Connection> connections = BOUtil.getAllPictogramElementsForBusinessObject(group, Connection.class,
                getFeatureProvider());
        // get two outer connections
        Connection[] outerConn = getOuterConnections(connections);

        // calculate new polygon points locations according to position of two outer connections
        ILocation p0 = Graphiti.getPeService().getLocationRelativeToDiagram(outerConn[0].getStart());
        Point p1 = calculatePoint(outerConn[0], POLIGON_SIZE);
        Point p2 = calculatePoint(outerConn[1], POLIGON_SIZE);
        int x0 = p0.getX();
        int y0 = p0.getY();
        int x1 = p1.getX();
        int y1 = p1.getY();
        int x2 = p2.getX();
        int y2 = p2.getY();
        
        // if both points are on the same horizontal or vertical line as the
        // source point, add or subtract some pixels to ensure a curve is visible
        int xCurveMiddle = x1 + ((x2 - x1) / 2);
        int yCurveMiddle = y2 + ((y1 - y2) / 2);        
        if (xCurveMiddle != x0) {
            xCurveMiddle -= (x0 - xCurveMiddle) / 2;
        }
        if (yCurveMiddle != y0) {
            yCurveMiddle -= (y0 - yCurveMiddle) / 2;
        }
        
        // define the degree rounding of the curve
        int curveSmoothing = (int) (POLIGON_SIZE);
        
        // draw a filled polygon or a line only depending on the group type
        if (RelationType.XOR.equals(BOUtil.getRelationType(group))) {
            int[] points = new int[] { x1, y1, xCurveMiddle , yCurveMiddle, x2, y2 };
            int[] beforeAfter = new int[]{0, 0, curveSmoothing, curveSmoothing, 0, 0};
            Polyline relationBorder = Graphiti.getGaService().createPolyline(pe, points, beforeAfter);
            relationBorder.setForeground(manageColor(ColorConstant.BLACK));
            relationBorder.setLineWidth(2);
            relationBorder.setLineVisible(true);
        } else {
            int[] points = new int[] { x0, y0, x1, y1, xCurveMiddle , yCurveMiddle, x2, y2 };
            int[] beforeAfter = new int[]{0, 0, 0, 0, curveSmoothing, curveSmoothing, 0, 0};
            Polygon relationGA = Graphiti.getGaService().createPolygon(pe, points, beforeAfter);
            relationGA.setLineVisible(false);
            relationGA.setBackground(manageColor(ColorConstant.BLACK));
        }
    }

    /**
     * Creates graphical representation of single relation.
     * 
     * @param group
     *            The Group the set relation associated to.
     * @param ga
     *            The container for the new graphical element.
     */
    private void createSingleRelationGraphic(Group group, GraphicsAlgorithmContainer ga) {
        // create circle
        Ellipse relationGA = Graphiti.getGaService().createEllipse(ga);
        relationGA.setHeight(15);
        relationGA.setWidth(15);
        relationGA.setForeground(manageColor(IColorConstant.BLACK));
        if (RelationType.Mandatory.equals(BOUtil.getRelationType(group))) {
            relationGA.setBackground(manageColor(ColorConstant.WHITE));
        } else {
            relationGA.setBackground(manageColor(ColorConstant.BLACK));
        }
        relationGA.setLineWidth(2);
    }

    /**
     * Gets the decorator of single relation.
     * 
     * @param connection
     *            The connection the decorator associated to.
     * @return The associated decorator.
     */
    private ConnectionDecorator getSingleRelationDecorator(Connection connection) {
        ConnectionDecorator result = null;
        for (ConnectionDecorator cd : connection.getConnectionDecorators()) {
            for (Property p : cd.getProperties()) {
                if ("type".equals(p.getKey()) && "relation".equals(p.getValue())) {
                    result = cd;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Deletes the pictogram element and the graphical representation of set relation.
     * 
     * @param group
     *            The Group delete set relation from.
     */
    private void deleteSetRelation(Group group) {
        // get pictogram element represents set relation
        Shape relationPE = BOUtil.getPictogramElementForBusinessObject(group, Shape.class, getFeatureProvider());
        if (relationPE != null) {
            Graphiti.getPeService().deletePictogramElement(relationPE);
        }
    }

    /**
     * Deletes the pictogram element and the graphical representation of single relation.
     * 
     * @param group
     *            The Group delete single relation from.
     */
    private void deleteSingleRelations(Group group) {
        List<Connection> connections = BOUtil.getAllPictogramElementsForBusinessObject(group, Connection.class,
                getFeatureProvider());
        for (Connection conn : connections) {
            // get pictogram element represents single relation
            Shape relationPE = getSingleRelationDecorator(conn);
            if (relationPE != null) {
                Graphiti.getPeService().deletePictogramElement(relationPE);
            }
        }
    }

    /**
     * Gets two outer connections of set relation.
     * 
     * @param connections
     *            The connections represent relations.
     * @return The array of two outer connections.
     */
    private Connection[] getOuterConnections(List<Connection> connections) {
        int xMin = Integer.MAX_VALUE; // X coordinate of the left outer connection
        int xMax = Integer.MIN_VALUE; // X coordinate of the right outer connection
        Connection[] result = new Connection[2];

        // run trough all connections a look for X coordinate of the connection end
        for (Connection conn : connections) {
            Point p = calculatePoint(conn, 40);

            if (p.getX() < xMin) {
                result[0] = conn;
                xMin = p.getX();
            }

            if (p.getX() >= xMax) {
                result[1] = conn;
                xMax = p.getX();
            }
        }
        return result;
    }

    /**
     * Calculates the coordinates of a connection line point in according to the given distance
     * <code>dis</code> from the connection start point.
     * 
     * @param connection
     *            the connection.
     * @param dis
     *            the distance from the connection start point.
     * @return The calculated point.
     */
    private Point calculatePoint(Connection connection, double dis) {
        // determine line start and end points
        ILocation a = Graphiti.getPeService().getLocationRelativeToDiagram(connection.getStart());
        ILocation b = Graphiti.getPeService().getLocationRelativeToDiagram(connection.getEnd());
        // line vector
        Point ba = Graphiti.getGaService().createPoint(b.getX() - a.getX(), b.getY() - a.getY());
        // norm of the line vector
        double norm = Math.sqrt(ba.getX() * ba.getX() + ba.getY() * ba.getY());
        // calculate coordinates
        double x = a.getX() + dis * (ba.getX() / norm);
        double y = a.getY() + dis * (ba.getY() / norm);

        return Graphiti.getGaService().createPoint((int) x, (int) y);
    }

    /**
     * Check whether the values in the pictogram element are up to date, that means whether the
     * graphics algorithm of this pictogram element contain the latest values from the business
     * objects.
     * 
     * @param context
     *            The context.
     * 
     * @return true if parts of the pictogram model needs to be updated with the latest values from
     *         the business model
     */
    @Override
    public IReason updateNeeded(final IUpdateContext context) {
        return Reason.createFalseReason();
    }
}
