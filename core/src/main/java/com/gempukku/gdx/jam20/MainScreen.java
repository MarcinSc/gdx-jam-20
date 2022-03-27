package com.gempukku.gdx.jam20;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gempukku.gdx.jam20.level.GameLevel;
import com.gempukku.gdx.jam20.level.LevelObject;
import com.gempukku.gdx.jam20.level.system.SoundSystem;

import java.io.IOException;

/**
 * First screen of the application. Displayed after the application is created.
 */
public class MainScreen implements Screen {
    private static final String[] tutorials = new String[]{
            "tutorial/level1.level",
            "tutorial/level2.level",
            "tutorial/level3.level",
            "tutorial/level4.level",
            "tutorial/level5.level",
            "tutorial/level6.level"
    };
    private static final String[] campaign = new String[]{
            "campaign/level1.level"
    };

    private Skin skin;
    private Stage stage;
    private AssetManager assetManager;
    private GameScreen gameScreen;
    private SoundSystem soundSystem;

    public MainScreen(Game game) {
        createSoundSystem();

        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        stage = new Stage(new ScreenViewport());

        assetManager = new AssetManager();
        assetManager.load("texture/ui.atlas", TextureAtlas.class);
        assetManager.load("images/title.png", Texture.class);
        assetManager.finishLoading();

        TextureAtlas uiTextureAtlas = assetManager.get("texture/ui.atlas", TextureAtlas.class);

        this.gameScreen = new GameScreen(game, soundSystem, this, skin);

        Table mainTable = new Table();
        mainTable.pad(10f);
        mainTable.setFillParent(true);

        Table levelsTable = new Table();
        levelsTable.pad(10f);
        levelsTable.setFillParent(true);

        Table settingsTable = new Table();
        settingsTable.pad(10f);
        settingsTable.setFillParent(true);

        Table editorTable = new Table();
        editorTable.pad(10f);
        editorTable.setFillParent(true);

        createMainTable(uiTextureAtlas, mainTable, levelsTable, settingsTable, editorTable);
        createLevelsTable(game, mainTable, levelsTable);
        createSettingsTable(mainTable, settingsTable);
        createEditorTable(game, mainTable, editorTable);

        stage.addActor(mainTable);
    }

    private void createSettingsTable(Table mainTable, Table settingsTable) {
        TextButton backButton = new TextButton("Back", skin, "menu-warning");
        backButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        settingsTable.remove();
                        stage.addActor(mainTable);
                    }
                });
        settingsTable.add(backButton).left().width(150).colspan(2).pad(10).height(50).row();

        Slider mainSlider = new Slider(0, 1, 0.01f, false, skin);
        mainSlider.setValue(soundSystem.getMainVolume());
        mainSlider.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        soundSystem.setMainVolume(mainSlider.getValue());
                    }
                });

        Slider ambientSlider = new Slider(0, 1, 0.01f, false, skin);
        ambientSlider.setValue(soundSystem.getMusicVolume());
        ambientSlider.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        soundSystem.setMusicVolume(ambientSlider.getValue());
                    }
                });

        Slider fxSlider = new Slider(0, 1, 0.01f, false, skin);
        fxSlider.setValue(soundSystem.getSoundVolume());
        fxSlider.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        soundSystem.setSoundVolume(fxSlider.getValue());
                    }
                });

        settingsTable.add(createLabel("Main Volume")).pad(5).fill();
        settingsTable.add(mainSlider).width(200).row();

        settingsTable.add(createLabel("Ambient Volume")).pad(5).fill();
        settingsTable.add(ambientSlider).width(200).row();

        settingsTable.add(createLabel("FX Volume")).pad(5).fill();
        settingsTable.add(fxSlider).width(200).row();

        TextButton testFx = new TextButton("Test FX Volume", skin, "menu");
        testFx.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        soundSystem.playSound("eat-dirt");
                    }
                });
        settingsTable.add(testFx).width(250).colspan(2).pad(10).height(50).row();
    }

    private Label createLabel(String text) {
        Label label = new Label(text, skin);
        label.setAlignment(Align.right);
        return label;
    }

    private void createSoundSystem() {
        soundSystem = new SoundSystem();

        soundSystem.loadMusic("water-in-cave", "sound/water-in-the-cave.ogg");
        soundSystem.playMusic("water-in-cave");

        soundSystem.loadSound("eat-dirt", "sound/eat-dirt.ogg");
        soundSystem.loadSound("eat-grub", "sound/eat-grub.ogg");
        soundSystem.loadSound("exit-level", "sound/exit-level.ogg");
        soundSystem.loadSound("open-door", "sound/open-door.ogg");
        soundSystem.loadSound("fox-death", "sound/fox-death.ogg");
        soundSystem.loadSound("death", "sound/death.ogg");
    }

    private void createEditorTable(Game game, Table mainTable, Table editorTable) {
        TextButton backButton = new TextButton("Back", skin, "menu-warning");
        backButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        editorTable.remove();
                        stage.addActor(mainTable);
                    }
                });
        editorTable.add(backButton).left().colspan(2).width(150).pad(10).height(50).row();

        Label grubsLabel = new Label("Number of grubs to collect", skin, "fixed");
        editorTable.add(grubsLabel);

        TextField collectibleField = new TextField("1", skin, "fixed");
        collectibleField.setAlignment(Align.right);
        editorTable.add(collectibleField).left().pad(5);

        editorTable.row();

        editorTable.add(new Label("Level data", skin, "fixed")).colspan(2).row();

        editorTable.add(new Label("[P] - player\n[G] - grub\n[W] - wall\n[D] - dirt\n[F] - fox\n[X] - exit\n[ ] - empty", skin, "fixed"));

        TextArea textArea = new TextArea("", skin, "fixed");
        editorTable.add(textArea).grow().row();

        Label validationLabel = new Label("", skin);
        editorTable.add(validationLabel).colspan(2).row();

        TextButton testButton = new TextButton("Test level", skin, "menu");
        testButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        int numberOfCollectibles = Integer.parseInt(collectibleField.getText());
                        LevelObject[][] levelData = GameLevel.createUnvalidatedLevelData(textArea.getText().toUpperCase());

                        GameLevel gameLevel = new GameLevel("Test", numberOfCollectibles, 3600, levelData);
                        gameScreen.loadLevel(gameLevel);
                        game.setScreen(gameScreen);
                    }
                });
        enableButton(testButton, false);
        editorTable.add(testButton).width(200).colspan(2).right().pad(10).height(50).row();

        ChangeListener validationListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    int numberOfCollectibles = Integer.parseInt(collectibleField.getText());
                    if (numberOfCollectibles < 1)
                        throw new IllegalArgumentException("Number of grubs must be at least 1");
                    GameLevel.createUnvalidatedLevelData(textArea.getText());
                    validationLabel.setText("Level valid");
                    enableButton(testButton, true);
                } catch (NumberFormatException exp) {
                    validationLabel.setText("Invalid number of grubs");
                    enableButton(testButton, false);
                } catch (Exception exp) {
                    validationLabel.setText(exp.getMessage());
                    enableButton(testButton, false);
                }
            }
        };

        collectibleField.addListener(validationListener);
        textArea.addListener(validationListener);

        validationListener.changed(null, null);
    }

    private void enableButton(Button button, boolean enabled) {
        button.setTouchable(enabled ? Touchable.enabled : Touchable.disabled);
        button.setDisabled(!enabled);
    }

    private void createLevelsTable(Game game, Table mainTable, Table levelsTable) {
        TextButton backButton = new TextButton("Back", skin, "menu-warning");
        backButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        levelsTable.remove();
                        stage.addActor(mainTable);
                    }
                });
        levelsTable.add(backButton).left().colspan(2).width(150).pad(10).height(50).row();

        levelsTable.add(new Label("Tutorials", skin, "menu")).colspan(2).height(50).row();

        int levelNo = 1;
        boolean left = true;
        for (String level : tutorials) {
            try {
                GameLevel gameLevel = GameLevel.loadGameLevel(Gdx.files.classpath(level));

                Table levelTable = new Table();

                TextButton levelButton = new TextButton("Level " + levelNo, skin, "menu");
                levelButton.addListener(
                        new ChangeListener() {
                            @Override
                            public void changed(ChangeEvent event, Actor actor) {
                                gameScreen.loadLevel(gameLevel);
                                game.setScreen(gameScreen);
                            }
                        });
                Label levelLabel = new Label(gameLevel.getName(), skin);

                levelTable.add(levelButton).width(250).height(50).pad(10).padBottom(0f).row();
                levelTable.add(levelLabel).row();

                levelsTable.add(levelTable);
                if (!left)
                    levelsTable.row();

                left = !left;
            } catch (IOException e) {
                e.printStackTrace();
            }
            levelNo++;
        }
        if (!left)
            levelsTable.row();

        levelsTable.add(new Label("Campaign", skin, "menu")).colspan(2).height(50).row();

        levelNo = 1;
        left = true;
        for (String level : campaign) {
            try {
                GameLevel gameLevel = GameLevel.loadGameLevel(Gdx.files.classpath(level));

                Table levelTable = new Table();

                TextButton levelButton = new TextButton("Level " + levelNo, skin, "menu");
                levelButton.addListener(
                        new ChangeListener() {
                            @Override
                            public void changed(ChangeEvent event, Actor actor) {
                                gameScreen.loadLevel(gameLevel);
                                game.setScreen(gameScreen);
                            }
                        });
                Label levelLabel = new Label(gameLevel.getName(), skin);

                levelTable.add(levelButton).width(250).height(50).pad(10).padBottom(0f).row();
                levelTable.add(levelLabel).row();

                levelsTable.add(levelTable);
                if (!left)
                    levelsTable.row();

                left = !left;
            } catch (IOException e) {
                e.printStackTrace();
            }
            levelNo++;
        }
    }

    private void createMainTable(TextureAtlas textureAtlas, Table mainTable, Table levelsTable, Table settingsTable, Table editorTable) {
        Image titleImage = new Image(assetManager.get("images/title.png", Texture.class));

        TextButton startButton = new TextButton("Start", skin, "menu");
        startButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        mainTable.remove();
                        stage.addActor(levelsTable);
                    }
                });

        TextButton editorButton = new TextButton("Level Editor", skin, "menu");
        editorButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        mainTable.remove();
                        stage.addActor(editorTable);
                    }
                });

        TextButton settingsButton = new TextButton("Settings", skin, "menu");
        settingsButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        mainTable.remove();
                        stage.addActor(settingsTable);
                    }
                });

        TextButton exit = new TextButton("Exit", skin, "menu-warning");
        exit.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        Gdx.app.exit();
                    }
                });

        Label label = new Label("Use WASD or arrow keys to move in game", skin, "menu");

        mainTable.add(titleImage).pad(20).row();
        mainTable.add(startButton).width(250).height(50).pad(10).row();
        mainTable.add(settingsButton).width(250).height(50).pad(10).row();
        mainTable.add(editorButton).width(250).height(50).pad(10).row();
        mainTable.add(exit).width(250).height(50).pad(10).row();
        mainTable.add(label).padTop(50).row();
    }

    @Override
    public void show() {
        // Prepare your screen here.
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        stage.act(delta);

        soundSystem.update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
        Viewport viewport = stage.getViewport();
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        soundSystem.dispose();
        gameScreen.dispose();
        skin.dispose();
        stage.dispose();
    }
}