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
package eionet.webq.service;

import eionet.webq.dao.WebFormStorage;
import eionet.webq.dao.orm.ProjectFile;
import eionet.webq.dto.WebFormType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * {@link eionet.webq.service.WebFormService} implementation.
 */
@Service("localWebForms")
public class WebFormsServiceImpl extends AbstractWebFormsService {
    /**
     * Web forms storage.
     */
    @Autowired
    WebFormStorage storage;

    @Override
    public Collection<ProjectFile> getAllActiveWebForms() {
        return storage.getAllActiveWebForms(WebFormType.LOCAL);
    }

    @Override
    public ProjectFile findActiveWebFormById(int id) {
        return storage.getActiveWebFormById(WebFormType.LOCAL, id);
    }

    @Override
    protected Collection<ProjectFile> findWebFormsForNotEmptyXmlSchemas(Collection<String> xmlSchemas) {
        return storage.findWebFormsForSchemas(WebFormType.LOCAL, xmlSchemas);
    }
}
