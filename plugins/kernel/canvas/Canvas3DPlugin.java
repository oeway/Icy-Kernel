/*
 * Copyright 2010-2013 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package plugins.kernel.canvas;

import icy.canvas.Canvas3D;
import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginCanvas;

/**
 * Plugin wrapper for Canvas2D
 * 
 * @author Stephane
 */
public class Canvas3DPlugin extends Plugin implements PluginCanvas
{
    @Override
    public IcyCanvas createCanvas(Viewer viewer)
    {
        return new Canvas3D(viewer);
    }

    @Override
    public String getCanvasClassName()
    {
        return Canvas3D.class.getName();
    }
}
