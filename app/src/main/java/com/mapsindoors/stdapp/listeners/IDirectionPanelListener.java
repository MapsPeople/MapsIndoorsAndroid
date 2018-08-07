package com.mapsindoors.stdapp.listeners;

/**
 * @author Jose J Varó - Copyright © 2018 MapsPeople A/S. All rights reserved.
 */
public interface IDirectionPanelListener
{
	void onLegSelected( int legIndex, int itemIndex );
	void onStepSelected( int legIndex, int stepIndex, int itemIndex );
}
