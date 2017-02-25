package org.orienteer.core.service.loader;

import org.orienteer.core.OrienteerWebApplication;

/**
 * @author Vitaliy Gonchar
 */
public abstract class OLoaderStorage {

    private static OrienteerClassLoader rootLoader;
    private static OrienteerClassLoader trustyModuleLoader;
    private static OrienteerClassLoader sandboxModuleLoader;
    private static final ClassLoader ORIENTEER_CLASS_LOADER = OrienteerWebApplication.class.getClassLoader();

    private OLoaderStorage() {}

    public static synchronized OrienteerClassLoader getRootLoader() {
        return rootLoader;
    }

    public static synchronized OrienteerClassLoader getTrustyModuleLoader(boolean create) {
        if (trustyModuleLoader == null || create) {
            trustyModuleLoader = OrienteerClassLoader.get();
            rootLoader.attachChild(trustyModuleLoader);
        }
        return trustyModuleLoader;
    }

    public static synchronized OrienteerClassLoader getSandboxModuleLoader(boolean create) {
        if (sandboxModuleLoader == null || create) {
            sandboxModuleLoader = OrienteerClassLoader.get();
            getTrustyModuleLoader(false).attachChild(sandboxModuleLoader);
        }
        return sandboxModuleLoader;
    }

    public static synchronized OrienteerClassLoader createNewRootLoader() {
        rootLoader = OrienteerClassLoader.get(ORIENTEER_CLASS_LOADER);
        if (trustyModuleLoader != null) rootLoader.attachChild(trustyModuleLoader);
        return rootLoader;
    }

    public static synchronized void clear() {
        deleteSandboxModuleLoader();
        deleteTrustyModuleLoader();
    }

    public static synchronized void deleteSandboxModuleLoader() {
        if (sandboxModuleLoader != null) {
            if (trustyModuleLoader != null) trustyModuleLoader.detachChild(sandboxModuleLoader);
            sandboxModuleLoader = null;
        }
    }

    private static void deleteTrustyModuleLoader() {
        if (trustyModuleLoader != null) {
            rootLoader.detachChild(trustyModuleLoader);
            trustyModuleLoader = null;
        }
    }

}
