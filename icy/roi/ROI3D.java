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
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle5D;
import icy.util.XMLUtil;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

/**
 * 3D ROI base class
 */
public abstract class ROI3D extends ROI
{
    /**
     * @deprecated Use {@link ROI3D#getROI3DList(List)} instead.
     */
    @Deprecated
    public static ArrayList<ROI3D> getROI3DList(ArrayList<ROI> rois)
    {
        final ArrayList<ROI3D> result = new ArrayList<ROI3D>();

        for (ROI roi : rois)
            if (roi instanceof ROI3D)
                result.add((ROI3D) roi);

        return result;
    }

    /**
     * Return all 3D ROI from the ROI list
     */
    public static List<ROI3D> getROI3DList(List<ROI> rois)
    {
        final List<ROI3D> result = new ArrayList<ROI3D>();

        for (ROI roi : rois)
            if (roi instanceof ROI3D)
                result.add((ROI3D) roi);

        return result;
    }

    public static final String ID_T = "t";
    public static final String ID_C = "c";

    /**
     * t coordinate attachment
     */
    protected int t;
    /**
     * c coordinate attachment
     */
    protected int c;

    public ROI3D()
    {
        super();

        // by default we consider no specific T and C attachment
        t = -1;
        c = -1;
    }

    @Override
    final public int getDimension()
    {
        return 3;
    }

    /**
     * Tests if a specified {@link Point3D} is inside the ROI.
     * 
     * @param p
     *        the specified <code>Point3D</code> to be tested
     * @return <code>true</code> if the specified <code>Point3D</code> is inside the boundary of the
     *         <code>ROI</code>; <code>false</code> otherwise.
     */
    public boolean contains(Point3D p)
    {
        return contains(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Tests if the interior of the <code>ROI</code> entirely contains the specified
     * <code>Rectangle3D</code>. The {@code ROI.contains()} method allows a implementation to
     * conservatively return {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * <code>Rectangle3D</code> are prohibitively expensive.
     * </ul>
     * This means that for some ROIs this method might return {@code false} even though the
     * {@code ROI} contains the {@code Rectangle3D}.
     * 
     * @param r
     *        The specified <code>Rectangle3D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the
     *         <code>Rectangle3D</code>; <code>false</code> otherwise or, if the <code>ROI</code>
     *         contains the <code>Rectangle3D</code> and the <code>intersects</code> method returns
     *         <code>true</code> and the containment calculations would be too expensive to perform.
     * @see #contains(double, double, double, double, double, double)
     */
    public boolean contains(Rectangle3D r)
    {
        return contains(r.getX(), r.getY(), r.getZ(), r.getSizeX(), r.getSizeY(), r.getSizeZ());
    }

    /**
     * Tests if the specified coordinates are inside the <code>ROI</code>.
     * 
     * @param x
     *        the specified X coordinate to be tested
     * @param y
     *        the specified Y coordinate to be tested
     * @param z
     *        the specified Z coordinate to be tested
     * @return <code>true</code> if the specified 3D coordinates are inside the <code>ROI</code>
     *         boundary; <code>false</code> otherwise.
     */
    public abstract boolean contains(double x, double y, double z);

    /**
     * Tests if the <code>ROI</code> entirely contains the specified 3D rectangular area. All
     * coordinates that lie inside the rectangular area must lie within the <code>ROI</code> for the
     * entire rectangular area to be considered contained within the <code>ROI</code>.
     * <p>
     * The {@code ROI.contains()} method allows a {@code ROI} implementation to conservatively
     * return {@code false} when:
     * <ul>
     * <li>the <code>intersect</code> method returns <code>true</code> and
     * <li>the calculations to determine whether or not the <code>ROI</code> entirely contains the
     * rectangular area are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code false} even though the
     * {@code ROI} contains the rectangular area.
     * 
     * @param x
     *        the X coordinate of the minimum corner position of the specified rectangular area
     * @param y
     *        the Y coordinate of the minimum corner position of the specified rectangular area
     * @param z
     *        the Z coordinate of the minimum corner position of the specified rectangular area
     * @param sizeX
     *        size for X dimension of the specified rectangular area
     * @param sizeY
     *        size for Y dimension of the specified rectangular area
     * @param sizeZ
     *        size for Z dimension of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>ROI</code> entirely contains the
     *         specified 3D rectangular area; <code>false</code> otherwise or, if the
     *         <code>ROI</code> contains the 3D rectangular area and the <code>intersects</code>
     *         method returns <code>true</code> and the containment calculations would be too
     *         expensive to perform.
     */
    public abstract boolean contains(double x, double y, double z, double sizeX, double sizeY, double sizeZ);

    @Override
    public boolean contains(double x, double y, double z, double t, double c)
    {
        final boolean tok;
        final boolean cok;

        if (getT() == -1)
            tok = true;
        else
            tok = (t > getT()) && (t < (getT() + 1d));
        if (getC() == -1)
            cok = true;
        else
            cok = (c >= getC()) && (c < (getC() + 1d));

        return contains(x, y, z) && tok && cok;
    }

    @Override
    public boolean contains(double x, double y, double z, double t, double c, double sizeX, double sizeY, double sizeZ,
            double sizeT, double sizeC)
    {
        final boolean tok;
        final boolean cok;

        if (getT() == -1)
            tok = true;
        else
            tok = (t >= getT()) && ((t + sizeT) <= (getT() + 1d));
        if (getC() == -1)
            cok = true;
        else
            cok = (c >= getC()) && ((c + sizeC) <= (getC() + 1d));

        return contains(x, y, z, sizeX, sizeY, sizeZ) && tok && cok;
    }

    /*
     * Generic implementation using the BooleanMask which is not accurate and slow.
     * Override this for specific ROI type.
     */
    @Override
    public boolean contains(ROI roi)
    {
        if (roi instanceof ROI3D)
        {
            final ROI3D roi3d = (ROI3D) roi;

            if (isActiveFor(roi3d.getT(), roi3d.getC()))
                return getBooleanMask(false).contains(roi3d.getBooleanMask(false));

            return false;
        }

        // do it the other way
        return roi.intersects(this);
    }

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * <code>Rectangle3D</code>. The {@code ROI.intersects()} method allows a {@code ROI}
     * implementation to conservatively return {@code true} when:
     * <ul>
     * <li>there is a high probability that the <code>Rectangle3D</code> and the <code>ROI</code>
     * intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the
     * {@code Rectangle3D} does not intersect the {@code ROI}.
     * 
     * @param r
     *        the specified <code>Rectangle3D</code>
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         specified <code>Rectangle3D</code> intersect, or are both highly likely to intersect
     *         and intersection calculations would be too expensive to perform; <code>false</code>
     *         otherwise.
     * @see #intersects(double, double, double,double, double, double)
     */
    public boolean intersects(Rectangle3D r)
    {
        return intersects(r.getX(), r.getY(), r.getZ(), r.getSizeX(), r.getSizeY(), r.getSizeZ());
    }

    /**
     * Tests if the interior of the <code>ROI</code> intersects the interior of a specified
     * 3D rectangular area. The 3D rectangular area is considered to intersect the <code>ROI</code>
     * if any point is contained in both the interior of the <code>ROI</code> and the specified
     * rectangular area.
     * <p>
     * The {@code ROI.intersects()} method allows a {@code ROI} implementation to conservatively
     * return {@code true} when:
     * <ul>
     * <li>there is a high probability that the 3D rectangular area and the <code>ROI</code>
     * intersect, but
     * <li>the calculations to accurately determine this intersection are prohibitively expensive.
     * </ul>
     * This means that for some {@code ROIs} this method might return {@code true} even though the
     * 3D rectangular area does not intersect the {@code ROI}.
     * 
     * @param x
     *        the X coordinate of the minimum corner position of the specified rectangular area
     * @param y
     *        the Y coordinate of the minimum corner position of the specified rectangular area
     * @param z
     *        the Z coordinate of the minimum corner position of the specified rectangular area
     * @param sizeX
     *        size for X dimension of the specified rectangular area
     * @param sizeY
     *        size for Y dimension of the specified rectangular area
     * @param sizeZ
     *        size for Z dimension of the specified rectangular area
     * @return <code>true</code> if the interior of the <code>ROI</code> and the interior of the
     *         rectangular area intersect, or are both highly likely to intersect and intersection
     *         calculations would be too expensive to perform; <code>false</code> otherwise.
     */
    public abstract boolean intersects(double x, double y, double z, double sizeX, double sizeY, double sizeZ);

    @Override
    public boolean intersects(double x, double y, double z, double t, double c, double sizeX, double sizeY,
            double sizeZ, double sizeT, double sizeC)
    {
        // easy discard
        if ((sizeX == 0d) || (sizeY == 0d) || (sizeZ == 0d) || (sizeT == 0d) || (sizeC == 0d))
            return false;

        final boolean tok;
        final boolean cok;

        if (getT() == -1)
            tok = true;
        else
            tok = ((t + sizeT) > getT()) && (t < (getT() + 1d));
        if (getC() == -1)
            cok = true;
        else
            cok = ((c + sizeC) > getC()) && (c < (getC() + 1d));

        return intersects(x, y, z, sizeX, sizeY, sizeZ) && tok && cok;
    }

    /*
     * Generic implementation using the BooleanMask which is not accurate and slow.
     * Override this for specific ROI type.
     */
    @Override
    public boolean intersects(ROI roi)
    {
        if (roi instanceof ROI3D)
        {
            final ROI3D roi3d = (ROI3D) roi;

            if (isActiveFor(roi3d.getT(), roi3d.getC()))
                return getBooleanMask(true).intersects(roi3d.getBooleanMask(true));

            return false;
        }

        // do it the other way
        return roi.intersects(this);
    }

    /**
     * Calculate and returns the 3D bounding box of the <code>ROI</code>.<br>
     * This method is used by {@link #getBounds3D()} which should try to cache the result as the
     * bounding box calculation can take some computation time for complex ROI.
     */
    public abstract Rectangle3D computeBounds3D();

    @Override
    public Rectangle5D computeBounds5D()
    {
        final Rectangle3D bounds3D = computeBounds3D();
        final Rectangle5D.Double result = new Rectangle5D.Double(bounds3D.getX(), bounds3D.getY(), bounds3D.getZ(), 0d,
                0d, bounds3D.getSizeX(), bounds3D.getSizeY(), bounds3D.getSizeZ(), 0d, 0d);

        if (getT() == -1)
        {
            result.t = Double.NEGATIVE_INFINITY;
            result.sizeT = Double.POSITIVE_INFINITY;
        }
        else
        {
            result.t = getT();
            result.sizeT = 1d;
        }
        if (getC() == -1)
        {
            result.c = Double.NEGATIVE_INFINITY;
            result.sizeC = Double.POSITIVE_INFINITY;
        }
        else
        {
            result.c = getC();
            result.sizeC = 1d;
        }

        return result;
    }

    /**
     * Returns an integer {@link Rectangle3D} that completely encloses the <code>ROI</code>. Note
     * that there is no guarantee that the returned <code>Rectangle3D</code> is the smallest
     * bounding box that encloses the <code>ROI</code>, only that the <code>ROI</code> lies entirely
     * within the indicated <code>Rectangle3D</code>. The returned <code>Rectangle3D</code> might
     * also fail to completely enclose the <code>ROI</code> if the <code>ROI</code> overflows the
     * limited range of the integer data type. The <code>getBounds3D</code> method generally returns
     * a tighter bounding box due to its greater flexibility in representation.
     * 
     * @return an integer <code>Rectangle3D</code> that completely encloses the <code>ROI</code>.
     */
    public Rectangle3D.Integer getBounds()
    {
        return getBounds3D().toInteger();
    }

    /**
     * Returns the bounding box of the <code>ROI</code>. Note that there is no guarantee that the
     * returned {@link Rectangle3D} is the smallest bounding box that encloses the <code>ROI</code>,
     * only that the <code>ROI</code> lies entirely within the indicated <code>Rectangle3D</code>.
     * 
     * @return an instance of <code>Rectangle3D</code> that is a bounding box of the
     *         <code>ROI</code>.
     */
    public Rectangle3D getBounds3D()
    {
        return getBounds5D().toRectangle3D();
    }

    /**
     * Returns the integer ROI position which normally correspond to the <i>minimum</i> point of the
     * ROI bounds.
     * 
     * @see #getBounds()
     */
    public Point3D.Integer getPosition()
    {
        final Rectangle3D.Integer bounds = getBounds();
        return new Point3D.Integer(bounds.x, bounds.y, bounds.z);
    }

    /**
     * Returns the high precision ROI position which normally correspond to the <i>minimum</i> point
     * of the ROI
     * bounds.<br>
     * 
     * @see #getBounds3D()
     */
    public Point3D getPosition3D()
    {
        return getBounds3D().getPosition();
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

    /**
     * Set the <code>ROI</code> 3D bounds.<br>
     * Note that not all ROI supports bounds modification and you should call
     * {@link #canSetBounds()} first to test if the operation is supported.<br>
     * 
     * @param bounds
     *        new ROI 3D bounds
     */
    public void setBounds3D(Rectangle3D bounds)
    {
        // do nothing by default (not supported)
    }

    @Override
    public void setBounds5D(Rectangle5D bounds)
    {
        // infinite T dim ?
        if (bounds.getSizeT() == Double.POSITIVE_INFINITY)
            setT(-1);
        else
            setT((int) bounds.getT());
        // infinite C dim ?
        if (bounds.getSizeC() == Double.POSITIVE_INFINITY)
            setC(-1);
        else
            setC((int) bounds.getC());

        setBounds3D(bounds.toRectangle3D());
    }

    /**
     * Set the <code>ROI</code> 3D position.<br>
     * Note that not all ROI supports position modification and you should call
     * {@link #canSetPosition()} first to test if the operation is supported.<br>
     * 
     * @param position
     *        new ROI 3D position
     */
    public void setPosition3D(Point3D position)
    {
        // do nothing by default (not supported)
    }

    @Override
    public void setPosition5D(Point5D position)
    {
        // infinite T dim ?
        if (position.getT() == Double.NEGATIVE_INFINITY)
            setT(-1);
        else
            setT((int) position.getT());
        // infinite C dim ?
        if (position.getC() == Double.NEGATIVE_INFINITY)
            setC(-1);
        else
            setC((int) position.getC());

        setPosition3D(position.toPoint3D());
    }

    @Override
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, int t, int c, boolean inclusive)
    {
        // not on the correct T, C position
        if (!isActiveFor(t, c))
            return null;

        return getBooleanMask2D(x, y, width, height, z, inclusive);
    }

    /**
     * Get the boolean bitmap mask for the specified rectangular area of the roi and for the
     * specified Z position.<br>
     * if the pixel (x,y) is contained in the roi Z position then result[(y * width) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi Z position then result[(y * width) + x] =
     * false
     * 
     * @param x
     *        the X coordinate of the upper-left corner of the specified rectangular area
     * @param y
     *        the Y coordinate of the upper-left corner of the specified rectangular area
     * @param width
     *        the width of the specified rectangular area
     * @param height
     *        the height of the specified rectangular area
     * @param z
     *        Z position we want to retrieve the boolean mask
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     * @return the boolean bitmap mask
     */
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, boolean inclusive)
    {
        final boolean[] result = new boolean[width * height];

        // simple and basic implementation, override it to have better performance
        int offset = 0;
        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                if (inclusive)
                    result[offset] = intersects(x + i, y + j, z, 1d, 1d, 1d);
                else
                    result[offset] = contains(x + i, y + j, z, 1d, 1d, 1d);
                offset++;
            }
        }

        return result;
    }

    /**
     * Get the boolean bitmap mask for the specified rectangular area of the roi and for the
     * specified Z position.<br>
     * if the pixel (x,y) is contained in the roi Z position then result[(y * width) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi Z position then result[(y * width) + x] =
     * false
     * 
     * @param rect
     *        2D rectangular area we want to retrieve the boolean mask
     * @param z
     *        Z position we want to retrieve the boolean mask
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public boolean[] getBooleanMask2D(Rectangle rect, int z, boolean inclusive)
    {
        return getBooleanMask2D(rect.x, rect.y, rect.width, rect.height, z, inclusive);
    }

    @Override
    public BooleanMask2D getBooleanMask2D(int z, int t, int c, boolean inclusive)
    {
        // not on the correct T, C position
        if (!isActiveFor(t, c))
            return null;

        return getBooleanMask2D(z, inclusive);
    }

    /**
     * Get the {@link BooleanMask2D} object representing the roi for the specified Z position.<br>
     * It contains the rectangle mask bounds and the associated boolean array mask.<br>
     * if the pixel (x,y) is contained in the roi Z position then result.mask[(y * w) + x] = true<br>
     * if the pixel (x,y) is not contained in the roi Z position then result.mask[(y * w) + x] =
     * false
     * 
     * @param z
     *        Z position we want to retrieve the boolean mask
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask2D getBooleanMask2D(int z, boolean inclusive)
    {
        final Rectangle bounds = getBounds3D().toRectangle2D().getBounds();

        // no mask
        if (bounds.isEmpty())
            return null;

        final BooleanMask2D result = new BooleanMask2D(bounds, getBooleanMask2D(bounds, z, inclusive));

        // optimized bounds to optimize memory usage for this specific Z slice mask
        result.optimizeBounds();

        return result;
    }

    /**
     * Get the {@link BooleanMask3D} object representing the roi.<br>
     * It contains the 3D rectangle mask bounds and the associated boolean array mask.<br>
     * 
     * @param inclusive
     *        If true then all partially contained (intersected) pixels are included in the mask.
     */
    public BooleanMask3D getBooleanMask(boolean inclusive)
    {
        final Rectangle3D.Integer bounds = getBounds();
        final BooleanMask2D masks[] = new BooleanMask2D[bounds.sizeZ];

        for (int z = 0; z < masks.length; z++)
            masks[z] = getBooleanMask2D(z, inclusive);

        return new BooleanMask3D(bounds, masks);
    }

    /*
     * Generic implementation for ROI3D using the BooleanMask object so
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
     * Generic implementation for ROI3D using the BooleanMask object so
     * the result is just an approximation.
     * Override to optimize for specific ROI.
     */
    @Override
    public double computeNumberOfPoints()
    {
        // approximation by using number of point of boolean mask
        return getBooleanMask(true).getPointsAsIntArray().length / getDimension();
    }

    /**
     * Return surface area of the 3D ROI in pixels.<br>
     * This is basically the number of pixel representing ROI edges.<br>
     * 
     * @see #getNumberOfContourPoints()
     * @see #computeNumberOfContourPoints()
     */
    public double getSurfaceArea()
    {
        return getNumberOfContourPoints();
    }

    /**
     * Return volume of the 3D ROI in pixels.<br>
     * This is basically the number of pixel contained in the ROI.<br>
     * 
     * @see #getNumberOfPoints()
     * @see #computeNumberOfPoints()
     */
    @Override
    public double getVolume()
    {
        return getNumberOfPoints();
    }

    /**
     * @return the t
     */
    public int getT()
    {
        return t;
    }

    /**
     * @param value
     *        the t to set
     */
    public void setT(int value)
    {
        if (t != value)
        {
            t = value;
            roiChanged();
        }
    }

    /**
     * @return the c
     */
    public int getC()
    {
        return c;
    }

    /**
     * @param value
     *        the c to set
     */
    public void setC(int value)
    {
        if (c != value)
        {
            c = value;
            roiChanged();
        }
    }

    @Override
    public boolean isActiveFor(IcyCanvas canvas)
    {
        return isActiveFor(canvas.getPositionT(), canvas.getPositionC());
    }

    /**
     * Return true if the ROI is active for the specified T, C coordinates
     */
    public boolean isActiveFor(int t, int c)
    {
        return ((getT() == -1) || (t == -1) || (getT() == t)) && ((getC() == -1) || (c == -1) || (getC() == c));
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            setT(XMLUtil.getElementIntValue(node, ID_T, -1));
            setC(XMLUtil.getElementIntValue(node, ID_C, -1));
        }
        finally
        {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean saveToXML(Node node)
    {
        if (!super.saveToXML(node))
            return false;

        XMLUtil.setElementIntValue(node, ID_T, getT());
        XMLUtil.setElementIntValue(node, ID_C, getC());

        return true;
    }
}
