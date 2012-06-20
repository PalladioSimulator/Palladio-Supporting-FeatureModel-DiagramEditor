package de.fzi.se.eclipse.featuremodel.treeeditor.features;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.CompoundDirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.EdgeList;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.featuremodel.FeatureModel;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;

/**
 * Layouts the tree structure of the Feature Model diagram. Refresh is triggered automatically by
 * the changes on the diagram model.
 * 
 * @author Alexander Moor
 */
public class LayoutDiagramFeature extends AbstractLayoutFeature {

    /**
     * Minimal distance between nodes.
     */
    private static final int PADDING = 30;

    /**
     * Creates an instance of {@link LayoutDiagramFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public LayoutDiagramFeature(IFeatureProvider fp) {
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
        if (obj instanceof FeatureModel) {
            return true;
        }

        return false;
    }

    /**
     * Layouts the whole tree of Feature Model. Maps the diagram to a graph structure, layouts the
     * graph and maps the new coordinates back to the diagram.
     * 
     * @param context
     *            The context.
     * @return true, if layout process was successfully
     */
    @Override
    public boolean layout(ILayoutContext context) {
        final CompoundDirectedGraph graph = mapDiagramToGraph();
        graph.setDefaultPadding(new Insets(PADDING));
        new CompoundDirectedGraphLayout().visit(graph);
        mapGraphCoordinatesToDiagram(graph);

        return true;
    }

    /**
     * Help method to map back the graph nodes to the diagram objects.
     * 
     * @param graph
     *            The graph to map back.
     */
    @SuppressWarnings("unchecked")
    private void mapGraphCoordinatesToDiagram(CompoundDirectedGraph graph) {
        NodeList myNodes = new NodeList();
        myNodes.addAll(graph.nodes);
        myNodes.addAll(graph.subgraphs);
        for (Object object : myNodes) {
            Node node = (Node) object;
            Shape shape = (Shape) node.data;
            shape.getGraphicsAlgorithm().setX(node.x);
            shape.getGraphicsAlgorithm().setY(node.y);
            shape.getGraphicsAlgorithm().setWidth(node.width);
            shape.getGraphicsAlgorithm().setHeight(node.height);
        }
    }

    /**
     * Help method to map diagram objects to a graph nodes.
     * 
     * @return The graph.
     */
    @SuppressWarnings("unchecked")
    private CompoundDirectedGraph mapDiagramToGraph() {
        Map<AnchorContainer, Node> shapeToNode = new HashMap<AnchorContainer, Node>();
        Diagram d = getDiagram();
        CompoundDirectedGraph dg = new CompoundDirectedGraph();
        EdgeList edgeList = new EdgeList();
        NodeList nodeList = new NodeList();
        EList<Shape> children = d.getChildren();

        for (Shape shape : children) {
            Node node = new Node();
            GraphicsAlgorithm ga = shape.getGraphicsAlgorithm();
            node.x = ga.getX();
            node.y = ga.getY();
            node.width = ga.getWidth();
            node.height = ga.getHeight();
            node.data = shape;
            shapeToNode.put(shape, node);
            nodeList.add(node);
        }

        EList<Connection> connections = d.getConnections();
        for (Connection connection : connections) {
            AnchorContainer source = connection.getStart().getParent();
            AnchorContainer target = connection.getEnd().getParent();
            Edge edge = new Edge(shapeToNode.get(source), shapeToNode.get(target));
            edge.data = connection;
            edgeList.add(edge);
        }
        dg.nodes = nodeList;
        dg.edges = edgeList;
        return dg;
    }

}
