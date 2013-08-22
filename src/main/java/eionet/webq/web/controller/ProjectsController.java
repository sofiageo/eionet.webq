package eionet.webq.web.controller;

/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Web Questionnaires 2
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Anton Dmitrijev
 */

import eionet.webq.dao.FileStorage;
import eionet.webq.dto.ProjectEntry;
import eionet.webq.dto.WebFormUpload;
import eionet.webq.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

/**
 * Spring controller to manage projects.
 *
 * @see Controller
 */
@Controller
@RequestMapping("projects")
public class ProjectsController {
    /**
     * Attribute name for storing project entry in model.
     */
    static final String PROJECT_ENTRY_MODEL_ATTRIBUTE = "projectEntry";
    /**
     * Attribute name for storing project entry in model.
     */
    static final String WEB_FORM_UPLOAD_ATTRIBUTE = "webFormUpload";
    /**
     * Attribute name for storing project entry in model.
     */
    static final String ALL_PROJECT_FILES_ATTRIBUTE = "allProjectFiles";
    /**
     * Access to project folders.
     */
    @Autowired
    private ProjectService projectService;
    /**
     * Access to project files.
     */
    @Autowired
    @Qualifier("project-files")
    private FileStorage<ProjectEntry, WebFormUpload> projectFileStorage;

    /**
     * All projects handler.
     *
     * @param model model attributes holder
     * @return view name
     */
    @RequestMapping({ "/", "*" })
    public String allProjects(Model model) {
        model.addAttribute("allProjects", projectService.getAllFolders());
        return "projects";
    }

    /**
     * Add or edit project.
     *
     * @param model model attributes holder
     * @return view name
     */
    @RequestMapping("/add")
    public String addProject(Model model) {
        return editForm(model, new ProjectEntry());
    }

    /**
     * Add or edit project.
     *
     * @param projectId projectId to be edited
     * @param model model attributes holder
     * @return view name
     */
    @RequestMapping("/edit")
    public String editProject(@RequestParam String projectId, Model model) {
        return editForm(model, projectService.getByProjectId(projectId));
    }

    /**
     * Adds new project.
     *
     * @param project new project
     * @param bindingResult request binding to {@link ProjectEntry} result
     * @param model model attribute holder
     * @return view name
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveProject(@Valid @ModelAttribute ProjectEntry project, BindingResult bindingResult, Model model) {
        if (!bindingResult.hasErrors()) {
            try {
                projectService.saveOrUpdate(project);
                return allProjects(model);
            } catch (DuplicateKeyException e) {
                bindingResult.rejectValue("projectId", "duplicate.project.id");
            }
        }
        return editForm(model, project);
    }

    /**
     * Removes project by project id.
     *
     * @param projectId project id for project to be removed
     * @param model model attributes holder
     * @return view name
     */
    @RequestMapping(value = "/remove")
    public String removeProject(@RequestParam String projectId, Model model) {
        projectService.remove(projectId);
        return allProjects(model);
    }

    /**
     * View project folder content by project id.
     *
     * @param projectId project id for project to be viewed
     * @param model model attributes holder
     * @return view name
     */
    @RequestMapping(value = "/{projectId}/view")
    public String viewProject(@PathVariable String projectId, Model model) {
        return viewProject(projectService.getByProjectId(projectId), model);
    }

    /**
     * Allow to add new webform under project folder.
     *
     * @param projectFolderId project id for webform
     * @param model model attribute holder
     * @param webFormUpload web form upload related object
     * @param bindingResult request binding to {@link WebFormUpload} result
     * @return view name
     */
    @RequestMapping(value = "/{projectFolderId}/webform/save")
    public String newWebForm(@PathVariable String projectFolderId, @Valid @ModelAttribute WebFormUpload webFormUpload,
            BindingResult bindingResult, Model model) {
        ProjectEntry currentProject = projectService.getByProjectId(projectFolderId);
        boolean update = webFormUpload.getId() > 0;
        if (!update && webFormUpload.getFileContent() == null) {
            bindingResult.rejectValue("file", "webform.file.null");
        }
        if (bindingResult.hasErrors()) {
            return addOrEditProjectFile(currentProject, webFormUpload, model);
        }
        if (update) {
            projectFileStorage.update(webFormUpload, currentProject);
        } else {
            projectFileStorage.save(webFormUpload, currentProject);
        }
        model.addAttribute("message", "Webform added/updated.");
        return viewProject(currentProject, model);
    }

    /**
     * Allows to add file.
     *
     * @param projectFolderId project id for webform
     * @param model model attribute holder
     * @return view name
     */
    @RequestMapping(value = "/{projectFolderId}/webform/add")
    public String addWebForm(@PathVariable String projectFolderId, Model model) {
        return addOrEditProjectFile(projectService.getByProjectId(projectFolderId), new WebFormUpload(), model);
    }

    /**
     * Allows to edit file.
     *
     * @param projectFolderId project id for webform
     * @param fileId file id to be edited
     * @param model model attribute holder
     * @return view name
     */
    @RequestMapping(value = "/{projectFolderId}/webform/edit")
    public String editWebForm(@PathVariable String projectFolderId, @RequestParam int fileId, Model model) {
        return addOrEditProjectFile(projectService.getByProjectId(projectFolderId), projectFileStorage.fileById(fileId), model);
    }

    /**
     * Removes webform from project.
     *
     * @param projectFolderId project folder id associated with file to remove
     * @param fileId file to be removed
     * @param model model attribute holder
     * @return view name
     */
    @RequestMapping(value = "/{projectFolderId}/webform/remove")
    public String removeWebForm(@PathVariable String projectFolderId, @RequestParam int fileId, Model model) {
        ProjectEntry byProjectId = projectService.getByProjectId(projectFolderId);
        projectFileStorage.remove(fileId, byProjectId);
        return viewProject(byProjectId, model);
    }

    /**
     * Sets model object and returns add/edit view.
     *
     * @param model model attribute holder
     * @param entry project
     * @return view name
     */
    private String editForm(Model model, ProjectEntry entry) {
        addProjectToModel(model, entry);
        return "add_edit_project";
    }

    /**
     * Sets model objects and returns view name.
     *
     * @param entry project in view
     * @param model model attribute holder
     * @return view name
     */
    private String viewProject(ProjectEntry entry, Model model) {
        addProjectToModel(model, entry);
        model.addAttribute(ALL_PROJECT_FILES_ATTRIBUTE, projectFileStorage.allFilesFor(entry));
        return "view_project";
    }

    /**
     * Sets model objects and returns view name.
     *
     * @param project project in view
     * @param webFormUpload web form upload
     * @param model model attribute holder
     * @return view name
     */
    private String addOrEditProjectFile(ProjectEntry project, WebFormUpload webFormUpload, Model model) {
        addProjectToModel(model, project);
        addWebFormToModel(model, webFormUpload);
        return "add_edit_project_file";
    }

    /**
     * Adds project entry to model.
     *
     * @param model model attribute holder
     * @param entry project
     */
    private void addProjectToModel(Model model, ProjectEntry entry) {
        addAttributeToModelIfNotAdded(model, PROJECT_ENTRY_MODEL_ATTRIBUTE, entry);
    }

    /**
     * Adds webform upload to model.
     *
     * @param model model attribute holder
     * @param webFormUpload web form upload
     */
    private void addWebFormToModel(Model model, WebFormUpload webFormUpload) {
        addAttributeToModelIfNotAdded(model, WEB_FORM_UPLOAD_ATTRIBUTE, webFormUpload);
    }

    /**
     * Adds attribute to model if model does not contain it.
     *
     * @param model model attribute holder
     * @param attributeName name of attribute to be added
     * @param attribute attribute to be added
     */
    private void addAttributeToModelIfNotAdded(Model model, String attributeName, Object attribute) {
        if (!model.containsAttribute(attributeName)) {
            model.addAttribute(attributeName, attribute);
        }
    }
}
