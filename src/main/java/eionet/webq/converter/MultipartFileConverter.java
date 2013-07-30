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
package eionet.webq.converter;

import eionet.webq.dto.UploadedXmlFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

public class MultipartFileConverter implements Converter<MultipartFile, UploadedXmlFile> {

    @Override
    public UploadedXmlFile convert(MultipartFile multipartFile) {
        byte[] bytes = toByteArray(multipartFile);
        return new UploadedXmlFile().setFileContent(bytes).setName(multipartFile.getOriginalFilename())
                .setFileSizeInBytes(multipartFile.getSize()).setXmlSchema(extractXmlSchema(bytes));
    }

    private String extractXmlSchema(byte[] bytes) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        XPathFactory xPathFactory = XPathFactory.newInstance();
        try {
            Document xml = builderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
            return xPathFactory.newXPath().evaluate("//@xsi:noNamespaceSchemaLocation", xml);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] toByteArray(MultipartFile multipartFile) {
        try {
            return multipartFile.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}