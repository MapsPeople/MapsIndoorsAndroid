package com.mapsindoors.stdapp.apis.googleplaces.listeners;


import androidx.annotation.NonNull;

import com.mapsindoors.stdapp.apis.googleplaces.models.AutoCompleteField;

import java.util.List;

public abstract class AutoCompleteSuggestionListener
{
	public abstract void onResult( @NonNull List<AutoCompleteField> autoCompleteFields, @NonNull String status );
}
