package de.fzi.se.eclipse.featuremodel.treeeditor.utilities;

import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * <code>IdGen</code> supports the generating of unique ids.
 * 
 * @author Alexander Moor
 * 
 */
public class IdGen {
    /**
     * Generates a unique id.
     * 
     * @return The unique id.
     */
    public static String generate() {
        return EcoreUtil.generateUUID();
    }
}
