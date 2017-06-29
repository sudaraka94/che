
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
package org.eclipse.che.api.vfs.watcher;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.project.shared.Constants.CHE_DIR;
import static org.eclipse.che.api.vfs.watcher.FileWatcherUtils.toNormalPath;

/**
 * Watches the file which contains exclude patterns for managing of tracking creation,
 * modification and deletion events for corresponding entries.
 */
@Singleton
public class FileWatcherIgnoreFileTracker {
    private static final Logger LOG                           = LoggerFactory.getLogger(ProjectManager.class);
    private static final String FILE_WATCHER_IGNORE_FILE_NAME = "fileWatcherExcludes";
    private static final String FILE_WATCHER_IGNORE_FILE_PATH = "/" + CHE_DIR + "/" + FILE_WATCHER_IGNORE_FILE_NAME;

    private final Map<Path, Set<Path>> excludes = new ConcurrentHashMap<>();
    private final FileWatcherManager fileWatcherManager;
    private final ProjectManager     projectManager;
    private final Path               root;
    private       int                ignoreFileWatchingOperationID;

    @Inject
    public FileWatcherIgnoreFileTracker(ProjectManager projectManager,
                                        FileWatcherManager fileWatcherManager,
                                        @Named("che.user.workspaces.storage") File root) {
        this.projectManager = projectManager;
        this.fileWatcherManager = fileWatcherManager;
        this.root = root.toPath().normalize().toAbsolutePath();
    }

    @PostConstruct
    public void initialize() {
        startTrackingIgnoreFile();
        readExcludesFromIgnoreFiles();
        addFileWatcherExcludesMatcher();
    }

    @PreDestroy
    public void stopWatching() {
        fileWatcherManager.unRegisterByMatcher(ignoreFileWatchingOperationID);
    }

    private void readExcludesFromIgnoreFiles() {
        try {
            projectManager.getProjects().stream()
                          .map(this::getFileWatcherIgnoreFileLocation)
                          .forEach(this::addExcludesFromIgnoreFile);
        } catch (ServerException e) {
            LOG.debug("Can not fill up file watcher excludes: " + e.getLocalizedMessage());
        }
    }

    private String getFileWatcherIgnoreFileLocation(RegisteredProject project) {
        FolderEntry baseFolder = project.getBaseFolder();
        return baseFolder == null ? "" : baseFolder.getPath().toString() + FILE_WATCHER_IGNORE_FILE_PATH;
    }

    private void startTrackingIgnoreFile() {
        fileWatcherManager.addIncludeMatcher(getIgnoreFileMatcher());
        ignoreFileWatchingOperationID = fileWatcherManager.registerByMatcher(getIgnoreFileMatcher(),
                                                                             getCreateConsumer(),
                                                                             getModifyConsumer(),
                                                                             getDeleteConsumer());
    }

    private PathMatcher getIgnoreFileMatcher() {
        return path -> !isDirectory(path) &&
                       FILE_WATCHER_IGNORE_FILE_NAME.equals(path.getFileName().toString()) &&
                       CHE_DIR.equals(path.getParent().getFileName().toString());
    }

    private Consumer<String> getCreateConsumer() {
        return getModifyConsumer();
    }

    private Consumer<String> getModifyConsumer() {
        return this::addExcludesFromIgnoreFile;
    }

    private Consumer<String> getDeleteConsumer() {
        return excludesFileLocation -> {
            Path excludesFilePath = toNormalPath(root, excludesFileLocation);
            Path projectPath = excludesFilePath.getParent().getParent();
            excludes.remove(projectPath);
        };
    }

    private void addExcludesFromIgnoreFile(String ignoreFileLocation) {
        try {
            if (isNullOrEmpty(ignoreFileLocation)) {
                return;
            }

            Path ignoreFilePath = toNormalPath(root, ignoreFileLocation);
            if (!exists(ignoreFilePath)) {
                return;
            }

            Path projectPath = ignoreFilePath.getParent().getParent();
            excludes.remove(projectPath);

            List<String> lines = Files.lines(ignoreFilePath).collect(toList());
            Set<Path> projectExcludes = lines.stream()
                                             .filter(line -> !isNullOrEmpty(line.trim()))
                                             .map(line -> projectPath.resolve(line.trim()))
                                             .filter(excludePath -> exists(excludePath))
                                             .collect(toSet());

            if (!projectExcludes.isEmpty()) {
                excludes.put(projectPath, projectExcludes);
            }
        } catch (IOException e) {
            LOG.debug(format("Can not fill up file watcher excludes from file %s, the reason is: %s",
                             ignoreFileLocation,
                             e.getLocalizedMessage()));
        }
    }

    private void addFileWatcherExcludesMatcher() {
        fileWatcherManager.addExcludeMatcher(path -> excludes.values().stream()
                                                             .flatMap(Collection::stream)
                                                             .anyMatch(path::startsWith));
    }
}
