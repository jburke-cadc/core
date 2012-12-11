/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2009.                            (c) 2009.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*                                       
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*                                       
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*                                       
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*                                       
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*                                       
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 4 $
*
************************************************************************
*/

package ca.nrc.cadc.log;

import java.security.Principal;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;

import ca.nrc.cadc.auth.HttpPrincipal;
import ca.nrc.cadc.util.StringUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * Class to be used by web services to log at INFO level the start and
 * end messages for each request.
 * 
 * @author majorb
 *
 */
public abstract class WebServiceLogInfo
{
    
    private static final String ANONYMOUS_USER = "anonUser";
    
    Gson gson;
    
    @Expose
    protected String method;
    
    @Expose
    protected String path;
    
    protected boolean userSuccess = true;
    
    @Expose
    protected Boolean success;
    
    @Expose
    protected String user;
    
    @Expose
    protected String from;
    
    @Expose
    protected Long time;
    
    @Expose
    protected Long bytes;
    
    @Expose
    protected String message;
    
    @Expose
    protected String jobID;
    
    protected WebServiceLogInfo()
    {
        GsonBuilder builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
        builder.disableHtmlEscaping();
        gson = builder.create();
    }
    
    /**
     * Generates the log.info message for the start of the request.
     * @return
     */
    public String start()
    {
        return "START: " + gson.toJson(this, this.getClass());
    }
    
    /**
     * Generates the log.info message for the end of the request.
     * @return
     */
    public String end()
    {
        this.success = userSuccess;
        return "END: " + gson.toJson(this, this.getClass());
    }

    /**
     * Set the success/fail boolean.
     * @param success
     */
    public void setSuccess(boolean success)
    {
        this.userSuccess = success;
    }

    /**
     * Set the subject.  This will automatically determine the
     * userid for logging.
     * @param subject
     */
    public void setSubject(Subject subject)
    {
        this.user = getUser(subject);
    }

    /**
     * Set the elapsed time for the request to complete.
     * @param elapsedTime
     */
    public void setElapsedTime(Long elapsedTime)
    {
        this.time = elapsedTime;
    }

    /**
     * Set the number of bytes transferred in the request.
     * @param bytes
     */
    public void setBytes(Long bytes)
    {
        this.bytes = bytes;
    }
    
    /**
     * Set a success or failure message.
     * @param message
     */
    public void setMessage(String message)
    {
        if (StringUtil.hasText(message))
            this.message = message.trim();
    }
    
    protected void setJobID(String jobID)
    {
        if (StringUtil.hasText(jobID))
            this.jobID = jobID.trim();
    }
    
    protected String getUser(Subject subject)
    {
        if (subject != null)
        {
            final Set<Principal> principals = subject.getPrincipals();
            if (!principals.isEmpty())
            {
                Principal userPrincipal = null;
                Iterator<Principal> i = principals.iterator();
                while (i.hasNext())
                {
                    Principal nextPrincipal = i.next();
                    if (!(userPrincipal instanceof HttpPrincipal))
                    {
                        userPrincipal = nextPrincipal;
                    }
                }
                return userPrincipal.getName();
            }
        }
        
        return ANONYMOUS_USER;
    }

}