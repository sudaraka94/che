/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.serverservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Example server service that greets the user.
 *
 * @author Edgar Mueller
 */
@Path("hello")
public class MyService {

  /**
   * Returns a greeting message.
   *
   * @param name the parameter
   * @return a greeting message
   */
  @GET
  @Path("{name}")
  public String sayHello(@PathParam("name") String name) {
    return "Hello " + name + "!";
  }
}
