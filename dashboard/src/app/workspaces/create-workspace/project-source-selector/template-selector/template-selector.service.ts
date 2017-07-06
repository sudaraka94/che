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

import {CheProjectTemplate} from '../../../../../components/api/che-project-template.factory';

/**
 * Service for template selector.
 *
 * @author Oleksii Kurinnyi
 */
export class TemplateSelectorSvc {
  /**
   * Filter service.
   */
  $filter: ng.IFilterService;
  /**
   * Promises service.
   */
  $q: ng.IQService;
  /**
   * Project template API interactions.
   */
  cheProjectTemplate: CheProjectTemplate;
  /**
   * The list of selected templates.
   */
  templates: Array<che.IProjectTemplate>;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($filter: ng.IFilterService, $q: ng.IQService, cheProjectTemplate: CheProjectTemplate) {
    this.$filter = $filter;
    this.$q = $q;
    this.cheProjectTemplate = cheProjectTemplate;

    this.templates = [];

    this.fetchTemplates();
  }

  /**
   * Fetches list of templates.
   */
  fetchTemplates(): ng.IPromise<any> {
    const defer = this.$q.defer();

    const templates = this.cheProjectTemplate.getAllProjectTemplates();
    if (templates.length) {
      defer.resolve();
    } else {
      this.cheProjectTemplate.fetchTemplates().finally(() => {
        defer.resolve();
      });
    }

    return defer.promise;
  }

  /**
   * Returns list of fetched project templates.
   *
   * @return {Array<che.IProjectTemplate>}
   */
  getAllTemplates(): Array<che.IProjectTemplate> {
    return this.cheProjectTemplate.getAllProjectTemplates();
  }

  /**
   * Returns project template by name.
   *
   * @param {string} name the project template name
   * @return {undefined|che.IProjectTemplate}
   */
  getTemplateByName(name: string): che.IProjectTemplate {
    return this.getAllTemplates().find((template: che.IProjectTemplate) => {
      return template.name === name;
    });
  }

  /**
   * Callback which is called when template is checked or unchecked.
   *
   * @param {Array<che.IProjectTemplate>} templates the list of selected templates
   */
  onTemplateSelected(templates: Array<che.IProjectTemplate>): void {
    this.templates = templates;
  }

  /**
   * Returns selected templates.
   *
   * @return {che.IProjectTemplate[]}
   */
  getTemplates(): Array<che.IProjectTemplate> {
    return angular.copy(this.templates);
  }

}
