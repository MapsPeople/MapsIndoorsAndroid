package com.mapsindoors.stdapp.ui.common.models;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;

import com.mapsindoors.stdapp.helpers.MapsIndoorsRouteHelper;
import com.mapsindoors.stdapp.ui.common.adapters.IconTextListAdapter;

public class IconTextElement
{
	public String name = null;
	public String subText = null;
	public String distText = null;

	@DrawableRes
	public Integer imgId;

	public Bitmap img;
	public Object obj;
	public IconTextListAdapter.Objtype type;

	public IconTextElement(String name, Integer imgId, Object obj, IconTextListAdapter.Objtype type)
	{
		this.name = name;
		this.imgId = imgId;
		this.img = null;
		this.obj = obj;
		this.type = type;
	}

	public IconTextElement( String name, String subline, @DrawableRes Integer imgId, Object obj, IconTextListAdapter.Objtype type)
	{
		this.name = name;
		this.subText = subline;

		this.imgId = imgId;
		this.img = null;
		this.obj = obj;
		this.type = type;
	}

	public IconTextElement(String name, Bitmap img, Object obj, IconTextListAdapter.Objtype type)
	{
		this.name = name;
		this.imgId = -1;
		this.img = img;
		this.obj = obj;
		this.type = type;
	}

	public IconTextElement(String name, String subline, double distance, @DrawableRes Integer imgId, Object obj, IconTextListAdapter.Objtype type)
	{
		this.name = name;
		this.subText = subline;
		this.distText = MapsIndoorsRouteHelper.getFormattedDistance( (int) distance );
		this.imgId = imgId;
		this.img = null;
		this.obj = obj;
		this.type = type;
	}

	public IconTextElement(String name, String subline, double distance, Bitmap img, Object obj, IconTextListAdapter.Objtype type)
	{
		this.name = name;
		this.subText = subline;
		this.distText = MapsIndoorsRouteHelper.getFormattedDistance( (int) distance );
		this.imgId = -1;
		this.img = img;
		this.obj = obj;
		this.type = type;
	}

	public IconTextElement(String name, String subline, int distance, @DrawableRes Integer imgId, Object obj, IconTextListAdapter.Objtype type)
	{
		this.name = name;
		this.subText = subline;
		this.distText = MapsIndoorsRouteHelper.getFormattedDistance( distance );
		this.imgId = imgId;
		this.img = null;
		this.obj = obj;
		this.type = type;
	}

	public IconTextElement(String name, String subline, int distance, Bitmap img, Object obj, IconTextListAdapter.Objtype type)
	{
		this.name = name;
		this.subText = subline;
		this.distText = MapsIndoorsRouteHelper.getFormattedDistance( distance );
		this.imgId = -1;
		this.img = img;
		this.obj = obj;
		this.type = type;
	}
}
