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
package org.eclipse.che.ide.ext.git.client;

import com.google.gwt.dom.client.Element;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.project.shared.dto.event.GitChangeEventDto;
import org.eclipse.che.ide.api.data.tree.HasAttributes;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.commit.CommitPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerViewImpl;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.TreeStyles;
import org.eclipse.che.ide.ui.smartTree.event.BeforeExpandNodeEvent;
import org.eclipse.che.ide.ui.smartTree.event.ExpandNodeEvent;
import org.eclipse.che.ide.ui.smartTree.presentation.HasPresentation;
import org.eclipse.che.ide.ui.smartTree.presentation.NodePresentation;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Receives git checkout notifications caught by server side VFS file watching system.
 * Support two type of notifications: git branch checkout and git revision checkout.
 * After a notification is received it is processed and passed to and instance of
 * {@link NotificationManager}.
 */
@Singleton
public class GitChangesHandler {

    private final EventBus                           eventBus;
    private       Provider<ProjectExplorerPresenter> projectExplorerPresenterProvider;
    private       Tree                               tree;
    private       Map<String, List<String>>          put;

    @Inject
    public GitChangesHandler(EventBus eventBus,
                             RequestHandlerConfigurator configurator,
                             Provider<ProjectExplorerPresenter> projectExplorerPresenterProvider) {
        this.eventBus = eventBus;
        this.projectExplorerPresenterProvider = projectExplorerPresenterProvider;


        put = new HashMap<>();
        put.put("colours", Collections.singletonList("red"));

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {

            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                tree = projectExplorerPresenterProvider.get().getTree();
                tree.addExpandHandler(new ExpandNodeEvent.ExpandNodeHandler() {

                    @Override
                    public void onExpand(ExpandNodeEvent event) {
                        String a = "dsgsdg";
                        tree.getAllChildNodes(Collections.singletonList(event.getNode()), false)
                            .stream()
                            .filter(node -> ((ResourceNode)node).getData().getLocation().equals(Path.valueOf("/blank-project/README.md")))
                            .forEach(node -> ((HasAttributes)node).setAttributes(put));
                    }
                });
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {

            }
        });

        configureHandler(configurator);
    }

    private void configureHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("event:git-change")
                    .paramsAsDto(GitChangeEventDto.class)
                    .noResult()
                    .withBiConsumer(this::apply);
    }

    public void apply(String endpointId, GitChangeEventDto dto) {
//        if (tree == null) {
//            tree = projectExplorerPresenterProvider.get().getTree();
//
//            tree.addExpandHandler(new ExpandNodeEvent.ExpandNodeHandler() {
//                @Override
//                public void onExpand(ExpandNodeEvent event) {
//                    String a = "dsgsdg";
//                    tree.getAllChildNodes(Collections.singletonList(event.getNode()), false)
//                        .stream()
//                        .filter(node -> ((ResourceNode)node).getData().getLocation().equals(Path.valueOf("/sprin/README.md")))
//                        .forEach(node -> {
//                            ((HasAttributes)node).setAttributes(put);
//                            tree.refresh(node);
//                        });
//                }
//            });
//        }

        tree.getNodeStorage()
            .getAll()
            .stream()
            .filter(node -> ((ResourceNode)node).getData().getLocation().equals(Path.valueOf(dto.getPath())))
            .forEach(node -> {
                ((HasAttributes)node).setAttributes(put);
                tree.refresh(node);
            });
    }
}
