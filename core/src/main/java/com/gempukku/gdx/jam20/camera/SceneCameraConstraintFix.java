package com.gempukku.gdx.jam20.camera;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gempukku.libgdx.graph.util.Alignment;
import com.gempukku.libgdx.lib.camera2d.constraint.CameraConstraint;

public class SceneCameraConstraintFix implements CameraConstraint {
    private Alignment backupAlignment = Alignment.center;
    private final Rectangle bounds = new Rectangle();

    private final Vector2 tmpVector1 = new Vector2();
    private final Vector2 tmpVector2 = new Vector2();

    public SceneCameraConstraintFix(Rectangle bounds) {
        setBounds(bounds);
    }

    public void setBounds(Rectangle bounds) {
        this.bounds.set(bounds);
    }

    public void setBackupAlignment(Alignment backupAlignment) {
        this.backupAlignment = backupAlignment;
    }

    @Override
    public void applyConstraint(Camera camera, Vector2 focus, float delta) {
        Vector2 visibleMin = tmpVector1.set(camera.position.x, camera.position.y).add(-camera.viewportWidth / 2f, -camera.viewportHeight / 2f);
        Vector2 visibleMax = tmpVector2.set(camera.position.x, camera.position.y).add(+camera.viewportWidth / 2f, +camera.viewportHeight / 2f);

        float moveX = Math.min(Math.max(0, bounds.x - visibleMin.x), bounds.x + bounds.width - visibleMax.x);
        float moveY = Math.min(Math.max(0, bounds.y - visibleMin.y), bounds.y + bounds.height - visibleMax.y);

        float previousX = camera.position.x;
        float previousY = camera.position.y;

        camera.position.x += moveX;
        camera.position.y += moveY;

        if (camera.viewportWidth > bounds.width)
            camera.position.x = bounds.x + backupAlignment.apply(camera.viewportWidth, camera.viewportHeight, bounds.width, bounds.height).x + camera.viewportWidth / 2;
        if (camera.viewportHeight > bounds.height)
            camera.position.y = bounds.y + backupAlignment.apply(camera.viewportWidth, camera.viewportHeight, bounds.width, bounds.height).y + camera.viewportHeight / 2;

        if (camera.position.x != previousX || camera.position.y != previousY)
            camera.update();
    }
}
