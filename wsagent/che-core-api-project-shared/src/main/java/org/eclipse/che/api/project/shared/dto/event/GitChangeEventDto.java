/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.shared.dto.event;

import org.eclipse.che.dto.shared.DTO;

/**
 * To transfer branch name after git checkout operation
 */
@DTO
public interface GitChangeEventDto {
    Type getType();

    String getPath();

    GitChangeEventDto withType(Type type);

    GitChangeEventDto withPath(String path);

    enum Type {
        NEW,
        MODIFIED,
        DELETED,
    }
}
