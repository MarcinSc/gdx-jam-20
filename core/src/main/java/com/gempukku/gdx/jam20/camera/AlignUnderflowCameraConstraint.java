package com.gempukku.gdx.jam20.camera;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gempukku.libgdx.graph.util.Alignment;
import com.gempukku.libgdx.lib.camera2d.constraint.CameraConstraint;

public class AlignUnderflowCameraConstraint implements CameraConstraint {
    private Rectangle bounds;
    private Alignment alignment;

    public AlignUnderflowCameraConstraint(Rectangle bounds, Alignment alignment) {
        this.bounds = bounds;
        this.alignment = alignment;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds.set(bounds);
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    @Override
    public void applyConstraint(Camera camera, Vector2 focus, float delta) {
        float previousX = camera.position.x;
        float previousY = camera.position.y;

        if (camera.viewportWidth > bounds.width)
            camera.position.x = bounds.x + alignment.apply(camera.viewportWidth, camera.viewportHeight, bounds.width, bounds.height).x + camera.viewportWidth / 2;
        if (camera.viewportHeight > bounds.height)
            camera.position.y = bounds.y + alignment.apply(camera.viewportWidth, camera.viewportHeight, bounds.width, bounds.height).y + camera.viewportHeight / 2;

        if (camera.position.x != previousX || camera.position.y != previousY)
            camera.update();
    }
}
