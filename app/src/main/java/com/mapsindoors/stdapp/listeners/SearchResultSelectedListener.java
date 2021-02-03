package com.mapsindoors.stdapp.listeners;

import androidx.annotation.Nullable;

/**
 * <p>Listener interface to catch location search results.</p>
 * @author Martin Hansen
 */
public interface SearchResultSelectedListener {
	/**
	 * Listener method to catch search events from the menu.
	 */
	void onSearchResultSelected( @Nullable String queryString, @Nullable Object searchResult );
}
