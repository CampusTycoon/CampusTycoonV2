package com.spacecomplexity.longboilife.game.globals;

import com.spacecomplexity.longboilife.game.utils.CameraManager;

/**
 * Class to represent the main camera which can be accessed from anywhere in the game.
 */
public class MainCamera {
    private static final MainCamera mainCamera = new MainCamera();

    private CameraManager camera;

    private MainCamera() {
    }

    /**
     * Set the main camera to a {@link CameraManager} instance.
     *
     * @param camera the {@link CameraManager} instance.
     */
    public static void setMainCamera(CameraManager camera) {
        mainCamera.camera = camera;
    }

    /**
     * Get the main camera.
     *
     * @return the main camera.
     */
    public static CameraManager camera() {
        return mainCamera.camera;
    }
}
