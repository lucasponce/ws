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
package org.exoplatform.services.rest.impl;

import javax.ws.rs.core.MediaType;

import org.exoplatform.common.util.HierarchicalProperty;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class RestInitializerTest extends BaseTest
{

   public void setUp() throws Exception
   {
      super.setUp();
      setContext();
   }

   // Check does we get all additional component specified in configuration.
   public void testDeployAdditionalComponents()
   {
      ProviderBinder providers = ProviderBinder.getInstance();
      // XXX Able (no NPE) to use null instead "path" because UriPattern
      // for filters in null and path will be never checked.
      assertEquals(1, providers.getRequestFilters(null).size());
      assertEquals(1, providers.getMethodInvokerFilters(null).size());
      // -----
      assertNotNull(providers.getMessageBodyReader(HierarchicalProperty.class, null, null,
         MediaType.APPLICATION_XML_TYPE));
   }

}
