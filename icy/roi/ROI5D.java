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
package icy.roi;

import icy.canvas.IcyCanvas;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;
import icy.type.rectangle.Rectangle5D;

import java.util.ArrayList;
import java.util.List;

/**
 * 5D ROI base class.
 */
public abstract class ROI5D extends ROI
{
    /**
     * @deprecated Use {@link ROI5D#getROI5DList(List)} instead.
     */
    @Deprecated
    public static ArrayList<ROI5D> getROI5DList(ArrayList<ROI> rois)
    {
        final ArrayList<ROI5D> result = new ArrayList<ROI5D>();

        for (ROI roi : rois)
            if (roi instanceof ROI5D)
                result.add((ROI5D) roi);

        return result;
    }

    /**
     * Return all 5D ROI from the ROI list
     */
    public static List<ROI5D> getROI5DList(List<ROI> rois)
    {
        final List<ROI5D> result = new ArrayList<ROI5D>();

        for (ROI roi : rois)
            if (roi instanceof ROI5D)
                result.add((ROI5D) roi);

        return result;
    }

    public ROI5D()
    {
        super();
    }

    @Override
    final public int getDimension()
    {
        return 5;
    }

    @Override
    public boolean isActiveFor(IcyCanvas canvas)
    {
        return true;
    }

    /**
     * Returns an integer {@link Rectangle5D} that completely encloses the <code>ROI</code>. Note
     * that there is no guarantee that the returned <code>Rectangle5D</code> is the smallest
     * bounding box that encloses the <code>ROI</code>, only that the <code>ROI</code> lies entirely
     * within the indicated <code>Rectangle5D</code>. The returned <code>Rectangle5D</code> might
     * also fail to completely enclose the <code>ROI</code> if the <code>ROI</code> overflows the
     * limited range of the integer data type. The <code>getBounds5D</code> method generally returns
     * a tighter bounding box due to its greater flexibility in representation.
     * 
     * @return an integer <code>Rectangle5D</code> that completely encloses the <code>ROI</code>.
     */
    public Rectangle5D.Integer getBounds()
    {
        return getBounds5D().toInteger();
    }

    /**
     * Returns the integer ROI position which normally correspond to the <i>minimum</i> point of the
     * ROI bounds.
     * 
     * @see #getBounds()
     */
    public Point5D.Integer getPosition()
    {
        final Rectangle5D.Integer bounds = getBounds();
        return new Point5D.Integer(bounds.x, bounds.y, bounds.z, bounds.t, bounds.c);
    }

    @Override
    public boolean canSetBounds()
    {
        // default
        return false;
    }

    @Override
    public boolean canSetPosition()
    {
        // default
        return false;
    }

    @Override
    public void setBounds5D(Rectangle5D bounds)
    {
        // do nothing by default (not supported)
    }

    @Override
    public void setPosition5D(Point5D position)
    {
        // do nothing by default (not supported)
    }

    /*
     * Generic implementation using the BooleanMask which is not accurate and slow.
     * Override this for specific ROI type.
     */
    @Override
    public boolean contains(ROI roi)
    {
        if (roi instanceof ROI5D)
            return getBooleanMask(false).contains(((ROI5D) roi).getBooleanMask(false));

        // do it the other way
        return roi.intersects(this);
    }

    /*
     * Generic implementation using the BooleanMask which is not accurate and slow.
     * Override this for specific ROI type.
     */
    @Override
    public boolean intersects(ROI roi)
    {
        if (roi instanceof ROI5D)
            return getBooleanMask(true).intersects(((ROI5D) roi).getBooleanMask(true));

        // do it the other way
        return roi.intersects(this);
    }

    /**
     * Get the {@link BooleanMask3D} object representing the roi for specified T,C position.<br>
     * It contains the 3D rectangle mask bounds and the associated boolean array mask.<br>
     * 
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask3D getBooleanMask3D(int t, int c, boolean inclusive)
    {
        final Rectangle3D.Integer bounds = getBounds5D().toRectangle3D().toInteger();
        final BooleanMask2D masks[] = new BooleanMask2D[bounds.sizeZ];

        for (int z = 0; z < masks.length; z++)
            masks[z] = getBooleanMask2D(z, t, c, inclusive);

        return new BooleanMask3D(bounds, masks);
    }

    /**
     * Get the {@link BooleanMask4D} object representing the roi for specified C position.<br>
     * It contains the 4D rectangle mask bounds and the associated boolean array mask.<br>
     * 
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask4D getBooleanMask4D(int c, boolean inclusive)
    {
        final Rectangle4D.Integer bounds = getBounds5D().toRectangle4D().toInteger();
        final BooleanMask3D masks[] = new BooleanMask3D[bounds.sizeT];

        for (int t = 0; t < masks.length; t++)
            masks[t] = getBooleanMask3D(t, c, inclusive);

        return new BooleanMask4D(bounds, masks);
    }

    /**
     * Get the {@link BooleanMask5D} object representing the roi.<br>
     * It contains the 5D rectangle mask bounds and the associated boolean array mask.<br>
     * 
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask5D getBooleanMask(boolean inclusive)
    {
        final Rectangle5D.Integer bounds = getBounds();
        final BooleanMask4D masks[] = new BooleanMask4D[bounds.sizeC];

        for (int c = 0; c < masks.length; c++)
            masks[c] = getBooleanMask4D(c, inclusive);

        return new BooleanMask5D(bounds, masks);
    }

    /*
     * Generic implementation for ROI5D using the BooleanMask object so
     * the result is just an approximation.
     * Override to optimize for specific ROI.
     */
    @Override
    public double computeNumberOfContourPoints()
    {
        // approximation by using number of point of the edge of boolean mask
        return getBooleanMask(true).getContourPointsAsIntArray().length / getDimension();
    }

    /*
     * Generic implementation for ROI5D using the BooleanMask object so
     * the result is just an approximation.
     * Override to optimize for specific ROI.
     */
    @Override
    public double computeNumberOfPoints()
    {
        // approximation by using number of point of boolean mask
        return getBooleanMask(true).getPointsAsIntArray().length / getDimension();
    }

}
