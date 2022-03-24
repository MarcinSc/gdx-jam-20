package com.gempukku.gdx.jam20;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GdxGraphDemo extends Game {
	private MainScreen mainScreen;

	@Override
	public void create() {
		mainScreen = new MainScreen(this);
		setScreen(mainScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		mainScreen.dispose();
	}
}