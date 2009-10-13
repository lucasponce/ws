/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.services.rest.servlet;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.ContainerResponseWriter;
import org.exoplatform.services.rest.GenericContainerResponse;
import org.exoplatform.services.rest.RequestHandler;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.impl.header.HeaderHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class BaseRestServlet extends HttpServlet
{

   /**
    * See {@link ContainerResponseWriter}.
    */
   class ServletContainerResponseWriter implements ContainerResponseWriter
   {

      /**
       * See {@link HttpServletResponse}.
       */
      private HttpServletResponse servletResponse;

      /**
       * @param response HttpServletResponse
       */
      ServletContainerResponseWriter(HttpServletResponse response)
      {
         this.servletResponse = response;
      }

      /**
       * {@inheritDoc}
       */
      @SuppressWarnings("unchecked")
      public void writeBody(GenericContainerResponse response, MessageBodyWriter entityWriter) throws IOException
      {
         Object entity = response.getEntity();
         if (entity != null)
         {
            OutputStream out = servletResponse.getOutputStream();
            entityWriter.writeTo(entity, entity.getClass(), response.getEntityType(), null, response.getContentType(),
               response.getHttpHeaders(), out);
            out.flush();
         }
      }

      /**
       * {@inheritDoc}
       */
      public void writeHeaders(GenericContainerResponse response) throws IOException
      {
         if (servletResponse.isCommitted())
            return;

         servletResponse.setStatus(response.getStatus());

         if (response.getHttpHeaders() != null)
         {
            // content-type and content-length should be preset in headers
            for (Map.Entry<String, List<Object>> e : response.getHttpHeaders().entrySet())
            {
               String name = e.getKey();
               for (Object o : e.getValue())
               {
                  String value = null;
                  if (o != null && (value = HeaderHelper.getHeaderAsString(o)) != null)
                     servletResponse.addHeader(name, value);
               }
            }
         }
      }
   }

   private static final Log LOG = ExoLogger.getLogger(RestServlet.class.getName());

   private RequestHandler requestHandler;

   private ServletConfig servletConfig;

   protected RequestHandler getRequestHandler()
   {
      return requestHandler;
   }

   public void init(ServletConfig servletConfig)
   {
      this.servletConfig = servletConfig;
      requestHandler = (RequestHandler)servletConfig.getServletContext().getAttribute(RequestHandler.class.getName());
   }

   public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
      ServletException
   {

      EnvironmentContext env = new EnvironmentContext();
      env.put(HttpServletRequest.class, httpRequest);
      env.put(HttpServletResponse.class, httpResponse);
      env.put(ServletConfig.class, servletConfig);
      env.put(ServletContext.class, getServletContext());

      try
      {
         EnvironmentContext.setCurrent(env);
         ServletContainerRequest request = new ServletContainerRequest(httpRequest);
         ContainerResponse response = new ContainerResponse(new ServletContainerResponseWriter(httpResponse));
         requestHandler.handleRequest(request, response);
      }
      catch (Exception e)
      {
         LOG.error(e);
         throw new ServletException(e);
      }
      finally
      {
         EnvironmentContext.setCurrent(null);
      }
   }

}
