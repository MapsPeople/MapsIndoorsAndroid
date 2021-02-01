package com.mapsindoors.stdapp.ui.common.listeners;

import androidx.annotation.Nullable;

/**
 * <p>Listener interface to catch floating action button events.</p>
 * @author Martin Hansen
 */
public interface FloatingActionListener {
	/**
	 * Listener method to catch search events from the menu.
	 */
	void onFABSelect( @Nullable String selectedType );
	void onFABListOpen();
	void onFABListClose();
	void onFABAnimationUpdate( float value );
}
