package org.eclipse.featuremodel.diagrameditor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.featuremodel.FeatureModel;
import org.eclipse.featuremodel.FeatureModelFactory;
import org.eclipse.featuremodel.diagrameditor.utilities.IdGen;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramLink;
import org.eclipse.graphiti.mm.pictograms.PictogramsFactory;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.ui.statushandlers.StatusManager;


/**
 * Diagram editor help methods. It contains functions to handle resources e.g. to create Feature
 * Model files/models or Feature Diagram files/models or save Feature Model objects to the file.
 * 
 * @author Alexander Moor
 */
public class FMEDiagramEditorUtil {

    /** The grid size in pixel (0 for no grid). */
    private static final int DIAGRAM_GRID_SIZE = 10;
    /** The setting for snapping to grid. */
    private static final boolean DIAGRAM_SNAP_TO_GRID = false;

    /**
     * Saves Feature Model objects to the Feature Model file instead of Feature Diagram file.
     * 
     * @param bo
     *            the object to save
     * @param diagram
     *            the Feature Diagram model
     */
    public static void saveToModelFile(final EObject bo, final Diagram diagram) {
        URI uri = diagram.eResource().getURI();
        uri = uri.trimFragment();
        uri = uri.trimFileExtension();
        uri = uri.appendFileExtension(FMEDiagramEditor.MODEL_FILE_EXTENSION);
        ResourceSet rSet = diagram.eResource().getResourceSet();

        IResource file = FMEDiagramEditorUtil.getResource(uri.toPlatformString(true));
        if (file == null || !file.exists()) {
            createFeatureModel(diagram, uri);
        }

        final Resource resource = rSet.getResource(uri, true);
        resource.getContents().add(bo);
    }

    /**
     * Creates Feature Model. It includes the creation of new Feature Model file and model.
     * 
     * @param featureDiagramModel
     *            the Feature Diagram model to associate with created Feature Model
     * @param modelURI
     *            The URI of the feature Model to create
     * @return the created resource for Feature Model
     */
    public static Resource createFeatureModel(final Diagram featureDiagramModel, final URI modelURI) {
        // create a editing domain
        TransactionalEditingDomain editingDomain = GraphitiUi.getEmfService().createResourceSetAndEditingDomain();

        // get an existing Feature Model model or create a new
        FeatureModel featureModelModel = FMEDiagramEditorUtil.createFeatureModelModel(editingDomain, modelURI);

        // link Feature Model and Feature Diagram models
        FMEDiagramEditorUtil.linkModelAndDiagram(editingDomain, featureModelModel, featureDiagramModel);

        // dispose the editing domain to eliminate memory leak
        editingDomain.dispose();

        return featureModelModel.eResource();
    }

    /**
     * Create a new Feature Diagram with given URIs. It includes the creation of new Feature Diagram
     * file and model. Additionally a new Feature Model model is created or an existing loaded.
     * 
     * @param diagramURI
     *            The Feature Diagram file URI.
     * @param modelURI
     *            The Feature Model file URI.
     * @param progressMonitor
     *            progress monitor
     * @return the resource for the new Feature Diagram file
     */
    public static Resource createFeatureDiagram(final URI diagramURI, final URI modelURI,
            final IProgressMonitor progressMonitor) {
        progressMonitor.beginTask("Creating Feature Diagram and Feature Model files", 3);
        // create a editing domain
        TransactionalEditingDomain editingDomain = GraphitiUi.getEmfService().createResourceSetAndEditingDomain();

        // create new Feature Diagram model
        Diagram featureDiagramModel = FMEDiagramEditorUtil.createFeatureDiagramModel(editingDomain, diagramURI);

        // get an existing Feature Model model or create a new
        FeatureModel featureModelModel;
        if (FMEDiagramEditorUtil.getResource(modelURI.toPlatformString(true)) == null) {
            featureModelModel = FMEDiagramEditorUtil.createFeatureModelModel(editingDomain, modelURI);
        } else {
            featureModelModel = FMEDiagramEditorUtil.loadFeatureModelModel(editingDomain, modelURI);
        }

        // link Feature Model and Feature Diagram models
        FMEDiagramEditorUtil.linkModelAndDiagram(editingDomain, featureModelModel, featureDiagramModel);
        progressMonitor.worked(1);

        // initialize Feature Diagram with Feature Model content
        initializeDiagram(editingDomain, featureDiagramModel, featureModelModel);
        progressMonitor.worked(1);

        // --- save ---
        // save changes to Feature Diagram
        saveResource(featureDiagramModel.eResource(), "Feature Diagram");

        // Dispose the editing domain to eliminate memory leak
        editingDomain.dispose();

        progressMonitor.done();
        return featureDiagramModel.eResource();
    }

    /**
     * Creates a new Feature Diagram model.
     * 
     * @param editingDomain
     *            The editing domain for performing this action.
     * @param diagramURI
     *            The URI of Feature Diagram.
     * @return the new Feature Diagram model.
     */
    private static Diagram createFeatureDiagramModel(final TransactionalEditingDomain editingDomain,
            final URI diagramURI) {

        editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
            @Override
            protected void doExecute() {
                // create Feature Diagram resource
                Resource diagramResource = editingDomain.getResourceSet().createResource(diagramURI);
                diagramResource.setTrackingModification(true);
                // create Feature Diagram model
                String diagramName = resolveDiagramName(diagramResource);
                Diagram diagram = Graphiti.getPeCreateService().createDiagram(FMEDiagramEditor.DIAGRAM_TYPE_NAME,
                        diagramName, DIAGRAM_GRID_SIZE, DIAGRAM_SNAP_TO_GRID);
                diagramResource.getContents().add(diagram);
                // save Feature Diagram resources
                saveResource(diagramResource, "Feature Diagram");
            }
        });

        return (Diagram) editingDomain.getResourceSet().getResource(diagramURI, true).getContents().get(0);
    }

    /**
     * Creates a new Feature Model model.
     * 
     * @param editingDomain
     *            The editing domain for performing this action.
     * @param modelURI
     *            The URI of Feature Model.
     * @return the new Feature Model model.
     */
    private static FeatureModel createFeatureModelModel(final TransactionalEditingDomain editingDomain,
            final URI modelURI) {
        editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
            @Override
            protected void doExecute() {
                // create Feature Model resource
                Resource modelResource = editingDomain.getResourceSet().createResource(modelURI);
                modelResource.setTrackingModification(true);
                // create Feature Model model
                FeatureModel model = FeatureModelFactory.eINSTANCE.createFeatureModel();
                model.setId(IdGen.generate());
                modelResource.getContents().add(model);
                // save Feature Model resources
                saveResource(modelResource, "Feature Model");

            }
        });

        return (FeatureModel) editingDomain.getResourceSet().getResource(modelURI, true).getContents().get(0);
    }

    /**
     * Load the existing Feature Model model.
     * 
     * @param editingDomain
     *            The editing domain for performing this action.
     * @param modelURI
     *            The URI of Feature Model.
     * @return the loaded Feature Model model.
     */
    private static FeatureModel loadFeatureModelModel(
                                    final TransactionalEditingDomain editingDomain, 
                                    final URI modelURI) {
        editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
            @Override
            protected void doExecute() {
                Resource modelResource = editingDomain.getResourceSet().getResource(modelURI, true);
                FeatureModel fm = (FeatureModel) modelResource.getContents().get(0);
                if (fm.getId() == null || fm.getId().isEmpty()) {
                    fm.setId(IdGen.generate());
                }
            }
        });

        return (FeatureModel) editingDomain.getResourceSet().getResource(modelURI, true).getContents().get(0);
    }

    /**
     * Link the Feature Model and Feature Diagram models.
     * 
     * @param editingDomain
     *            The editing domain for performing this action.
     * @param featureModelModel
     *            The Feature Model model.
     * @param featureDiagramModel
     *            The Feature Diagram model.
     */
    private static void linkModelAndDiagram(TransactionalEditingDomain editingDomain,
            final FeatureModel featureModelModel, final Diagram featureDiagramModel) {

        editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
            @Override
            protected void doExecute() {

                PictogramLink link = PictogramsFactory.eINSTANCE.createPictogramLink();
                link.setPictogramElement(featureDiagramModel);
                link.getBusinessObjects().add(featureModelModel);
                // save link to Feature Diagram
                saveResource(featureDiagramModel.eResource(), "Feature Diagram");
            }
        });
    }

    /**
     * Initialize Feature Diagram with content from the given Feature Model.
     * 
     * @param editingDomain
     *            The editing domain for performing this action.
     * @param featureDiagramModel
     *            The Feature Diagram model.
     * @param featureModelModel
     *            The Feature Model model.
     */
    private static void initializeDiagram(TransactionalEditingDomain editingDomain, final Diagram featureDiagramModel,
            final FeatureModel featureModelModel) {

        editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
            @Override
            protected void doExecute() {
                IFeatureProvider dtp = GraphitiUi.getExtensionManager().createFeatureProvider(featureDiagramModel);
                AddContext addContext = new AddContext();
                addContext.setNewObject(featureModelModel);
                addContext.setTargetContainer(featureDiagramModel);
                dtp.addIfPossible(addContext);
            }
        });
    }

    /**
     * Saves the given resource.
     * 
     * @param resource
     *            the resource to save
     * @param resourceName
     *            the resource name for a error message
     */
    private static void saveResource(Resource resource, String resourceName) {
        try {
            resource.save(FMEDiagramEditorUtil.getSaveOptions());
        } catch (IOException exception) {
            IStatus status = new Status(IStatus.ERROR, FMEPlugin.PLUGIN_ID, "Unable to store " + resourceName
                    + " resource", exception);
            StatusManager.getManager().handle(status);
        }
    }

    /**
     * Gets resource for given path.
     * 
     * @param path
     *            The path to the resource .
     * @return the resource, or null if no such resource exists
     */
    public static IResource getResource(String path) {
        return ResourcesPlugin.getWorkspace().getRoot().findMember(path);
    }

    /**
     * Gets save options for resources.
     * 
     * @return new save options
     */
    public static Map<?, ?> getSaveOptions() {
        HashMap<String, Object> saveOptions = new HashMap<String, Object>();
        saveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8");
        saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
        return saveOptions;
    }

    /**
     * Resolve the Feature Diagram name from the given resource. Returns the Feature Model file name
     * without file extension.
     * 
     * @param diagramResource
     *            a Feature Diagram file resource
     * @return the name
     */
    private static String resolveDiagramName(Resource diagramResource) {
        URI diagramURI = diagramResource.getURI();
        String name = diagramURI.lastSegment();
        String extension = diagramURI.fileExtension();
        // cut the file extension
        String diagramName = name.substring(0, name.length() - (extension == null ? 0 : extension.length() + 1));

        return diagramName;
    }
}
