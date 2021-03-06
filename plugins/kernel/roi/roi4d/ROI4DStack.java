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
package plugins.kernel.roi.roi4d;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.canvas.IcyCanvas3D;
import icy.roi.BooleanMask2D;
import icy.roi.ROI;
import icy.roi.ROI3D;
import icy.roi.ROI4D;
import icy.roi.ROIEvent;
import icy.roi.ROIListener;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.type.rectangle.Rectangle4D;
import icy.util.XMLUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstract class defining a generic 4D ROI as a stack of individual 3D ROI slices.<br>
 * A stack having a single slice set at position Z = -1 means the slice apply on the whole Z
 * dimension.
 * 
 * @author Alexandre Dufour
 * @author Stephane Dallongeville
 * @param <R>
 *        the type of 3D ROI for each slice of this 4D ROI
 */
public class ROI4DStack<R extends ROI3D> extends ROI4D implements ROIListener, Iterable<R>
{
    public static final String PROPERTY_USECHILDCOLOR = "useChildColor";

    protected final TreeMap<Integer, R> slices = new TreeMap<Integer, R>();

    protected final Class<R> roiClass;
    protected boolean useChildColor;

    /**
     * Creates a new 4D ROI based on the given 3D ROI type.
     */
    public ROI4DStack(Class<R> roiClass)
    {
        super();

        this.roiClass = roiClass;
        useChildColor = false;
    }

    @Override
    protected ROIPainter createPainter()
    {
        return new ROI4DStackPainter();
    }

    /**
     * Create a new empty 3D ROI slice.
     */
    protected R createSlice()
    {
        try
        {
            return roiClass.newInstance();
        }
        catch (Exception e)
        {
            IcyExceptionHandler.showErrorMessage(e, true, true);
            return null;
        }
    }

    /**
     * Returns <code>true</code> if the ROI directly uses the 3D slice color draw property and
     * <code>false</code> if it uses the global 4D ROI color draw property.
     */
    public boolean getUseChildColor()
    {
        return useChildColor;
    }

    /**
     * Set to <code>true</code> if you want to directly use the 3D slice color draw property and
     * <code>false</code> to keep the global 4D ROI color draw property.
     * 
     * @see #setColor(int, Color)
     */
    public void setUseChildColor(boolean value)
    {
        if (useChildColor != value)
        {
            useChildColor = value;
            propertyChanged(PROPERTY_USECHILDCOLOR);
            // need to redraw it
            getOverlay().painterChanged();
        }
    }

    /**
     * Set the painter color for the specified ROI slice.
     * 
     * @see #setUseChildColor(boolean)
     */
    public void setColor(int t, Color value)
    {
        final ROI3D slice = getSlice(t);

        if (slice != null)
            slice.setColor(value);
    }

    @Override
    public void setColor(Color value)
    {
        beginUpdate();
        try
        {
            super.setColor(value);

            if (!getUseChildColor())
            {
                for (R slice : slices.values())
                    slice.setColor(value);
            }
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setOpacity(float value)
    {
        beginUpdate();
        try
        {
            super.setOpacity(value);

            for (R slice : slices.values())
                slice.setOpacity(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setStroke(double value)
    {
        beginUpdate();
        try
        {
            super.setStroke(value);

            for (R slice : slices.values())
                slice.setStroke(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setCreating(boolean value)
    {
        beginUpdate();
        try
        {
            super.setCreating(value);

            for (R slice : slices.values())
                slice.setCreating(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setReadOnly(boolean value)
    {
        beginUpdate();
        try
        {
            super.setReadOnly(value);

            for (R slice : slices.values())
                slice.setReadOnly(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setFocused(boolean value)
    {
        beginUpdate();
        try
        {
            super.setFocused(value);

            for (R slice : slices.values())
                slice.setFocused(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setSelected(boolean value)
    {
        beginUpdate();
        try
        {
            super.setSelected(value);

            for (R slice : slices.values())
                slice.setSelected(value);
        }
        finally
        {
            endUpdate();
        }
    }

    @Override
    public void setC(int value)
    {
        beginUpdate();
        try
        {
            super.setC(value);

            for (R slice : slices.values())
                slice.setC(value);
        }
        finally
        {
            endUpdate();
        }
    }

    /**
     * @return The size of this ROI stack along T.<br>
     *         Note that the returned value indicates the difference between upper and lower bounds
     *         of this ROI, but doesn't guarantee that all slices in-between exist (
     *         {@link #getSlice(int)} may still return <code>null</code>.<br>
     */
    public int getSizeT()
    {
        if (slices.isEmpty())
            return 0;

        return (slices.lastKey().intValue() - slices.firstKey().intValue()) + 1;
    }

    /**
     * Returns the ROI slice at given T position.
     */
    public R getSlice(int t)
    {
        return slices.get(Integer.valueOf(t));
    }

    /**
     * Returns the ROI slice at given T position.
     */
    public R getSlice(int t, boolean createIfNull)
    {
        R result = getSlice(t);

        if ((result == null) && createIfNull)
        {
            result = createSlice();
            if (result != null)
                setSlice(t, result);
        }

        return result;
    }

    /**
     * Sets the slice for the given T position.
     */
    protected void setSlice(int t, R roi3d)
    {
        // set T and C position
        roi3d.setT(t);
        roi3d.setC(getC());
        // listen events from this ROI
        roi3d.addListener(this);

        slices.put(Integer.valueOf(t), roi3d);

        // notify ROI changed
        roiChanged();
    }

    /**
     * Removes slice at the given T position and returns it.
     */
    protected R removeSlice(int t)
    {
        // remove the current slice (if any)
        final R result = slices.remove(Integer.valueOf(t));

        if (result != null)
            result.removeListener(this);

        // notify ROI changed
        roiChanged();

        return result;
    }

    /**
     * Removes all slices.
     */
    protected void clear()
    {
        for (R slice : slices.values())
            slice.removeListener(this);

        slices.clear();
    }

    /**
     * Called when a ROI slice has changed.
     */
    protected void sliceChanged(ROIEvent event)
    {
        final ROI source = event.getSource();

        switch (event.getType())
        {
            case ROI_CHANGED:
                roiChanged();
                break;

            case FOCUS_CHANGED:
                setFocused(source.isFocused());
                break;

            case SELECTION_CHANGED:
                setSelected(source.isSelected());
                break;

            case PROPERTY_CHANGED:
                final String propertyName = event.getPropertyName();

                if ((propertyName == null) || propertyName.equals(PROPERTY_READONLY))
                    setReadOnly(source.isReadOnly());
                if ((propertyName == null) || propertyName.equals(PROPERTY_CREATING))
                    setCreating(source.isCreating());
                break;
        }
    }

    @Override
    public Rectangle4D computeBounds4D()
    {
        final Rectangle3D xyzBounds = new Rectangle3D.Double();

        for (R slice : slices.values())
            xyzBounds.add(slice.getBounds3D());

        final int t;
        final int sizeT;

        if (!slices.isEmpty())
        {
            t = slices.firstKey().intValue();
            sizeT = getSizeT();
        }
        else
        {
            t = 0;
            sizeT = 0;
        }

        return new Rectangle4D.Double(xyzBounds.getX(), xyzBounds.getY(), xyzBounds.getZ(), t, xyzBounds.getSizeX(),
                xyzBounds.getSizeY(), xyzBounds.getSizeZ(), sizeT);
    }

    @Override
    public boolean contains(double x, double y, double z, double t)
    {
        final R roi3d = getSlice((int) t);

        if (roi3d != null)
            return roi3d.contains(x, y, z);

        return false;
    }

    @Override
    public boolean contains(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ,
            double sizeT)
    {
        final Rectangle4D bounds = getBounds4D();

        // easy discard
        if (!bounds.contains(x, y, z, t, sizeX, sizeY, sizeZ, sizeT))
            return false;

        for (int tc = (int) t; tc < (int) (t + sizeT); tc++)
        {
            final R roi3d = getSlice(tc);
            if ((roi3d == null) || !roi3d.contains(x, y, z, sizeX, sizeY, sizeZ))
                return false;
        }

        return true;
    }

    @Override
    public boolean intersects(double x, double y, double z, double t, double sizeX, double sizeY, double sizeZ,
            double sizeT)
    {
        final Rectangle4D bounds = getBounds4D();

        // easy discard
        if (!bounds.intersects(x, y, z, t, sizeX, sizeY, sizeZ, sizeT))
            return false;

        for (int tc = (int) t; tc < (int) (t + sizeT); tc++)
        {
            final R roi3d = getSlice(tc);
            if ((roi3d != null) && roi3d.intersects(x, y, z, sizeX, sizeY, sizeZ))
                return true;
        }

        return false;
    }

    @Override
    public boolean hasSelectedPoint()
    {
        // default
        return false;
    }

    @Override
    public double computeNumberOfContourPoints()
    {
        // 3D edge points = first slice points + inter slices edge points + last slice points
        // TODO: only approximation, fix this to use real 4D edge point
        double perimeter = 0;

        if (slices.size() <= 2)
        {
            for (R slice : slices.values())
                perimeter += slice.getNumberOfPoints();
        }
        else
        {
            final Entry<Integer, R> firstEntry = slices.firstEntry();
            final Entry<Integer, R> lastEntry = slices.lastEntry();
            final Integer firstKey = firstEntry.getKey();
            final Integer lastKey = lastEntry.getKey();

            perimeter = firstEntry.getValue().getNumberOfPoints();

            for (R slice : slices.subMap(firstKey, false, lastKey, false).values())
                perimeter += slice.getNumberOfContourPoints();

            perimeter += lastEntry.getValue().getNumberOfPoints();
        }

        return perimeter;
    }

    @Override
    public double computeNumberOfPoints()
    {
        double volume = 0;

        for (R slice : slices.values())
            volume += slice.getNumberOfPoints();

        return volume;
    }

    @Override
    public boolean[] getBooleanMask2D(int x, int y, int width, int height, int z, int t, boolean inclusive)
    {
        final R roi3d = getSlice(t);

        if (roi3d != null)
            return roi3d.getBooleanMask2D(x, y, width, height, z, inclusive);

        return null;
    }

    @Override
    public BooleanMask2D getBooleanMask2D(int z, int t, boolean inclusive)
    {
        final R roi3d = getSlice(t);

        if (roi3d != null)
            return roi3d.getBooleanMask2D(z, inclusive);

        return null;
    }

    // called when one of the slice ROI changed
    @Override
    public void roiChanged(ROIEvent event)
    {
        // propagate children change event
        sliceChanged(event);
    }

    @Override
    public Iterator<R> iterator()
    {
        return slices.values().iterator();
    }

    @Override
    public boolean loadFromXML(Node node)
    {
        beginUpdate();
        try
        {
            if (!super.loadFromXML(node))
                return false;

            // we don't need to save the 3D ROI class as the parent class already do it
            clear();

            for (Element e : XMLUtil.getElements(node, "slice"))
            {
                // faster than using complete XML serialization
                final R slice = createSlice();

                // error while reloading the ROI from XML
                if ((slice == null) || !slice.loadFromXML(e))
                    return false;

                setSlice(slice.getT(), slice);
            }
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

        for (R slice : slices.values())
        {
            Element sliceNode = XMLUtil.addElement(node, "slice");

            if (!slice.saveToXML(sliceNode))
                return false;
        }

        return true;
    }

    public class ROI4DStackPainter extends ROIPainter
    {
        R getSliceForCanvas(IcyCanvas canvas)
        {
            final int t = canvas.getPositionT();

            if (t >= 0)
                return getSlice(t);

            return null;
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (isActiveFor(canvas))
            {
                if (canvas instanceof IcyCanvas3D)
                {
                    // TODO

                }
                else if (canvas instanceof IcyCanvas2D)
                {
                    // forward event to current slice
                    final R slice = getSliceForCanvas(canvas);

                    if (slice != null)
                        slice.getOverlay().paint(g, sequence, canvas);
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.keyPressed(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final R slice = getSliceForCanvas(canvas);

                if (slice != null)
                    slice.getOverlay().keyPressed(e, imagePoint, canvas);
            }
        }

        @Override
        public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.keyReleased(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final R slice = getSliceForCanvas(canvas);

                if (slice != null)
                    slice.getOverlay().keyReleased(e, imagePoint, canvas);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseEntered(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final R slice = getSliceForCanvas(canvas);

                if (slice != null)
                    slice.getOverlay().mouseEntered(e, imagePoint, canvas);
            }
        }

        @Override
        public void mouseExited(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseExited(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final R slice = getSliceForCanvas(canvas);

                if (slice != null)
                    slice.getOverlay().mouseExited(e, imagePoint, canvas);
            }
        }

        @Override
        public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseMove(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final R slice = getSliceForCanvas(canvas);

                if (slice != null)
                    slice.getOverlay().mouseMove(e, imagePoint, canvas);
            }
        }

        @Override
        public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseDrag(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final R slice = getSliceForCanvas(canvas);

                if (slice != null)
                    slice.getOverlay().mouseDrag(e, imagePoint, canvas);
            }
        }

        @Override
        public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mousePressed(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final R slice = getSliceForCanvas(canvas);

                if (slice != null)
                    slice.getOverlay().mousePressed(e, imagePoint, canvas);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseReleased(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final R slice = getSliceForCanvas(canvas);

                if (slice != null)
                    slice.getOverlay().mouseReleased(e, imagePoint, canvas);
            }
        }

        @Override
        public void mouseClick(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseClick(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final R slice = getSliceForCanvas(canvas);

                if (slice != null)
                    slice.getOverlay().mouseClick(e, imagePoint, canvas);
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
        {
            // send event to parent first
            super.mouseWheelMoved(e, imagePoint, canvas);

            // then send it to active slice
            if (isActiveFor(canvas))
            {
                // forward event to current slice
                final R slice = getSliceForCanvas(canvas);

                if (slice != null)
                    slice.getOverlay().mouseWheelMoved(e, imagePoint, canvas);
            }
        }
    }
}
