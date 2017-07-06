/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * This class is handling the service for the blank project import.
 *
 * @author Oleksii Kurinnyi
 */
export class ImportBlankProjectService {
  /**
   * Project's name.
   */
  private _name: string;
  /**
   * Project's description.
   */
  private _description: string;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this._name = '';
    this._description = '';
  }

  get name(): string {
    return this._name;
  }

  get description(): string {
    return this._description;
  }

  /**
   * Callback which is called when project's name or description is changed.
   *
   * @param {string=""} name the project's name
   * @param {string=""} description the project's description
   */
  onChanged(name: string = '', description: string = ''): void {
    this._name = name;
    this._description = description;
  }

  /**
   * Returns project's properties.
   *
   * @return {che.IProjectTemplate}
   */
  getProjectProps(): che.IProjectTemplate {
    const props = {} as che.IProjectTemplate;

    props.name = this._name;
    props.displayName = this._name;
    props.description = this._description;
    const path = '/' +  this._name.replace(/[^\w-_]/g, '_');
    props.path = path;
    props.category = '';

    return props;
  }

}
