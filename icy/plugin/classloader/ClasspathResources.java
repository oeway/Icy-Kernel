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

package icy.plugin.classloader;

import icy.file.FileUtil;
import icy.plugin.classloader.exception.JclException;
import icy.plugin.classloader.exception.ResourceNotFoundException;
import icy.system.IcyExceptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that builds a local classpath by loading resources from different
 * files/paths
 * 
 * @author Kamran Zafar
 * @author Stephane Dallongeville
 */
public class ClasspathResources extends JarResources
{

    private static Logger logger = Logger.getLogger(ClasspathResources.class.getName());
    private boolean ignoreMissingResources;

    public ClasspathResources()
    {
        super();
        ignoreMissingResources = Configuration.suppressMissingResourceException();
    }

    /**
     * Attempts to load a remote resource (jars, properties files, etc)
     * 
     * @param url
     */
    protected void loadRemoteResource(URL url)
    {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Attempting to load a remote resource.");

        if (url.toString().toLowerCase().endsWith(".jar"))
        {
            try
            {
                loadJar(url);
            }
            catch (IOException e)
            {
                System.err.print("JarResources.loadJar(" + url + ") error:");
                IcyExceptionHandler.showErrorMessage(e, false, true);
            }
            return;
        }

        if (jarEntryUrls.containsKey(url.toString()))
        {
            if (!collisionAllowed)
                throw new JclException("Resource " + url.toString() + " already loaded");

            if (logger.isLoggable(Level.FINEST))
                logger.finest("Resource " + url.toString() + " already loaded; ignoring entry...");
            return;
        }

        if (logger.isLoggable(Level.FINEST))
            logger.finest("Loading remote resource.");

        jarEntryUrls.put(url.toString(), url);
    }

    /**
     * Loads and returns content the remote resource (jars, properties files, etc)
     */
    protected byte[] loadRemoteResourceContent(URL url)
    {
        InputStream stream = null;
        ByteArrayOutputStream out = null;
        try
        {
            stream = url.openStream();
            out = new ByteArrayOutputStream();

            int byt;
            while (((byt = stream.read()) != -1))
                out.write(byt);

            loadedSize += out.size();

            // System.out.println("Entry Name: " + url.getPath() + ", Size: " + out.size() + " (" +
            // (loadedSize / 1024)
            // + " KB)");

            return out.toByteArray();
        }
        catch (IOException e)
        {
            throw new JclException(e);
        }
    }

    /**
     * Reads local and remote resources
     * 
     * @param url
     */
    protected void loadResource(URL url)
    {
        try
        {
            final File file = new File(url.toURI());
            // Is Local
            loadResource(file, FileUtil.getGenericPath(file.getAbsolutePath()));
        }
        catch (IllegalArgumentException iae)
        {
            // Is Remote
            loadRemoteResource(url);
        }
        catch (URISyntaxException e)
        {
            throw new JclException("URISyntaxException", e);
        }
    }

    /**
     * Reads local resources from - Jar files - Class folders - Jar Library
     * folders
     * 
     * @param path
     */
    protected void loadResource(String path)
    {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Resource: " + path);

        File fp = new File(path);

        if (!fp.exists() && !ignoreMissingResources)
        {
            throw new JclException("File/Path does not exist");
        }

        loadResource(fp, FileUtil.getGenericPath(path));
    }

    /**
     * Reads local resources from - Jar files - Class folders - Jar Library
     * folders
     * 
     * @param fol
     * @param packName
     */
    protected void loadResource(File fol, String packName)
    {
        // FILE
        if (fol.isFile())
        {
            // if (fol.getName().toLowerCase().endsWith(".class"))
            // {
            // loadClass(fol.getAbsolutePath(), packName);
            // }
            // else
            // {
            if (fol.getName().toLowerCase().endsWith(".jar"))
            {
                try
                {
                    loadJar(fol.getAbsolutePath());
                }
                catch (IOException e)
                {
                    System.err.print("JarResources.loadJar(" + fol.getAbsolutePath() + ") error:");
                    IcyExceptionHandler.showErrorMessage(e, false, true);
                }
            }
            else
                loadResourceInternal(fol, packName);
            // }
        }
        // DIRECTORY
        else
        {
            if (fol.list() != null)
            {
                for (String f : fol.list())
                {
                    File fl = new File(fol.getAbsolutePath() + "/" + f);

                    String pn = packName;

                    if (fl.isDirectory())
                    {

                        if (!pn.equals(""))
                            pn = pn + "/";

                        pn = pn + fl.getName();
                    }

                    loadResource(fl, pn);
                }
            }
        }
    }

    /**
     * Loads the local resource.
     */
    protected void loadResourceInternal(File file, String pack)
    {
        String entryName = "";

        if (pack.length() > 0)
            entryName = pack + "/";
        entryName += file.getName();

        if (jarEntryUrls.containsKey(entryName))
        {
            if (!collisionAllowed)
                throw new JclException("Resource " + entryName + " already loaded");

            if (logger.isLoggable(Level.FINEST))
                logger.finest("Resource " + entryName + " already loaded; ignoring entry...");
            return;
        }

        if (logger.isLoggable(Level.FINEST))
            logger.finest("Loading resource: " + entryName);

        try
        {
            jarEntryUrls.put(entryName, file.toURI().toURL());
        }
        catch (Exception e)
        {
            if (logger.isLoggable(Level.SEVERE))
                logger.finest("Error while loading: " + entryName);
        }
    }

    @Override
    protected boolean loadContent(String name, URL url)
    {
        // not loaded by parent ?
        if (!super.loadContent(name, url))
        {
            if (url.getProtocol().equalsIgnoreCase(("file")))
            {
                final byte content[] = loadResourceContent(url);

                if (content != null)
                {
                    setResourceContent(name, content);
                    return true;
                }

                return false;
            }

            final byte content[] = loadRemoteResourceContent(url);

            if (content != null)
            {
                setResourceContent(name, content);
                return true;
            }

            return false;
        }

        return true;
    }

    /**
     * Loads and returns the local resource content.
     */
    protected byte[] loadResourceContent(URL url)
    {
        File resourceFile;

        try
        {
            resourceFile = new File(url.toURI());
        }
        catch (Exception e)
        {
            return null;
        }

        FileInputStream fis = null;
        byte[] content = null;
        try
        {
            final int len = (int) resourceFile.length();
            fis = new FileInputStream(resourceFile);
            content = new byte[len];

            loadedSize += len;

            // System.out.println("Entry Name: " + url.getPath() + ", Size: " + len + " (" +
            // (loadedSize / 1024) + " KB)");

            if (fis.read(content) != -1)
                return content;

            return null;
        }
        catch (IOException e)
        {
            throw new JclException(e);
        }
        finally
        {
            try
            {
                fis.close();
            }
            catch (IOException e)
            {
                throw new JclException(e);
            }
        }
    }

    /**
     * Removes the loaded resource
     * 
     * @param resource
     */
    public void unload(String resource)
    {
        if (jarEntryContents.containsKey(resource))
        {
            if (logger.isLoggable(Level.FINEST))
                logger.finest("Removing resource " + resource);
            jarEntryContents.remove(resource);
        }
        else
        {
            throw new ResourceNotFoundException(resource, "Resource not found in local ClasspathResources");
        }
    }

    public boolean isCollisionAllowed()
    {
        return collisionAllowed;
    }

    public void setCollisionAllowed(boolean collisionAllowed)
    {
        this.collisionAllowed = collisionAllowed;
    }

    public boolean isIgnoreMissingResources()
    {
        return ignoreMissingResources;
    }

    public void setIgnoreMissingResources(boolean ignoreMissingResources)
    {
        this.ignoreMissingResources = ignoreMissingResources;
    }
}
