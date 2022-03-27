package com.gempukku.gdx.jam20;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gempukku.gdx.jam20.level.GameLevel;
import com.gempukku.gdx.jam20.level.system.InputSystem;
import com.gempukku.gdx.jam20.level.system.LevelSystem;
import com.gempukku.gdx.jam20.level.system.SoundSystem;
import com.gempukku.libgdx.graph.pipeline.PipelineLoader;
import com.gempukku.libgdx.graph.pipeline.PipelineRenderer;
import com.gempukku.libgdx.graph.pipeline.RenderOutputs;
import com.gempukku.libgdx.graph.plugin.sprites.SpritesPluginRuntimeInitializer;
import com.gempukku.libgdx.graph.plugin.ui.UIPluginPublicData;
import com.gempukku.libgdx.graph.plugin.ui.UIPluginRuntimeInitializer;
import com.gempukku.libgdx.graph.util.DefaultTimeKeeper;
import com.gempukku.libgdx.lib.camera2d.FocusCameraController;
import com.gempukku.libgdx.lib.camera2d.constraint.LockedToWindowCameraConstraint;
import com.gempukku.libgdx.lib.camera2d.constraint.SnapToWindowCameraConstraint;

public class GameScreen implements Screen {
    private static final float TILE_SIZE = 48f;
    private static final float MAJOR_TICK = 0.20f;

    private float lastMajorTick = -MAJOR_TICK;

    private PipelineRenderer pipelineRenderer;
    private TextureAtlas textureAtlas;
    private final Game game;
    private SoundSystem soundSystem;
    private final Screen exitScreen;
    private final Skin skin;

    private Stage mainStage;
    private Stage pauseStage;
    private Stage finishStage;

    private OrthographicCamera camera;
    private DefaultTimeKeeper timeKeeper;

    private InputSystem inputSystem;
    private LevelSystem levelSystem;
    private FocusCameraController focusCameraController;
    private Label collectableCountLabel;
    private Label timeLabel;

    private boolean paused;
    private boolean finished;
    private Label finishLabel;
    private TextButton retryButton;
    private AssetManager assetManager;

    public GameScreen(Game game, SoundSystem soundSystem, Screen exitScreen, Skin skin) {
        this.game = game;
        this.soundSystem = soundSystem;
        this.exitScreen = exitScreen;
        this.skin = skin;

        initialize();
    }

    private void initialize() {
        this.mainStage = new Stage(new ScreenViewport());
        this.pauseStage = new Stage(new ScreenViewport());
        this.finishStage = new Stage(new ScreenViewport());

        initializeMainStage();
        initializePauseStage();
        initializeFinishStage();

        timeKeeper = new DefaultTimeKeeper();

        camera = new OrthographicCamera(Gdx.graphics.getWidth() / TILE_SIZE, Gdx.graphics.getHeight() / TILE_SIZE);

        loadPipeline();
        loadTextureAtlas();

        inputSystem = new InputSystem();
        levelSystem = new LevelSystem(timeKeeper, inputSystem, soundSystem, pipelineRenderer);
        focusCameraController = new FocusCameraController(camera, levelSystem,
                new SnapToWindowCameraConstraint(new Rectangle(0.45f, 0.45f, 0.1f, 0.1f), new Vector2(0.1f, 0.1f)),
                new LockedToWindowCameraConstraint(new Rectangle(0.4f, 0.4f, 0.2f, 0.2f)));
        //new SceneCameraConstraint(new Rectangle(0, 0, gameLevel.getWidth(), gameLevel.getHeight())));
    }

    private void initializeFinishStage() {
        Table table = new Table(skin);
        table.setFillParent(true);

        finishLabel = new Label("", skin, "menu");
        table.add(finishLabel).row();

        retryButton = new TextButton("Retry", skin, "menu");
        retryButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        loadLevel(levelSystem.getGameLevel());
                    }
                });
        table.add(retryButton).width(250).height(50).pad(10).row();

        TextButton exit = new TextButton("Exit level", skin, "menu");
        exit.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        exitToMenu();
                    }
                });
        table.add(exit).width(250).height(50).pad(10).row();

        finishStage.addActor(table);
    }

    private void initializePauseStage() {
        Table table = new Table(skin);
        table.setFillParent(true);

        TextButton resume = new TextButton("Resume", skin, "menu");
        resume.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        setPaused(false);
                    }
                });
        table.add(resume).width(250).height(50).pad(10).row();

        TextButton exitToMenu = new TextButton("Exit to menu", skin, "menu");
        exitToMenu.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        exitToMenu();
                    }
                });
        table.add(exitToMenu).width(250).height(50).pad(10).row();

        TextButton exitGame = new TextButton("Exit game", skin, "menu-warning");
        exitGame.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Gdx.app.exit();
                    }
                });
        table.add(exitGame).width(250).height(50).pad(10).row();

        pauseStage.addActor(table);
    }

    private void initializeMainStage() {
        collectableCountLabel = new Label("", skin, "fixed");
        timeLabel = new Label("", skin, "fixed");
        timeLabel.setAlignment(Align.right);

        Table table = new Table(skin);
        table.setFillParent(true);
        table.left().top();

        table.add(collectableCountLabel).pad(10f).left().growX();
        table.add(timeLabel).pad(10f).right().growX();
        table.row();

        mainStage.addActor(table);
    }

    public void loadLevel(GameLevel gameLevel) {
        setFinished(null);
        setPaused(false);

        camera.position.x = gameLevel.getSpawnPosition().x;
        camera.position.y = gameLevel.getHeight() - gameLevel.getSpawnPosition().y;
        camera.update(true);

        lastMajorTick = -MAJOR_TICK;
        timeKeeper.setTime(0f);

        if (levelSystem.getGameLevel() != null)
            levelSystem.unloadLevel();

        levelSystem.loadLevel(gameLevel, textureAtlas);
    }

    private String getTimeString(int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        return String.format("%03d:%02d", minutes, seconds);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        soundSystem.update(delta);

        if (!finished) {
            LevelSystem.FinishReason finishReason = levelSystem.getFinishReason();
            if (finishReason != null) {
                setFinished(finishReason);
            }
        }

        if (!finished) {
            inputSystem.update();

            if (inputSystem.isPauseRequested()) {
                setPaused(!paused);
            }

            if (!paused) {
                timeKeeper.updateTime(delta);

                boolean majorUpdate = false;
                if (timeKeeper.getTime() >= lastMajorTick + MAJOR_TICK) {
                    majorUpdate = true;
                    lastMajorTick = timeKeeper.getTime();
                }

                levelSystem.update(majorUpdate);

                collectableCountLabel.setText("Grubs: " + levelSystem.getCollectablesCollected() + "/" + levelSystem.getCollectablesRequired());
                int remainingTime = levelSystem.getGameLevel().getMaximumLevelTime() - MathUtils.floor(timeKeeper.getTime());
                timeLabel.setText("Time: " + getTimeString(remainingTime));
            }
        }

        focusCameraController.update(delta);

        pipelineRenderer.render(RenderOutputs.drawToScreen);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width / TILE_SIZE;
        camera.viewportHeight = height / TILE_SIZE;
        camera.update(true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        pipelineRenderer.dispose();
        mainStage.dispose();
        assetManager.dispose();
    }

    private void setPaused(boolean paused) {
        if (this.paused != paused) {
            this.paused = paused;

            pipelineRenderer.setPipelineProperty("Blur", this.paused);
            pipelineRenderer.setPipelineProperty("Pause", this.paused);

            Gdx.input.setInputProcessor(this.paused ? pauseStage : null);
        }
    }

    private void setFinished(LevelSystem.FinishReason finishReason) {
        boolean finished = finishReason != null;
        if (this.finished != finished) {
            this.finished = finished;

            if (this.finished) {
                finishLabel.setText(finishReason.getText());
                retryButton.setVisible(finishReason.isRetry());
            }

            pipelineRenderer.setPipelineProperty("Blur", this.finished);
            pipelineRenderer.setPipelineProperty("Finish", this.finished);

            Gdx.input.setInputProcessor(this.finished ? finishStage : null);
        }
    }

    private void exitToMenu() {
        this.game.setScreen(exitScreen);
    }

    private void loadPipeline() {
        SpritesPluginRuntimeInitializer.register();
        UIPluginRuntimeInitializer.register();

        pipelineRenderer = PipelineLoader.loadPipelineRenderer(Gdx.files.classpath("pipeline/game-pipeline.json"), timeKeeper);
        pipelineRenderer.setPipelineProperty("Camera", camera);

        UIPluginPublicData uiPlugin = pipelineRenderer.getPluginData(UIPluginPublicData.class);
        uiPlugin.setStage("Main-Stage", mainStage);
        uiPlugin.setStage("Pause-Stage", pauseStage);
        uiPlugin.setStage("Finish-Stage", finishStage);
    }

    private void loadTextureAtlas() {
        assetManager = new AssetManager();
        assetManager.load("texture/game.atlas", TextureAtlas.class);
        assetManager.finishLoading();
        textureAtlas = assetManager.get("texture/game.atlas");
    }
}
